/*
 * @author guilai.ming
 * @date 2020/7/15
 */
package com.hisense.gateway.management.service.impl;

import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.PublishApiBatch;
import com.hisense.gateway.library.model.dto.web.PublishApiDto;
import com.hisense.gateway.library.model.pojo.base.DataItem;
import com.hisense.gateway.library.model.pojo.base.EurekaService;
import com.hisense.gateway.library.model.pojo.base.PublishApi;
import com.hisense.gateway.library.model.pojo.base.PublishApiGroup;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.library.service.MappingRuleService;
import com.hisense.gateway.library.service.PublishApiService;
import com.hisense.gateway.management.service.AutoCreateService;
import com.hisense.gateway.library.utils.api.AutoCreateUtil;
import com.hisense.gateway.library.utils.api.MappingRuleUtil;
import com.hisense.gateway.library.utils.api.PublishApiUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.hisense.gateway.library.model.ModelConstant.*;
import static com.hisense.gateway.library.constant.BaseConstants.TAG;
import static com.hisense.gateway.library.constant.ApiConstant.*;

@Slf4j
@Service
public class AutoCreateServiceImpl implements AutoCreateService {
    @Autowired
    PublishApiService publishApiService;

    @Autowired
    DataItemRepository dataItemRepository;

    @Autowired
    PublishApiRepository publishApiRepository;
    @Autowired
    PublishApiGroupRepository publishApiGroupRepository;

    @Autowired
    EurekaServiceRepository eurekaServiceRepository;

    @Autowired
    MappingRuleService mappingRuleService;

    @Transactional
    @Override
    public Result<Integer> createApis(Integer groupId, EurekaService service, String user) {
        long start = System.currentTimeMillis();
        Result<Integer> result = new Result<>(Result.FAIL, "Fail", 0);
        if (!service.isValid()) {
            result.setMsg(String.format("Invalid content for eureka %s", service.toString()));
            return result;
        }

        if (log.isTraceEnabled()) {
            log.trace("{}service={}", TAG, service);
        }

        try {
            Result<EurekaService> result1 = saveEurekaService(service);
            if (result1.isFailure()) {
                log.info("{}{}", TAG, result1.getMsg());
                result.setMsg(result1.getMsg());
                return result;
            }

            EurekaService serviceRes = result1.getData();
            service.setId(serviceRes.getId());

            if (MiscUtil.isNotEmpty(service.getPublishApiDtos())) {
                updatePublishApiDtos(service.getPublishApiDtos(), serviceRes, groupId);
                ApiSetPart apiSetPart = initApiSetPart(service, service.getPublishApiDtos());

                // 新建
                Set<PublishApiDto> newApis;
                if (MiscUtil.isNotEmpty(newApis = apiSetPart.getSetOpsNewList())) {
                    log.info("{}Try to create {} api", TAG, newApis.size());
                    if (publishApiService.createPublishApis(newApis, user).isFailure()) {
                        log.info("{}Fail to save Apis {}", TAG, newApis.size());
                    }
                }

                // 删除
                Set<PublishApi> deleteApis;
                if (MiscUtil.isNotEmpty(deleteApis = apiSetPart.getSetOpsDeleteList())) {
                    log.info("{}Try to delete {} api", TAG, deleteApis.size());
                    PublishApiBatch batch = new PublishApiBatch();
                    batch.setIds(deleteApis.stream().map(PublishApi::getId).collect(Collectors.toList()));
                    if (publishApiService.deletePublishApis(batch).isFailure()) {
                        log.info("{}Fail to delete Apis {}", TAG, deleteApis.size());
                    }
                }

                // 一对一更新
                Map<PublishApi, Set<PublishApiDto>> updateApis;
                if (MiscUtil.isNotEmpty(updateApis = apiSetPart.getSetOpsUpdateList())) {
                    log.info("{}Try to update {} api", TAG, updateApis.size());
                    for (Map.Entry<PublishApi, Set<PublishApiDto>> entry : updateApis.entrySet()) {
                        PublishApi savedApi = entry.getKey();
                        PublishApiDto newApi = new ArrayList<>(entry.getValue()).get(0);

                        if (AutoCreateUtil.equalsByBasic(savedApi, newApi)) {// API内容相同
                            if (newApi.getGroupId() != null) {
                                // 新建API指定的分组不同于已入库API时
                                if (savedApi.getGroup() == null ||
                                        (savedApi.getGroup() != null && !newApi.getGroupId().equals(savedApi.getGroup().getId()))) {
                                    //可以修改分组，但是不能变更状态（主要是有已发布的）
                                    publishApiRepository.updateGroupId(savedApi.getId(), groupId,savedApi.getStatus());
                                    log.info("{}Set saved Api {} with new group {}", TAG, newApi.getSimpleName(),
                                            newApi.getGroupId());
                                } else {
                                    // 主动同步
                                    log.info("{}Skip to update Api {} with same content by pull sync", TAG,
                                            newApi.getSimpleName());
                                }
                            } else {
                                // 被动同步
                                log.info("{}Skip to update Api {} with same content by push sync", TAG,
                                        newApi.getSimpleName());
                            }
                        } else if (publishApiService.updatePublishApi(null, savedApi, null, newApi, InstanceEnvironment.ENVIRONMENT_STAGING.getName()).isFailure()) {
                            log.info("{}Fail to update Api {}", TAG, newApi.getSimpleName());
                        }
                    }
                }

                // TODO 邮件通知
            }

            result.setCode(Result.OK);
            result.setMsg(String.format("Success to save EurekaService cost (%s)ms",
                    (System.currentTimeMillis() - start)));
            return result;
        } catch (Exception e) {
            log.error("createApis exception:",e);
            return result;
        }
    }

    @Override
    public Result<Boolean> logicDeleteApis(String systemName) {
        Result<Boolean> result = new Result<>(Result.FAIL, "Fail", false);
        log.info("{}Start logic delete EurekaService", TAG);

        EurekaService eurekaService;
        if ((eurekaService = eurekaServiceRepository.findBySystemName(systemName)) == null) {
            log.info("{}Service named with {} not exist", TAG, systemName);
            result.setMsg(String.format("Service named with %s not exist", systemName));
            return result;
        }

        eurekaService.setStatus(0);
        eurekaServiceRepository.saveAndFlush(eurekaService);

        List<PublishApi> offlineApis = publishApiRepository.findByServiceIdAndOffline(eurekaService.getId());
        if (MiscUtil.isNotEmpty(offlineApis)) {
            for (PublishApi publishApi : offlineApis) {
                publishApi.setStatus(API_DELETE);
                publishApiService.deletePublishApi(publishApi.getId());
            }
        }

        return result;
    }

    /**
     * 存储EurekaService
     *
     * @param service EurekaService
     * @return Result<EurekaService>
     */
    private Result<EurekaService> saveEurekaService(EurekaService service) {
        Result<EurekaService> result = new Result<>(Result.FAIL, "", null);

        log.info("{}Start to save eureka {}", TAG, service.getSystemName());

        // 同名服务已存在时,仅更新其status字段
        EurekaService resultService;
        if ((resultService = eurekaServiceRepository.findBySystemName(service.getSystemName())) != null) {
            if (resultService.getStatus() != null && !resultService.getStatus().equals(service.getStatus())) {
                resultService.setStatus(service.getStatus());
                eurekaServiceRepository.saveAndFlush(resultService);
                log.info("{}Update eureka service with new status {}", TAG, resultService.getStatus());
            }
        } else {
            log.info("{}Add new eureka service", TAG);
            // 根据eureka systemname查找对应的DataItem,存储其id,作为service的systemId
            if (MiscUtil.isNotEmpty(service.getSystemName())) {
                DataItem item = dataItemRepository.findSystemByItemKey(service.getSystemName());
                if (item != null) {
                    service.setSystemId(item.getId());
                }
            }

            resultService = eurekaServiceRepository.save(service);
            if (resultService.getId() == null) {
                result.setMsg("Fail to save EurekaService");
                return result;
            }
            log.info("{}Add eureka service success", TAG);
        }

        log.info("{}EurekaService save done {} {}", TAG, resultService.getId(), resultService.getSystemName());

        result.setCode(Result.OK);
        result.setMsg("Success");
        result.setData(resultService);
        return result;
    }

    /**
     * 更新API中service\system相关的信息
     *
     * @param publishApiDtos API列表
     * @param service        Eureka服务
     * @param groupId        主动拉取时指定的分组
     */
    private void updatePublishApiDtos(List<PublishApiDto> publishApiDtos, EurekaService service, Integer groupId) {
        PublishApiGroup group = publishApiGroupRepository.findOne(groupId);
        if (MiscUtil.isNotEmpty(publishApiDtos)) {
            for (PublishApiDto publishApiDto : publishApiDtos) {
                publishApiDto.setSystemId(service.getSystemId());
                publishApiDto.setServiceId(service.getId());
                publishApiDto.setPartition(0);
                publishApiDto.setGroupId(groupId);
                //所属系统
                publishApiDto.setSystemId(group.getSystem());
                //API密级
                publishApiDto.setSecretLevel("0");
                publishApiDto.setTimeout(120);
            }
        }
    }

    /**
     * 查询Eureka service 主被动同步过程中创建的API
     *
     * @return MappingRule为key的API列表
     */
    private Map<String, PublishApi> findAutoCreatedApis(EurekaService service) {
        Map<String, PublishApi> map = new HashMap<>();
        if (service.getId() == null) {
            log.error("{}Invalid id for service {}", TAG, service.getSystemName());
            return map;
        }

        List<PublishApi> savedApis = publishApiRepository.findByServiceId(service.getId());
        log.info("{} findAutoCreatedApis {}", TAG, savedApis.size());
        for (PublishApi api : savedApis) {
            api.setApiMappingRules(mappingRuleService.findRuleByApiId(api.getId()));
            if (log.isDebugEnabled()) {
                log.debug("{}{} {}", TAG, api.getMappingRuleHash(), api.getSimpleName());
            }
            map.put(api.getMappingRuleHash(), api);
        }
        return map;
    }

    /**
     * 查询内网环境中用户手动创建的API
     *
     * @return MappingRule为key的API列表, 若API拥有多个MappingRule, 则拆分成多个节点
     */
    private Map<String, PublishApi> findManualCreatedApisWithInnerPartition() {
        List<PublishApi> savedApis = publishApiRepository.findManualApiByPartition(0);
        Map<String, PublishApi> map = new HashMap<>();
        for (PublishApi api : savedApis) {
            api.setApiMappingRules(mappingRuleService.findRuleByApiId(api.getId()));
            String[] hashArray = MiscUtil.splitItems(api.getMappingRuleHash(), ",");

            log.info("{}api.getMappingRuleHash() = {}", TAG, api.getMappingRuleHash());
            log.info("{}hashArray = {}", TAG, hashArray);

            if (MiscUtil.isNotEmpty(hashArray)) {
                for (String hash : hashArray) {
                    if (log.isDebugEnabled()) {
                        log.debug("{}{} {}", TAG, hash, api.getSimpleName());
                    }
                    map.put(hash, api);
                }
            }
        }
        return map;
    }

    private ApiSetPart initApiSetPart(EurekaService eurekaService, List<PublishApiDto> newApis) {
        Map<String, PublishApi> savedApis = new HashMap<>();
        savedApis.putAll(findAutoCreatedApis(eurekaService));
        savedApis.putAll(findManualCreatedApisWithInnerPartition());

        return initApiSetPart(savedApis, PublishApiUtil.buildDtoMap(newApis));
    }

    /**
     * 查询同service下已入库的API列表,基于唯一性hash,查询是否有相同API及对应交集状态
     *
     * @param savedApis 已入库API映射表,注意: 对于手动创建的API可能存在重复
     * @param newApis   新扫描的API, 不重复
     */
    private ApiSetPart initApiSetPart(@NonNull Map<String, PublishApi> savedApis,
                                      @NonNull Map<String, PublishApiDto> newApis) {
        ApiSetPart setPart = new ApiSetPart();

        log.info("{}Init ApiSetPart saved Apis {}", TAG, savedApis.size());
        log.info("{}Init ApiSetPart new   Apis {}", TAG, newApis.size());

        log.info("{}Start to compute set A", TAG);
        for (Map.Entry<String, PublishApiDto> entryNewApi : newApis.entrySet()) {
            String hash = entryNewApi.getKey();
            PublishApiDto newApi = entryNewApi.getValue();
            boolean inSet = savedApis.containsKey(hash);

            ApiSetPartStatus partStatus;
            if (inSet) {// B
                PublishApi savedApi = savedApis.get(hash);
                partStatus = ApiSetPartStatus.from(inSet, true, savedApi.getIsOnline() == 1, savedApi.getStatus());
                addPartStatus(hash, setPart, partStatus, newApi, savedApi);
            } else {// A
                partStatus = ApiSetPartStatus.from(false, false, false, -1);
                addPartStatus(hash, setPart, partStatus, newApi, null);
            }
        }

        log.info("{}Start to compute set B", TAG);
        for (Map.Entry<String, PublishApi> entrySavedApi : savedApis.entrySet()) {
            String hash = entrySavedApi.getKey();
            PublishApi savedApi = entrySavedApi.getValue();
            boolean inSet = newApis.containsKey(hash);

            ApiSetPartStatus partStatus;
            if (!inSet) { // C
                PublishApiDto newApi = newApis.get(hash);
                partStatus = ApiSetPartStatus.from(false, true, savedApi.getIsOnline() == 1, savedApi.getStatus());
                addPartStatus(hash, setPart, partStatus, newApi, savedApi);
            }
        }

        // 移除手动创建API上的mappingRule,若只有单个mappingRule,则保留在更新列表上
        log.info("{}Start to merge or move saved Api from manual list", TAG);
        Map<PublishApi, Set<PublishApiDto>> updateApis;
        if (MiscUtil.isEmpty(updateApis = setPart.getSetOpsUpdateList())) {
            for (Map.Entry<PublishApi, Set<PublishApiDto>> entry : updateApis.entrySet()) {
                PublishApi savedApi = entry.getKey();
                if (savedApi.getApiMappingRules().size() > 1) {
                    for (PublishApiDto newApi : entry.getValue()) {
                        MappingRuleUtil.removeDupRules(savedApi.getApiMappingRules(), newApi.getApiMappingRuleDtos());
                    }

                    // rule已全部移除,则添加到删除列表上
                    if (savedApi.getApiMappingRules().size() == 0) {
                        setPart.getSetOpsDeleteList().add(savedApi);
                        log.info("{}Add saved {} to delete list", TAG, savedApi.getSimpleName());
                    } else { // 否则更新已入库API的mappingRule,新扫描上的API添加到创建列表中
                        log.info("{}Update rule for saved {},then add new api to create list", TAG,
                                savedApi.getSimpleName());
                        publishApiService.updateMappingRules(savedApi);
                        mappingRuleService.updateRules(savedApi.getApiMappingRules(), savedApi.getId());

                        setPart.getSetOpsNewList().addAll(entry.getValue());
                    }

                    // 已入库api从更新列表移除
                    setPart.getSetOpsUpdateList().remove(savedApi);
                    log.info("{}Remove saved {} from update list", TAG, savedApi.getSimpleName());
                }
            }
        }

        log.info("{}Init ApiSetPart done with {}", TAG, setPart);

        return setPart;
    }

    private void addPartStatus(String hash, ApiSetPart setPart, ApiSetPartStatus partStatus, PublishApiDto newApi,
                               PublishApi savedApi) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("{}[{}] {} newApi={},savedApi={}", TAG,
                        partStatus.getApiOps(),
                        hash,
                        newApi != null ? newApi.getSimpleName() : null,
                        savedApi != null ? savedApi.getSimpleName() : null);
            }

            switch (partStatus.getApiOps()) {
                case API_OPS_NEW:
                    setPart.getSetOpsNewList().add(newApi);
                    break;

                case API_OPS_DELETE:
                    setPart.getSetOpsDeleteList().add(savedApi);
                    break;

                case API_OPS_MAIL_NOTIFY:
                    if (newApi != null && savedApi != null) {
                        Set<PublishApiDto> newApis;
                        if ((newApis = setPart.getSetOpsNotifyList().get(savedApi)) == null) {
                            newApis = new HashSet<>();
                        }
                        newApis.add(newApi);
                        setPart.getSetOpsNotifyList().put(savedApi, newApis);
                    }
                    break;

                case API_OPS_UPDATE_FIELDS:
                    if (newApi != null && savedApi != null) {
                        Set<PublishApiDto> newApis;

                        if ((newApis = setPart.getSetOpsUpdateList().get(savedApi)) == null) {
                            newApis = new HashSet<>();
                        }
                        newApis.add(newApi);
                        setPart.getSetOpsUpdateList().put(savedApi, newApis);
                    }
                    break;
            }
        } catch (Exception e) {
            log.error("context",e);
        }
    }
}
