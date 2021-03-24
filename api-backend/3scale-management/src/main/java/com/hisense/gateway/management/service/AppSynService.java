/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/3/3
 */
package com.hisense.gateway.management.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.AppSynDto;

public interface AppSynService {
    Result<Boolean> synApplication(AppSynDto appSynDto);
}
