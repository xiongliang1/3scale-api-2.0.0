/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/18 @author peiyun
 */
package com.hisense.gateway.library.model.pojo.base;

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
@Table(name = "publish_api_group")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishApiGroup implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private Integer categoryOne;
    private Integer categoryTwo;
    private Integer system;
    private Integer status = 1;
    private String projectId;
    private String tenantId;

    @Column(columnDefinition = "varchar(1024) default null")
    private String description;//mingguilai.ex

    @Column(columnDefinition = "number(1)")
    private Integer environment;//liyouzhi.ex-20200924

    String creator;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date updateTime;

    @Transient
    private Integer apiNum; // api数量

    @Transient
    private String systemName;

    @Transient
    private String systemEnName;

    @Transient
    private String categoryOneName;

    @Transient
    private String categoryTwoName;

    @Transient
    private Integer tolerate;

    public PublishApiGroup(String name, Integer categoryOne, Integer categoryTwo, Integer system, String projectId,
                           String tenantId, Date createTime, Date updateTime, String description) {
        this.name = name;
        this.categoryOne = categoryOne;
        this.categoryTwo = categoryTwo;
        this.system = system;
        this.projectId = projectId;
        this.tenantId = tenantId;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.description = description;
    }
}
