package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sun.xml.internal.ws.developer.MemberSubmissionAddressing;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.DictionaryMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.dto.UpdateCourseDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.Dictionary;
import com.xuecheng.content.service.CourseBaseInfoService;

import com.xuecheng.content.service.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Resource
    CourseBaseMapper courseBaseMapper;
    @Resource
    CourseMarketMapper courseMarketMapper;
    @Resource
    CourseCategoryMapper courseCategoryMapper;
   @Resource
    DictionaryService dictionaryService;
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        log.info("到了业务层了");
        //我们封装分页的参数
        //这里就执行有相应的业务
        //根据课程名称查询
        LambdaQueryWrapper<CourseBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName());
        //根据审核情况查询
        wrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());
        //根据发布情况查询
        wrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());
       //还有就是根据字典表查询状态转为文字202001

        //然后分页查询
        Page<CourseBase> courseBasePage = new Page<>(pageParams.getPageNo(),pageParams.getPageSize());
        //根据mapperde自带分页查询得到结果
        Page<CourseBase> courseBasePage1 = courseBaseMapper.selectPage(courseBasePage, wrapper);
        //拿到结果
        List<CourseBase> list = courseBasePage1.getRecords();

        //然后遍历list
        for (CourseBase cour:list) {
            log.info("已经拿到了数据");
            //在这里拿到他的审核码联表查询
           List<Dictionary> res= dictionaryService.querybyCode(cour.getAuditStatus());
        }
        long total = courseBasePage1.getTotal();
        //然后封装返回结果
        PageResult<CourseBase> courseBasePageResult = new PageResult<CourseBase>(list,total, pageParams.getPageNo(), pageParams.getPageSize());
       //返回
        return courseBasePageResult;
    }

    @Override
    public CourseBaseInfoDto updateCrourseBase(Long compainId, UpdateCourseDto updateCourseDto) {
        CourseBase courseBase = courseBaseMapper.selectById(updateCourseDto.getId());
        if(courseBase==null){
            XueChengPlusException.cast("课程不存在");
        }
        //并且不能修改其他机构的信息
        if(!compainId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("bun修改其他机构的课程");
        }
        //然后进行基本表的更新
        BeanUtils.copyProperties(updateCourseDto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        int i = courseBaseMapper.updateById(courseBase);
        if(i<=0){
            XueChengPlusException.cast("修改课程基本表数据库失败");
        }
        CourseMarket courseMarket = new CourseMarket();
        //然后进行营销表的更新
        BeanUtils.copyProperties(updateCourseDto,courseMarket);
        CourseMarket courseMarket1 = courseMarketMapper.selectById(courseMarket.getId());
        if(courseMarket1==null){
            //没有就添加
            int insert = courseMarketMapper.insert(courseMarket);
            if(insert<=0){
                XueChengPlusException.cast("添加营销表失败");
            }
        }else{
            //有的话则新增
            int i1 = courseMarketMapper.updateById(courseMarket);
            if(i1<=0){
                XueChengPlusException.cast("修改课程营销表表数据库失败");
            }
        }

        //查询课程信息然后返回
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(updateCourseDto.getId());
        return courseBaseInfo;
    }

    @Override
    public CourseBaseInfoDto CreateCourse(Long id, AddCourseDto addCourseDto) {

        //这里先要进行校验
        //合法性校验
//        if (StringUtils.isBlank(addCourseDto.getName())) {
////            throw new RuntimeException("课程名称为空");
//             XueChengPlusException.cast("课程名称为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getMt())) {
//            XueChengPlusException.cast("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getSt())) {
//            XueChengPlusException.cast("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getGrade())) {
//          XueChengPlusException.cast("课程等级为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
//           XueChengPlusException.cast("教育模式为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getUsers())) {
//          XueChengPlusException.cast("适应人群为空");
//        }
//
//        if (StringUtils.isBlank(addCourseDto.getCharge())) {
//            XueChengPlusException.cast("收费规则为空");
//        }
        //然后开始将加入的信息拷贝到基本表里
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto,courseBase);
        //设置时间
        courseBase.setCreateDate(LocalDateTime.now());
        //设置id
        courseBase.setCompanyId(id);
        //设置发布状态
        courseBase.setAuditStatus("202002");
        //设置审核状态
        courseBase.setStatus("203001");
        //基本表的插入
        int insert = courseBaseMapper.insert(courseBase);
        if(insert<=0){
            XueChengPlusException.cast("新增课程基本信息失败");
        }
        //更新营销表
        //课程营销信息
        CourseMarket courseMarketNew = new CourseMarket();
        Long courseId = courseBase.getId();
        BeanUtils.copyProperties(addCourseDto,courseMarketNew);
        courseMarketNew.setId(courseId);
        int i = saveCourseMarket(courseMarketNew);
        if(i<=0){
            XueChengPlusException.cast("保存课程营销信息失败");
        }
        //查询课程基本信息及营销信息并返回
        return getCourseBaseInfo(courseId);
    }
    //保存课程营销信息
    private int saveCourseMarket(CourseMarket courseMarketNew){
        //收费规则
        String charge = courseMarketNew.getCharge();
        if(StringUtils.isBlank(charge)){
            XueChengPlusException.cast("收费规则没有选择");
        }
        //收费规则为收费
        if(charge.equals("201001")){
            if(courseMarketNew.getPrice() == null || courseMarketNew.getPrice().floatValue()<=0){
                XueChengPlusException.cast("课程为收费价格不能为空且必须大于0");
            }
        }

        //根据id从课程营销表查询
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarketNew.getId());
        if(courseMarketObj == null){
            //没有则新建
            return courseMarketMapper.insert(courseMarketNew);
        }else{
            //否则就更新
            BeanUtils.copyProperties(courseMarketNew,courseMarketObj);
            courseMarketObj.setId(courseMarketNew.getId());
            return courseMarketMapper.updateById(courseMarketObj);
        }
    }

    @Override
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }

        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;

    }


}
