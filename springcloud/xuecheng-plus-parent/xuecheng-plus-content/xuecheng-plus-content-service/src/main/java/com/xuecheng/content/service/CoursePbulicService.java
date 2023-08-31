package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePbulicDto;
import com.xuecheng.content.model.po.CoursePublish;

public interface CoursePbulicService {
    /**
     * 查询基本信息，预览页面
     * @param courseId
     * @return
     */
    public CoursePbulicDto selectPublic(Long courseId);

    public void commitCoursePublic(Long companyId,Long courseId);

    /**
     * 课程发布接口
     * @param companyId
     * @param courseId
     */
    public  void publish(Long companyId,Long courseId);

    /**
     * 查询课程发布表
     * @param courseId
     * @return
     */
    CoursePublish getCoursePublish(Long courseId);
}
