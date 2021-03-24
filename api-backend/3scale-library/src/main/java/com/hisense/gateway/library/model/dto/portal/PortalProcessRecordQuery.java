/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.model.dto.portal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.models.auth.In;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 订阅数据查询入参
 * @date 2020/09/185 14:53
 * @author liyouzhi.ex
 * @version v1.0
 */
@Data
public class PortalProcessRecordQuery {
    private String   apiName;//名称
    private Integer  page;
    private Integer  size;
    private String  sort;
    private List<Integer> applicationSystem;//订阅系统
    private List<Integer>  approvalStatus;//审批状态
    private boolean isApprovalComplete = false;//是否审批完成
}
