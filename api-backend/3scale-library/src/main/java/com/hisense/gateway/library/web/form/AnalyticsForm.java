package com.hisense.gateway.library.web.form;

import com.hisense.gateway.library.constant.AnalyticsConstant;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
public class AnalyticsForm implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;
    private String apiId;
    private String appId;
    private String since;
    private String until;
    private String granularity;
    private String partition;
    private String applicationId;
    private String accessToken;
    private String metricName;
    private AnalyticsConstant.StatType statType;
}
