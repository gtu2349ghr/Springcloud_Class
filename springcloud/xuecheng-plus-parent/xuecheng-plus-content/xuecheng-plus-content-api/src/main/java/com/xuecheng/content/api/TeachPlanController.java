package com.xuecheng.content.api;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.TeachPlanService;
import com.xuecheng.content.serviceClient.MediaServiceClient;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin
public class TeachPlanController {
    @Resource
    TeachPlanService teachPlanServicel;
        @Autowired
    MediaServiceClient mediaServiceClient;

    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId",name = "课程基础Id值",required = true,dataType = "Long",paramType = "path")
    @GetMapping("teachplan/{courseId}/tree-nodes")
    public List<TeachPlanDto> getTreeNodes(@PathVariable Long courseId){
        return teachPlanServicel.selectTreeNodes(courseId);
    }

    /**
     * 新增节/章节/修改
     * @param teachplan
     */
    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan( @RequestBody SaveTeachplanDto teachplan){
        teachPlanServicel.saveTeachplan(teachplan);
    }

    /**
     * 添加视频给章节
     * @param bindTeachplanMediaDto
     */
    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachPlanServicel.updateVideoPass(bindTeachplanMediaDto);
    }
    @ApiOperation(value = "测试")
    @GetMapping("/tack/dd")
    public String associationMea(){
                File file = new File("D:\\BBB\\111.html");
        String upload="lll";
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        try {
            upload = mediaServiceClient.upload(multipartFile, "course/111.html");
        } catch (IOException e) {
            e.printStackTrace();
        }

            return  upload;
//        return "ajsiodjas";
    }


}
