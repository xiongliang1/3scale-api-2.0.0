/*
 * @author guilai.ming
 * @date 2020/7/15
 */
package com.hisense.gateway.library.utils.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.constant.ApiConstant;
import com.hisense.gateway.library.constant.InstanceSecretLevel;
import com.hisense.gateway.library.model.base.PolicyHeader;
import com.hisense.gateway.library.model.dto.web.ApiMappingRuleDto;
import com.hisense.gateway.library.model.dto.web.PublishApiDto;
import com.hisense.gateway.library.model.dto.web.PublishApiGroupDto;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.utils.CommonUtil;
import com.hisense.gateway.library.utils.RequestUtils;
import com.hisense.gateway.library.beans.CommonBeanUtil;
import com.hisense.gateway.library.utils.meta.GlobalSettings;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.hisense.gateway.library.constant.ApiConstant.API_SRC_USER_CREATE;
import static com.hisense.gateway.library.model.ModelConstant.API_INIT;

@Slf4j
public class PublishApiUtil {
    /**
     * DTO转Pojo,仅用于创建(自动创建、手动创建)
     *
     * @param publishApiDto dto
     * @return PublishApi
     */
    public static PublishApi buildApiWithDto(@NonNull PublishApiDto publishApiDto,Integer environment) {
        PublishApi publishApi = new PublishApi();

        updateApiWithDto(publishApi, publishApiDto, true,environment);

        return publishApi;
    }

    /**
     * DTO转POJO, 可用于创建和更新
     *
     * @param publishApi    PublishApi
     * @param publishApiDto PublishApiDto
     * @param create        boolean
     */
    public static void updateApiWithDto(PublishApi publishApi, PublishApiDto publishApiDto, boolean create,Integer environment) {
        publishApi.setName(publishApiDto.getName());
        publishApi.setDescription(publishApiDto.getDescription());
        publishApi.setAccessProtocol(publishApiDto.getAccessProtocol());
        publishApi.setHost(publishApiDto.getHost());
        publishApi.setPort(publishApiDto.getPort());
        publishApi.setUrl(publishApiDto.getUrl());
        publishApi.setEnvironment(environment);// guilai 2020/09/16
        publishApi.setUpdateTime(publishApiDto.getUpdateTime());
        publishApi.setTargetType(publishApiDto.getTargetType());//可能修改调用方式
        if(StringUtils.isNotBlank(publishApiDto.getSecretLevel()))publishApi.setSecretLevel(InstanceSecretLevel.fromName(publishApiDto.getSecretLevel()).getCode() );//liyouzhi 2020/10/21
        if(StringUtils.isNotBlank(publishApiDto.getSecretToken()))publishApi.setSecretToken(publishApiDto.getSecretToken());//liyouzhi 2020/10/21
        // for update and create by end user
        if (publishApiDto.getGroupId() != null) {
            PublishApiGroup publishApiGroup = new PublishApiGroup();
            publishApiGroup.setId(publishApiDto.getGroupId());
            publishApi.setGroup(publishApiGroup);
        }
        publishApi.setAccessProType(publishApiDto.getAccessProType());
        publishApi.setPartition(publishApiDto.getPartition());
        publishApi.setNeedSubscribe(publishApiDto.isNeedSubscribe());
        publishApi.setNeedAuth(publishApiDto.isNeedAuth());
        publishApi.setNeedLogging(publishApiDto.isNeedLogging());
        publishApi.setNeedRecordRet(publishApiDto.isNeedRecordRet());
        publishApi.setIpWhiteList(CommonUtil.encodeStrListWithComma(publishApiDto.getIpWhiteList()));
        publishApi.setIpBlackList(CommonUtil.encodeStrListWithComma(publishApiDto.getIpBlackList()));
        publishApi.setTimeout(publishApiDto.getTimeout());
        publishApi.setHostHeader(publishApiDto.getHostHeader());
        publishApi.setMappingRuleHash(publishApiDto.getMappingRuleHash());
        publishApi.setMethod(publishApiDto.getMethod());
        publishApi.setRealmName(publishApiDto.getRealmName());
        publishApi.setRequestHeader(JSONObject.toJSONString(publishApiDto.getRequestHeader()));
        publishApi.setResponseHeader(JSONObject.toJSONString(publishApiDto.getResponseHeader()));
        publishApi.setAuthType(publishApiDto.getAuthType());
        if (MiscUtil.isNotEmpty(publishApiDto.getApiMappingRuleDtos())) {
            publishApi.setApiMappingRules(MappingRuleUtil.buildRuleWithDtos(publishApiDto.getApiMappingRuleDtos(),
                    true));
        }

        if (MiscUtil.isNotEmpty(publishApiDto.getFileDocIds())) {
            publishApi.setFileDocIds(buildFileDocIdStr(publishApiDto.getFileDocIds()));
        }

        publishApi.setProxy(publishApiDto.getProxy());

        if (publishApiDto.getSystemId() != null) {
            publishApi.setSystemId(publishApiDto.getSystemId());
        }

        if (create) {
            publishApi.setSourceType(publishApiDto.getSourceType());
            publishApi.setStatus(publishApiDto.getStatus());
            publishApi.setIsOnline(publishApiDto.getIsOnline());
            publishApi.setCreateTime(publishApiDto.getCreateTime());

            publishApi.setCreator(publishApiDto.getCreator());

            if (publishApiDto.getServiceId() != null) {
                EurekaService eurekaService = new EurekaService();
                eurekaService.setId(publishApiDto.getServiceId());
                publishApi.setEurekaService(eurekaService);
            }
        }
    }

    /**
     * POJO转DTO, 用于分页查询,详情查询
     *
     * @param publishApi   PublishApi
     * @param dtoBuildType ApiConstant.ApiDtoBuildType
     * @return PublishApiDto
     */
    public static PublishApiDto buildDtoWithApi(PublishApi publishApi, ApiConstant.ApiDtoBuildType dtoBuildType) {
        if (publishApi == null) {
            return null;
        }

        PublishApiDto publishApiDto = new PublishApiDto();

        publishApiDto.setSourceType(publishApi.getSourceType());
        publishApiDto.setId(publishApi.getId());
        publishApiDto.setSystemId(publishApi.getSystemId());
        publishApiDto.setName(publishApi.getName());
        publishApiDto.setDescription(publishApi.getDescription());
        publishApiDto.setAuthType(publishApi.getAuthType());
        publishApiDto.setRealmName(publishApi.getRealmName());
        publishApiDto.setUrl(publishApi.getUrl());
        publishApiDto.setAccessProtocol(publishApi.getAccessProtocol());
        publishApiDto.setTargetType(publishApi.getTargetType());
        publishApiDto.setHost(publishApi.getHost());
        publishApiDto.setPort(publishApi.getPort());
        publishApiDto.setStatus(publishApi.getStatus());
        publishApiDto.setCreator(publishApi.getCreator());
        publishApiDto.setCreateTime(publishApi.getCreateTime());
        publishApiDto.setUpdateTime(publishApi.getUpdateTime());
        publishApiDto.setIsOnline(publishApi.getIsOnline());
        publishApiDto.setGroupId(publishApi.getGroup() != null ? publishApi.getGroup().getId() : 0);
        publishApiDto.setSecretLevel(InstanceSecretLevel.fromCode(publishApi.getSecretLevel()).getName());//liyouzhi 2020/10/21
        publishApiDto.setSecretToken(publishApi.getSecretToken());
        if (publishApi.getGroup() != null) {
            publishApiDto.setPublishApiGroupDto(new PublishApiGroupDto(publishApi.getGroup()));
        }
        publishApiDto.setAccessProType(publishApi.getAccessProType());
        publishApiDto.setPartition(publishApi.getPartition());
        publishApiDto.setNeedSubscribe(publishApi.isNeedSubscribe());
        publishApiDto.setNeedAuth(publishApi.isNeedAuth());
        publishApiDto.setNeedLogging(publishApi.isNeedLogging());
        publishApiDto.setNeedRecordRet(publishApi.isNeedRecordRet());
        publishApiDto.setIpWhiteList(CommonUtil.decodeStrListWithComma(publishApi.getIpWhiteList()));
        publishApiDto.setIpBlackList(CommonUtil.decodeStrListWithComma(publishApi.getIpBlackList()));
        publishApiDto.setTimeout(publishApi.getTimeout());
        publishApiDto.setHostHeader(publishApi.getHostHeader());
        publishApiDto.setRequestHeader(JSONArray.parseArray(publishApi.getRequestHeader(), PolicyHeader.class));
        publishApiDto.setResponseHeader(JSONArray.parseArray(publishApi.getResponseHeader(), PolicyHeader.class));
        switch (dtoBuildType) {
            case API_DTO_BUILD_QUERY_LIST:
                break;

            case API_DTO_BUILD_QUERY_DETAIL:
                if (MiscUtil.isNotEmpty(publishApi.getApiMappingRules())) {
                    publishApiDto.setApiMappingRuleDtos(MappingRuleUtil.buildDtoWithRules(publishApi.getApiMappingRules()));
                    MappingRuleUtil.decodeMappingRule(publishApiDto.getApiMappingRuleDtos(), publishApiDto.getUrl());
                }

                if (MiscUtil.isNotEmpty(publishApi.getFileDocIds())) {
                    publishApiDto.setFileDocIds(
                            MiscUtil.fromJson(publishApi.getFileDocIds(), PublishApiDto.class).getFileDocIds());
                }
                break;
        }

        return publishApiDto;
    }

    public static String buildFileDocIdStr(List<Integer> fileIds) {
        return String.format("{\"fileDocIds\":%s}", MiscUtil.toJson(fileIds));
    }

    /**
     * 构建API的Hash值(partion,url前缀,mappingRule)
     *
     * @param publishApiDto 已初始化完毕的API
     * @return hash值
     */
    public static String buildApiHash(PublishApiDto publishApiDto) {
        StringBuilder hash = new StringBuilder();

        if (MiscUtil.isNotEmpty(publishApiDto.getApiMappingRuleDtos())) {
            for (ApiMappingRuleDto ruleDto : publishApiDto.getApiMappingRuleDtos()) {
                hash.append(MiscUtil.MD5(String.format("%s,%s,%s", ruleDto.getPartition(),
                        ruleDto.getHttpMethod(), ruleDto.getPattern())))
                        .append(",");
            }
        }

        return MiscUtil.removeSuffix(hash.toString(), ",");
    }

    public static Map<String, PublishApiDto> buildDtoMap(List<PublishApiDto> dtoList) {
        Map<String, PublishApiDto> map = new HashMap<>();
        for (PublishApiDto dto : dtoList) {
            map.put(dto.getMappingRuleHash(), dto);
        }
        return map;
    }

    public static void doPreProcessForDto(PublishApiDto apiDto, boolean create) {
        if (create) {
            apiDto.setStatus(API_INIT);
            apiDto.setCreator(CommonBeanUtil.getLoginUserName());
            apiDto.setCreateTime(new Date());
            apiDto.setIsOnline(0);
            apiDto.setIsInUse(0);
            apiDto.setSourceType(API_SRC_USER_CREATE);
        }

        apiDto.setUpdateTime(new Date());

        if (apiDto.getProxy() == null) {
            apiDto.setProxy(GlobalSettings.getDefaultProxy());// TODO
        }
        //去掉host里的空格
        String host = apiDto.getHost().replaceAll(" ","");
        apiDto.setHost(host);

        apiDto.setAuthType(apiDto.isNeedAuth() ? "auth" : "noauth");
        apiDto.setTargetType(GlobalSettings.getTargetType(apiDto.getAccessProtocol()));

        if (MiscUtil.isNotEmpty(apiDto.getApiMappingRuleDtos()) && MiscUtil.isNotEmpty(apiDto.getUrl())) {
            MappingRuleUtil.encodeMappingRule(apiDto.getApiMappingRuleDtos(), apiDto.getUrl());
            apiDto.getApiMappingRuleDtos().forEach(item -> item.setPartition(apiDto.getPartition()));
        }
        apiDto.setMappingRuleHash(PublishApiUtil.buildApiHash(apiDto));
    }

    /**
     * 根据数据ip及ip段获取ip列表
     * @param ipList
     * @return
     */
    public static List<String> getAddressList(List<String> ipList){
        Set<String> addressSet = new LinkedHashSet<>();
        if(!CollectionUtils.isEmpty(ipList)){
            for(String ipStr:ipList){
                if(ipStr.contains("*") || ipStr.contains("-")){
                    ipStr = ipStr.replaceAll(" ","");
                    List<String> ips = new ArrayList<>();
                    if(ipStr.contains("-")){
                        String[] split = ipStr.split("-");
                        if(split.length==2){
                            String fromIp = split[0];
                            String endIp = split[1];
                            String[] froms = fromIp.split("\\.");
                            String[] ends = endIp.split("\\.");
                            for(int i=0;i<froms.length;i++){
                                if("*".equals(froms[i])){
                                    if(CollectionUtils.isEmpty(ips)){
                                        for(int m=0;m<256;m++){
                                            ips.add(m+"");
                                        }
                                    }else{
                                        int length = ips.size();
                                        for(int j=0;j<length;j++){
                                            String itemIp = ips.get(j);
                                            for(int n=0;n<256;n++){
                                                if(n==0){
                                                    ips.set(j,itemIp+"."+n);
                                                }else{
                                                    ips.add(itemIp+"."+n);
                                                }
                                            }
                                        }
                                    }
                                }else if(!froms[i].equals(ends[i])){
                                    int start = Integer.parseInt(froms[i]);
                                    int end = Integer.parseInt(ends[i]);
                                    if(CollectionUtils.isEmpty(ips)){
                                        for(int m=start;m<end+1;m++){
                                            ips.add(m+"");
                                        }
                                    }else{
                                        int length = ips.size();
                                        for(int j=0;j<length;j++){
                                            String itemIp = ips.get(j);
                                            for(int n=start;n<end+1;n++){
                                                if(n==start){
                                                    ips.set(j,itemIp+"."+n);
                                                }else{
                                                    ips.add(itemIp+"."+n);
                                                }
                                            }
                                        }
                                    }
                                }else{
                                    if(CollectionUtils.isEmpty(ips)){
                                        ips.add(froms[i]);
                                    }else{
                                        for(int j=0;j<ips.size();j++){
                                            ips.set(j,ips.get(j)+"."+froms[i]);
                                        }
                                    }
                                }
                            }
                        }
                    }else{
                        //考虑任意一位是*的情况
                        String[] split = ipStr.split("\\.");
                        for(String item:split){
                            if("*".equals(item)){
                                //*位置从0-255循环代替
                                if(CollectionUtils.isEmpty(ips)){
                                    for(int i=0;i<256;i++){
                                        ips.add(i+"");
                                    }
                                }else{
                                    int length = ips.size();
                                    for(int j=0;j<length;j++){
                                        String itemIp = ips.get(j);
                                        for(int i=0;i<256;i++){
                                            if(i==0){
                                                ips.set(j,itemIp+"."+i);
                                            }else{
                                                ips.add(itemIp+"."+i);
                                            }
                                        }
                                    }
                                }
                            }else{
                                if(CollectionUtils.isEmpty(ips)){
                                    ips.add(item);
                                }else{
                                    for(int i=0;i<ips.size();i++){
                                        ips.set(i,ips.get(i)+"."+item);
                                    }
                                }
                            }
                        }
                    }
                    addressSet.addAll(ips);
                }else{
                    if(StringUtils.isNotBlank(ipStr)){
                        String[] split = ipStr.split(",");
                        for(String ip:split){
                            addressSet.add(ip);
                        }
                    }
                }
            }
        }
        return new ArrayList<>(addressSet);
    }
}
