/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/20 @author peiyun
 */
package com.hisense.gateway.library.stud.impl;

import com.alibaba.fastjson.JSON;
import com.hisense.gateway.library.stud.MappingRuleStud;
import com.hisense.gateway.library.stud.model.MappingRuleDto;
import com.hisense.gateway.library.stud.model.MappingRuleDtos;
import com.hisense.gateway.library.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MappingRuleStudImpl implements MappingRuleStud {

    private static final String PATH_SERVICES = "/admin/api/services/";

    @Override
    public MappingRuleDto createMapingRule(String host, String accessToken, String serviceId, String httpMethod, String pattern,
                                           String metricId) {
        log.info("******start to invoke createMapingRule, host is {}, serviceId is {}",
                host, serviceId);
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("access_token", accessToken);
            map.put("http_method", httpMethod);
            if (!pattern.startsWith("/")) {
                pattern = "/" + pattern;
            }
            map.put("pattern", pattern);
            map.put("delta", "1");
            map.put("metric_id", metricId);
            map.put("position", "1");
            String rlt =
                    HttpUtil.sendPost(host + PATH_SERVICES + serviceId + "/proxy/mapping_rules.json",map);
            log.info("******end to invoke createMapingRule, "
                            + "host is {}, serviceId is {}, rlt is {}",
                    host, serviceId, rlt);
//            if (null == rlt) {
//                return dto;
//            } else {
//                dto = JSON.parseObject(rlt, AppPlanDto.class);
//            }
        } catch (Exception e) {
            log.error("******fail to invoke createMapingRule, host is {}, serviceId is {}, e is {}",
                    host, serviceId, e);
        }
        return null;
    }

    @Override
    public List<MappingRuleDto> searchMappingRulesList(String host, String accessToken, String serviceId) {
        log.info("******start to invoke searchMappingRulesList, "
                        + "host is {}, serviceId is {}",
                host, serviceId);
        MappingRuleDtos mappingRuleDtos = new MappingRuleDtos();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_SERVICES + serviceId + "/proxy/mapping_rules.json?access_token=" + accessToken);
            log.info("******end to invoke searchMappingRulesList, host is {}, id is {}, rlt is {}",
                    host, serviceId, rlt);
            if (null == rlt) {
                return null;
            } else {
                mappingRuleDtos = JSON.parseObject(rlt, MappingRuleDtos.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke searchMappingRulesList, "
                            + "host is {}, id is {}, e is {}",
                    host, serviceId, e);
        }
        return mappingRuleDtos.getMapping_rules();
    }

    @Override
    public MappingRuleDto updateMappingRules(String host, String accessToken, String serviceId, String mappingRuleId,
                                             String httpMethod, String pattern) {
        log.info("******start to invoke updateMappingRules, host is {}, serviceId is {}",
                host, serviceId);
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("access_token", accessToken);
            map.put("http_method", httpMethod);
            map.put("pattern", pattern);
            String rlt =
                    HttpUtil.sendPatch(host + PATH_SERVICES + serviceId + "/proxy/mapping_rules/+"+mappingRuleId+".json",map);
            log.info("******end to invoke updateMappingRules,host is {}, serviceId is {}, rlt is {}",
                    host, serviceId, rlt);
//            if (null == rlt) {
//                return dto;
//            } else {
//                dto = JSON.parseObject(rlt, AppPlanDto.class);
//            }
        } catch (Exception e) {
            log.error("******fail to invoke updateMappingRules, host is {}, serviceId is {}, e is {}",
                    host, serviceId, e);
        }
        return null;
    }

    @Override
    public void deleteMappingRules(String host, String accessToken, String serviceId, String mappingRuleId) {
        log.info("******start to invoke deleteMappingRules, host is {}, serviceId is {}",
                host, serviceId);
        try {
            String rlt =
                    HttpUtil.sendDel(host + PATH_SERVICES + serviceId + "/proxy/mapping_rules/+"+mappingRuleId+
                            ".json?access_token="+accessToken);
            log.info("******end to invoke deleteMappingRules,host is {}, serviceId is {}, rlt is {}",
                    host, serviceId, rlt);
        } catch (Exception e) {
            log.error("******fail to invoke deleteMappingRules, host is {}, serviceId is {}, e is {}",
                    host, serviceId, e);
        }
    }
}
