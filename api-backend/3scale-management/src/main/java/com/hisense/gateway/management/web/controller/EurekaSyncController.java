/*
 * @author guilai.ming
 * @date 2020/7/15
 */
package com.hisense.gateway.management.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.hisense.api.library.model.eureka.EurekaNotification;
import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.eureka.EurekaApp;
import com.hisense.gateway.library.model.base.eureka.EurekaPullConfig;
import com.hisense.gateway.library.model.base.eureka.EurekaServer;
import com.hisense.gateway.library.model.pojo.base.EurekaPullApi;
import com.hisense.gateway.library.utils.api.AutoCreateUtil;
import com.hisense.gateway.management.service.AutoCreateService;
import com.hisense.gateway.management.service.EurekaSyncService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.hisense.gateway.library.constant.BaseConstants.*;

/**
 * Eureka同步处理器, 用于 被动响应Eureka Server发来的事件、主动拉取API、定时拉取API
 * <p>
 * 被动响应事件类型 {@link com.hisense.api.library.model.eureka.EurekaEvent}
 *
 * @author mingguilai.ex
 * @date 2020-07-15
 */
@Api
@Slf4j
@RestController
public class EurekaSyncController {
    @Autowired
    AutoCreateService autoCreateService;

    @Autowired
    EurekaSyncService eurekaSyncService;

    /**
     * 响应Eureka事件
     *
     * @param notification @link{EurekaNotification} notification
     * @return Result<Boolean>
     */
    @PostMapping(value = URL_EUREKA_PUSH_METADATA)
    public Result<Boolean> notification(@RequestBody EurekaNotification notification) {
        Result<Boolean> result = new Result<>(Result.FAIL, "Fail", false);
        if (notification == null || notification.getEvent() == null ||
                MiscUtil.isEmpty(notification.getAppName()) ||
                MiscUtil.isEmpty(notification.getInstanceId())) {
            result.setMsg("Invalid params");
            return result;
        }

        log.info("{}Receive {}", TAG, notification);
        // api-monitor已确保同一个eureka-service只允许一个instance提交一次注册请求,不会重复
        switch (notification.getEvent()) {
            case API_REGISTER:
                Result<Integer> result1 = eurekaSyncService.pushApisFromEureka(notification.getMetaData());
                result.setCode(result1.getCode());
                result.setMsg(result1.getMsg());
                result.setData(result1.isSuccess());
                break;

            case API_UNREGISTER:
                result = autoCreateService.logicDeleteApis(notification.getAppName());
                break;

            case INSTANCE_UP:
                eurekaSyncService.instanceOnline(notification.getAppName(), notification.getInstanceId());
                break;

            case INSTANCE_DOWN:
                eurekaSyncService.instanceOffline(notification.getAppName(), notification.getInstanceId());
                break;
        }

        result.setCode(Result.OK);
        result.setMsg("Success");
        return result;
    }

    /**
     * 主动拉取API
     */
    @PostMapping(value = URL_EUREKA_PULL_METADATA)
    public Result<Integer> pullApis(@PathVariable String environment, @RequestBody EurekaPullConfig config,
                                    HttpServletRequest servletRequest) {
        Result<Integer> result = new Result<>("0", "Fail", 0);
        if (!config.isValid()) {
            result.setMsg("Eureka地址和分组为不能为空");
            return result;
        }

        eurekaSyncService.schedulePullTask(config);
        return eurekaSyncService.pullApisFromEureka(config);
    }

    /**
     * 获取上次填写数据
     * @return
     */
    @GetMapping(value = EUREKA_CONFIG)
    public Result<EurekaPullApi> pullApiResult(){
        return eurekaSyncService.findEurekaConfig();
    }


    @GetMapping(URL_EUREKA_PULL_METADATA+"/test")
    public List<EurekaApp> test(HttpServletRequest request,String url) {
        Properties properties = new Properties();
        String nacosUrl = "/nacos/v1/ns/instance/list";
        List<EurekaApp> eurekaApps = new ArrayList<>();
        String serviceName = "api-management";
        String authorization = request.getHeader("Authorization");
        try {
            log.info(String.format("%s/%s?serviceName=%s", AutoCreateUtil.fixEurekaZoneUrl(url), nacosUrl,serviceName));
            url = String.format("%s/%s?serviceName=%s", AutoCreateUtil.fixEurekaZoneUrl(url), nacosUrl,serviceName);
            RestTemplate client = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            HttpMethod method = HttpMethod.GET;
            // 以表单的方式提交
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("content-type", "application/json;charset=UTF-8");
            headers.set("Authorization", authorization);
            //将请求头部和参数合成一个请求
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(new HashMap<>(), headers);
            log.info(String.format("请求url=%s,请求方法=%s,Authorization=%s",url,method,authorization));
            //执行HTTP请求，将返回的结构使用ResultVO类格式化
            ResponseEntity<Object> response = client.exchange(url, method, requestEntity, Object.class);
            log.info("获取服务返回结果：{}", JSONObject.toJSONString(response.getBody()));
            String responseStr = (String) response.getBody();
            if (MiscUtil.isEmpty(responseStr)) {
                log.error("从nacos服务器读取失败,请联系服务提供者");
            }
            EurekaServer eurekaServer = MiscUtil.fromJson(responseStr, EurekaServer.class);
            if (eurekaServer == null || eurekaServer.getApplications() == null ||
                    MiscUtil.isEmpty(eurekaApps = eurekaServer.getApplications().getApplication())) {
                log.error("指定的nacos服务器无在线服务, 请稍后重试");
            }
        }catch(Exception e){
            log.error("get nacos service exception",e);
        }

        return eurekaApps;
    }
}
