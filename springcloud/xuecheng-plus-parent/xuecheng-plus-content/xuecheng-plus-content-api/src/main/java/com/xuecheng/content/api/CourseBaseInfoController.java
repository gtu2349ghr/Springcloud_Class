package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.dto.UpdateCourseDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 课程信息编辑接口
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
@Api(value = "课程信息管理接口",tags = "课程信息管理接口")
@RestController
@Slf4j
@CrossOrigin
public class CourseBaseInfoController {
    @Resource
    CourseBaseInfoService courseBaseInfoService;
    @ApiOperation(value = "课程信息查询接口")
    @PostMapping("/course/list")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")//权限校验
    public PageResult<CourseBase> list( PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParams){
   log.info("执行前");
        //取出当前用户身份
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        SceriUtil.XcUser user = SceriUtil.getUser();
        System.out.println(principal);
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParams);

        return courseBasePageResult;

    }
    @ApiOperation("新增课程基础信息")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated AddCourseDto addCourseDto){
        return      courseBaseInfoService.CreateCourse(10L,addCourseDto);
    }
    @ApiOperation("修改查询课程基础信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto updateSelectCourseBase(@PathVariable  Long courseId){
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        return   courseBaseInfo;
    }
    @ApiOperation("修改课程基础信息")
    @PutMapping("/course")
    public CourseBaseInfoDto updateCourseBase(@RequestBody @Validated UpdateCourseDto updateCourseDto){
        Long compainId=1232141425L;
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.updateCrourseBase(compainId,updateCourseDto);
        return   courseBaseInfo;
    }


}
