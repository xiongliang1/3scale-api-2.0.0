package com.hisense.gateway.library.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.PolicyHeader;
import com.hisense.gateway.library.service.ApiCastService;
import com.hisense.gateway.library.stud.ProxyPoliciesStud;
import com.hisense.gateway.library.stud.model.ApiCastParam;
import com.hisense.gateway.library.stud.model.PolicieConfig;
import com.hisense.gateway.library.stud.model.PolicieConfigDto;
import com.hisense.gateway.library.utils.HttpUtil;
import com.hisense.gateway.library.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hisense.gateway.library.constant.ApiCastConstant.*;

@Slf4j
@Service
public class ApiCastServiceImpl implements ApiCastService {
    @Autowired
    ProxyPoliciesStud policiesStud;

    /**
     * 返回排除指定policy的策略列表
     */
    private List<PolicieConfig> excludeSpecialPolicy(ApiCastParam param, String policyName) {
        PolicieConfigDto existConfig = policiesStud.proxyPoliciesChainShow(param.getHost(), param.getAccessToken(),
                param.getServiceId());

        List<PolicieConfig> configs = new ArrayList<>();
        if (existConfig != null) {
            configs = existConfig.getPolicies_config();
        }
        configs.removeIf(item -> policyName.equals(item.getName()));
        return configs;
    }

    @Override
    public Result<PolicieConfigDto> configIpBlackWhiteList(ApiCastParam param, List<String> ipAddress, boolean blackList) {
        Result<PolicieConfigDto> result = new Result<>(Result.FAIL, "", null);
        Result<String> ipCheckResult = IpUtils.ipValidationCheck(ipAddress);
        if (Result.FAIL.equals(ipCheckResult.getCode())) {
            result.setMsg(String.format("指定的IP地址无效,IP=%s",ipCheckResult.getMsg()));
            return result;
        }

        log.info("Start to ConfigIpBlackWhiteList {} {} {}", param, blackList, ipAddress);

        List<PolicieConfig> existPolicies = excludeSpecialPolicy(param, API_POLICY_IPCHECK);

        PolicieConfigDto policieConfigDto = new PolicieConfigDto();
        try {
            Map<String, String> map = new HashMap<>();
            map.put("access_token", param.getAccessToken());

            JSONObject configuration = new JSONObject();
            configuration.put("ips", ipAddress.toArray(new String[0]));
            configuration.put("check_type", blackList ? "blacklist" : "whitelist");
            configuration.put("client_ip_sources", new String[]{"X-Forwarded-For", "X-Real-IP", "last_caller"});
            String errorMsg = "";
            if(blackList){
                errorMsg = "ipcheck:caller's ip is in the blacklist";
            }else{
                errorMsg = "ipcheck:caller‘s ip is not in the whitelist";
            }
            configuration.put("error_msg", errorMsg);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", API_POLICY_IPCHECK);
            jsonObject.put("version", "builtin");
            jsonObject.put("configuration", configuration);
            jsonObject.put("enabled", true);

            JSONArray policiesConfig = new JSONArray();
            if (existPolicies.size() > 0) {
                policiesConfig.addAll(existPolicies);
            }
            policiesConfig.add(jsonObject);

            map.put("policies_config", JSONArray.toJSONString(policiesConfig));

            String rlt = HttpUtil.sendPut(String.format(SERVICES_POLICY_JSON, param.getHost(), param.getServiceId()), map);
            log.info("End to ConfigIpBlackWhiteList {} {}", param, rlt);
            if (rlt == null) {
                result.setMsg("3scale通信异常");
                result.setData(policieConfigDto);
                return result;
            } else {
                policieConfigDto = JSON.parseObject(rlt, PolicieConfigDto.class);
            }
        } catch (Exception e) {
            log.error("Error to ConfigIpBlackWhiteList {} {} {} e={}", param, blackList, ipAddress, e);
        }

        result.setCode(Result.OK);
        result.setData(policieConfigDto);
        return result;
    }

    @Override
    public Result<PolicieConfigDto> configConnectionTimeout(ApiCastParam param, Integer timeout) {
        Result<PolicieConfigDto> result = new Result<>(Result.OK, "", null);
        log.info("Start to ConfigConnectionTimeout {} timeout={}", param, timeout);

        List<PolicieConfig> existPolicies = excludeSpecialPolicy(param, API_POLICY_UPCONN);

        PolicieConfigDto policieConfigDto = new PolicieConfigDto();
        try {
            Map<String, String> map = new HashMap<>();
            map.put("access_token", param.getAccessToken());

            JSONObject configuration = new JSONObject();
            configuration.put("connect_timeout", timeout);
            configuration.put("send_timeout", timeout);
            configuration.put("read_timeout", timeout);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", API_POLICY_UPCONN);
            jsonObject.put("version", "builtin");
            jsonObject.put("configuration", configuration);
            jsonObject.put("enabled", true);

            JSONArray policiesConfig = new JSONArray();
            if (existPolicies.size() > 0) {
                policiesConfig.addAll(existPolicies);
            }
            policiesConfig.add(jsonObject);

            map.put("policies_config", JSONArray.toJSONString(policiesConfig));

            String rlt = HttpUtil.sendPut(String.format(SERVICES_POLICY_JSON, param.getHost(), param.getServiceId()),
                    map);
            log.info("End to ConfigConnectionTimeout {} {}", param, rlt);
            if (rlt == null) {
                result.setMsg("3scale通信异常");
                result.setData(policieConfigDto);
                return result;
            } else {
                policieConfigDto = JSON.parseObject(rlt, PolicieConfigDto.class);
            }
        } catch (Exception e) {
            log.info("Start to ConfigConnectionTimeout {} timeout={}, e={}", param, timeout, e);
        }

        result.setData(policieConfigDto);
        return result;
    }

    @Override
    public Result<PolicieConfigDto> configHeaderpolicy(ApiCastParam param, List<PolicyHeader> requestHeader, List<PolicyHeader> responseHeader) {
        Result<PolicieConfigDto> result = new Result<>(Result.FAIL, "", null);
        log.info("Start to configHeaderpolicy {} {} {}", param, requestHeader, responseHeader);

        List<PolicieConfig> existPolicies = excludeSpecialPolicy(param, API_POLICY_HEADER);

        PolicieConfigDto policieConfigDto = new PolicieConfigDto();
        try {
            Map<String, String> map = new HashMap<>();
            map.put("access_token", param.getAccessToken());

            JSONObject configuration = new JSONObject();
            if(!CollectionUtils.isEmpty(requestHeader)){
                List<JSONObject> requestList = new ArrayList<>();
                for(PolicyHeader item:requestHeader){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("op", item.getOp());
                    jsonObject.put("header", item.getHeader());
                    jsonObject.put("value_type", item.getValueType());
                    jsonObject.put("value", item.getValue());
                    requestList.add(jsonObject);
                }
                configuration.put("request", requestList);
            }
            if(!CollectionUtils.isEmpty(responseHeader)){
                List<JSONObject> responseList = new ArrayList<>();
                for(PolicyHeader item:responseHeader){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("op", item.getOp());
                    jsonObject.put("header", item.getHeader());
                    jsonObject.put("value_type", item.getValueType());
                    jsonObject.put("value", item.getValue());
                    responseList.add(jsonObject);
                }
                configuration.put("response", responseList);
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", API_POLICY_HEADER);
            jsonObject.put("version", "builtin");
            jsonObject.put("configuration", configuration);
            jsonObject.put("enabled", true);

            JSONArray policiesConfig = new JSONArray();
            if (existPolicies.size() > 0) {
                policiesConfig.addAll(existPolicies);
            }
            policiesConfig.add(jsonObject);

            map.put("policies_config", JSONArray.toJSONString(policiesConfig));

            String rlt = HttpUtil.sendPut(String.format(SERVICES_POLICY_JSON, param.getHost(), param.getServiceId()), map);
            log.info("End to configHeaderpolicy {} {}", param, rlt);
            if (rlt == null) {
                result.setMsg("3scale通信异常");
                result.setData(policieConfigDto);
                return result;
            } else {
                policieConfigDto = JSON.parseObject(rlt, PolicieConfigDto.class);
            }
        } catch (Exception e) {
            log.error("Error to configHeaderpolicy {} {} {} e={}", param, requestHeader, responseHeader, e);
        }

        result.setCode(Result.OK);
        result.setData(policieConfigDto);
        return result;
    }
}
