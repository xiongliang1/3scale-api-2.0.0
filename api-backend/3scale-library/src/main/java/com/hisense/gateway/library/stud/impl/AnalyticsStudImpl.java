package com.hisense.gateway.library.stud.impl;

import com.alibaba.fastjson.JSON;
import com.hisense.gateway.library.model.base.analytics.ApiTrafficStatQuery;
import com.hisense.gateway.library.stud.AnalyticsStud;
import com.hisense.gateway.library.stud.model.AnalyticsDto;
import com.hisense.gateway.library.utils.HttpUtil;
import com.hisense.gateway.library.utils.api.AnalyticsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.hisense.gateway.library.constant.AnalyticsConstant.StatType.INVOKE_BY_SINGLE_SUB_SYSTEM;

@Slf4j
@Service
public class AnalyticsStudImpl implements AnalyticsStud {
    @Override
    public AnalyticsDto serviceAnalytics(String host, String accessToken, ApiTrafficStatQuery statQuery) {
        AnalyticsDto dtos = new AnalyticsDto();
        try {
            String urlPath = host + String.format(
                    statQuery.getStatType().getScaleApiPath(),
                    statQuery.getStatType().equals(INVOKE_BY_SINGLE_SUB_SYSTEM) ? statQuery.getAppId() :
                            statQuery.getApiId(),
                    accessToken,
                    statQuery.getStatType().getMetricName(),
                    AnalyticsUtil.formatTime(statQuery.getTimeQuery().getStart()),
                    AnalyticsUtil.formatTime(statQuery.getTimeQuery().getEnd()),
                    statQuery.getGranularity().getGranularity(),"Beijing");

            log.info("urlPath {}",urlPath);
            log.info("start to serviceAnalytics,host {}, statQuery {},\n urlPath= {}", host, statQuery, urlPath);
            String rlt = HttpUtil.sendGet(urlPath);
            log.info("end to serviceAnalytics,host {}, statQuery {},rlt {}", host, statQuery, rlt);
            if (rlt == null) {
                return dtos;
            } else {
                dtos = JSON.parseObject(rlt, AnalyticsDto.class);
            }
        } catch (Exception e) {
            log.error("error to serviceAnalytics,host {}, statQuery {}, e {}", host, statQuery, e);
        }
        return dtos;
    }

    @Override
    public AnalyticsDto serviceAnalytics2(String host, String accessToken, ApiTrafficStatQuery statQuery) {
        AnalyticsDto dtos = new AnalyticsDto();
        try {
            String urlPath = host + String.format(
                    statQuery.getStatType().getScaleApiPath(),
                    statQuery.getStatType().equals(INVOKE_BY_SINGLE_SUB_SYSTEM) ? statQuery.getAppId() :
                            statQuery.getApiId(),
                    accessToken,
                    statQuery.getStatType().getMetricName(),
                    AnalyticsUtil.formatTime(statQuery.getTimeQuery().getStart()),
                    AnalyticsUtil.formatTime(statQuery.getTimeQuery().getEnd()),
                    statQuery.getGranularity().getGranularity(),"Beijing");

            String rlt = HttpUtil.sendGet2(urlPath);
            if (rlt == null) {
                return dtos;
            } else {
                dtos = JSON.parseObject(rlt, AnalyticsDto.class);
            }
        } catch (Exception e) {
            log.error("error to serviceAnalytics,host {}, statQuery {}, e {}", host, statQuery, e);
        }
        return dtos;
    }
}
