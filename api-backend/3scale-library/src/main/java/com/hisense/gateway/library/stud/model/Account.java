/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.stud.model;

import lombok.Data;

@Data
public class Account {
    private Long id;
    private String accessToken;
    private String orgName;
    private String username;
    private String email;
    private String password;
}
