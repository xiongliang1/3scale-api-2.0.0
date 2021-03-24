package com.hisense.gateway.library.model.dto.web;

import lombok.Data;

import java.util.List;

/**
 * @Author: Gosin
 * @Date: 2020/12/8 15:00
 */
@Data
public class SapConvertPromoteDto{
    Integer id;
    String env;
    /**
     * 是否兼容
     */
    String isCompatible;
    /**
     * 环境信息
     */
    private List<String> partitions;

    /**
     * 是否创建(true:创建,false:非创建)
     */
    Boolean create;
}
