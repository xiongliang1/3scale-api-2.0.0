/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/20 @author peiyun
 */
package com.hisense.gateway.library.stud.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.constant.SystemNameConstant;
import com.hisense.gateway.library.stud.model.PolicieConfig;
import com.hisense.gateway.library.stud.model.PolicieConfigDto;
import com.hisense.gateway.library.stud.model.Proxy;
import com.hisense.gateway.library.stud.model.ProxyDto;
import com.hisense.gateway.library.utils.HttpUtil;
import com.hisense.gateway.library.stud.ProxyPoliciesStud;
import com.hisense.gateway.library.utils.meta.GlobalSettings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ProxyPoliciesStudImpl implements ProxyPoliciesStud {

    private static final String PATH_SERVICES = "/admin/api/services/";

    @Override
    public PolicieConfigDto updateAnonymousProxyPolicies(String host, String accessToken, String serviceId,
                                                         String appId, String appKey, String userKey) {
        log.info("start to updateAnonymousProxyPolicies,host {}, serviceId {}", host, serviceId);
        PolicieConfigDto policieConfigDto = new PolicieConfigDto();
        PolicieConfigDto oldPolicieConfigDto = this.proxyPoliciesChainShow(host, accessToken, serviceId);
        List<PolicieConfig> policieConfigs = new ArrayList<>();
        if (oldPolicieConfigDto != null) {
            policieConfigs = oldPolicieConfigDto.getPolicies_config();
        }

        try {
            Map<String, String> map = new HashMap<>();
            map.put("access_token", accessToken);
            JSONObject configuration = new JSONObject();
            if (GlobalSettings.isDefaultCredentialsWithUserKey()) {
                // user_key
                configuration.put("auth_type", "user_key");
                configuration.put("user_key", userKey);
            } else {
                configuration.put("auth_type", "app_id_and_app_key");
                configuration.put("app_key", appKey);
                configuration.put("app_id", appId);
            }

            JSONObject anonymous = new JSONObject();
            anonymous.put("name", "default_credentials");
            anonymous.put("version", "builtin");
            anonymous.put("configuration", configuration);
            anonymous.put("enabled", true);

            JSONArray policiesConfig = new JSONArray();
            policiesConfig.add(anonymous);
            if (policieConfigs != null && policieConfigs.size() > 0) {
                policiesConfig.addAll(policieConfigs);
            }
            map.put("policies_config", JSONArray.toJSONString(policiesConfig));
            String rlt =
                    HttpUtil.sendPut(host + PATH_SERVICES + serviceId + "/proxy/policies.json",map);
            log.info("******end to invoke updateAnonymousProxyPolicies, "
                            + "host is {}, serviceId is {}, rlt is {}",
                    host, serviceId, rlt);
            if (null == rlt) {
                return policieConfigDto;
            } else {
                policieConfigDto = JSON.parseObject(rlt, PolicieConfigDto.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke updateAnonymousProxyPolicies, "
                            + "host is {}, serviceId is {}, e is {}",
                    host, serviceId, e);
        }
        return policieConfigDto;
    }

    @Override
    public ProxyDto readProxy(String host, String accessToken, String serviceId) {
        log.info("******start to invoke readProxy, "
                        + "host is {}, serviceId is {}",
                host, serviceId);
        ProxyDto proxyDto = new ProxyDto();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_SERVICES + serviceId + "/proxy.xml?access_token=" + accessToken);
            log.info("******end to invoke readProxy, "
                            + "host is {}, id is {}, rlt is {}",
                    host, serviceId, rlt);
            if (null == rlt) {
                return proxyDto;
            } else {
                proxyDto = JSON.parseObject(rlt, ProxyDto.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke readProxy, "
                            + "host is {}, id is {}, e is {}",
                    host, serviceId, e);
        }
        return proxyDto;
    }

    @Override
    public ProxyDto editProxy(String host, String accessToken, String serviceId, String apiBackend,
                              Proxy proxy, String endpoint,String sandboxEndpoint,String secretToken) {
        log.info("******start to invoke editProxy, host is {}, serviceId is {}",
                host, serviceId);
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("access_token", accessToken);
            map.put("api_backend", apiBackend);
            map.put("hostname_rewrite", proxy.getHostnameRewrite());

            map.put("endpoint", endpoint);
            map.put("sandbox_endpoint", sandboxEndpoint);

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
            String rlt =
                    HttpUtil.sendPatch(host + PATH_SERVICES + serviceId + "/proxy.xml", map);

            log.info("******end to invoke editProxy, host is {}, serviceId is {}, rlt is {}",
                    host, serviceId, rlt);
        } catch (Exception e) {
            log.error("******fail to invoke editProxy, host is {}, serviceId is {}, e is {}",
                    host, serviceId, e);
        }
        return null;
    }

    @Override
    public PolicieConfigDto proxyPoliciesChainShow(String host, String accessToken, String serviceId) {
        log.info("******start to invoke proxyPoliciesChainShow, "
                        + "host is {}, serviceId is {}", host, serviceId);
        PolicieConfigDto policieConfigDto = new PolicieConfigDto();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_SERVICES + serviceId + "/proxy/policies.json?access_token=" + accessToken);
            log.info("******end to invoke proxyPoliciesChainShow, "
                            + "host is {}, serviceId is {}, rlt is {}",host, serviceId, rlt);
            if (null == rlt) {
                return policieConfigDto;
            } else {
                policieConfigDto = JSON.parseObject(rlt, PolicieConfigDto.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke proxyPoliciesChainShow, "
                            + "host is {}, serviceId is {}, e is {}",host, serviceId, e);
        }
        return policieConfigDto;
    }

    @Override
    public PolicieConfigDto offAnonymousProxyPolicies(String host, String accessToken, String serviceId, List<PolicieConfig> policieConfigs) {
        log.info("******start to invoke offAnonymousProxyPolicies, "
                        + "host is {}, serviceId is {}",host, serviceId);
        PolicieConfigDto policieConfigDto = new PolicieConfigDto();
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("access_token", accessToken);
            map.put("policies_config", JSONArray.toJSONString(policieConfigs));
            String rlt =
                    HttpUtil.sendPut(host + PATH_SERVICES + serviceId + "/proxy/policies.json",map);
            log.info("******end to invoke offAnonymousProxyPolicies, "
                            + "host is {}, serviceId is {}, rlt is {}", host, serviceId, rlt);
            if (null == rlt) {
                return policieConfigDto;
            } else {
                policieConfigDto = JSON.parseObject(rlt, PolicieConfigDto.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke offAnonymousProxyPolicies, "
                            + "host is {}, serviceId is {}, e is {}", host, serviceId, e);
        }
        return policieConfigDto;
    }

    @Override
    public PolicieConfigDto updateUrlRewritingProxyPolicies(String host, String accessToken, String serviceId, String url,
                                                            String httpMethod) {
        log.info("******start to invoke updateUrlRewritingProxyPolicies, host is {}, serviceId is {}",host, serviceId);
        PolicieConfigDto policieConfigDto = new PolicieConfigDto();
        //先读取
        PolicieConfigDto oldPolicieConfigDto = this.proxyPoliciesChainShow(host,accessToken, serviceId);
        List<PolicieConfig> policieConfigs = new ArrayList<>();
        if (null != oldPolicieConfigDto) {
            policieConfigs = oldPolicieConfigDto.getPolicies_config();
        }
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("access_token", accessToken);
            JSONObject configuration = new JSONObject();
            JSONArray commands = new JSONArray();
            JSONObject command = new JSONObject();
            command.put("op","sub");
            if (!url.startsWith("/")) {
                url = "/" + url;
            }
            /*if (!url.endsWith("/")) {
                url = url + "/";
            }*/

            command.put("regex", url);
            command.put("replace", "/");
            commands.add(command);
            configuration.put("commands",commands);
            if("webservice".equalsIgnoreCase(httpMethod)){
                JSONArray argesCommands = new JSONArray();
                JSONObject argeCommand = new JSONObject();
                argeCommand.put("op","delete");
                argeCommand.put("arg","user_key");
                argeCommand.put("value_type","liquid");
                argesCommands.add(argeCommand);
                configuration.put("query_args_commands",argesCommands);
            }
            JSONObject urlRewriting = new JSONObject();
            urlRewriting.put("name","url_rewriting");
            urlRewriting.put("version","builtin");
            urlRewriting.put("configuration",configuration);
            urlRewriting.put("enabled",true);
            JSONArray policiesConfig = new JSONArray();
            if (null != policieConfigs && policieConfigs.size() > 0) {
                policiesConfig.addAll(policieConfigs);
            }
            policiesConfig.add(urlRewriting);
            map.put("policies_config", JSONArray.toJSONString(policiesConfig));
            String rlt =
                    HttpUtil.sendPut(host + PATH_SERVICES + serviceId + "/proxy/policies.json",map);
            log.info("******end to invoke updateUrlRewritingProxyPolicies, "
                            + "host is {}, serviceId is {}, rlt is {}",
                    host, serviceId, rlt);
            if (null == rlt) {
                return policieConfigDto;
            } else {
                policieConfigDto = JSON.parseObject(rlt, PolicieConfigDto.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke updateUrlRewritingProxyPolicies, host is {}, serviceId is {}, e is {}",
                    host, serviceId, e);
        }
        return policieConfigDto;
    }

    @Override
    public PolicieConfigDto updateBatcherProxyPolicies(String host, String accessToken, String serviceId) {
        log.info("******start to invoke updateBatcherProxyPolicies,host is {}, serviceId is {}",
                host, serviceId);
        PolicieConfigDto policieConfigDto = new PolicieConfigDto();
        //先读取
        PolicieConfigDto oldPolicieConfigDto = this.proxyPoliciesChainShow(host,accessToken, serviceId);
        List<PolicieConfig> policieConfigs = new ArrayList<>();
        if (null != oldPolicieConfigDto) {
            policieConfigs = oldPolicieConfigDto.getPolicies_config();
        }
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("access_token", accessToken);
            JSONObject configuration = new JSONObject();
            configuration.put("batch_report_seconds",30);
            configuration.put("auths_ttl", 0);// mingguilai.ex
            JSONObject batch = new JSONObject();
            batch.put("name","3scale_batcher");
            batch.put("version","builtin");
            batch.put("configuration",configuration);
            batch.put("enabled",true);
            JSONArray policiesConfig = new JSONArray();
            policiesConfig.add(batch);
            if (null != policieConfigs && policieConfigs.size() > 0) {
                policiesConfig.addAll(policieConfigs);
            }
            map.put("policies_config", JSONArray.toJSONString(policiesConfig));
            String rlt =
                    HttpUtil.sendPut(host + PATH_SERVICES + serviceId + "/proxy/policies.json",map);
            log.info("******end to invoke updateBatcherProxyPolicies, "
                            + "host is {}, serviceId is {}, rlt is {}",
                    host, serviceId, rlt);
            if (null == rlt) {
                return policieConfigDto;
            } else {
                policieConfigDto = JSON.parseObject(rlt, PolicieConfigDto.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke updateBatcherProxyPolicies, "
                            + "host is {}, serviceId is {}, e is {}",
                    host, serviceId, e);
        }
        return policieConfigDto;
    }

    @Override
    public PolicieConfigDto changeUrlRewritingUrl(String host, String accessToken, String serviceId, String url,String httpMethod) {
        log.info("******start to invoke changeUrlRewritingUrl, host is {}, serviceId is {}",host, serviceId);
        PolicieConfigDto policieConfigDto = new PolicieConfigDto();
        //先读取
        PolicieConfigDto oldPolicieConfigDto = this.proxyPoliciesChainShow(host,accessToken, serviceId);
        List<PolicieConfig> policieConfigs = new ArrayList<>();
        if (null != oldPolicieConfigDto) {
            policieConfigs = oldPolicieConfigDto.getPolicies_config();
        }
        if(null != policieConfigs){
            policieConfigs.removeIf(item -> "url_rewriting".equals(item.getName()));
        }
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("access_token", accessToken);
            JSONObject configuration = new JSONObject();
            JSONArray commands = new JSONArray();
            JSONObject command = new JSONObject();
            command.put("op","sub");
            if (!url.startsWith("/")) {
                url = "/" + url;
            }
            if (!url.endsWith("/")) {
                url = url + "/";
            }
            command.put("regex",url);
            command.put("replace","/");
            commands.add(command);
            configuration.put("commands",commands);
            if("webservice".equalsIgnoreCase(httpMethod)){
                JSONArray argesCommands = new JSONArray();
                JSONObject argeCommand = new JSONObject();
                argeCommand.put("op","delete");
                argeCommand.put("arg","user_key");
                argeCommand.put("value_type","liquid");
                argesCommands.add(argeCommand);
                configuration.put("query_args_commands",argesCommands);
            }
            JSONObject urlRewriting = new JSONObject();
            urlRewriting.put("name","url_rewriting");
            urlRewriting.put("version","builtin");
            urlRewriting.put("configuration",configuration);
            urlRewriting.put("enabled",true);
            JSONArray policiesConfig = new JSONArray();
            if (null != policieConfigs && policieConfigs.size() > 0) {
                policiesConfig.addAll(policieConfigs);
            }
            policiesConfig.add(urlRewriting);
            map.put("policies_config", JSONArray.toJSONString(policiesConfig));
            String rlt =
                    HttpUtil.sendPut(host + PATH_SERVICES + serviceId + "/proxy/policies.json",map);
            log.info("******end to invoke changeUrlRewritingUrl, host is {}, serviceId is {}, rlt is {}",
                    host, serviceId, rlt);
            if (null == rlt) {
                return policieConfigDto;
            } else {
                policieConfigDto = JSON.parseObject(rlt, PolicieConfigDto.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke changeUrlRewritingUrl, host is {}, serviceId is {}, e is {}",
                    host, serviceId, e);
        }
        return policieConfigDto;
    }

    @Override
    public PolicieConfigDto configHisenseLog(String host, String accessToken, String serviceId, boolean enable, boolean verbose, Integer apiId) {
        log.info("start to configHisenseLog,host {}, serviceId {},enable={},verbose={}",
                host, serviceId,enable,verbose);
        PolicieConfigDto policieConfigDto = new PolicieConfigDto();
        PolicieConfigDto oldPolicieConfigDto = this.proxyPoliciesChainShow(host, accessToken, serviceId);
        List<PolicieConfig> policieConfigs = new ArrayList<>();
        if (oldPolicieConfigDto != null) {
            policieConfigs = oldPolicieConfigDto.getPolicies_config();
        }
        if(null != policieConfigs){
            policieConfigs.removeIf(item -> "hisense_log_config".equals(item.getName()));
        }

        try {
            Map<String, String> map = new HashMap<>();
            map.put("access_token", accessToken);
            JSONObject configuration = new JSONObject();
            configuration.put("enable", enable);
            configuration.put("serviceId", apiId);// do not modify this key,avoid replacing apicast image
            configuration.put("maxLength", 1024 * 1024);
            configuration.put("advanceLog", verbose);

            JSONObject hisenseLog = new JSONObject();
            hisenseLog.put("name", "hisense_log_config");
            hisenseLog.put("version", "builtin");
            hisenseLog.put("configuration", configuration);
            hisenseLog.put("enabled", true);

            JSONArray policiesConfig = new JSONArray();
            if (policieConfigs.size() > 0) {
                policiesConfig.addAll(policieConfigs);
            }
            policiesConfig.add(hisenseLog);

            map.put("policies_config", JSONArray.toJSONString(policiesConfig));

            String rlt = HttpUtil.sendPut(host + PATH_SERVICES + serviceId + "/proxy/policies.json", map);
            log.info("end to configHisenseLog,host {}, serviceId {}, rlt {}", host, serviceId, rlt);
            if (rlt == null) {
                return policieConfigDto;
            } else {
                policieConfigDto = JSON.parseObject(rlt, PolicieConfigDto.class);
            }
        } catch (Exception e) {
            log.error("error to configHisenseLog,host {}, serviceId {}, e {}", host, serviceId, e);
        }
        return policieConfigDto;
    }

    @Override
    public PolicieConfigDto updateAuthCachingPolicies(String host, String accessToken, String serviceId) {
        log.info("start to updateAuthCachingPolicies,host {}, serviceId {}", host, serviceId);
        PolicieConfigDto policieConfigDto = new PolicieConfigDto();
        PolicieConfigDto oldPolicieConfigDto = this.proxyPoliciesChainShow(host, accessToken, serviceId);
        List<PolicieConfig> policieConfigs = new ArrayList<>();
        if (oldPolicieConfigDto != null) {
            policieConfigs = oldPolicieConfigDto.getPolicies_config();
        }

        try {
            Map<String, String> map = new HashMap<>();
            map.put("access_token", accessToken);
            JSONObject configuration = new JSONObject();
            configuration.put("caching_type", "resilient");
            JSONObject batch = new JSONObject();
            batch.put("name", "caching");
            batch.put("version", "builtin");
            batch.put("configuration", configuration);
            batch.put("enabled", true);

            JSONArray policiesConfig = new JSONArray();
            policiesConfig.add(batch);
            if (policieConfigs != null && policieConfigs.size() > 0) {
                policiesConfig.addAll(policieConfigs);
            }

            map.put("policies_config", JSONArray.toJSONString(policiesConfig));

            String rlt = HttpUtil.sendPut(host + PATH_SERVICES + serviceId + "/proxy/policies.json", map);
            log.info("end to updateAuthCachingPolicies,host {}, serviceId {}, rlt {}", host, serviceId, rlt);
            if (rlt == null) {
                return policieConfigDto;
            } else {
                policieConfigDto = JSON.parseObject(rlt, PolicieConfigDto.class);
            }
        } catch (Exception e) {
            log.error("error to updateAuthCachingPolicies,host {}, serviceId {}, e {}", host, serviceId, e);
        }
        return policieConfigDto;
    }

    /**
     * 开启or更新流控策略
     * @param host 域名
     * @param accessToken token
     * @param serviceId scaleServiceId
     * @param window period
     * @param count value
     * @return result
     */
    @Override
    public PolicieConfigDto openEdgeLimiting(String host, String accessToken, String serviceId, Long window, Integer count) {
        log.info("start to openEdgeLimiting,host {}, serviceId {}", host, serviceId);
        PolicieConfigDto oldPolicieConfigDto = new PolicieConfigDto();
        try {
            oldPolicieConfigDto = this.proxyPoliciesChainShow(host, accessToken, serviceId);
            List<PolicieConfig> policieConfigs  = oldPolicieConfigDto.getPolicies_config();
            policieConfigs.removeIf(m -> "rate_limit".equals(m.getName()));

            JSONObject obj0 = new JSONObject();
            obj0.put("op","==");
            obj0.put("right","1");
            obj0.put("left_type","plain");
            obj0.put("left","1");
            obj0.put("right_type","plain");
            JSONArray operations = new JSONArray();
            operations.add(obj0);
            JSONObject condition = new JSONObject();
            condition.put("combine_op","and");
            condition.put("operations",operations);
            JSONObject key = new JSONObject();
            key.put("scope","service");
            key.put("name", UUID.randomUUID().toString().replaceAll("-",""));
            key.put("name_type","plain");
            JSONObject obj = new JSONObject();
            obj.put("window",window);
            obj.put("count",count);
            obj.put("condition",condition);
            obj.put("key",key);
            JSONArray fixedWindowLimiters = new JSONArray();//fixed_window_limiters
            fixedWindowLimiters.add(obj);
            JSONArray leakyBucketLimiters = new JSONArray();//leaky_bucket_limiters
            JSONObject limitsExceededError = new JSONObject();//limits_exceeded_error
            limitsExceededError.put("status_code",429);
            limitsExceededError.put("error_handling","exit");
            JSONObject configurationError  =new JSONObject();//configuration_error
            configurationError.put("status_code",500);
            configurationError.put("error_handling","exit");
            JSONObject configuration = new JSONObject();
            configuration.put("limits_exceeded_error",limitsExceededError);
            configuration.put("configuration_error",configurationError);
            configuration.put("fixed_window_limiters",fixedWindowLimiters);
            configuration.put("leaky_bucket_limiters",leakyBucketLimiters);
            PolicieConfig  rateLimit = new PolicieConfig();
            rateLimit.setName("rate_limit");
            rateLimit.setVersion("builtin");
            rateLimit.setEnabled(true);
            rateLimit.setConfiguration(configuration);
            policieConfigs.add(rateLimit);
            Map<String, String> map = new HashMap<>();
            map.put("access_token", accessToken);
            map.put("policies_config", JSONArray.toJSONString(policieConfigs));
            String rlt = HttpUtil.sendPut(host + PATH_SERVICES + serviceId + "/proxy/policies.json", map);
            log.info("end to openEdgeLimiting,host {}, serviceId {}, rlt {}", host, serviceId, rlt);
            if (rlt == null) {
                return oldPolicieConfigDto;
            } else {
                oldPolicieConfigDto = JSON.parseObject(rlt, PolicieConfigDto.class);
            }
        } catch (Exception e) {
            log.error("error to openEdgeLimiting,host {}, serviceId {}, e {}", host, serviceId, e);
        }
        return oldPolicieConfigDto;
    }

    /**
     * 关闭流控策略
     * @param host 域名
     * @param accessToken token
     * @param serviceId scaleServiceId
     * @return result
     */
    @Override
    public PolicieConfigDto closeEdgeLimiting(String host, String accessToken, String serviceId) {
        log.info("start to closeEdgeLimiting,host {}, serviceId {}", host, serviceId);
        PolicieConfigDto oldPolicieConfigDto = new PolicieConfigDto();
        try {
            oldPolicieConfigDto = this.proxyPoliciesChainShow(host, accessToken, serviceId);
            List<PolicieConfig> policieConfigs  = oldPolicieConfigDto.getPolicies_config();
            policieConfigs.removeIf(m -> "rate_limit".equals(m.getName()));
            Map<String, String> map = new HashMap<>();
            map.put("access_token", accessToken);
            map.put("policies_config", JSONArray.toJSONString(policieConfigs));
            String rlt = HttpUtil.sendPut(host + PATH_SERVICES + serviceId + "/proxy/policies.json", map);
            log.info("end to closeEdgeLimiting,host {}, serviceId {}, rlt {}", host, serviceId, rlt);
            if (rlt == null) {
                return oldPolicieConfigDto;
            } else {
                oldPolicieConfigDto = JSON.parseObject(rlt, PolicieConfigDto.class);
            }
        } catch (Exception e) {
            log.error("error to closeEdgeLimiting,host {}, serviceId {}, e {}", host, serviceId, e);
        }
        return oldPolicieConfigDto;
    }

}
