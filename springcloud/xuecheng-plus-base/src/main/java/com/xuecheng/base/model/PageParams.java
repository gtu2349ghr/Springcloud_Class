package com.xuecheng.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * 分页类
 */
@Data
@ToString
public class PageParams {

    //当前页码
    @ApiModelProperty("分页插件当前页码")
    private Long pageNo = 1L;

    //每页记录数默认值
    @ApiModelProperty("分页插件的每页记录数")
    private Long pageSize =10L;

    public PageParams(){

    }

    public PageParams(long pageNo,long pageSize){
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }



}
