package com.hisense.gateway.library.model.dto.web;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 一键发生产入参VO
 */
@Data
@ApiModel(description = "一键发生产入参VO")
public class PromoteApiToProd {
    @ApiModelProperty(value = "项目ID")
    Integer projectId;//项目ID
    @ApiModelProperty(value = "api名称")
    String apiName;//api名称
    @ApiModelProperty(value = "协议")
    String accessProtocol;//协议
    @ApiModelProperty(value = "后端服务")
    String host;//后端服务

    @ApiModelProperty(value = "描述")
    String description;//描述

    @ApiModelProperty(value = "描述")
    String secretLevel;// API密级等级

    @ApiModelProperty(value = "描述")
    boolean needSubscribe = false;//是否需要审批

    @ApiModelProperty(value = "描述")
    boolean needAuth = false;//是否鉴权

    @ApiModelProperty(value = "描述")
    boolean needLogging = false;//是否需要记录日志

    @ApiModelProperty(value = "描述")
    Integer timeout;//超时时间

    @ApiModelProperty(value = "描述")
    String url;;//url前缀
    /**
     * MappingRulesDto 列表, 其包含(url, method, requestParams, requestBody, responseBody)
     */
    @ApiModelProperty(value = "路由规则")
    List<ApiMappingRuleDto> apiMappingRuleDtos;

    @ApiModelProperty(value = "网关校验token")
    String secretToken;
}
