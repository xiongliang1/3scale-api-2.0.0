package com.hisense.gateway.library.utils.api;

import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.model.dto.web.ApiMappingRuleDto;
import com.hisense.gateway.library.model.pojo.base.ApiMappingRule;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static com.hisense.gateway.library.constant.BaseConstants.TAG;

@Slf4j
public class MappingRuleUtil {
    /**
     * API MappingRule 校验
     * self-managed模式下,同一实例上mappingRule必须确保唯一,若不同API拥有相同MappingRule,则3scale路由不会与API一一对应
     * 即,同partition中,不允许 存在 具有一或多个相同mappingRule的不同API
     */
    public static List<ApiMappingRule> buildRuleWithDtos(@NonNull List<ApiMappingRuleDto> dtos, boolean nested) {
        List<ApiMappingRule> apiMappingRules = new ArrayList<>();

        for (ApiMappingRuleDto apiMappingRuleDto : dtos) {
            apiMappingRules.add(buildRuleWithDto(apiMappingRuleDto, nested));
        }

        return apiMappingRules;
    }

    public static List<ApiMappingRuleDto> buildDtoWithRules(@NonNull List<ApiMappingRule> rules) {
        List<ApiMappingRuleDto> dtos = new ArrayList<>();

        for (ApiMappingRule apiMappingRule : rules) {
            dtos.add(buildDtoWithRule(apiMappingRule));
        }

        return dtos;
    }

    public static boolean existDuplicateRules(@NonNull List<ApiMappingRuleDto> ruleDtos) {
        Set<String> ruleString = ruleDtos.stream().map(item -> String.format("%s/%s",
                item.getHttpMethod(),
                item.getPattern())).collect(Collectors.toSet());
        return ruleString.size() < ruleDtos.size();
    }

    public static boolean existSimilarRules(@NonNull List<ApiMappingRuleDto> ruleDtos,List<ApiMappingRule> mappingRules,Integer apiId){

        for(ApiMappingRuleDto dto : ruleDtos){
            log.info("url+pattern:{}",dto.getPattern());
            String[] arr =dto.getPattern().split("/");
            String url = arr[1];
            log.info("url:{}",url);
            for(ApiMappingRule rule : mappingRules){
                if(null != apiId && String.valueOf(rule.getPublishApi().getId()).equals(String.valueOf(apiId))){
                    continue;
                }
                Boolean status = rule.getPattern().contains(dto.getPattern());
                String[] strArr = rule.getPattern().split("/");
                String uri = strArr[1];
                log.info("uri:{}",uri);
                if(url.equals(uri)){
                    if(status){
                        return true;
                    }else{
                        if(dto.getPattern().contains(rule.getPattern()) || rule.getPattern().contains(dto.getPattern()) ){
                            return true;
                        }
                    }
                }

            }
        }
        return false;
    }

    public static void removeDupRules(@NonNull List<ApiMappingRule> savedRules,
                                      @NonNull Collection<ApiMappingRuleDto> newRules) {
        Set<String> ruleTags = newRules.stream().map(item -> buildRuleTag(item.getPartition(), item.getHttpMethod(),
                item.getPattern())).collect(Collectors.toSet());
        savedRules.removeIf(rule -> ruleTags.contains(buildRuleTag(rule.getPartition(), rule.getHttpMethod(),
                rule.getPattern())));
    }

    private static String buildRuleTag(Integer partition, String method, String pattern) {
        return String.format("%d,%s,%s", partition, method, pattern);
    }

    /**
     * @param dto    dto
     * @param nested 是否深拷贝
     * @return MappingRule
     */
    private static ApiMappingRule buildRuleWithDto(@NonNull ApiMappingRuleDto dto, boolean nested) {
        ApiMappingRule apiMappingRule = new ApiMappingRule();
        apiMappingRule.setPartition(dto.getPartition());
        apiMappingRule.setHttpMethod(dto.getHttpMethod());
        apiMappingRule.setPattern(dto.getPattern());

        if (nested) {
            if (MiscUtil.isNotEmpty(dto.getRequestParams())) {
                apiMappingRule.setRequestParams(String.format("{\"requestParams\":%s}",
                        MiscUtil.toJson(dto.getRequestParams())));
            }

            if (dto.getRequestBody() != null) {
                apiMappingRule.setRequestBody(String.format("{\"requestBody\":%s}",
                        MiscUtil.toJson(dto.getRequestBody())));
            }

            if (dto.getResponseBody() != null) {
                apiMappingRule.setResponseBody(String.format("{\"responseBody\":%s}",
                        MiscUtil.toJson(dto.getResponseBody())));
            }
        }

        return apiMappingRule;
    }

    private static ApiMappingRuleDto buildDtoWithRule(@NonNull ApiMappingRule rule) {
        ApiMappingRuleDto apiMappingRuleDto = new ApiMappingRuleDto();
        apiMappingRuleDto.setHttpMethod(rule.getHttpMethod());
        apiMappingRuleDto.setPattern(rule.getPattern());

        if (MiscUtil.isNotEmpty(rule.getRequestParams())) {
            apiMappingRuleDto.setRequestParams(
                    MiscUtil.fromJson(rule.getRequestParams(), ApiMappingRuleDto.class).getRequestParams());
        }

        if (MiscUtil.isNotEmpty(rule.getRequestBody())) {
            apiMappingRuleDto.setRequestBody(
                    MiscUtil.fromJson(rule.getRequestBody(), ApiMappingRuleDto.class).getRequestBody());
        }

        if (MiscUtil.isNotEmpty(rule.getResponseBody())) {
            apiMappingRuleDto.setResponseBody(
                    MiscUtil.fromJson(rule.getResponseBody(), ApiMappingRuleDto.class).getResponseBody());
        }

        return apiMappingRuleDto;
    }

    public static String buildRuleHashWithContent(List<ApiMappingRule> rules) {
        StringBuilder sb = new StringBuilder();
        for (ApiMappingRule rule : rules) {
            sb.append(rule.getPartition())
                    .append(rule.getHttpMethod())
                    .append(rule.getPattern());
        }
        return sb.toString();
    }

    public static String buildDtoHashWithContent(List<ApiMappingRuleDto> ruleDtos) {
        return buildRuleHashWithContent(buildRuleWithDtos(ruleDtos, true));
    }

    /**
     * 入库时,pattern添加url前缀
     */
    public static void encodeMappingRule(@NonNull List<ApiMappingRuleDto> dtos, String prefixUrl) {
        for (ApiMappingRuleDto apiMappingRuleDto : dtos) {
            String pattern = apiMappingRuleDto.getPattern();
            if (pattern.startsWith("/") || prefixUrl.endsWith("/")) {
                if (pattern.startsWith("/") && prefixUrl.endsWith("/")) {
                    pattern = prefixUrl.substring(0, prefixUrl.length() - 1) + pattern;
                } else {
                    pattern = prefixUrl + pattern;
                }
            } else {
                pattern = prefixUrl + "/" + pattern;
            }

            log.info("{}prefixUrl={}",TAG,prefixUrl);
            log.info("{}pattern={}",TAG,pattern);
            apiMappingRuleDto.setPattern(pattern);
        }
    }

    /**
     * 前台展示时,pattern去掉前缀
     */
    public static void decodeMappingRule(@NonNull List<ApiMappingRuleDto> dtos, String prefixUrl) {
        dtos.forEach(item -> {
            String pattern = item.getPattern();
            if (pattern.contains(prefixUrl)) {
                item.setPattern(pattern.substring(prefixUrl.length()));
            }
        });
    }
}
