/*
 * @author guilai.ming
 * @date 2020/7/15
 */
package com.hisense.gateway.management.service.impl;

import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.beans.CommonBeanUtil;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.eureka.*;
import com.hisense.gateway.library.model.base.nacos.NacosInstance;
import com.hisense.gateway.library.model.base.nacos.NacosService;
import com.hisense.gateway.library.model.pojo.base.EurekaPullApi;
import com.hisense.gateway.library.model.pojo.base.EurekaService;
import com.hisense.gateway.library.model.pojo.base.PublishApiGroup;
import com.hisense.gateway.library.service.EurekaPullApiService;
import com.hisense.gateway.management.service.PublishApiGroupService;
import com.hisense.gateway.library.utils.HttpUtil;
import com.hisense.gateway.management.service.EurekaSyncService;
import com.hisense.gateway.management.service.AutoCreateService;
import com.hisense.gateway.library.utils.TaskBroker;
import com.hisense.gateway.library.utils.api.AutoCreateUtil;
import com.hisense.gateway.library.utils.meta.GlobalSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.hisense.gateway.library.constant.BaseConstants.*;

@Slf4j
@Service
public class EurekaSyncServiceImpl implements EurekaSyncService {
    private volatile boolean asyncCreatedDone = true;
    private InstanceCache instanceCache = new InstanceCache();
    private SchedulePullTaskBroker schedulePullTaskBroker = new SchedulePullTaskBroker();
    private ExecutorService executorService = TaskBroker.buildSingleExecutorService(this.getClass().getSimpleName());

    @Autowired
    private AutoCreateService autoCreateService;

    @Autowired
    private PublishApiGroupService publishApiGroupService;

    @Autowired
    private EurekaPullApiService eurekaPullApiService;

    @Override
    public void instanceOnline(String appName, String instanceId) {
        log.info("{}Online {} {}", TAG, appName, instanceId);
        instanceCache.addInstance(appName, instanceId);
    }

    @Override
    public void instanceOffline(String appName, String instanceId) {
        log.info("{}Offline {} {}", TAG, appName, instanceId);
        instanceCache.removeInstance(appName, instanceId);
    }

    @Override
    public Result<Integer> pushApisFromEureka(Map<String, String> metaData) {
        Result<Integer> result = new Result<>(Result.FAIL, "Fail", 0);
        if (metaData == null) {
            result.setMsg("Invalid metadata");
            return result;
        }

        EurekaService service;
        log.info("{}Start auto save api with metaData {}", TAG, metaData.size());
        if ((service = AutoCreateUtil.buildEurekaService(metaData, null)) == null) {
            result.setMsg("Fail to build api from metadata");
            return result;
        }

        List<EurekaService> eurekaServices = new ArrayList<>();
        eurekaServices.add(service);
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(sra, true);
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        executorService.submit(() -> startEurekaApiSavingTask("Automatically Push from eureka", null, eurekaServices,authentication));
        result.setCode(Result.OK);
        result.setMsg("创建任务已提交");
        result.setData(getApiCount(eurekaServices));
        return result;
    }

    @Override
    public Result<Integer> pullApisFromEureka(EurekaPullConfig config) {
        return pullApisFromEureka(config, executorService);
    }

    /**
     * Eureka实例缓存
     */
    private static class InstanceCache {
        private final Object lock = new Object();
        private final Map<String, Set<String>> instanceMap = new ConcurrentHashMap<>();

        private void addInstance(String appName, String instanceId) {
            Set<String> instanceIds;
            if ((instanceIds = instanceMap.get(appName)) == null) {
                instanceIds = new HashSet<>();
                instanceMap.put(appName, instanceIds);
            }

            synchronized (lock) {
                instanceIds.add(instanceId);
            }
        }

        private void removeInstance(String appName, String instanceId) {
            Set<String> instanceIds;
            if ((instanceIds = instanceMap.get(appName)) == null) {
                return;
            }

            synchronized (lock) {
                instanceIds.remove(instanceId);
            }
        }

        private String getFirstInstanceId(String appName) {
            Set<String> instanceIds;
            if ((instanceIds = instanceMap.get(appName)) == null) {
                return null;
            }

            synchronized (lock) {
                if (instanceIds.size() > 0) {
                    return instanceIds.iterator().next();
                }
            }
            return null;
        }

        private Set<String> getAllInstanceId(String appName) {
            return instanceMap.get(appName);
        }

        private Result<Integer> traverseAppInstance(Callback callback) {
            Result<Integer> result = new Result<>(Result.FAIL, "", 0);
            if (callback == null) {
                result.setMsg("程序错误,未指定回调");
                return result;
            }

            if (instanceMap.size() == 0) {
                result.setMsg("当前服务已下线,请联系服务提供者或者更换其他地址");
                result.setCode(Result.FAIL);
                result.setData(-1);
            }

            log.info("{}Try to pull api", TAG);
            for (Map.Entry<String, Set<String>> entry : instanceMap.entrySet()) {
                String appName = entry.getKey();
                Set<String> instances = entry.getValue();
                synchronized (lock) {
                    if (instances != null && instances.size() > 0) {
                        Result<Integer> innerResult = null;
                        for (String instance : instances) {
                            innerResult = callback.process(appName, instance);
                            if (innerResult.getData() < 0) {
                            } else if (innerResult.getData() > 0) {
                                log.info("{}Success to get metadata from apps/{}/{}", TAG, appName, instance);
                                break;
                            }
                        }

                        if (innerResult==null || innerResult.getData() == 0) {
                            log.info("{}Fail to get metadata from apps/{}", TAG, appName);
                        }

                        result.setData(result.getData() + (innerResult==null?0: innerResult.getData()));
                    }
                }
            }

            log.info("{}End to pull api with result {}", TAG, result);

            if (result.getData() <= 0) {
                result.setCode(Result.FAIL);
                result.setMsg("Fail to get metadata from eureka");
                return result;
            }

            log.info("{}Success to get metadata from eureka with {}", TAG, result.getData());
            return result;
        }

        public interface Callback {
            Result<Integer> process(String appName, String instance);
        }
    }

    /**
     * Eureka实例遍历器
     */
    private class InstanceTraverser implements InstanceCache.Callback {
        private String host;

        public InstanceTraverser(String host) {
            this.host = host;
        }

        @Override
        public Result<Integer> process(String appName, String instance) {
            Result<Integer> result = new Result<>("1", "fail", 0);
            String instanceUrl = String.format("%s/%s/%s/%s/", host, EUREKA_APPS, appName, instance);

            try {
                log.info("{}Start pull api from {}", TAG, instanceUrl);
                String response = HttpUtil.sendGetJson(instanceUrl);
                if (MiscUtil.isEmpty(response)) {
                    result.setMsg("Fail to get metadata from remote");
                    return result;
                }

                WrapperInstance wrapperInstance = MiscUtil.fromJson(response, WrapperInstance.class);
                if (wrapperInstance == null ||
                        wrapperInstance.getInstance() == null ||
                        wrapperInstance.getInstance().getMetadata() == null) {
                    result.setMsg("Invalid EurekaApp deserialization from server");
                    return result;
                }

                Map<String, String> metaData = wrapperInstance.getInstance().getMetadata();
                EurekaService service;
                log.info("{}Start auto save api with metaData {}", TAG, metaData.size());
                if ((service = AutoCreateUtil.buildEurekaService(metaData, null)) == null) {
                    result.setMsg("Fail to build api from metadata");
                    return result;
                }

                result = autoCreateService.createApis(null, service, GlobalSettings.getVisitor().getUsername());
            } catch (Exception e) {
                result.setMsg(e.toString());
            }

            return result;
        }
    }

    /**
     * 返回API数量
     *
     * @param eurekaServices 服务列表
     * @return API数量
     */
    private int getApiCount(List<EurekaService> eurekaServices) {
        int count = 0;
        for (EurekaService eurekaService : eurekaServices) {
            count += eurekaService.getPublishApiDtos().size();
        }
        return count;
    }

    /**
     * 在指定线程中主动拉取API
     */
    private Result<Integer> pullApisFromEureka(EurekaPullConfig config, ExecutorService executorService) {
        Result<Integer> result = new Result<>("1", "fail", 0);
        result.setAlert(1);
        if (!asyncCreatedDone) {
            result.setMsg("后台创建任务尚未完成, 请等待1分钟后再拉取");
            return result;
        }

        Result<PublishApiGroup> apiGroupResult =
                publishApiGroupService.findGroupAndSystemByGroupId(config.getGroupId());
        if (apiGroupResult.isFailure()) {
            log.error("{}{}", TAG, apiGroupResult.getMsg());
            result.setMsg(apiGroupResult.getMsg());
            return result;
        }

        PublishApiGroup publishApiGroup = apiGroupResult.getData();

        try {
            List<EurekaService> eurekaServices = null;
            if("eureka".equals(config.getType())){
                Result<List<EurekaService>> serviceFromEureka = getServiceFromEureka(config, publishApiGroup);
                if(Result.FAIL.equals(serviceFromEureka.getCode())){
                    result.setError(serviceFromEureka.getMsg());
                    return  result;
                }
                eurekaServices = serviceFromEureka.getData();
            }else if("nacos".equals(config.getType())){
                Result<List<EurekaService>> serviceFromNacos = getServiceFromNacos(config, publishApiGroup);
                if(Result.FAIL.equals(serviceFromNacos.getCode())){
                    result.setError(serviceFromNacos.getMsg());
                    return  result;
                }
                eurekaServices = serviceFromNacos.getData();
            }
            if(CollectionUtils.isEmpty(eurekaServices)){
                result.setMsg("获取到0个API!");
                return  result;
            }
            ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            RequestContextHolder.setRequestAttributes(sra, true);
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
           /* if (executorService != null) {
                List<EurekaService> finalEurekaServices = eurekaServices;
                executorService.submit(() -> startEurekaApiSavingTask(config.getEurekaUrl(), config.getGroupId(),
                        finalEurekaServices,authentication));
            } else {
                startEurekaApiSavingTask(config.getEurekaUrl(), config.getGroupId(), eurekaServices,authentication);
            }*/

            int count = getApiCount(eurekaServices);
            result.setMsg(count > 0 ? String.format("成功获取%d个API,后台已启动创建", count) : "拉取成功，API数量为0");
            result.setData(count);
            result.setCode(Result.OK);
        } catch (Exception e) {
            log.error(TAG + " Exception for pulling Apis From Eureka: ", e);
            result.setMsg(HttpUtil.buildExceptionMessage(e));
        }
        return result;
    }

    /**
     * 从eureka注册中心获取服务列表信息
     * @param config
     * @param publishApiGroup
     * @return
     */
    public Result<List<EurekaService>> getServiceFromEureka(EurekaPullConfig config,PublishApiGroup publishApiGroup){
        Result<List<EurekaService>> result = new Result<>(Result.OK, "获取正常！", null);
        log.info(String.format("%S==%s/%s",config.getType(),AutoCreateUtil.fixEurekaZoneUrl(config.getEurekaUrl()), EUREKA_APPS));
        String response = HttpUtil.sendGetJson(String.format("%s/%s",
                AutoCreateUtil.fixEurekaZoneUrl(config.getEurekaUrl()), EUREKA_APPS));
        if (MiscUtil.isEmpty(response)) {
            result.setError("从Eureka服务器读取失败,请联系服务提供者");
            return result;
        }

        List<EurekaApp> eurekaApps;
        EurekaServer eurekaServer = MiscUtil.fromJson(response, EurekaServer.class);
        if (eurekaServer == null || eurekaServer.getApplications() == null ||
                MiscUtil.isEmpty(eurekaApps = eurekaServer.getApplications().getApplication())) {
            result.setError("指定的Eureka服务器无在线服务, 请稍后重试");
            return result;
        }
        List<EurekaService> eurekaServices = AutoCreateUtil.buildEurekaServices(eurekaApps, publishApiGroup);
        log.info("{}Probe {} EurekaApp, {} EurekaService", TAG, eurekaApps.size(), eurekaServices.size());
        result.setData(eurekaServices);
        return result;
    }

    /**
     * 从nacos注册中心获取服务列表信息
     * @param config
     * @param publishApiGroup
     * @return
     */
    public Result<List<EurekaService>> getServiceFromNacos(EurekaPullConfig config,PublishApiGroup publishApiGroup){
        Result<List<EurekaService>> result = new Result<>(Result.OK, "获取正常！", null);
        log.info(String.format("%s==%s/%s%s",config.getType(),AutoCreateUtil.fixEurekaZoneUrl(config.getEurekaUrl()), NACOS_APP,config.getServiceName()));
        String response = HttpUtil.sendGetJson(String.format("%s/%s%s",
                AutoCreateUtil.fixEurekaZoneUrl(config.getEurekaUrl()), NACOS_APP,config.getServiceName()));
        if (MiscUtil.isEmpty(response)) {
            result.setError("从Eureka服务器读取失败,请联系服务提供者");
            return result;
        }

        List<NacosInstance> nacosInstances;
        NacosService nacosServer = MiscUtil.fromJson(response, NacosService.class);
        if (nacosServer == null || nacosServer.getHosts() == null ||
                MiscUtil.isEmpty(nacosInstances = nacosServer.getHosts())) {
            result.setError("指定的nacos服务器无在线服务, 请稍后重试");
            return result;
        }

        List<EurekaService> eurekaServices = AutoCreateUtil.buildNacosServices(nacosServer, publishApiGroup);

        log.info("{}Probe {} EurekaApp, {} EurekaService", TAG, nacosInstances.size(), eurekaServices.size());
        result.setData(eurekaServices);
        return result;
    }

    /**
     * 开启异步存储任务: API入库,scale入库
     *
     * @param groupId        所属分组
     * @param tip            消息提示
     * @param eurekaServices 服务列表
     */
   /* private void startEurekaApiSavingTask(String tip, Integer groupId, List<EurekaService> eurekaServices,Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        long cost = 0;
        asyncCreatedDone = false;
        log.info("{}Start saving task", TAG);
        Result<Integer> result = new Result<>("1", "fail", 0);
//        String username = SecurityUtils.getLoginUser().getLoginName();
//        log.info("username ==> {}", username);
        for (EurekaService eurekaService : eurekaServices) {
            Result<Integer> result1;
            long start = System.currentTimeMillis();
            log.info("{}Start to save apis pulled from {}", TAG, eurekaService.getSystemName());
          *//*  result1 = autoCreateService.createApis(groupId, eurekaService, username);
            log.info("{}Save apis from {} {} {} cost {}-ms", TAG, eurekaService.getSystemName(),
                    eurekaService.getInstanceId(),
                    result1.isSuccess() ? "Success " + result1.getData() : "Failure " + result1.getMsg(),
                    (System.currentTimeMillis() - start));
            cost += System.currentTimeMillis() - start;
            result.setData(result.getData() + result1.getData());*//*
        }

        String message = String.format("Cost[%d-ms],success to poll %d apis from %s", cost, result.getData(), tip);
        log.info("{}{}", TAG, message);
        asyncCreatedDone = true;
    }
*/
    private class SchedulePullTaskBroker {
        private Map<String, EurekaPullConfig> pullConfigMap = new ConcurrentHashMap<>();
        private ScheduledExecutorService scheduledExecutorService =
                TaskBroker.buildSingleScheduledExecutorService(this.getClass().getSimpleName());

        public void updatePullConfig(EurekaPullConfig pullConfig) {
            EurekaPullConfig savedConfig;
            if ((pullConfigMap.get(pullConfig.getEurekaUrl())) == null) {
                savedConfig = new EurekaPullConfig();
                savedConfig.setEurekaUrl(pullConfig.getEurekaUrl());
                savedConfig.setGroupId(pullConfig.getGroupId());
                savedConfig.setScheduleEnable(pullConfig.isScheduleEnable());
                pullConfigMap.put(pullConfig.getEurekaUrl(), savedConfig);
            } else {
                savedConfig = pullConfigMap.get(pullConfig.getEurekaUrl());
                synchronized (savedConfig.getLock()) {
                    savedConfig.setEurekaUrl(pullConfig.getEurekaUrl());
                    savedConfig.setGroupId(pullConfig.getGroupId());
                    savedConfig.setScheduleEnable(pullConfig.isScheduleEnable());
                }
            }

            synchronized (savedConfig.getLock()) {
                if (savedConfig.isScheduleEnable()) {
                    if (!savedConfig.isScheduled()) {
                        savedConfig.setScheduled(true);
                        scheduleAtFixedRate(savedConfig);
                        log.info("{}Task scheduled with {}", TAG, savedConfig);
                    } else {
                        log.info("{}Task had always scheduled with {}", TAG, savedConfig);
                    }
                }
            }

           // eurekaPullApiService.createEurekaConfig(savedConfig);
        }

        public void scheduleAtFixedRate(EurekaPullConfig config) {
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                if (config.isScheduleEnable()) {
                    pullApisFromEureka(config, null);
                } else {
                    log.info("{}Task had been disabled with {}", TAG, config);
                }
            }, 12, 12, TimeUnit.HOURS);
        }
    }

    /**
     * 定时任务设定
     */
    @Override
    public void schedulePullTask(EurekaPullConfig config) {
        log.info("{}Setting schedule pull task", config);
        schedulePullTaskBroker.updatePullConfig(config);
    }

    /**
     * 获取上次填写数据
     * @return
     */
    @Override
    public Result<EurekaPullApi> findEurekaConfig() {
        return eurekaPullApiService.findEurekaConfig();
    }
}
