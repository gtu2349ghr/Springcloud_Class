package com.xuecheng.learning.service;

import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;

public interface MyCourseTableService {
    XcChooseCourseDto addCourseTable(String userId, Long courseId);
    XcCourseTablesDto selectCourState(String userId, Long courseId);
}
