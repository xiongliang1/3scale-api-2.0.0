/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020-02-19 @author jinshan
 */
package com.hisense.gateway.library.model.dto.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hisense.gateway.library.model.pojo.base.PublishApplication;
import com.hisense.gateway.library.stud.model.Proxy;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.Date;
import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.DATE_TIME_FORMAT;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishApplicationDto {
    private Integer id;

    @JsonProperty("name")
    // @Setter(AccessLevel.NONE)
    private String name;

    private Integer system;
	private Integer systemName;

    @Setter(AccessLevel.NONE)
    private PublishApiDto publishApiDto;

    private Integer instanceId;
    private String scaleApplicationId;
    private Integer apiPlanId;
    /**
     * 状态: 0-删除,1-正常,2-隐藏
     */
    private Integer status;
    /**
     * 创建者
     */
    private String creator;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date updateTime;

    private String description;
    private String state;

    private String appId;

    private String appKey;

    public PublishApplicationDto() {
    }

    public PublishApplicationDto(PublishApplication publishApplication) {
        this.id = publishApplication.getId();
        this.name = publishApplication.getName();
        this.system = publishApplication.getSystem();
        this.publishApiDto = null == publishApplication.getPublishApi() ?
                null : new PublishApiDto(publishApplication.getPublishApi());
        this.instanceId = publishApplication.getInstance().getId();
        this.scaleApplicationId = publishApplication.getScaleApplicationId();
        this.apiPlanId = publishApplication.getApiPlan().getId();
        this.status = publishApplication.getStatus();
        this.creator = publishApplication.getCreator();
        this.createTime = publishApplication.getCreateTime();
        this.description = publishApplication.getDescription();
        this.state = publishApplication.getState();
        this.appId = publishApplication.getAppId();
        this.appKey = publishApplication.getAppKey();
    }

    /**
     * API分组ID
     */
    private Integer groupId;
    /**
     * 环境信息
     */
    private List<String> partitions;
    private Integer bindingPolicyId;
    private String bindingPolicyName;
    private Proxy proxy;
    private List<ApiMappingRuleDto> apiMappingRuleDtos;
    private String version;
}
