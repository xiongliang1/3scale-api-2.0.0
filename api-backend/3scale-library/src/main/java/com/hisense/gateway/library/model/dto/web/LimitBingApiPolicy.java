/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/3/21 @author peiyun
 */
package com.hisense.gateway.library.model.dto.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LimitBingApiPolicy {
    private Integer instanceId;
    private Integer apiId;
    private Integer policyId;
    private String scalePolicyId;
    /**
     * api名称
     */
    private String apiName;
    /**
     * 分组Id
     */
    private Integer groupId;
    /**
     * 分组名称
     */
    private String groupName;
    /**
     * 发布环境
     */
    private String partition;
    /**
     * 绑定时间
     */
    private Date createTime;

    public LimitBingApiPolicy() {
    }

    public LimitBingApiPolicy(Integer instanceId, Integer apiId, Integer policyId, String scalePolicyId, String apiName,
                              Integer groupId, String groupName, String partition, Date createTime) {
        this.instanceId = instanceId;
        this.apiId = apiId;
        this.policyId = policyId;
        this.scalePolicyId = scalePolicyId;
        this.apiName = apiName;
        this.groupId = groupId;
        this.groupName = groupName;
        this.partition = partition;
        this.createTime = createTime;
    }
}
