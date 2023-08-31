package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachPlanService;
import io.swagger.annotations.ApiModelProperty;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class TeachPlanServiceImpl implements TeachPlanService {
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;
    @Override
    public List<TeachPlanDto> selectTreeNodes(Long courseId) {
        //因为这个结果我们已经在mapper.xml封装好了所以直接在mysql曾做了
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        //在这里还要根据id判断是新增章节还是修改章节
        Long id = saveTeachplanDto.getId();
        if(id==null){
            //表示新增章节
            Long courseId = saveTeachplanDto.getCourseId();
            Long parentid = saveTeachplanDto.getParentid();
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            //然后这里还应该设置他的order顺序
            LambdaQueryWrapper<Teachplan> Wrapper = new LambdaQueryWrapper<>();
            LambdaQueryWrapper<Teachplan> eq = Wrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentid);
            Integer count = teachplanMapper.selectCount(eq);
            teachplan.setOrderby(count+1);
            int insert = teachplanMapper.insert(teachplan);
            if(insert<=0){
                XueChengPlusException.cast("新增失败");
            }
        }else{
            //表示修改章节
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            int i = teachplanMapper.updateById(teachplan);
            if(i<=0){
                XueChengPlusException.cast("修改章节失败");
            }
        }
    }

    /**
     * 添加视频给章节
     * @param bindTeachplanMediaDto
     */
    @Override
    public void updateVideoPass(BindTeachplanMediaDto bindTeachplanMediaDto) {
         //先查数据库有没有记录t
        Teachplan teachplan = teachplanMapper.selectById(bindTeachplanMediaDto.getTeachplanId());
        if(teachplan==null){
            XueChengPlusException.cast("课程不存在");
        }
        //再查数据库有没有视频数据
        LambdaQueryWrapper<TeachplanMedia> teachplanMediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanMediaLambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId,bindTeachplanMediaDto.getTeachplanId());
        TeachplanMedia teachplanMedia1 = teachplanMediaMapper.selectOne(teachplanMediaLambdaQueryWrapper);
        if(teachplanMedia1==null){
            //那么就进行插入
            TeachplanMedia teachplanMedia = new TeachplanMedia();
            teachplanMedia.setTeachplanId(bindTeachplanMediaDto.getTeachplanId());
            teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
            teachplanMedia.setCourseId(teachplan.getCourseId());
            teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
            teachplanMedia.setCreateDate(LocalDateTime.now());
            int insert = teachplanMediaMapper.insert(teachplanMedia);
            if(insert<=0){
                XueChengPlusException.cast("插入章节失败");
            }
            return ;

        }
        //之后呢进行更新
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setTeachplanId(bindTeachplanMediaDto.getTeachplanId());
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        //然后更新
        int i = teachplanMediaMapper.update(teachplanMedia,teachplanMediaLambdaQueryWrapper);
        if(i<=0){
            XueChengPlusException.cast("更新视频加入内同表失败");
        }

    }
}
