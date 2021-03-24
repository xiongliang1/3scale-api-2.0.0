/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/20 @author peiyun
 */
package com.hisense.gateway.library.model.dto.buz;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hisense.gateway.library.stud.model.ProxyConfigDto;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProxyConfigsDto {
    private List<ProxyConfigDto> innerProxyConfigs;

    private List<ProxyConfigDto> outerProxyConfigs;
}
