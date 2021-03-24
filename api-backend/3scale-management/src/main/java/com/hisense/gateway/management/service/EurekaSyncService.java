/*
 * @author guilai.ming
 * @date 2020/7/15
 */
package com.hisense.gateway.management.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.eureka.EurekaPullConfig;
import com.hisense.gateway.library.model.pojo.base.EurekaPullApi;

import java.util.Map;

/**
 * Eureka同步处理服务
 */
public interface EurekaSyncService {
    /**
     * 服务上线
     *
     * @param appName    服务名
     * @param instanceId 服务实例名
     */
    void instanceOnline(String appName, String instanceId);

    /**
     * 服务下线
     *
     * @param appName    服务名
     * @param instanceId 服务实例名
     */
    void instanceOffline(String appName, String instanceId);

    /**
     * 被动接收Eureka推送的API
     *
     * @param metaData Eureka Metadata表
     * @return Result<Integer>
     */
    Result<Integer> pushApisFromEureka(Map<String, String> metaData);

    /**
     * 主动从Eureka拉取API
     *
     * @param config EurekaPullConfig
     * @return Result<Integer>
     */
    Result<Integer> pullApisFromEureka(EurekaPullConfig config);

    /**
     * 定时拉取
     * @param config
     */
    void schedulePullTask(EurekaPullConfig config);

    /**
     * 获取上传填写数据
     * @return
     */
    Result<EurekaPullApi> findEurekaConfig();
}
