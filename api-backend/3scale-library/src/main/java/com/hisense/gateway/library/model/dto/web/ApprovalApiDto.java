/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/3/2 @author peiyun
 */
package com.hisense.gateway.library.model.dto.web;

import lombok.Data;

/**
 * API审批
 */
@Data
public class ApprovalApiDto {
    Integer mark;// 1,查询已办, 2：查询待办
    Integer appId;
    Integer processRecordId;
    String remark;// 审批备注
    Integer status;// 审批状态: 1.初始, 2.完成,审批通过 3.废弃,审批不通过

    Integer updateId;
    String tenantId;
    String projectId;
    String system;
    String apiName;
    String[] sort;
    Integer pageNum;
    Integer pageSize;
    String  supplementContent;//补充说明
}
