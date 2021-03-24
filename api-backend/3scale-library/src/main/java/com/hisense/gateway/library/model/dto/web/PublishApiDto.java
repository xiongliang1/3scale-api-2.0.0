/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020-02-19 @author jinshan
 */
package com.hisense.gateway.library.model.dto.web;

import com.fasterxml.jackson.annotation.*;
import com.hisense.gateway.library.model.base.PolicyHeader;
import com.hisense.gateway.library.model.pojo.base.ApiDocFile;
import com.hisense.gateway.library.model.pojo.base.PublishApi;
import com.hisense.gateway.library.stud.model.Proxy;
import lombok.Data;
import lombok.Getter;

import java.util.Date;
import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.DATE_TIME_FORMAT;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishApiDto {
    private Integer id;

    private Integer groupId;

    private List<Integer> subscribeSystem; //当前用户已经给api订阅过的系统

    @Getter
    private PublishApiGroupDto publishApiGroupDto;

    private String name;
    private Integer subscribeCount;//订阅量
    private String description;
    private Integer partition;// 发布环境,0-内网网关,1-外网网关
    private String environment;//当前环境
    private boolean needSubscribe = false;
    private boolean needAuth = false;
    private boolean needLogging = false;
    private boolean needRecordRet = false;
    private String accessProType ;//接口协议类型：http，webservice
    private List<String> ipWhiteList;
    private List<String> ipBlackList;
    private String url;
    private List<PolicyHeader> requestHeader;//request header
    private List<PolicyHeader> responseHeader;//response header
    /**
     * MappingRulesDto 列表, 其包含(url, method, requestParams, requestBody, responseBody)
     */
    private List<ApiMappingRuleDto> apiMappingRuleDtos;
    private Integer timeout = 0;
    private String hostHeader;
    private List<Integer> fileDocIds;
    private List<ApiDocFile> picFiles;//图片
    private List<ApiDocFile> attFiles;//附件
    private String accessProtocol;//http,https
    private String host;
    private String secretLevel;// API密级等级

    private Integer status;
    private Integer isOnline = 1;// 0-下线,1-上线
    private String creator;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date updateTime;

    private Proxy proxy;

    // for internal use
    @JsonIgnore
    private Integer serviceId;//所属的微服务

    @JsonIgnore
    private Long scaleApiId;

    @JsonIgnore
    private String realmName;// realm_name;

    @JsonIgnore
    private String targetType;// 2,http,

    @JsonIgnore
    private String port;

    @JsonIgnore
    private String mappingRuleHash;

    @JsonIgnore
    private String authType;

    @JsonIgnore
    private Integer systemId;// 所属的系统

    @JsonIgnore
    private String systemName;

    @JsonIgnore
    private Integer sourceType;//API产生来源

    @JsonIgnore
    private String method;

    //-3
    @JsonIgnore
    private Integer isInUse = 0;

//    @JsonIgnore
    private List<String> partitions;

    //@JsonIgnore
    private Integer bindingPolicyId;

    //@JsonIgnore
    private String bindingPolicyName;

//    @JsonIgnore
    private String version;

    private String secretToken;//网管校验token（3scale网管与业务系统接口交互）

    @JsonIgnore
    private String requestProduction;

    private Boolean bindingPolicyEnabled;
    private Integer bindingPolicyValue;
    private String bindingPolicyPeriod;

    private Integer alertPolicyId;
    private String alertPolicyName;

    public PublishApiDto() {
    }

    public PublishApiDto(Integer id, String name, String description, String authType, String realmName, String url,
                         String accessProtocol, String targetType, String host, String port, Integer status,
                         Date createTime, Date updateTime, Integer isOnline, PublishApiGroupDto publishApiGroupDto,
                         String creator) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.authType = authType;
        this.realmName = realmName;
        this.url = url;
        this.accessProtocol = accessProtocol;
        this.targetType = targetType;
        this.host = host;
        this.port = port;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.isOnline = isOnline;
        this.publishApiGroupDto = publishApiGroupDto;
        this.creator = creator;
    }

    public PublishApiDto(PublishApi publishApi) {
        this.id = publishApi.getId();
        this.accessProtocol = publishApi.getAccessProtocol();
        this.authType = publishApi.getAuthType();
        this.createTime = publishApi.getCreateTime();
        this.description = publishApi.getDescription();
        this.host = publishApi.getHost();
        this.name = publishApi.getName();
        this.port = publishApi.getPort();
        this.realmName = publishApi.getRealmName();
        this.status = publishApi.getStatus();
        this.targetType = publishApi.getTargetType();
        this.updateTime = publishApi.getUpdateTime();
        this.url = publishApi.getUrl();
        this.publishApiGroupDto = new PublishApiGroupDto(publishApi.getGroup());
        this.creator = publishApi.getCreator();
        this.isOnline = publishApi.getIsOnline();
    }

    @JsonIgnore
    public String getSimpleName() {
        return String.format("API(partition(%s),system(%s),name(%s),url(%s))", partition, systemName, name, url);
    }

    @Override
    public String toString() {
        return "PublishApiDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", authType='" + authType + '\'' +
                ", scaleApiId=" + scaleApiId +
                ", realmName='" + realmName + '\'' +
                ", url='" + url + '\'' +
                ", accessProtocol='" + accessProtocol + '\'' +
                ", targetType='" + targetType + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", isOnline=" + isOnline +
                ", isInUse=" + isInUse +
                ", groupId=" + groupId +
                ", partitions=" + partitions +
                ", bindingPolicyId=" + bindingPolicyId +
                ", bindingPolicyName='" + bindingPolicyName + '\'' +
                ", proxy=" + proxy +
                ", mappingRulesDtos=" + apiMappingRuleDtos +
                ", publishApiGroupDto=" + publishApiGroupDto +
                ", version='" + version + '\'' +
                ", creator='" + creator + '\'' +
                ", requestProduction='" + requestProduction + '\'' +
                '}';
    }
}
