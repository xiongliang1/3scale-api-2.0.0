/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.stud;

import com.hisense.gateway.library.stud.model.MetricsDto;

public interface MetricStud {
    MetricsDto getMetricByServiceId(String host,String accessToken,String serviceId);
}
