/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/3/10 @author peiyun
 */
package com.hisense.gateway.library.model.dto.portal;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceRes {
    private String id;
    private String accessToken;

    private String name;
    private String systemName;
    private String description;
    private String backendVersion;
    private Date createTime;
    private Date updateTime;
    private String target;
    private String secTarget;
}
