/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2019/11/25
 */
package com.hisense.gateway.library.stud.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.stud.AccountStud;
import com.hisense.gateway.library.stud.model.*;
import com.hisense.gateway.library.utils.HttpUtil;
import com.hisense.gateway.library.utils.XmlUtils;
import com.hisense.gateway.library.constant.SystemNameConstant;
import com.hisense.gateway.library.stud.FeatureStud;
import com.hisense.gateway.library.stud.ServiceStud;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.jdom2.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

@Slf4j
@org.springframework.stereotype.Service
public class ServicesStudImpl implements ServiceStud {

    private static final String PATH_LIST_SERVICES = "/admin/api/services.json";
    private static final String PATH_SERVICES = "/admin/api/services/";
    private static final String PATH_APPLICATIONS = "/admin/api/applications.xml";
    private static final String PATH_APPLICATION_PLANS = "/admin/api/application_plans.json";
    private static final String PATH_LIST_SERVICES_XML = "/admin/api/services.xml";

    @Autowired
    AccountStud accountStud;

    @Resource
    FeatureStud featureStud;

    @Override
    public List<Integer> serviceDtoIdList(String host, String accessToken) {
        log.info("start to serviceDtoIdList,host {}", host);
        ServiceDtos serviceDtos = new ServiceDtos();
        try {
            String rlt = HttpUtil.sendGet(host + PATH_LIST_SERVICES + "?access_token=" + accessToken);
            log.info("end to serviceDtoIdList,host {}, rlt {}", host, rlt);
            if (rlt == null) {
                return null;
            } else {
                serviceDtos = JSON.parseObject(rlt, ServiceDtos.class);
            }
        } catch (Exception e) {
            log.error("error to serviceDtoIdList,host {}, e {}", host, e);
        }

        List<Integer> serviceIdList = new ArrayList<>();
        if (MiscUtil.isNotEmpty(serviceDtos.getServices())) {
            for (ServiceDto serviceDto : serviceDtos.getServices()) {
                serviceIdList.add(Integer.valueOf(serviceDto.getService().getId()));
            }
        }
        return serviceIdList;
    }

    @Override
    public List<ServiceDto> serviceDtoList(String host, String accessToken) {
        log.info("******start to invoke serviceDtoList, "
                        + "host is {}",
                host);
        ServiceDtos serviceDtos = new ServiceDtos();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_LIST_SERVICES + "?access_token=" + accessToken);
            log.info("******end to invoke serviceDtoList, "
                            + "host is {},rlt is {}",
                    host, rlt);
            if (null == rlt) {
                return serviceDtos.getServices();
            } else {
                serviceDtos = JSON.parseObject(rlt, ServiceDtos.class);
                //2019-11-28非常影响后端响应，暂时屏蔽，后续接管网关发布之后处理
//                Iterator<ServiceDto> iterator = serviceDtos.getServices().iterator();
//                while (iterator.hasNext()) {
//                    ServiceDto serviceDto = iterator.next();
//                    //查询服务标签
//                    Boolean mark = false;
//                    List<Feature> featureDtos = featureStud.findFeatureByServiceId(host, accessToken, serviceDto
//                    .getService().getId()).getFeature();
//                    if (null != featureDtos && featureDtos.size() > 0) {
//                        for (int i = 0, size = featureDtos.size(); i < size; i++) {
//                            if(LdapResConstant.NO_PLAY.equals(featureDtos.get(i).getName()) && featureDtos.get(i)
//                            .getVisible()){
//                                //过滤不显示的服务
//                                mark = true;
//                                break;
//                            }
//                        }
//                        if (mark) {
//                            //这个服务过滤掉
//                            iterator.remove();
//                            continue;
//                        }
//                    }
//                }
            }
        } catch (Exception e) {
            log.error("******fail to invoke serviceDtoList, "
                            + "host is {},e is {}",
                    host, e);
        }
        return serviceDtos.getServices();
    }

    @Override
    public ServiceDto serviceDtoRead(String host, String accessToken, String id) {
        log.info("******start to invoke serviceDtoRead, "
                        + "host is {}, id is {}",
                host, id);
        ServiceDto serviceDto = new ServiceDto();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_SERVICES + id + ".json?access_token=" + accessToken);
            log.info("******end to invoke serviceDtoRead, "
                            + "host is {}, id is {}, rlt is {}",
                    host, id, rlt);
            if (null == rlt) {
                return serviceDto;
            } else {
                serviceDto = JSON.parseObject(rlt, ServiceDto.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke serviceDtoRead, "
                            + "host is {}, id is {}, e is {}",
                    host, id, e);
        }
        return serviceDto;
    }

    @Override
    public ServiceXmlDto serviceXmlDtoRead(String host, String accessToken, String serviceId) {
        log.info("******start to invoke serviceXmlDtoRead, host is {}, id is {}", host, serviceId);
        ServiceXmlDto serviceXmlDto = new ServiceXmlDto();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_SERVICES + serviceId + ".xml?access_token=" + accessToken);
            log.info("******end to invoke serviceXmlDtoRead, "
                            + "host is {}, id is {}, rlt is {}",
                    host, serviceId, rlt);
            if (null == rlt) {
                return serviceXmlDto;
            } else {
                serviceXmlDto = (ServiceXmlDto) XmlUtils.xmlStrToObject(ServiceXmlDto.class, rlt);
            }
        } catch (Exception e) {
            log.error("******fail to invoke serviceXmlDtoRead, "
                    + "host is {}, id is {}, e is {}", host, serviceId, e);
        }
        return serviceXmlDto;
    }

    @Override
    public List<AppPlanDto> appPlanDtoList(String host, String accessToken, String serviceId) {
        log.info("******start to invoke appPlanDtoList, "
                        + "host is {}, serviceId is {}",
                host, serviceId);
        AppPlanDtos dtos = new AppPlanDtos();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_SERVICES + serviceId + "/application_plans.json?access_token=" + accessToken);
            log.info("******end to invoke appPlanDtoList, "
                            + "host is {}, serviceId is {}, rlt is {}",
                    host, serviceId, rlt);
            if (null == rlt) {
                return dtos.getPlans();
            } else {
                dtos = JSON.parseObject(rlt, AppPlanDtos.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke appPlanDtoList, "
                            + "host is {}, serviceId is {}, e is {}",
                    host, serviceId, e);
        }
        return dtos.getPlans();
    }

    @Override
    public List<AppPlanDto> allServicePlanDtoList(String host, String accessToken) {
        log.info("******start to invoke allSerevicePlanDtoList, "
                        + "host is {}",
                host);
        AppPlanDtos dtos = new AppPlanDtos();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_APPLICATION_PLANS + "?access_token=" + accessToken);
            log.info("******end to invoke allSerevicePlanDtoList, "
                            + "host is {}, rlt is {}",
                    host, rlt);
            if (null == rlt) {
                return dtos.getPlans();
            } else {
                dtos = JSON.parseObject(rlt, AppPlanDtos.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke allSerevicePlanDtoList, "
                            + "host is {}, e is {}",
                    host, e);
        }
        return dtos.getPlans();
    }

    @Override
    public List<ApplicationCount> countPlanApps(String host, String accessToken, String serviceId, Long accountId) {
        log.info("******start to invoke CountPlanApps, "
                        + "host is {}, serviceId is {}",
                host, serviceId);
        List<ApplicationCount> applicationCounts = new ArrayList<>();
        ApplicationXmlDtos dtos;
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_APPLICATIONS + "?access_token=" + accessToken + "&service_id=" + serviceId);
            log.info("******end to invoke CountPlanApps, "
                            + "host is {}, serviceId is {}",
                    host, serviceId);
            if (null == rlt) {
                return applicationCounts;
            } else {
                dtos = (ApplicationXmlDtos) XmlUtils.xmlStrToObject(ApplicationXmlDtos.class, rlt);
                Map<String, ApplicationCount> map = new HashMap<>();
                for (ApplicationXml applicationXml : dtos.getApplication()) {
                    String planId = applicationXml.getPlan().getId();
                    if (map.containsKey(planId)) {
                        int appCount = map.get(planId).getApplications() + 1;
                        map.get(planId).setApplications(appCount);
                        if (applicationXml.getUserAccountId().equals(accountId.toString())) {
                            map.get(planId).setSubscribed(true);
                        }
                    } else {
                        ApplicationCount applicationCount = new ApplicationCount();
                        applicationCount.setApplications(1);
                        applicationCount.setPlanId(planId);
                        if (applicationXml.getUserAccountId().equals(accountId.toString())) {
                            applicationCount.setSubscribed(true);
                        } else {
                            applicationCount.setSubscribed(false);
                        }
                        map.put(planId, applicationCount);
                    }
                }
                for (Map.Entry<String, ApplicationCount> entry : map.entrySet()) {
                    applicationCounts.add(entry.getValue());
                }
            }
        } catch (Exception e) {
            log.error("******fail to invoke CountPlanApps, "
                            + "host is {}, serviceId is {}",
                    host, serviceId);
        }
        return applicationCounts;
    }

    @Override
    public AppPlanDto appPlanDtoRead(String host, String accessToken, String serviceId, String planId) {
        log.info("******start to invoke appPlanDtoRead, "
                        + "host is {}, serviceId is {}, planId is {}",
                host, serviceId, planId);
        AppPlanDto dto = new AppPlanDto();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_SERVICES + serviceId + "/application_plans/" + planId + ".json" +
                            "?access_token=" + accessToken);
            log.info("******end to invoke appPlanDtoRead, "
                            + "host is {}, serviceId is {}, planId is {}, rlt is {}",
                    host, serviceId, planId, rlt);
            if (null == rlt) {
                return dto;
            } else {
                dto = JSON.parseObject(rlt, AppPlanDto.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke appPlanDtoRead,"
                            + "host is {}, serviceId is {}, planId is {}, e is {}",
                    host, serviceId, planId, e);
        }
        return dto;
    }

    @Override
    public List<ServiceDto> searchServiceByName(String host, String accessToken, String serviceName) {
        log.info("******start to invoke searchServiceByName, "
                        + "host is {}, serviceName is {}",
                host, serviceName);
        List<ServiceDto> resultService = new ArrayList<>();
        ServiceDtos serviceDtos = new ServiceDtos();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_LIST_SERVICES + "?access_token=" + accessToken);
            log.info("******end to invoke searchServiceByName, "
                            + "host is {}, serviceName is {}, rlt is {}",
                    host, serviceName, rlt);
            if (null == rlt) {
                return serviceDtos.getServices();
            } else {
                serviceDtos = JSON.parseObject(rlt, ServiceDtos.class);
                Iterator<ServiceDto> iterator = serviceDtos.getServices().iterator();
                while (iterator.hasNext()) {
                    ServiceDto serviceDto = iterator.next();
                    if (!serviceDto.getService().getName().contains(serviceName)) {
                        //服务名不包含的直接移除
                        iterator.remove();
                        continue;
                    }
                    //2019-11-28非常影响后端响应，暂时屏蔽，后续接管网关发布之后处理
                    //查询服务标签
//                    Boolean mark = false;
//                    List<Feature> featureDtos = featureStud.findFeatureByServiceId(host, accessToken, serviceDto
//                    .getService().getId()).getFeature();
//                    if (null != featureDtos && featureDtos.size() > 0) {
//                        for (int i = 0, size = featureDtos.size(); i < size; i++) {
//                            if(LdapResConstant.NO_PLAY.equals(featureDtos.get(i).getName()) && featureDtos.get(i)
//                            .getVisible()){
//                                //过滤不显示的服务
//                                mark = true;
//                                break;
//                            }
//                        }
//                        if (mark) {
//                            //这个服务过滤掉
//                            iterator.remove();
//                            continue;
//                        }
//                    }
                    // 如果 完全匹配上，列表放到第一个
                    if (serviceDto.getService().getName().equals(serviceName)) {
                        resultService.add(0, serviceDto);
                    } else {
                        resultService.add(serviceDto);
                    }
                }
            }
        } catch (Exception e) {
            log.error("******fail to invoke searchServiceByName, "
                            + "host is {}, serviceName is {}, e is {}",
                    host, serviceName, e);
        }
        return resultService;
    }

    @Override
    public MetricsDto getServiceMetricList(String host, String accessToken, String serviceId) {
        log.info("******start to invoke getServiceMetricList, "
                        + "host is {}, serviceId is {}",
                host, serviceId);
        MetricsDto metricsDto = new MetricsDto();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_SERVICES + serviceId + "/metrics.json?access_token=" + accessToken);
            log.info("******end to invoke getServiceMetricList, "
                            + "host is {}, id is {}, rlt is {}",
                    host, serviceId, rlt);
            if (null == rlt) {
                return metricsDto;
            } else {
                metricsDto = JSON.parseObject(rlt, MetricsDto.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke getServiceMetricList, "
                            + "host is {}, id is {}, e is {}",
                    host, serviceId, e);
        }
        return metricsDto;
    }

    @Override
    public MetricXmls getServiceMetricXmlList(String host, String accessToken, String serviceId) {
        log.info("******start to invoke getServiceMetricList, "
                        + "host is {}, serviceId is {}",
                host, serviceId);
        MetricXmls metricXmls = new MetricXmls();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_SERVICES + serviceId + "/metrics.xml?access_token=" + accessToken);
            log.info("******end to invoke getServiceMetricList, "
                            + "host is {}, id is {}, rlt is {}",
                    host, serviceId, rlt);
            if (null == rlt) {
                return metricXmls;
            } else {
                metricXmls = (MetricXmls) XmlUtils.xmlStrToObject(MetricXmls.class, rlt);
            }
        } catch (Exception e) {
            log.error("******fail to invoke getServiceMetricList, "
                            + "host is {}, id is {}, e is {}",
                    host, serviceId, e);
        }
        return metricXmls;
    }

    @Override
    public ServiceDto createService(String host, String accessToken, String name, String description,
                                    String systemName) {
        log.info("******start to invoke createService, "
                        + "host is {}",
                host);
        ServiceDto serviceDto = new ServiceDto();
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("access_token", accessToken);
            map.put("name", name);
            map.put("description", description);
            map.put("deployment_option", SystemNameConstant.DEPLOYMENT_OPTION);
            map.put("backend_version", SystemNameConstant.BACKEND_VERSION);
            map.put("system_name", systemName);
            String rlt =
                    HttpUtil.sendPost(host + PATH_LIST_SERVICES, map);
            log.info("******end to invoke createService, "
                            + "host is {},rlt is {}",
                    host, rlt);
            if (null == rlt) {
                return serviceDto;
            } else {
                serviceDto = JSON.parseObject(rlt, ServiceDto.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke createService, "
                            + "host is {},e is {}",
                    host, e);
        }
        return serviceDto;
    }

    @Override
    public void serviceDelete(String host, String accessToken, String serviceId) {
        log.info("******start to invoke serviceDelete, "
                        + "host is {}, serviceId is {}",
                host, serviceId);
        try {
            String rlt =
                    HttpUtil.sendDel(host + PATH_SERVICES + serviceId + ".json?access_token=" + accessToken);
            log.info("******end to invoke serviceDelete, "
                            + "host is {}, serviceId is {}, rlt is {}",
                    host, serviceId, rlt);
        } catch (Exception e) {
            log.error("******fail to invoke serviceDelete, "
                            + "host is {}, serviceId is {}, e is {}",
                    host, serviceId, e);
        }
    }

    @Override
    public Result<Object> proxyUpdate(String host, String accessToken, String serviceId, String endpoint, String apiBackend,
                            String sandboxEndpoint, Proxy proxy,String secretToken) {
        Result<Object> rlt = new Result<Object>();
        log.info("******start to invoke proxyUpdate, host is {}", host);
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("access_token", accessToken);
            map.put("endpoint", endpoint);
            map.put("api_backend", apiBackend);
            map.put("credentials_location", SystemNameConstant.CREDENTIALS_LOCATION);
            map.put("sandbox_endpoint", sandboxEndpoint);
            map.put("hostname_rewrite", proxy.getHostnameRewrite());

            map.put("error_auth_failed", proxy.getErrorAuthFailed());
            map.put("error_status_auth_failed", proxy.getErrorStatusAuthFailed());
            map.put("error_headers_auth_failed", proxy.getErrorHeadersAuthFailed());

            map.put("error_auth_missing", proxy.getErrorAuthMissing());
            map.put("error_status_auth_missing", proxy.getErrorStatusAuthMissing());
            map.put("error_headers_auth_missing", proxy.getErrorHeadersAuthMissing());

            map.put("error_no_match", proxy.getErrorNoMatch());
            map.put("error_status_no_match", proxy.getErrorStatusNoMatch());
            map.put("error_headers_no_match", proxy.getErrorHeadersNoMatch());

            map.put("error_limits_exceeded", proxy.getErrorLimitsExceeded());
            map.put("error_status_limits_exceeded", proxy.getErrorStatusLimitsExceeded());
            map.put("error_headers_limits_exceeded", proxy.getErrorHeadersLimitsExceeded());
            if(StringUtils.isNotBlank(secretToken))  map.put("secret_token", secretToken);
            String url = host + PATH_SERVICES + serviceId + "/proxy.xml";
            String strxml = HttpUtil.sendPatch(url, map);

            log.info("******end to invoke proxyUpdate,host is {},apiBackend is {},rlt is {}", host, apiBackend,strxml);
            if (strxml == null || "".equals(strxml)) {
                rlt.setError("调用网关接口失败,url:"+url);
                return rlt;
            } else {
                JSONObject json = XmlUtils.xml2Json(strxml);
                if (json.containsKey("errors")) {
                    rlt.setError(String.valueOf(json.get("errors")));
                    return rlt;
                }
                rlt.setData(json);
                return rlt;
            }
        } catch (Exception e) {
            log.error("******fail to invoke proxyUpdate, "+ "host is {},e is {}",host, e);
            rlt.setError(String.format("******fail to invoke proxyUpdate, host is %s,e is %s",host, e.getMessage()));
            return rlt;
        }
    }

    @Override
    public void configPromote(String host, String accessToken, String serviceId, String env, String version,
                              String to) {
        log.info("******start to invoke configPromote, "
                        + "host is {}",
                host);
//        ServiceDto serviceDto = new ServiceDto();
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("access_token", accessToken);
//            map.put("serviceId", serviceId);
//            map.put("env", env);
//            map.put("version", version);
            map.put("to", to);
            String rlt =
                    HttpUtil.sendPost(host + PATH_SERVICES + serviceId + "/proxy/configs/" + env + "/" + version +
                            "/promote.json", map);
            log.info("******end to invoke configPromote, "
                            + "host is {},rlt is {}",
                    host, rlt);
//            if (null == rlt) {
//                return serviceDto;
//            } else {
//                serviceDto = JSON.parseObject(rlt, ServiceDto.class);
//            }
        } catch (Exception e) {
            log.error("******fail to invoke configPromote, "
                            + "host is {},e is {}",
                    host, e);
        }
    }

    @Override
    public ProxyConfigDto latestPromote(String host, String accessToken, Long id, String env) {
        {
            log.info("******start to invoke latestPromote, "
                            + "host is {}, id is {}",
                    host, id);
            ProxyConfigDto proxyConfigDto = new ProxyConfigDto();
            try {
                String rlt =
                        HttpUtil.sendGet(host + PATH_SERVICES + id + "/proxy/configs/" + env + "/latest" +
                                ".json?access_token=" + accessToken);
                log.info("******end to invoke latestPromote, "
                                + "host is {}, id is {}, rlt is {}",
                        host, id, rlt);
                if (null == rlt) {
                    return proxyConfigDto;
                } else {
                    proxyConfigDto = JSON.parseObject(rlt, ProxyConfigDto.class);
                }
            } catch (Exception e) {
                log.error("******fail to invoke latestPromote, "
                                + "host is {}, id is {}, e is {}",
                        host, id, e);
            }
            return proxyConfigDto;
        }
    }

    @Override
    public ProxyConfigDtos listPromote(String host, String accessToken, Long id, String env) {
        log.info("******start to invoke listPromote, "
                        + "host is {}, id is {}",
                host, id);
        ProxyConfigDtos proxyConfigDtos = new ProxyConfigDtos();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_SERVICES + id + "/proxy/configs/" + env + ".json?access_token=" + accessToken);
            log.info("******end to invoke listPromote, "
                            + "host is {}, id is {}, rlt is {}",
                    host, id, rlt);
            if (null == rlt) {
                return proxyConfigDtos;
            } else {
                proxyConfigDtos = JSON.parseObject(rlt, ProxyConfigDtos.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke listPromote, "
                            + "host is {}, id is {}, e is {}",
                    host, id, e);
        }
        return proxyConfigDtos;

    }

    @Override
    public ProxyConfigDto getConfigByVersion(String host, String accessToken, Long id, String env, String version) {
        log.info("******start to invoke getConfigByVersion, "
                        + "host is {}, id is {}",
                host, id);
        ProxyConfigDto proxyConfigDto = new ProxyConfigDto();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_SERVICES + id + "/proxy/configs/" + env + "/" + version + ".json" +
                            "?access_token=" + accessToken);
            log.info("******end to invoke getConfigByVersion, "
                            + "host is {}, id is {}, rlt is {}",
                    host, id, rlt);
            if (null == rlt) {
                return proxyConfigDto;
            } else {
                proxyConfigDto = JSON.parseObject(rlt, ProxyConfigDto.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke getConfigByVersion, "
                            + "host is {}, id is {}, e is {}",
                    host, id, e);
        }
        return proxyConfigDto;
    }

    @Override
    public ServiceXmlDto createXmlService(String host, String accessToken, String name, String description,
                                          String systemName) {
        log.info("******start to invoke createXmlService, " + "host is {}", host);
        ServiceXmlDto serviceXmlDto = new ServiceXmlDto();
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("access_token", accessToken);
            map.put("name", name);
            map.put("description", description);
            map.put("deployment_option", SystemNameConstant.DEPLOYMENT_OPTION);
            map.put("backend_version", SystemNameConstant.BACKEND_VERSION);
            map.put("system_name", systemName);
            String rlt =
                    HttpUtil.sendPost(host + PATH_LIST_SERVICES_XML, map);
            log.info("******end to invoke createXmlService, "
                    + "host is {},rlt is {}", host, rlt);
            if (null == rlt) {
                return serviceXmlDto;
            } else {
                serviceXmlDto = (ServiceXmlDto) XmlUtils.xmlStrToObject(ServiceXmlDto.class, rlt);
            }
        } catch (Exception e) {
            log.error("******fail to invoke createXmlService, "
                    + "host is {},e is {}", host, e);
        }
        return serviceXmlDto;
    }

    @Override
    public void updateServiceDesc(String host, String accessToken, String serviceId, String description, String name) {
        log.info("******start to invoke updateService, " + "host is {}", host);
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("access_token", accessToken);
            map.put("description", description);
            map.put("name", name);
            String rlt =
                    HttpUtil.sendPut(host + PATH_SERVICES + serviceId + ".json", map);
            log.info("******end to invoke updateService, host is {},rlt is {}", host, rlt);
        } catch (Exception e) {
            log.error("******fail to invoke updateService, host is {},e is {}", host, e);
        }
    }

}
