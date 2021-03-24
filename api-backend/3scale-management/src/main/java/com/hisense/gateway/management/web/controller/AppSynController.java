/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2019/11/25
 */
package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.AppSynDto;
import com.hisense.gateway.management.service.AppSynService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static com.hisense.gateway.library.constant.BaseConstants.URL_APPSYN;

@RequestMapping(URL_APPSYN)
@RestController
public class AppSynController {
    @Autowired
    AppSynService appSynService;

    @GetMapping("/")
    public Result<Boolean> appSyn(HttpServletRequest request) {
        int instanceId = (null == request.getParameter("instanceId") ? 0 :
                Integer.parseInt(request.getParameter("instanceId")));
        String accountId = request.getParameter("accountId");
        AppSynDto appSynDto = new AppSynDto();
        appSynDto.setAccountId(accountId);
        appSynDto.setInstanceId(instanceId);
        return appSynService.synApplication(appSynDto);
    }
}
