package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;

import java.util.List;

public interface TeachPlanService {
     public List<TeachPlanDto> selectTreeNodes (Long courseId);
     public  void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /**
     * 给内容表章节添加视频
     * @param bindTeachplanMediaDto
     */
    void updateVideoPass(BindTeachplanMediaDto bindTeachplanMediaDto);
}
