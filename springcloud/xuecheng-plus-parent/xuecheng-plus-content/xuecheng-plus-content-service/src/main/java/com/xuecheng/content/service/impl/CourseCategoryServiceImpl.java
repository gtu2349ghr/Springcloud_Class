package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Resource
    CourseCategoryMapper courseCategoryMapper;

    @Override
    /**
     * 这里开始进行排序了
     */
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        //先拿到数据
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);
        //然后定义一个我们返回前端的list集合
        ArrayList<CourseCategoryTreeDto> list = new ArrayList<>();
        //然后将数据转化为map
        Map<String, CourseCategoryTreeDto> map = courseCategoryTreeDtos.stream().filter(item -> !id.equals(item.getId())).collect(Collectors.toMap(key -> key.getId(), value -> value,(key1,key2)->key2));
        courseCategoryTreeDtos.stream().filter(item->!id.equals(item.getId())).forEach(item->{
            //开始将二级的加入到list
          if(item.getParentid().equals(id)){
              //如果他的二级节点的父节点是传进来的id那么加入到集合
              list.add(item);
          }
          //然后开始将下面的加入到属性中
            //先拿到他的父节点
            CourseCategoryTreeDto courseCategoryTreeParent = map.get(item.getParentid());
            if(courseCategoryTreeParent!=null){
                //如果他的父节点为空，则创建一个list集合
                if(courseCategoryTreeParent.getChildrenTreeNodes()==null){
                    courseCategoryTreeParent.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                //然后将当前流加入到父节点的集合里
                courseCategoryTreeParent.getChildrenTreeNodes().add(item);
            }
        });
        return list;
    }
}
