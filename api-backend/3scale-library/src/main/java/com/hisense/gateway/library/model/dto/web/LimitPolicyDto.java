/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/25 @author peiyun
 */
package com.hisense.gateway.library.model.dto.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LimitPolicyDto {
    @JsonProperty("policyId")
    private Integer policyId;

    @JsonProperty("name")
    private String name;//策略名称

    @JsonProperty("description")
    private String description;

    private Boolean enabled;//是否开启

    /**
     * 次数阈值
     */
    private String value;//次数阈值

    /**
     * 时间窗口
     */
    private String period;//时间窗口
    private Integer pageNum;
    private Integer pageSize;
    private String[] sort;
    private Integer apiId;//apiiD
}
