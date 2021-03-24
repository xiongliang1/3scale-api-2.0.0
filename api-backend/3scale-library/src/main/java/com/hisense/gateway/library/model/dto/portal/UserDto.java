/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author wuhuhu
 */
package com.hisense.gateway.library.model.dto.portal;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * OverviewDto
 *
 * @author wangjinshan
 * @version v1.0
 * @date 2019-11-21 15:39
 */
@Getter
@Setter
public class UserDto implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;

    Integer id;
    String name;
    String pwd;
    Date createTime;
    Integer type;
    Integer role;
    String email;
    String phone;
    String description;
    Integer status;
    Integer domain;
    String orgName;
}
