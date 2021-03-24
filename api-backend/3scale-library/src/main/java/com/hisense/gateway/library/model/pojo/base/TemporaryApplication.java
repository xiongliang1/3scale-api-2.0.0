/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/2/19
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
import java.util.Date;

import static com.hisense.gateway.library.constant.BaseConstants.DATE_TIME_FORMAT;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "temporary_application")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemporaryApplication {
    private static final long serialVersionUID = 5105223900137785597L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;//订阅名称
    private Integer userId;//订阅ID
    private String system;//订阅系统
    private Integer apiId;
    private Integer instanceId;
    private String scaleApplicationId;
    private Integer apiPlanId;
    private Integer status;//0删除,1正常,2隐藏
    private Integer createId; // 创建人

    //@Column(columnDefinition = "datetime(0) default null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date createTime;

    //@Column(columnDefinition = "datetime(0) default null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date updateTime;

    public TemporaryApplication(String name, Integer userId, String system,
                                Integer apiId, Integer apiPlanId, Integer status,
                                Integer createId, Date createTime, Date updateTime) {
        this.name = name;
        this.userId = userId;
        this.system = system;
        this.apiId = apiId;
        this.apiPlanId = apiPlanId;
        this.status = status;
        this.createId = createId;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}
