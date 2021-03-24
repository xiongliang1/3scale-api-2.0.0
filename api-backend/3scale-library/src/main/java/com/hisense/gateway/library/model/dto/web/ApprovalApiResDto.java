/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/3/3 @author peiyun
 */
package com.hisense.gateway.library.model.dto.web;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ApprovalApiResDto implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;
    Integer apiId;
    String apiName;
    Integer groupId;
    String apiGroupName;
    String systemName;
    String categoryOneName;
    String categoryTwoName;
    Integer processRecordId;

    /**
     * 申请人名称
     */
    String creator;
    /**
     * 项目ID
     */
    String projectId;
    /**
     * 项目名称
     */
    String projectName;

    /**
     * 租户ID
     */
    String tenantId;

    /**
     * 申请租户名称
     */
    String tenantName;

    private Date createTime;

    /**
     * 审批人名称
     */
    String updater;
    private Date updateTime;
    private Integer status;
    private String type;
    private String remark;
    private String ext_var2;

    public ApprovalApiResDto() {
    }

    public ApprovalApiResDto(Integer apiId, String apiName) {
        this.apiId = apiId;
        this.apiName = apiName;
    }

    public ApprovalApiResDto(Integer apiId, String apiName, Integer groupId, String systemName, String createName,
                             Integer processRecordId, Integer status, Date createTime, String type, String remark) {
        this.apiId = apiId;
        this.apiName = apiName;
        this.groupId = groupId;
        this.systemName = systemName;
        this.processRecordId = processRecordId;
        this.creator = createName;
        this.createTime = createTime;
        this.status = status;
        this.type = type;
        this.remark = remark;
    }

    public ApprovalApiResDto(Integer apiId, String apiName, Integer groupId, String systemName,
                             Integer processRecordId, String createName, String projectId, String tenantId,
                             Date createTime, Integer status, String type, String remark) {
        this.apiId = apiId;
        this.apiName = apiName;
        this.groupId = groupId;
        this.apiGroupName = apiGroupName;
        this.systemName = systemName;
        this.processRecordId = processRecordId;
        this.creator = createName;
        this.projectId = projectId;
        this.tenantId = tenantId;
        this.createTime = createTime;
        this.status = status;
        this.type = type;
        this.remark = remark;
    }
}
