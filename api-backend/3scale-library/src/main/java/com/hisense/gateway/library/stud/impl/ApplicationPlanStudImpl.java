/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/20 @author peiyun
 */
package com.hisense.gateway.library.stud.impl;

import com.alibaba.fastjson.JSON;
import com.hisense.gateway.library.constant.SystemNameConstant;
import com.hisense.gateway.library.stud.ApplicationPlanStud;
import com.hisense.gateway.library.stud.model.AppPlanDto;
import com.hisense.gateway.library.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ApplicationPlanStudImpl implements ApplicationPlanStud {

    private static final String PATH_APPLICATION_PLANS = "/admin/api/application_plans.json";
    private static final String PATH_SERVICES = "/admin/api/services/";

    @Override
    public AppPlanDto createApplicationPlan(String host, String accessToken, String serviceId, String name,
                                            String systemName) {
        log.info("******start to invoke createApplicationPlan, "
                        + "host is {}, serviceId is {}",
                host, serviceId);
        AppPlanDto dto = new AppPlanDto();
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("access_token", accessToken);
            map.put("name", name);
            map.put("system_name", systemName);
            map.put("state_event", SystemNameConstant.STATE_EVENT_PUBLISH);
            String rlt =
                    HttpUtil.sendPost(host + PATH_SERVICES + serviceId + "/application_plans.json",map);
            log.info("******end to invoke createApplicationPlan, "
                            + "host is {}, serviceId is {}, rlt is {}",
                    host, serviceId, rlt);
            if (null == rlt) {
                return dto;
            } else {
                dto = JSON.parseObject(rlt, AppPlanDto.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke createApplicationPlan, "
                            + "host is {}, serviceId is {}, e is {}",
                    host, serviceId, e);
        }
        return dto;
    }

    @Override
    public void updateApplicationPlan(String host, String accessToken, String serviceId, String scalePlanId,
                                      Integer stateEvent) {
        log.info("******start to invoke updateApplicationPlan, "
                        + "host is {}, serviceId is {}",
                host, serviceId);
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("access_token", accessToken);
            if (0 == stateEvent) {//隐藏
                map.put("state_event", SystemNameConstant.STATE_EVENT_HIDE);
            } else if (1 == stateEvent) {
                map.put("state_event", SystemNameConstant.STATE_EVENT_PUBLISH);
            }
            String rlt =
                    HttpUtil.sendPut(host + PATH_SERVICES + serviceId + "/application_plans/"+scalePlanId+".json",map);
            log.info("******end to invoke updateApplicationPlan, "
                            + "host is {}, serviceId is {}, rlt is {}",
                    host, serviceId, rlt);
        } catch (Exception e) {
            log.error("******fail to invoke updateApplicationPlan, "
                            + "host is {}, serviceId is {}, e is {}",
                    host, serviceId, e);
        }
    }
}
