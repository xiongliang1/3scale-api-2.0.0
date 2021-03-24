/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther wangjinshan
 * @date 2019/11/25
 */
package com.hisense.gateway.developer.web.controller;

import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.SubscribeSystem;
import com.hisense.gateway.library.model.base.analytics.ApiTrafficStatQuery;
import com.hisense.gateway.library.model.base.monitor.ApiResponseStatistics;
import com.hisense.gateway.library.service.AnalyticsService;
import com.hisense.gateway.library.service.ElasticSearchService;
import com.hisense.gateway.library.stud.model.AnalyticsDto;
import com.hisense.gateway.library.web.form.AnalyticsForm;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.URL_ANALYTICS;

@Slf4j
@RequestMapping("/api/v1/{environment}/analytics")
@RestController
public class AnalyticsController {

    @Autowired
	AnalyticsService analyticsService;

	@Autowired
	ElasticSearchService elasticSearchService;
    
    /*@GetMapping("/applications/{applicationId}/metrics/{metricName}/since/{since}/until/{until}/granularity/{granularity}")
    public AnalyticsDto getMetricByServiceId(@PathVariable String domain,
                                             @PathVariable String applicationId,
                                             @PathVariable String metricName,
                                             @PathVariable String since,
                                             @PathVariable String until,
                                             @PathVariable String granularity) {
    	AnalyticsForm analyticsForm = new AnalyticsForm();
    	analyticsForm.setApplicationId(applicationId);
    	analyticsForm.setGranularity(granularity);
    	analyticsForm.setMetricName(metricName);
    	analyticsForm.setSince(since);
    	analyticsForm.setUntil(until);
    	return analyticsService.serviceAnalytics(domain, analyticsForm);
    }*/

	@ApiOperation("获取当前API的调用量(全部,或者指定订阅系统的,或者2XX,4XX,5XX返回码相关的统计)")
	@ApiImplicitParams({
			@ApiImplicitParam(
					name = "apiId",
					paramType = "body",
					defaultValue = "0",
					required = true,
					dataType = "int",
					value = "当前所在的API ID"),
			@ApiImplicitParam(
					name = "appId",
					paramType = "body",
					defaultValue = "0",
					required = true,
					dataType = "int",
					value = "当前指定的系统对应的application的ID  \n" +
							"使用getSubscribeSystems查询得到  \n" +
							"点击具体某个订阅系统的调用量时, 需传入"),
			@ApiImplicitParam(
					name = "statType",
					paramType = "body",
					defaultValue = "0",
					required = true,
					dataType = "int",
					value = "统计类型:  \n" +
							"0-调用总量  \n" +
							"1-所有订阅系统总调用量  \n" +
							"2-单个订阅系统调用量  \n" +
							"3-错误码-2XX-对应次数  \n" +
							"4-错误码-4XX-对应次数  \n" +
							"5-错误码-5XX-对应次数  \n"),
			@ApiImplicitParam(
					name = "granularity",
					paramType = "body",
					defaultValue = "0",
					required = true,
					dataType = "int",
					value = "统计粒度:  \n" +
							"0: 当前时间点往前 24小时内, 无需指定timeQuery  \n" +
							"1: 当前时间点往前 7天内, 无需指定timeQuery  \n" +
							"2: 当前时间点往前 30天内, 无需指定timeQuery  \n" +
							"3: 当前时间点往前 12月内, 无需指定timeQuery  \n" +
							"4: 指定起始终止区间,以小时为单位, 必须指定timeQuery  \n" +
							"5: 指定起始终止区间,以天为单位, 必须指定timeQuery  \n" +
							"6: 指定起始终止区间,以月为单位, 必须指定timeQuery  \n"),
			@ApiImplicitParam(
					name = "timeQuery",
					paramType = "body",
					defaultValue = "{\"start\":\"2020-09-05 07:42:16\",\"end\":\"2020-09-06 07:42:16\"}",
					required = true,
					dataType = "Date",
					value = "统计区间-起始终止时间")
	})
	@PostMapping("/apiTrafficStatistics")
	public Result<AnalyticsDto> getApiTrafficStatistics(@PathVariable String environment,
														@RequestBody ApiTrafficStatQuery statQuery) {
		statQuery.setEnvironment(InstanceEnvironment.fromCode(environment).getName());
		return analyticsService.getServiceAnalytics(statQuery);
	}

	@ApiOperation("获取当前API的接口网络延迟百分比(所有系统,或者指定订阅系统的)")
	@ApiImplicitParams({
			@ApiImplicitParam(
					name = "apiId",
					paramType = "body",
					defaultValue = "0",
					required = true,
					dataType = "int",
					value = "当前所在的API ID"),
			@ApiImplicitParam(
					name = "appId",
					paramType = "body",
					defaultValue = "0",
					required = false,
					dataType = "int",
					value = "当前指定的系统对应的application的ID  \n" +
							"使用getSubscribeSystems查询得到  \n" +
							"点击具体某个订阅系统的调用量时, 需传入"),
			@ApiImplicitParam(
					name = "statType",
					paramType = "body",
					defaultValue = "0",
					required = true,
					dataType = "int",
					value = "统计类型:  \n" +
							"6-(含所有系统)接口网络延迟百分比-  \n" +
							"7-(仅含指定的订阅系统)接口网络延迟百分比  \n"),
			@ApiImplicitParam(
					name = "granularity",
					paramType = "body",
					defaultValue = "0",
					required = true,
					dataType = "int",
					value = "统计粒度:  \n" +
							"4: 指定起始终止区间,以小时为单位, 必须指定timeQuery  \n" +
							"5: 指定起始终止区间,以天为单位, 必须指定timeQuery  \n" +
							"6: 指定起始终止区间,以月为单位, 必须指定timeQuery  \n"),
			@ApiImplicitParam(
					name = "timeQuery",
					paramType = "body",
					defaultValue = "{\"start\":\"2020-09-05 07:42:16\",\"end\":\"2020-09-06 07:42:16\"}",
					required = true,
					dataType = "Date",
					value = "统计区间-起始终止时间")
	})
	@PostMapping("/apiResponseStatistics")
	public Result<ApiResponseStatistics> getApiResponseStatistics(@PathVariable String environment,
																  @RequestBody ApiTrafficStatQuery statQuery,
																  HttpServletRequest request) {
		statQuery.setEnvironment(InstanceEnvironment.fromCode(environment).getName());
		Result<ApiResponseStatistics> result = elasticSearchService.queryForStatistics(statQuery);
		log.info("apiResponseStatistics {} {}", result.getCode(), result.getMsg());
		return result;
	}

	@ApiOperation("返回当前API的订阅系统")
	@GetMapping("/getSubscribeSystems/{apiId}/{recordId}")
	public Result<SubscribeSystem> getSubscribeSystems(@PathVariable Integer apiId,@PathVariable Integer recordId) {
		return analyticsService.getSubscribeSystems(apiId,recordId);
	}
}
