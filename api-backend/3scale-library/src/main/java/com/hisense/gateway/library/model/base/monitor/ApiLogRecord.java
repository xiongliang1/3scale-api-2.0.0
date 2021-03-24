package com.hisense.gateway.library.model.base.monitor;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @author guilai.ming 2020/09/22
 * <p>
 * Api调用记录
 */
@Data
public class ApiLogRecord {
    Integer apiId;
    long serviceId;
    String apiName;// api名称
    String requestTime;// 请求时间
    String httpMethod;// 请求方式
    String httpPattern;// 路由规则
    String callSystem;// 调用系统
    String apiSystem;// 接口系统
    String requestParam;// 请求参数
    String requestBody;// 请求体
    String responseBody;// 响应体
    String httpStatusCode;// 请求状态码
    String responseCode;// 响应状态码
    Integer responseTime;// 请求耗时
    Map<String,String> requestHeader;// 请求header
    String hashId;// ES存储日志记录的id
    String ipList;//调用方ip
}
