package com.hisense.gateway.library.stud.impl;/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2020 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/2/24
 */

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.stud.LimitStud;
import com.hisense.gateway.library.stud.model.LimitDto;
import com.hisense.gateway.library.utils.HttpUtil;
import com.hisense.gateway.library.stud.model.Limit;
import com.hisense.gateway.library.utils.XmlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class LimitStudImpl implements LimitStud {

    private static final String PATH_LIMIT_CREATE = "/admin/api/application_plans/%s/metrics/%s/limits.xml?access_token=%s";
    private static final String PATH_LIMIT_UPDATE = "/admin/api/application_plans/%s/metrics/%s/limits/%s.xml?access_token=%s";
    private static final String PATH_LIMITS_LIST_PER_APPLICATION_PLAN = "/admin/api/application_plans/%s/limits.xml?access_token=%s";

    @Override
    public Limit limitCreate(String host, String accessToken, Limit limit) {
        log.info("******start to invoke limitCreate,"
                        + "host is {}, limit is {}",
                host, limit);
        Map<String, String> map = new HashMap<String, String>();
        map.put("period", limit.getPeriod());
        map.put("value", limit.getValue());
        try {
            String rlt =
                    HttpUtil.sendPost(host + String.format(PATH_LIMIT_CREATE, limit.getPlanId(), limit.getMetricId(), accessToken), map);
            log.info("******end to invoke limitCreate,"
                            + "host is {}, limit is {}, rlt is {}",
                    host, limit, rlt);
            if(null==rlt) {
                return null;
            }else {
                limit = (Limit)
                        XmlUtils.xmlStrToObject(Limit.class, rlt);
                log.info("limitCreate result:{}", JSONObject.toJSONString(limit));
            }
        } catch (Exception e) {
            log.error("******fail to invoke limitUpdate,"
                            + "host is {}, limit is {}, e is {}",
                    host, limit, e);
        }
        return limit;
    }

    @Override
    public Limit limitUpdate(String host, String accessToken, Limit limit) {
        log.info("******start to invoke limitCreate,"
                        + "host is {}, limit is {}",
                host, limit);
        Map<String, String> map = new HashMap<String, String>();
        map.put("period", limit.getPeriod());
        map.put("value", limit.getValue());
        try {
            String rlt =
                    HttpUtil.sendPut(host + String.format(PATH_LIMIT_UPDATE, limit.getPlanId(), limit.getMetricId(), limit.getId(), accessToken), map);
            log.info("******end to invoke limitUpdate,"
                            + "host is {}, limit is {}, rlt is {}",
                    host, limit, rlt);
            if(null==rlt) {
                return null;
            }else {
                limit = (Limit)
                        XmlUtils.xmlStrToObject(Limit.class, rlt);
                log.info("limitUpdate result:{}", JSONObject.toJSONString(limit));
            }
        } catch (Exception e) {
            log.error("******fail to invoke findFeatureByServiceId,"
                            + "host is {}, limit is {}, e is {}",
                    host, limit, e);
        }
        return limit;
    }

    @Override
    public void limitDelete(String host, String accessToken, Limit limit) {
        log.info("******start to invoke limitDelete,"
                        + "host is {}, limit is {}",
                host, limit);
        try {
            String rlt =
                    HttpUtil.sendDel(host + String.format(PATH_LIMIT_UPDATE, limit.getPlanId(), limit.getMetricId(), limit.getId(), accessToken));
            log.info("******end to invoke limitDelete,"
                            + "host is {}, limit is {}",
                    host, limit, rlt);
        } catch (Exception e) {
            log.error("******fail to invoke limitDelete,"
                            + "host is {}, limit is {}",
                    host, limit, e);
        }
    }

    @Override
    public LimitDto limitsListPerApplicationPlan(String host, String accessToken, String scalePlanId) {
        log.info("******start to invoke limitsListPerApplicationPlan,"
                        + "host is {}, scalePlanId is {}",
                host, scalePlanId);
        LimitDto limitDto =  null;
        try {
            String rlt =
                    HttpUtil.sendGet(host + String.format(PATH_LIMITS_LIST_PER_APPLICATION_PLAN, scalePlanId, accessToken));
            log.info("******end to invoke limitsListPerApplicationPlan,"
                            + "host is {}, scalePlanId is {}, rlt is {}",
                    host, scalePlanId, rlt);
            if(null==rlt || "".equals(rlt)) {
                return null;
            }else {
                limitDto = (LimitDto)
                        XmlUtils.xmlStrToObject(LimitDto.class, rlt);
                log.info("limitsListPerApplicationPlan result:{}", JSONObject.toJSONString(limitDto));
            }
        } catch (Exception e) {
            log.error("******fail to invoke findFeatureByServiceId,"
                            + "host is {}, scalePlanId is {}, e is {}",
                    host, scalePlanId, e);
        }
        return limitDto;
    }

}
