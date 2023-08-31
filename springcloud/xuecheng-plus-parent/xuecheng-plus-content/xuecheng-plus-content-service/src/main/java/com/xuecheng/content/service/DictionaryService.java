package com.xuecheng.content.service;



import com.xuecheng.content.model.po.Dictionary;

import java.util.List;

/**
 * <p>
 * 数据字典 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-08-07
 */
public interface DictionaryService {
     List<Dictionary> querybyCode(String code);
}
