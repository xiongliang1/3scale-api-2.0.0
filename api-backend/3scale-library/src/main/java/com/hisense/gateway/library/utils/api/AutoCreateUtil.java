/*
 * @author mingguilai.ex
 * @date 2020-07-15
 */
package com.hisense.gateway.library.utils.api;

import com.hisense.api.library.model.*;
import com.hisense.api.library.utils.MiscUtil;
import com.hisense.api.library.utils.TypeUtil;
import com.hisense.gateway.library.constant.ApiConstant;
import com.hisense.gateway.library.model.base.eureka.EurekaApp;
import com.hisense.gateway.library.model.base.eureka.EurekaInstance;
import com.hisense.gateway.library.model.base.meta.*;
import com.hisense.gateway.library.model.base.nacos.NacosInstance;
import com.hisense.gateway.library.model.base.nacos.NacosService;
import com.hisense.gateway.library.model.dto.web.ApiMappingRuleDto;
import com.hisense.gateway.library.model.dto.web.PublishApiDto;
import com.hisense.gateway.library.model.pojo.base.EurekaService;
import com.hisense.gateway.library.model.pojo.base.PublishApi;
import com.hisense.gateway.library.model.pojo.base.PublishApiGroup;
import com.hisense.gateway.library.utils.RequestUtils;
import com.hisense.gateway.library.beans.CommonBeanUtil;
import com.hisense.gateway.library.utils.meta.GlobalSettings;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.hisense.api.library.Constants.*;
import static com.hisense.gateway.library.constant.ApiConstant.API_SRC_EUREKA_AUTO;
import static com.hisense.gateway.library.constant.BaseConstants.TAG;
import static com.hisense.gateway.library.model.ModelConstant.API_INIT;
import static com.hisense.gateway.library.constant.ApiConstant.ApiParamDataType;

/**
 * 自动创建API相关例程
 */
@Slf4j
public class AutoCreateUtil {
    /**
     * 修正Eureka的服务地址
     *
     * @param url 原始地址
     * @return 增加schema, 去掉尾部slash的地址
     */
    public static String fixEurekaZoneUrl(String url) {
        return MiscUtil.addHttpPrefix(MiscUtil.removeSuffixSlash(url));
    }

    /**
     * 使用EurekaApp构建EurekaService列表
     *
     * @param eurekaApps      EurekaApp列表
     * @param publishApiGroup 分组
     * @return EurekaService列表
     */
    public static List<EurekaService> buildEurekaServices(List<EurekaApp> eurekaApps, PublishApiGroup publishApiGroup) {
        List<EurekaService> eurekaServices = new ArrayList<>();

        for (EurekaApp eurekaApp : eurekaApps) {
            log.info("{}Start to build EurekaService for {}", TAG, eurekaApp.getName());

            EurekaInstance eurekaInstance;
            if (MiscUtil.isEmpty(eurekaApp.getInstance()) || MiscUtil.isEmpty((eurekaInstance =
                    eurekaApp.getInstance().get(0)).getMetadata())) {
                continue;
            }

            EurekaService service;
            if ((service = buildEurekaService(eurekaInstance.getMetadata(), publishApiGroup)) == null) {
                log.error("{}Fail to build api from metadata", TAG);
                continue;
            }

            if (!service.isValid()) {
                log.error("{}Invalid content for eureka %{}", TAG, service.toString());
                continue;
            }

            eurekaServices.add(service);
        }
        return eurekaServices;
    }

    /**
     * 用Eureka Metadata构建EurekaService
     *
     * @param map             Eureka Metadata
     * @param publishApiGroup 分组
     * @return EurekaService
     */
    public static EurekaService buildEurekaService(Map<String, String> map, PublishApiGroup publishApiGroup) {
        if (MiscUtil.isEmpty(map)) {
            log.error("{}Invalid map", TAG);
            return null;
        }

        EurekaService eurekaService = new EurekaService();
        eurekaService.setInstanceId(map.get(META_MAP_KEY_INS_INSTANCE_ID));
        eurekaService.setEurekaZone(fixEurekaZoneUrl(map.get(META_MAP_KEY_EUREKA_ZONE)));
        eurekaService.setServiceName(map.get(META_MAP_KEY_APP_SERVICE_NAME));

        // 优先使用微服务自带的systemName字段,其次是主动拉取指定的group携带的systemName,最后是SpringServiceName
        String systemName = null;
        if (MiscUtil.isNotEmpty(map.get(META_MAP_KEY_CUS_SYSTEM_NAME))) {
            systemName = map.get(META_MAP_KEY_CUS_SYSTEM_NAME);
        } else if (publishApiGroup != null && MiscUtil.isNotEmpty(publishApiGroup.getSystemName())) {
            systemName = publishApiGroup.getSystemName();
        } else if (MiscUtil.isNotEmpty(map.get(META_MAP_KEY_APP_SERVICE_NAME))) {
            systemName = map.get(META_MAP_KEY_APP_SERVICE_NAME);
        }
        eurekaService.setSystemName(systemName);

        Integer groupId = null;
        Integer systemId = null;
        if (publishApiGroup != null) {
            groupId = publishApiGroup.getId();
            systemId = publishApiGroup.getSystem();
        }

        eurekaService.setManagementPort(map.get(META_MAP_KEY_CUS_MANAGE_PORT));
        eurekaService.setUrlPrefix(map.get(META_MAP_KEY_ENV_SERVER_CONTEXT_PATH));
        eurekaService.setStatus(1);
        eurekaService.setHost(String.format("%s:%s",map.get(META_MAP_KEY_INS_HOST_NAME),map.get(META_MAP_KEY_INS_PORT)));
        String noneSwaggerApiJson = map.get(META_MAP_KEY_NONE_SWAGGER_API);

        if (MiscUtil.isNotEmpty(noneSwaggerApiJson)) {
            log.info("{}Start build common api from metadata", TAG);
            if (log.isInfoEnabled()) {
                log.info("{} noneSwaggerApiJson {}", TAG, noneSwaggerApiJson);
            }

            CommonApi commonApi = MiscUtil.fromJson(noneSwaggerApiJson, CommonApi.class);
            if (commonApi == null) {
                log.error("{}Invalid common api from deSerialization", TAG);
                return null;
            }

            log.debug("{}commonApi={}", TAG, commonApi);

            eurekaService.setPublishApiDtos(buildPublishApiDtos(eurekaService, commonApi, groupId, systemId));
            log.info("{}End build common api", TAG);
        }

        String swaggerApiJson = map.get(META_MAP_KEY_SWAGGER_API);
        if (MiscUtil.isNotEmpty(swaggerApiJson)) {
            log.info("{}Start build swagger api from metadata", TAG);

            if (log.isTraceEnabled()) {
                log.info("{} swaggerApiJson {}", TAG, swaggerApiJson);
            }

            log.info("{}End build swagger api", TAG);
        }

        return eurekaService;
    }

    /**
     * 解析CommonApi内容,构造成ApiDto列表
     */
    private static List<PublishApiDto> buildPublishApiDtos(EurekaService eurekaService, CommonApi commonApi,
                                                           Integer groupId, Integer systemId) {
        if (eurekaService == null) {
            log.error("{}Invalid EurekaService", TAG);
            return null;
        }

        if (MiscUtil.isEmpty(commonApi.getPaths())) {
            log.error("{}Current apiList has no paths", TAG);
            return null;
        }

        // <Hash, PublishApiDto>
        Map<String, PublishApiDto> publishApiDtoMap = new HashMap<>();

        // <url>
        for (Map.Entry<String, Map<String, ApiPath>> entry1 : commonApi.getPaths().entrySet()) {
            String url = entry1.getKey();
            Map<String, ApiPath> apiPathMap = entry1.getValue();

            // 自动创建的api,使用(method+system+url)构造hash 唯一对应一个落库的API,
            // groupId仅用于分组查看, 不参与system确定,system来自Eureka服务自定义
            // 3scale也做相同处理,多个mappingRule不做合并
            // <method>
            for (Map.Entry<String, ApiPath> entry : apiPathMap.entrySet()) {
                ApiPath apiPath = entry.getValue();
                PublishApiDto publishApiDto = new PublishApiDto();
                initPublishApiDto(publishApiDto, url, apiPath, groupId, systemId, eurekaService);
                initMappingRules(publishApiDto, apiPath, commonApi.getDefinitions());
                if (publishApiDto.getSystemName() != null) {
                    publishApiDto.setUrl(String.format("/%s", publishApiDto.getSystemName()));
                }
                publishApiDto.setMappingRuleHash(PublishApiUtil.buildApiHash(publishApiDto));
                publishApiDtoMap.put(publishApiDto.getMappingRuleHash(), publishApiDto);
            }
        }

        log.info("{}Build {} APIS", TAG, publishApiDtoMap.size());
        return new ArrayList<>(publishApiDtoMap.values());
    }

    /**
     * 初始化PublishApiDto
     * <p>
     * make sure this routine called in springmvc-main-task,avoid exception for RequestContextHolder
     *
     * @param publishApiDto PublishApiDto
     * @param url           API-URL前缀
     * @param apiPath       ApiPath
     * @param groupId       主动拉取时指定的groupId,被动拉取的为null
     * @param systemId      主动拉取时指定的systemId,被动拉取的为null
     * @param eurekaService EurekaService
     */
    private static void initPublishApiDto(PublishApiDto publishApiDto, String url, ApiPath apiPath,
                                          Integer groupId, Integer systemId, EurekaService eurekaService) {
        publishApiDto.setName(apiPath.getName());
        publishApiDto.setDescription(apiPath.getDescription());
        publishApiDto.setUrl(url);
        publishApiDto.setAccessProType("http");
        publishApiDto.setAccessProtocol(GlobalSettings.getDefaultAccessProtocol());
        publishApiDto.setTargetType(GlobalSettings.getTargetType(publishApiDto.getAccessProtocol()));
        publishApiDto.setHost(eurekaService.getHost());
        publishApiDto.setPort(eurekaService.getManagementPort());
        publishApiDto.setStatus(API_INIT);
        // 自动创建的API,创建者统一设定为固定一个用户
        publishApiDto.setCreator(CommonBeanUtil.getLoginUserName());
        publishApiDto.setCreateTime(new Date());
        publishApiDto.setUpdateTime(new Date());
        publishApiDto.setIsOnline(0);
        publishApiDto.setIsInUse(0);
        publishApiDto.setGroupId(groupId);
        publishApiDto.setProxy(GlobalSettings.getDefaultProxy());
        publishApiDto.setCreator(CommonBeanUtil.getLoginUserName());
        publishApiDto.setPartition(GlobalSettings.getDefaultPartition());
        publishApiDto.setEnvironment(GlobalSettings.getDefaultEnvironment());
        publishApiDto.setNeedSubscribe(true);
        publishApiDto.setNeedAuth(true);
        publishApiDto.setNeedLogging(true);
        publishApiDto.setNeedRecordRet(true);
        publishApiDto.setAuthType(publishApiDto.isNeedAuth() ? "auth" : "noauth");
        publishApiDto.setTimeout(0);
        publishApiDto.setSourceType(API_SRC_EUREKA_AUTO);
        publishApiDto.setMethod(apiPath.getHttpMethod());
        publishApiDto.setSystemId(systemId);
        publishApiDto.setSystemName(eurekaService.getSystemName());
    }

    /**
     * 构建MappingRule
     */
    private static void initMappingRules(PublishApiDto publishApiDto, ApiPath apiPath,
                                         Map<String, ApiModelDefinition> definitions) {
        List<ApiMappingRuleDto> apiMappingRuleDtos = new ArrayList<>();
        publishApiDto.setApiMappingRuleDtos(apiMappingRuleDtos);

        ApiMappingRuleDto apiMappingRuleDto = new ApiMappingRuleDto();
        if (publishApiDto.getSystemName() != null) {
            apiMappingRuleDto.setPattern(String.format("/%s%s", publishApiDto.getSystemName(), publishApiDto.getUrl()));
        } else {
            apiMappingRuleDto.setPattern(publishApiDto.getUrl());
        }

        apiMappingRuleDto.setHttpMethod(publishApiDto.getMethod());
        apiMappingRuleDto.setPartition(GlobalSettings.getDefaultPartition());

        // Build ResponseBody
        ApiModelType apiResponse = apiPath.getResponse();
        if (!TypeUtil.isValid(apiResponse)) {
            log.error("{}Invalid apiResponse {}", TAG, apiResponse);
        } else {
            log.info("{}URL={},name={},apiResponse={}", TAG, apiPath.getUrl(), apiPath.getName(), apiResponse);

            ResponseBody responseBody = new ResponseBody();
            responseBody.setDataType(
                    ApiParamDataType.from(apiResponse.getDefineMode(), apiResponse.getDataType()).getDataType());

            if (MiscUtil.isNotEmpty(definitions) && apiResponse.isHasSubField()) {
                List<NestedTypeFirst> nestedTypeFirsts = buildNestedType(apiResponse.getDataType(), definitions);
                if (MiscUtil.isNotEmpty(nestedTypeFirsts)) {
                    responseBody.setObject(nestedTypeFirsts);
                }
            }

            apiMappingRuleDto.setResponseBody(responseBody);
        }

        // Build RequestParam & RequestBody
        List<ApiParam> apiParams;
        if (MiscUtil.isNotEmpty(apiParams = apiPath.getApiParams())) {
            RequestBody requestBody = null;
            List<RequestBody> requestParams = new ArrayList<>();

            for (ApiParam apiParam : apiParams) {
                ApiModelType modelType = apiParam.getModelType();
                if (modelType == null || !TypeUtil.isValid(modelType)) {
                    log.error("{}Invalid ApiParam for {} {} with {}", TAG, publishApiDto.getUrl(),
                            publishApiDto.getName(), apiParam);
                    continue;
                }

                RequestBody request = new RequestBody();

                request.setName(apiParam.getName());
                request.setValue(apiParam.getValue());
                request.setRequired(apiParam.isRequired());
                request.setParamType(apiParam.getParamType());

                ApiParamDataType dataType = ApiParamDataType.from(modelType.getDefineMode(), modelType.getDataType());
                request.setDataType(dataType.getDataType());
                request.setDefaultValue(MiscUtil.isNotEmpty(apiParam.getDefaultValue()) ? apiParam.getDefaultValue() :
                        dataType.getDefaultValue());

                if (MiscUtil.isNotEmpty(definitions) && modelType.isHasSubField()) {
                    List<NestedTypeFirst> nestedTypeFirsts = buildNestedType(modelType.getDataType(), definitions);
                    if (MiscUtil.isNotEmpty(nestedTypeFirsts)) {
                        request.setObject(nestedTypeFirsts);
                    }
                }

                switch (ApiConstant.ApiHttpParamType.from(apiParam.getParamType())) {
                    case FORM:
                    case PATH:
                    case QUERY:
                    case HEADER:
                        requestParams.add(request);
                        break;

                    case BODY:
                        if (requestBody == null) {
                            requestBody = request;
                        }
                        break;
                }
            }

            if (MiscUtil.isNotEmpty(requestParams)) {
                apiMappingRuleDto.setRequestParams(requestParams);
            }

            apiMappingRuleDto.setRequestBody(requestBody);
        }
        apiMappingRuleDtos.add(apiMappingRuleDto);
    }

    /**
     * 构建嵌套类型(避免Json循环解析,仅支持3层嵌套)
     *
     * @param dataType    微服务接口入参\返回值的类型
     * @param definitions 类型定义
     * @return 嵌套类型列表
     */
    private static List<NestedTypeFirst> buildNestedType(String dataType, Map<String, ApiModelDefinition> definitions) {
        ApiModelDefinition definition;
        Map<String, ApiModelType> nameTypeMap;
        if ((definition = definitions.get(dataType)) == null ||
                MiscUtil.isEmpty(nameTypeMap = definition.getNameTypeMap())) {
            return null;
        }

        // build NestedTypeFirst
        List<NestedTypeFirst> nestedTypeFirsts = new ArrayList<>();
        for (Map.Entry<String, ApiModelType> entry : nameTypeMap.entrySet()) {
            // check valid for current dataType;
            ApiModelType modelType = entry.getValue();
            if (modelType == null || !TypeUtil.isValid(modelType)) {
                log.error("{}{}.{} type invalid {}", TAG, dataType, entry.getKey(), modelType);
                continue;
            }

            NestedTypeFirst nestedTypeFirst = new NestedTypeFirst();
            nestedTypeFirst.setName(entry.getKey());
            nestedTypeFirst.setDataType(
                    ApiParamDataType.from(modelType.getDefineMode(), modelType.getDataType()).getDataType());
            nestedTypeFirsts.add(nestedTypeFirst);

            if (!modelType.isHasSubField()) {
                continue;
            }

            ApiModelDefinition definitionSecond;
            Map<String, ApiModelType> nameTypeMapSecond;
            if ((definitionSecond = definitions.get(modelType.getDataType())) == null ||
                    MiscUtil.isEmpty(nameTypeMapSecond = definitionSecond.getNameTypeMap())) {
                log.error("{}{}.{} has subfield,but no content found", TAG, dataType, entry.getKey());
                continue;
            }

            // build NestedTypeSecond
            List<NestedTypeSecond> nestedTypeSeconds = new ArrayList<>();
            for (Map.Entry<String, ApiModelType> entrySecond : nameTypeMapSecond.entrySet()) {
                ApiModelType modelTypeSecond = entrySecond.getValue();
                if (modelTypeSecond == null || !TypeUtil.isValid(modelTypeSecond)) {
                    log.error("{}{}.{} type invalid {}", TAG, nestedTypeFirst.getDataType(), entrySecond.getKey(),
                            modelTypeSecond);
                    continue;
                }

                NestedTypeSecond nestedTypeSecond = new NestedTypeSecond();
                nestedTypeSecond.setName(entrySecond.getKey());
                nestedTypeSecond.setDataType(
                        ApiParamDataType.from(modelTypeSecond.getDefineMode(), modelTypeSecond.getDataType()).getDataType());
                nestedTypeSeconds.add(nestedTypeSecond);

                if (!modelTypeSecond.isHasSubField()) {
                    continue;
                }

                ApiModelDefinition definitionThird;
                Map<String, ApiModelType> nameTypeMapThird;
                if ((definitionThird = definitions.get(modelType.getDataType())) == null ||
                        MiscUtil.isEmpty(nameTypeMapThird = definitionThird.getNameTypeMap())) {
                    log.error("{}{}.{} has subfield,but no content found", TAG, nestedTypeSecond.getDataType(),
                            entrySecond.getKey());
                    continue;
                }

                // build NestedTypeThird
                List<NestedTypeThird> nestedTypeThirds = new ArrayList<>();
                for (Map.Entry<String, ApiModelType> entryThird : nameTypeMapThird.entrySet()) {
                    ApiModelType modelTypeThird = entryThird.getValue();
                    if (modelTypeThird == null || !TypeUtil.isValid(modelTypeThird)) {
                        log.error("{}{}.{} type invalid {}", TAG, nestedTypeSecond.getDataType(), entryThird.getKey(),
                                modelTypeThird);
                        continue;
                    }

                    NestedTypeThird nestedTypeThird = new NestedTypeThird();
                    nestedTypeThird.setName(entryThird.getKey());
                    nestedTypeThird.setDataType(
                            ApiParamDataType.from(modelTypeThird.getDefineMode(), modelTypeThird.getDataType()).getDataType());
                    nestedTypeThirds.add(nestedTypeThird);
                }

                nestedTypeSecond.setObject(nestedTypeThirds);
            }
            nestedTypeFirst.setObject(nestedTypeSeconds);
        }

        return nestedTypeFirsts;
    }

    /**
     * 自动创建的API,执行基础内容比较
     *
     * @param savedApi 已入库的API
     * @param newApi   新扫描上来的API
     * @return API内容是否相同
     */
    public static boolean equalsByBasic(PublishApi savedApi, PublishApiDto newApi) {
        return buildApiHashByContent(savedApi).equals(buildApiHashByContent(newApi));
    }

    public static String buildApiHashByContent(PublishApi api) {
        StringBuilder sb = new StringBuilder();

        sb.append(api.getName())
                .append(api.getDescription())
                .append(api.getPartition())
                .append(api.isNeedSubscribe())
                .append(api.isNeedAuth())
                .append(api.isNeedLogging())
                .append(api.isNeedRecordRet())
                .append(MiscUtil.isNotEmpty(api.getIpWhiteList()) ? api.getIpWhiteList() : "")
                .append(api.getUrl())
                .append(api.getTimeout())
                .append(api.getHostHeader())
                .append(api.getAccessProtocol())
                .append(api.getHost());

        if (log.isDebugEnabled()) {
            log.debug("{} savedApi1 {}", TAG, sb.toString());
        }

        if (MiscUtil.isNotEmpty(api.getApiMappingRules())) {
            sb.append(MappingRuleUtil.buildRuleHashWithContent(api.getApiMappingRules()));
        }

        if (log.isDebugEnabled()) {
            log.debug("{} savedApi2 {}", TAG, sb.toString());
        }

        return MiscUtil.MD5(sb.toString());
    }

    public static String buildApiHashByContent(PublishApiDto api) {
        StringBuilder sb = new StringBuilder();

        sb.append(api.getName())
                .append(api.getDescription())
                .append(api.getPartition())
                .append(api.isNeedSubscribe())
                .append(api.isNeedAuth())
                .append(api.isNeedLogging())
                .append(api.isNeedRecordRet())
                .append(MiscUtil.isNotEmpty(api.getIpWhiteList()) ? api.getIpWhiteList() : "")
                .append(api.getUrl())
                .append(api.getTimeout())
                .append(api.getHostHeader())
                .append(api.getAccessProtocol())
                .append(api.getHost());

        if (log.isDebugEnabled()) {
            log.debug("{} newApi1 {}", TAG, sb.toString());
        }

        if (MiscUtil.isNotEmpty(api.getApiMappingRuleDtos())) {
            sb.append(MappingRuleUtil.buildDtoHashWithContent(api.getApiMappingRuleDtos()));
        }

        if (log.isDebugEnabled()) {
            log.debug("{} newApi2 {}", TAG, sb.toString());
        }
        return MiscUtil.MD5(sb.toString());
    }


    public static List<EurekaService> buildNacosServices(NacosService nacosServer, PublishApiGroup publishApiGroup) {
        List<EurekaService> eurekaServices = new ArrayList<>();

        log.info("{}Start to build NacosService for {}", TAG, nacosServer.getDom());

        NacosInstance nacosInstance;
        if (MiscUtil.isEmpty(nacosServer.getHosts()) || MiscUtil.isEmpty((nacosInstance =
                nacosServer.getHosts().get(0)).getMetadata())) {
            return eurekaServices;
        }

        EurekaService service;
        if ((service = buildEurekaService(nacosInstance.getMetadata(), publishApiGroup)) == null) {
            log.error("{}Fail to build api from metadata", TAG);
            return eurekaServices;
        }

        if (!service.isValid()) {
            log.error("{}Invalid content for eureka %{}", TAG, service.toString());
            return eurekaServices;
        }

        eurekaServices.add(service);
        return eurekaServices;
    }
}
