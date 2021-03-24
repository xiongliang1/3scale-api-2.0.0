/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @author douyanwei
 * 2019/11/25
 */
package com.hisense.gateway.library.stud.model;

import lombok.Data;

@Data
public class ApplicationSearchForm {
    private String serviceName;
    private String planName;
    private String since;
    private String until;
    private String state;
    private String pageNum;
    private String pageSize;
	// portal
    private Integer system;
    private Integer status;
}
