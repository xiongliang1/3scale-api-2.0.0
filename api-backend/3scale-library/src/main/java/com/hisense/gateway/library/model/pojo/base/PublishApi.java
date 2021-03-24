/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/17 @author wangjinshan
 */
package com.hisense.gateway.library.model.pojo.base;

import com.fasterxml.jackson.annotation.*;
import com.hisense.gateway.library.model.dto.buz.ApiInstanceDto;
import com.hisense.gateway.library.stud.model.Proxy;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.DATE_TIME_FORMAT;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "publish_api")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishApi implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // For ui input
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "group_id", nullable = true)
    private PublishApiGroup group;

    private String name;

    @Column(columnDefinition = "varchar(1024) default null")
    private String description;

    @Column(name = "publish_partition")
    private Integer partition;// 发布环境,0-内网网关,1-外网网关

    private boolean needSubscribe;
    private boolean needAuth;
    private boolean needLogging;
    private boolean needRecordRet;

    //@Column(columnDefinition = "text default null")
    @Column(columnDefinition = "clob default null")
    private String ipWhiteList;

    @Column(columnDefinition = "clob default null")
    private String ipBlackList;

    private String url;

    @Column(columnDefinition = "clob default null")
    private String requestHeader;

    @Column(columnDefinition = "clob default null")
    private String responseHeader;

    @Column(columnDefinition = "varchar(1024) default null")
    private String mappingRuleIds;

    @Transient
    @JsonIgnore
    private List<ApiMappingRule> apiMappingRules;

    private Integer timeout;

    @Column(columnDefinition = "varchar(256) default null")
    private String hostHeader;

    @Column(columnDefinition = "varchar(1024) default null")
    private String fileDocIds;

    @Column(name="access_pro_type",columnDefinition = "varchar(64) default 'http'")
    private String accessProType = "http";//接口协议类型：http，webservice
    private String accessProtocol;
    private String host;

    private Integer status = 1;
    private Integer isOnline = 0;

    @Transient
    private Integer subscribeCount;

    String creator;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")// TODO
    private Date createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")// TODO
    private Date updateTime;

    /**
     * 告警策略id, guilai.ming 2020/09/10
     */
    private Integer alertPolicyId;

    @Transient
    private Proxy proxy;

    // for internal process
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "service_id", nullable = true)
    private EurekaService eurekaService;

    private String realmName;
    private String targetType;
    private String port;

    private String mappingRuleHash;
    private String authType;
    private Integer systemId;// 所属的系统
    private Integer sourceType;//API产生来源

    @Column(columnDefinition = "number(1) default null")
    //@Column(columnDefinition = "int(1) default null")
    private Integer environment;//guilai 2020/09/16

    @Column(columnDefinition = "number(1) default null")
    private Integer secretLevel;// API密级等级

    @Column(columnDefinition = "varchar(1024) default null")
    private String secretToken;//网关校验token

    @Transient
    private String method;

    @Transient
    private List<ApiInstanceDto> apiInstanceDtos;

    public PublishApi(String name, String authType, Integer groupId, Integer instanceId, Long scaleApiId,
                      Integer status, Integer createId, Date createTime, Date updateTime) {
        this.name = name;
        this.authType = authType;
        this.status = status;
        this.createTime = createTime;
    }

    public PublishApi(String name, String description, String authType, String realmName, String url,
                      String accessProtocol, String targetType, String host,
                      String port, Date createTime, Date updateTime, Integer isOnline) {
        this.name = name;
        this.description = description;
        this.authType = authType;
        this.accessProtocol = accessProtocol;
        this.targetType = targetType;
        this.host = host;
        this.port = port;
        this.realmName = realmName;
        this.url = url;
        this.isOnline = isOnline;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    @JsonProperty("apiGroup")
    public PublishApiGroup getApiGroup() {
        return this.group;
    }

    @JsonIgnore
    public String getSimpleName() {
        return String.format("API(partition(%s),system(%s),name(%s),url(%s))", partition, systemId, name, url);
    }

    @Override
    public String toString() {
        return "PublishApi{" +
                "id=" + id +
                ", environment='" + environment + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", authType='" + authType + '\'' +
                ", accessProtocol='" + accessProtocol + '\'' +
                ", targetType='" + targetType + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", realmName='" + realmName + '\'' +
                ", url='" + url + '\'' +
                ", isOnline=" + isOnline +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", group=" + (group != null ? group.getId() : "") +
                ", apiInstanceDtos=" + (apiInstanceDtos != null ? apiInstanceDtos.size() : "") +
                ", creator=" + creator +
                ", partition=" + partition +
                ", needSubscribe=" + needSubscribe +
                ", needAuth=" + needAuth +
                ", needLogging=" + needLogging +
                ", needRecordRet=" + needRecordRet +
                ", ipWhiteList='" + ipWhiteList + '\'' +
                ", timeout=" + timeout +
                ", hostHeader='" + hostHeader + '\'' +
                ", method='" + method + '\'' +
                ", eurekaService=" + (eurekaService != null ? eurekaService.getId() : "") +
                '}';
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }
}
