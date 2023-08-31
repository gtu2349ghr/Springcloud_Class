package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

/**
 * 树形模型表
 */
@Data
public class TeachPlanDto extends Teachplan {
    //因为他关联了那个媒体的表所以这个类型
    private TeachplanMedia teachplanMedia;
   private List<TeachPlanDto> teachPlanTreeNodes;
}
