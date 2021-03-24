/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/17 @author wangjinshan
 */
package com.hisense.gateway.library.model.pojo.base;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

import static com.hisense.gateway.library.constant.BaseConstants.DATE_TIME_FORMAT;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "publish_application")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

public class PublishApplication implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "varchar2(512) default null")
    private String name;// 订阅名称

    private String creator;

    private Integer system;// 订阅系统所属ID

    @Column(columnDefinition = "varchar2(512) default null")
    private String systemName;// guilai.ming 2020/09/10

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "api_id", nullable = false)
    private PublishApi publishApi;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "instance_id", nullable = false)
    private Instance instance;

    private String scaleApplicationId;

    /**
     * application类型：0-创建api；1-订阅api
     */
    @Column(name = "type",columnDefinition = "number(1) default null")
    //@Column(name = "type",columnDefinition = "int(1) default null")
    private Integer type;

    @Column(columnDefinition = "varchar2(256) default null")
    private String userKey;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "api_plan_id", nullable = false)
    private PublishApiPlan apiPlan;

    /**
     * 订阅状态: 0-取消订阅,1-正常-创建,2-审批通过,3-审批取消，4-重置密匙前的数据
     */
    private Integer status;

    //@Column(columnDefinition = "datetime(0) default null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date createTime;

    //@Column(columnDefinition = "datetime(0) default null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date updateTime;

    @Column(columnDefinition = "varchar2(1024) default null")
    private String description;

    private String state;

    private String appId;

    private String appKey;

    public PublishApplication(String name, String creator, Integer system,
                              PublishApiPlan apiPlan, Integer status,
                              Date createTime, Date updateTime, PublishApi publishApi,
                              String description, String scaleApplicationId) {
        this.name = name;
        this.creator = creator;
        this.system = system;
        this.publishApi = publishApi;
        this.apiPlan = apiPlan;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.description = description;
        this.scaleApplicationId = scaleApplicationId;
    }

    // 2020/10/20 guilai.ming
    public PublishApplication(Integer system, String scaleApplicationId) {
        this.system = system;
        this.scaleApplicationId = scaleApplicationId;
    }
}
