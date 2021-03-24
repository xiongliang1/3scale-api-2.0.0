package com.hisense.gateway.library.model.base.monitor;

import lombok.Data;

/**
 * API响应时间
 */
@Data
public class ApiResponseTime {
    long timestamp;
    String startTime;
    double responseTime;
}
