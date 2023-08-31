package com.xuecheng.content.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.DictionaryMapper;
import com.xuecheng.content.model.po.Dictionary;
import com.xuecheng.content.service.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class DictionaryServiceImpl implements DictionaryService {
    @Resource
    DictionaryMapper dictionaryMapper;
    @Override
    public List<Dictionary> querybyCode(String code) {
        log.info("开始进入了数据字典实现类");
        LambdaQueryWrapper<Dictionary> wrapper = new LambdaQueryWrapper<>();
        //分割字符串取前三位
        String substring = code.substring(0, 3);
        wrapper.eq(StringUtils.isNotEmpty(code),Dictionary::getCode,substring);
        //这里
        List<Dictionary> dictionaries = dictionaryMapper.selectList(wrapper);
        for(Dictionary dictionary:dictionaries){
            System.out.println(dictionary.getItemValues());
        }
        return dictionaries;
    }
}
