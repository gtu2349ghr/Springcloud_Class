package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePbulicDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePbulicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
@Api(value = "课程预览发布接口",tags = "课程预览发布接口")
public class CoursePublicController {
    @Autowired
    CoursePbulicService coursePbulicService;
    @RequestMapping("/coursepreview/{courseId}")
    public String test(@PathVariable("courseId") Long courseId, Model model){
        CoursePbulicDto coursePbulicDto = coursePbulicService.selectPublic(courseId);
        log.info("这个是数据：{}",coursePbulicDto);
        model.addAttribute("coursePbulicDto",coursePbulicDto);
        return "course_template";
    }
//    @RequestMapping("/test")
//    public String test11(){
//
//        return "course_template";
//    }
    @RequestMapping("/courseaudit/commit/{courseId}")
    public void courseAudit(@PathVariable("courseId")Long courseId){
        Long companyId=1232141425L;
        coursePbulicService.commitCoursePublic(companyId,courseId);
        return ;
    }
    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId) {

    }
    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId) {
        CoursePublish coursePublish = coursePbulicService.getCoursePublish(courseId);
        return coursePublish;
    }

}
