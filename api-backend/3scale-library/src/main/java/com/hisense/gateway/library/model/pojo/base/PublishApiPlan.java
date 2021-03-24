/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/17 @author wangjinshan
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
@Table(name = "publish_api_plan")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishApiPlan implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer apiId;
    private Integer instanceId;
    private Long scalePlanId;
    /**
     * 状态0 删除 1正常 2隐藏
     */
    private Integer status;

    private String creator;

    //@Column(columnDefinition = "datetime(0) default null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date createTime;

    //@Column(columnDefinition = "datetime(0) default null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_MS_FORMAT)
    private Date updateTime;

    private String name;

    public PublishApiPlan(Integer apiId, Integer instanceId, Long scalePlanId, Integer status,
                          String creator, Date createTime, Date updateTime, String name) {
        this.apiId = apiId;
        this.instanceId = instanceId;
        this.scalePlanId = scalePlanId;
        this.status = status;
        this.creator = creator;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.name = name;
    }
}
