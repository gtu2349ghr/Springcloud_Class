package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CoursePbulicDto implements Serializable {
    //先是基本信息，营销信息
    private  CourseBaseInfoDto coursebase;
    //还有师资信息表
    //还有课程信息表
    private  List<TeachPlanDto> teachPlans;
}
