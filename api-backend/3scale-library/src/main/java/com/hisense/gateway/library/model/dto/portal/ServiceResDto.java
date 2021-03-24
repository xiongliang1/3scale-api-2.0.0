/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/3/10 @author peiyun
 */
package com.hisense.gateway.library.model.dto.portal;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceResDto {

    private ServiceRes service;

    private String systemName;

    private String description;

    private TargetRes target;

    private List<TargetRes> secTarget;
}
