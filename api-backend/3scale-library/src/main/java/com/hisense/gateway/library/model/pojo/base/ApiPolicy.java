/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @author douyanwei
 * @date 2020/2/20
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

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "api_policy")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiPolicy {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(columnDefinition = "varchar(1024) default null")
    private String description;

    @Column(columnDefinition = "varchar(1024) default null")
    private String config;

    /**
     * 启用状态
     */
    private Boolean enabled;
    /**
     * 策略类型
     */
    private String type;
    /**
     * 项目Id
     */
    private String projectId;
    /**
     * 创建人
     */
    private String creator;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",timezone = "GMT+8")
    private Date updateTime;

    /**
     * 次数阈值
     */
    @Transient
    private Integer value;

    /**
     * 时间窗口
     */
    @Transient
    private String period;

    /**
     * 绑定
     */
    @Transient
    private Integer bindApiNum;

    public ApiPolicy(String name, String description, String config, Boolean enabled, String type, String projectId,
                     String creator, Date createTime, Date updateTime) {
        this.name = name;
        this.description = description;
        this.config = config;
        this.enabled = enabled;
        this.type = type;
        this.projectId = projectId;
        this.creator = creator;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}
