package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTableService;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MyCourseTableServiceImpl implements MyCourseTableService {
    @Autowired
    ContentServiceClient contentServiceClient;
    @Autowired
    XcChooseCourseMapper xcChooseCourseMapper;
    @Autowired
    XcCourseTablesMapper xcCourseTablesMapper;
    @Override
    @Transactional
    public XcChooseCourseDto addCourseTable(String userId, Long courseId) {
      //先查看收费规则
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if(coursepublish==null){
            XueChengPlusException.cast("课程不存在");
        }
        XcChooseCourse xcChooseCourse=null;
        String charge = coursepublish.getCharge();
        //如果是免费的话就添加选课表和我的课程表
        if(charge.equals("201000")){
             xcChooseCourse = addFreeTable(userId, courseId, coursepublish);
            XcCourseTables xcCourseTables = addCourseTable(xcChooseCourse);

        }else{
             xcChooseCourse = addchargeTable(userId, courseId,coursepublish);
            //收费的话就添加选课表
        }
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        XcCourseTablesDto xcCourseTablesDto = selectCourState(userId, courseId);
        //然后还要判断学习的资格
        BeanUtils.copyProperties(xcChooseCourse,xcChooseCourseDto);
        xcChooseCourseDto.setLearnStatus(xcCourseTablesDto.getLearnStatus());
        return xcChooseCourseDto;
    }

    /**
     * 查询学习状态
     * @param userId
     * @param courseId
     * @return
     */
    @Override
    public XcCourseTablesDto selectCourState(String userId, Long courseId) {
        //先查询状态
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        XcCourseTables courseTableById = getCourseTableById(userId, courseId);
        if(courseTableById==null){
            //如果数据库没有的话，就说明没有选课
           xcCourseTablesDto = new XcCourseTablesDto();
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }
        //找到了判断石佛过期
        LocalDateTime validtimeEnd = courseTableById.getValidtimeEnd();
        boolean before = validtimeEnd.isBefore(LocalDateTime.now());
        if(before){
            //那么已经过期
         BeanUtils.copyProperties(courseTableById,xcCourseTablesDto);
            xcCourseTablesDto.setLearnStatus("702003");
            return xcCourseTablesDto;
        }
        BeanUtils.copyProperties(courseTableById,xcCourseTablesDto);
        xcCourseTablesDto.setLearnStatus("702001");
        return xcCourseTablesDto;
    }

    /**
     * 添加课程记录表，收费的
     * @param userId
     * @param courseId
     * @return
     */
    private XcChooseCourse addchargeTable(String userId,Long courseId,CoursePublish coursepublish){
        LambdaQueryWrapper<XcChooseCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(XcChooseCourse::getCourseId,courseId)
                .eq(XcChooseCourse::getUserId,userId)
                .eq(XcChooseCourse::getOrderType,"700002")
                .eq(XcChooseCourse::getStatus,"701002");
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(wrapper);
        if(xcChooseCourses.size()>0){
            //证明我们已经选过了
            return xcChooseCourses.get(0);
        }
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(0f);//免费课程价格为0
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");//免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701002");//

        xcChooseCourse.setValidDays(365);//免费课程默认365
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        int insert = xcChooseCourseMapper.insert(xcChooseCourse);
        if(insert<=0){
            XueChengPlusException.cast("添加数据库选课表失败");
        }
        return xcChooseCourse;
    }

    /**
     * 添加免费的课程，选课记录和我的课程都要添加
     * @param userId
     * @param courseId
     * @return
     */
    private XcChooseCourse addFreeTable(String userId, Long courseId,CoursePublish coursepublish){
       //先像选课表加入数据
        LambdaQueryWrapper<XcChooseCourse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(XcChooseCourse::getCourseId,courseId)
                .eq(XcChooseCourse::getUserId,userId)
                .eq(XcChooseCourse::getOrderType,"700001")
                .eq(XcChooseCourse::getStatus,"701000");
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(wrapper);
        if(xcChooseCourses.size()>0){
            //证明我们已经选过了
            return xcChooseCourses.get(0);
        }
        //否则就插入
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(0f);//免费课程价格为0
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");//免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701001");//选课成功

        xcChooseCourse.setValidDays(365);//免费课程默认365
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        int insert = xcChooseCourseMapper.insert(xcChooseCourse);
        if(insert<=0){
             XueChengPlusException.cast("添加数据库选课表失败");
         }
        //这里选课成功了，然后插入课程表
        XcCourseTables xcCourseTables = addCourseTable(xcChooseCourse);
        //这里就加入课程表成功了
        //然后进行加入
        return xcChooseCourse;
    }

    /**
     * 添加课程表
     * @param xcChooseCourse
     * @return
     */
    private XcCourseTables addCourseTable(XcChooseCourse xcChooseCourse){

        //只有选课成功了才能添加
        String status = xcChooseCourse.getStatus();
        if(!status.equals("701001")){
            XueChengPlusException.cast("选课没有成功无法添加课程表");
        }
        //否则添加课程表 ，但是还是要查是否也已经添加过了
        XcCourseTables courseTableById = getCourseTableById(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if(courseTableById!=null){
            XueChengPlusException.cast("已经存在课程记录");
        }
        //然后进行添加
        XcCourseTables xcCourseTables = new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse,xcCourseTables);
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTables.setCourseType(xcChooseCourse.getOrderType());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now());
        int insert = xcCourseTablesMapper.insert(xcCourseTables);
        if(insert<=0){
            XueChengPlusException.cast("添加数据库课程表失败");

        }
        return xcCourseTables;
    }
    private XcCourseTables getCourseTableById(String userId,Long courseId){
        LambdaQueryWrapper<XcCourseTables> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(XcCourseTables::getCourseId,courseId)
                .eq(XcCourseTables::getUserId,userId);
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(wrapper);
        return xcCourseTables;
    }

}
