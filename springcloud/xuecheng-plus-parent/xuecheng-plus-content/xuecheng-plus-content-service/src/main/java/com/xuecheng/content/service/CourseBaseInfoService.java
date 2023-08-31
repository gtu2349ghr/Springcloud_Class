package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.dto.UpdateCourseDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * 查询课程信息
 */
public interface CourseBaseInfoService {
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);
    public CourseBaseInfoDto updateCrourseBase(Long compainId, UpdateCourseDto updateCourseDto);
    public CourseBaseInfoDto CreateCourse(Long id, AddCourseDto addCourseDto);
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId);
}
