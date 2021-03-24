/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/3/20 @author peiyun
 */
package com.hisense.gateway.library.model.dto.portal;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationCountRes {

    private String planId;
    private String scalePlanId;
    private int applications;
    private boolean isSubscribed;
    private String appSystem;
    private Integer system;
    private Integer status;
}
