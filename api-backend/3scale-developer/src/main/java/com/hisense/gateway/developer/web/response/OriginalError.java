/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2018 TenxCloud. All Rights Reserved.
 */

package com.hisense.gateway.developer.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 2018/7/18 @author wuhuhu
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OriginalError {

    long timestamp;
    int status;
    String error;
    String exception;
    String message;
    String path;
}
