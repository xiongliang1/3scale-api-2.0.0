/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/11/25 @author peiyun
 */
package com.hisense.gateway.library.stud.impl;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.stud.FeatureStud;
import com.hisense.gateway.library.stud.model.FeatureDtos;
import com.hisense.gateway.library.utils.HttpUtil;
import com.hisense.gateway.library.utils.XmlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FeatureStudImpl implements FeatureStud {

    private static final String PATH_METRIC_LIST = "/admin/api/services/%s/features.xml?access_token=%s";

    @Override
    public FeatureDtos findFeatureByServiceId(String host, String accessToken, String serviceId) {
    	log.info("******start to invoke findFeatureByServiceId,"
				+ "host is {},serviceId is {}",
    			host,serviceId);
    	FeatureDtos featureDtos =  null;
        try {
            String rlt =
                    HttpUtil.sendGet(host + String.format(PATH_METRIC_LIST,serviceId,accessToken));
            log.info("******end to invoke findFeatureByServiceId,"
    				+ "host is {},serviceId is {},rlt is {}",
        			host,serviceId,rlt);
            if(null==rlt) {
                return null;
            }else {
                featureDtos = (FeatureDtos)
                        XmlUtils.xmlStrToObject(FeatureDtos.class, rlt);
                log.info("findFeatureByServiceId result:{}", JSONObject.toJSONString(featureDtos));
            }
        } catch (Exception e) {
        	log.error("******fail to invoke findFeatureByServiceId,"
    				+ "host is {},serviceId is {},e is {}",
        			host,serviceId,e);
        }
        return featureDtos;
    }
}
