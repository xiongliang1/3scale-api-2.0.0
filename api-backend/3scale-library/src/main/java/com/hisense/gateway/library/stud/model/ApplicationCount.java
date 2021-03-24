/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @author douyanwei
 * 2019/11/25
 */
package com.hisense.gateway.library.stud.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationCount {
    private String planId;
    private int applications;
    private boolean isSubscribed;
}
