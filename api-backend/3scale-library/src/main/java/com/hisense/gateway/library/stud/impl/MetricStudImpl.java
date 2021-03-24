/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.stud.impl;

import com.hisense.gateway.library.stud.MetricStud;
import com.hisense.gateway.library.stud.model.MetricsDto;
import com.hisense.gateway.library.utils.HttpUtil;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MetricStudImpl implements MetricStud {

	private static final String PATH_METRIC_LIST = "/admin/api/services/%s/metrics.json?access_token=%s";
	

	@Override
	public MetricsDto getMetricByServiceId(String host, String accessToken,
                                           String serviceId) {
		log.info("******start to invoke getMetricByServiceId,"
				+ "host is {},serviceId is {}",
    			host,serviceId);
		MetricsDto o =  null;
		try {
			String rlt = 
					HttpUtil.sendGet(host + String.format(PATH_METRIC_LIST,serviceId,accessToken));
			log.info("******end to invoke getMetricByServiceId,"
					+ "host is {},serviceId is {},rlt is {}",
	    			host,serviceId,rlt);
			if(null==rlt) {
				return o;
			}else {
				o = JSON.parseObject(rlt, MetricsDto.class);
			}
		} catch (Exception e) {
			log.info("******fail to invoke getMetricByServiceId,"
					+ "host is {},serviceId is {},e is {}",
	    			host,serviceId,e);
		}
		return o;
	}

}
