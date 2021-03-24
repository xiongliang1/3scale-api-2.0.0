/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/26 @author peiyun
 */
package com.hisense.gateway.library.model.dto.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hisense.gateway.library.model.base.meta.RequestBody;
import com.hisense.gateway.library.model.base.meta.ResponseBody;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiMappingRuleDto {
    private String httpMethod;
    private String pattern;

    List<RequestBody> requestParams;// 请求参数
    RequestBody requestBody; // 请求体
    ResponseBody responseBody;// 返回体

    Integer partition;
}
