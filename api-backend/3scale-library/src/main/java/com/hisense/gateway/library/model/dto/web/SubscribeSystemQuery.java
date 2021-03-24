/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.model.dto.web;

import com.hisense.gateway.library.model.base.TimeQuery;
import lombok.Data;

import java.util.List;

@Data
public class SubscribeSystemQuery {
    private Integer page;
    private Integer size;
    private List<Integer> system;
    private List<String> user;

    private String sort;
    private TimeQuery timeQuery;
}
