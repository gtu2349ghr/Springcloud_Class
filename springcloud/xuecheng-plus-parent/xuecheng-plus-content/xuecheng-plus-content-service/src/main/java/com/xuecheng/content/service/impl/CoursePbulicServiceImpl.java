package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePbulicDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePbulicService;
import com.xuecheng.content.service.TeachPlanService;
import com.xuecheng.messagesdk.mapper.MqMessageMapper;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CoursePbulicServiceImpl implements CoursePbulicService {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    @Autowired
    TeachPlanService teachPlanService;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    MqMessageService mqMessageService;
    @Override
    public CoursePbulicDto selectPublic(Long courseId) {
        CoursePbulicDto coursePbulicDto = new CoursePbulicDto();
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        coursePbulicDto.setCoursebase(courseBaseInfo);
        List<TeachPlanDto> teachPlanDtos = teachPlanService.selectTreeNodes(courseId);
        coursePbulicDto.setTeachPlans(teachPlanDtos);
        return coursePbulicDto;
    }

    @Override
    public void commitCoursePublic(Long companyId,Long courseId) {
        //先去查询他的包含表的信息
        //因为这里是只能提交本机构的课程
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if(courseBaseInfo==null){
            XueChengPlusException.cast("课程不存在");
        }
//        if(!courseBaseInfo.getCompanyId().equals(companyId)){
//            XueChengPlusException.cast("不能提交其他机构的课程");
//        }
        if(courseBaseInfo.getAuditStatus().equals("202003")){
            XueChengPlusException.cast("已提交过，不能再次提交");
        }
        //tup信息不能为空
        if(courseBaseInfo.getPic()==null){
            XueChengPlusException.cast("请上传图片");
        }
       //然后查询课程计划信息，没有就不进行
        List<TeachPlanDto> teachPlanDtos = teachPlanService.selectTreeNodes(courseId);
        if(teachPlanDtos==null||teachPlanDtos.size()==0){
            XueChengPlusException.cast("课程计划不能为空");
        }
        //然后进行封装
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);
        //然后封装营销表和课程大章表
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //然后转化为json
        String s = JSON.toJSONString(courseMarket);
        String s1 = JSON.toJSONString(teachPlanDtos);
        coursePublishPre.setCreateDate(LocalDateTime.now());
        coursePublishPre.setMarket(s);
        coursePublishPre.setTeachplan(s1);
        //然后先判断
        CoursePublishPre coursePublishPre1 = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre1==null){
            int insert = coursePublishPreMapper.insert(coursePublishPre);
            if(insert<=0){
                XueChengPlusException.cast("插入与审核表失败");
            }
            return;
        }
        int i = coursePublishPreMapper.updateById(coursePublishPre);
        if(i<=0){
            XueChengPlusException.cast("更新与审核表失败");
        }
        return ;
    }

    /**
     * 课程发布接口
     * @param companyId
     * @param courseId
     */
    @Override
    public void publish(Long companyId, Long courseId) {
        //先查预发布表，如果审核状态通过
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        String status = coursePublishPre.getStatus();
        if(!status.equals("202004")){
            XueChengPlusException.cast("课程没有通过审核不允许发布");
        }
        //写入发布表中
        CoursePublish coursePublish1 = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre,coursePublish1);
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        if(coursePublish==null){
            //进行插入
            coursePublishMapper.insert(coursePublish1);
        }else{
            coursePublishMapper.updateById(coursePublish1);
        }
        //然后写入消息表中
       savePubliceMessage(courseId);
      //  然后删除预发布表
        coursePublishPreMapper.deleteById(courseId);
    }

    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish;
    }

    private void savePubliceMessage(Long courseId){
        MqMessage course_pbulic = mqMessageService.addMessage("course_pbulic", String.valueOf(courseId), null, null);
        if(course_pbulic==null){
            XueChengPlusException.cast("添加消息失败");
        }
    }
}
