package com.hisense.gateway.management.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.beans.CommonBeanUtil;
import com.hisense.gateway.library.constant.ApiConstant;
import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.constant.InstancePartition;
import com.hisense.gateway.library.constant.OperationType;
import com.hisense.gateway.library.exception.OperationFailed;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.monitor.ApiCastLog;
import com.hisense.gateway.library.model.dto.buz.ProcessDataDto;
import com.hisense.gateway.library.model.dto.web.ProcessRecordDto;
import com.hisense.gateway.library.model.dto.web.PublishApiBatch;
import com.hisense.gateway.library.model.dto.web.SubscribedApiQuery;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.library.service.ApiInvokeRecordService;
import com.hisense.gateway.library.service.ElasticSearchService;
import com.hisense.gateway.library.service.impl.AlertPolicyServiceImpl;
import com.hisense.gateway.management.service.SubscribedApiService;
import com.hisense.gateway.library.stud.ApplicationStud;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.hisense.gateway.library.model.Result.FAIL;
import static com.hisense.gateway.library.model.Result.OK;

@Slf4j
@Service
public class SubscribedApiServiceImpl implements SubscribedApiService {

    private static final String CREATE_TIME = "createTime";
    private static final String LASTEST_PUBLISH_TIME = "lastestPublishTime";
    private static final String LASTEST_CALLED_TIME = "lastestCalledTime";

    @Resource
    PublishApiGroupRepository publishApiGroupRepository;
    @Autowired
    PublishApiRepository publishApiRepository;
    @Autowired
    PublishApplicationRepository publishApplicationRepository;
    @Autowired
    ProcessRecordRepository processRecordRepository;
    @Autowired
    DataItemRepository dataItemRepository;
    @Autowired
    OperationApiRepository operationApiRepository;
    @Autowired
    ApplicationStud applicationStud;
    @Autowired
    UserInstanceRelationshipRepository userInstanceRelationshipRepository;
    @Autowired
    ElasticSearchService elasticSearchService;
    @Autowired
    AlertPolicyServiceImpl alertPolicyService;
    @Autowired
    ApiInvokeRecordService apiInvokeRecordService;

    @Override
    public Page<ProcessRecordDto> findByPage(String tenantId, String projectId, String environment, SubscribedApiQuery apiQuery) {
        List<ProcessRecordDto> processRecordDtoList = new ArrayList<>();
        Sort.Direction direction = Sort.Direction.DESC;
        String property = CREATE_TIME;
        if (StringUtils.isNoneBlank(apiQuery.getPublishTimeSort())) {
            direction = "d".equalsIgnoreCase(apiQuery.getPublishTimeSort()) ? Sort.Direction.DESC : Sort.Direction.ASC;
            property = LASTEST_PUBLISH_TIME;
        } else if (StringUtils.isNoneBlank(apiQuery.getCallTimeSort())) {
            direction = "d".equalsIgnoreCase(apiQuery.getCallTimeSort()) ? Sort.Direction.DESC : Sort.Direction.ASC;
            property = LASTEST_CALLED_TIME;
        }
        PageRequest pageable = PageRequest.of(
                0 != apiQuery.getPage() ? apiQuery.getPage() - 1 : 0, apiQuery.getSize(), Sort.by(direction, property));
        Page<ProcessRecordDto> returnP = new PageImpl<ProcessRecordDto>(processRecordDtoList, pageable, 0);

        int environmentCode = InstanceEnvironment.fromCode(environment).getCode();

        //查询分组
        Specification<PublishApiGroup> groupSpec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            /*if (tenantId != null) {
                andList.add(builder.equal(root.get("tenantId").as(String.class), tenantId));
            }
            if (projectId != null) {
                andList.add(builder.equal(root.get("projectId").as(String.class), projectId));
            }*/
            CriteriaBuilder.In<Integer> categoryOne = builder.in(root.get("categoryOne"));
            if (apiQuery.getCategoryOnes() != null && apiQuery.getCategoryOnes().size() > 0) {
                apiQuery.getCategoryOnes().forEach(one -> categoryOne.value(one));
                andList.add(categoryOne);
            }
            CriteriaBuilder.In<Integer> categoryTwo = builder.in(root.get("categoryTwo"));
            if (apiQuery.getCategoryTwos() != null && apiQuery.getCategoryTwos().size() > 0) {
                apiQuery.getCategoryTwos().forEach(two -> categoryTwo.value(two));
                andList.add(categoryTwo);
            }
            andList.add(builder.equal(root.get("status").as(Integer.class), 1));
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApiGroup> publishApiGroups = publishApiGroupRepository.findAll(groupSpec);
        if (CollectionUtils.isEmpty(publishApiGroups)) {
            return returnP;
        }

        //查询api
        Specification<PublishApi> apiSpec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            CriteriaBuilder.In<PublishApiGroup> groupIn = builder.in(root.get("group"));
            for (PublishApiGroup pg : publishApiGroups) {
                groupIn.value(pg);
            }
            andList.add(groupIn);
            if (StringUtils.isNotBlank(apiQuery.getName())) {
                andList.add(builder.like(root.get("name").as(String.class), "%" + apiQuery.getName() + "%"));
            }
            andList.add(builder.equal(root.get("environment").as(Integer.class), InstanceEnvironment.fromCode(environment).getCode()));
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApi> apis = publishApiRepository.findAll(apiSpec);
        if (CollectionUtils.isEmpty(apis)) {
            return returnP;
        }
        //出来前端需要展示的数据
        try{
            //查询订阅application
            List<DataItem> systemByproject = dataItemRepository.findSystemByproject(Integer.parseInt(projectId));
            if(CollectionUtils.isEmpty(systemByproject)){
                return returnP;
            }
            Specification<PublishApplication> applicationSpec = (root, query, builder) -> {
                List<Predicate> andList = new LinkedList<>();
                CriteriaBuilder.In<Integer> apiIn = builder.in(root.get("publishApi").get("id"));
                if (!CollectionUtils.isEmpty(apis)) {
                    apis.forEach(api -> apiIn.value(api.getId()));
                    andList.add(apiIn);
                }
                if (environment != null) {
                    andList.add(builder.equal(root.get("publishApi").get("environment").as(Integer.class), environmentCode));
                }
                if (StringUtils.isNotBlank(apiQuery.getName())) {
                    andList.add(builder.like(root.get("publishApi").get("name").as(String.class), "%" + apiQuery.getName() + "%"));
                }
                if(!CollectionUtils.isEmpty(systemByproject)){
                    CriteriaBuilder.In<Integer> systemIDS = builder.in(root.get("system"));
                    systemByproject.forEach(item -> systemIDS.value(item.getId()));
                    andList.add(systemIDS);
                }
                andList.add(builder.equal(root.get("type").as(Integer.class), 1));
                return builder.and(andList.toArray(new Predicate[andList.size()]));
            };
            List<PublishApplication> applications = publishApplicationRepository.findAll(applicationSpec);
            if (CollectionUtils.isEmpty(applications)) {
                return returnP;
            }

            //查询订阅记录
            Specification<ProcessRecord> recordSpec = (root, query, builder) -> {
                List<Predicate> andList = new LinkedList<>();
                CriteriaBuilder.In<Integer> appId = builder.in(root.get("relId"));
                for (PublishApplication publishApplication : applications) {
                    appId.value(publishApplication.getId());
                }
                andList.add(appId);
                andList.add(builder.equal(root.get("type").as(String.class), "application"));
                andList.add(builder.equal(root.get("status").as(Integer.class), 2));//订阅成功的,liyouzhi.ex-20200914
                return builder.and(andList.toArray(new Predicate[andList.size()]));
            };
            List<ProcessRecord> records = processRecordRepository.findAll(recordSpec);
            if (CollectionUtils.isEmpty(records)) {
                return returnP;
            }
            processRecordDtoList = handleRecordsResult(records);
            //排序和分页
            processRecordDtoList = sortProcessRecords(processRecordDtoList, direction, property, pageable, records.size());
            returnP = new PageImpl<ProcessRecordDto>(processRecordDtoList, pageable, records.size());
        }catch (Exception e){
            log.error("查询异常：",e);
            return returnP;
        }
        return returnP;
    }

    /**
     * 获取我的订阅列表结果集
     *
     * @param records
     * @return
     */
    public List<ProcessRecordDto> handleRecordsResult(List<ProcessRecord> records) throws Exception {
        List<ProcessRecordDto> processRecordDtoList = new ArrayList<>();
        for (ProcessRecord processRecord : records) {
            ProcessRecordDto processRecordDto = new ProcessRecordDto(processRecord);
            PublishApplication publishApplication = publishApplicationRepository.findOne(processRecord.getRelId());
            ProcessDataDto processDataDto = JSONObject.parseObject(processRecord.getExtVar(), ProcessDataDto.class);
            if (null != processDataDto) {
                PublishApiGroup publishApiGroup = publishApiGroupRepository.findOne(processDataDto.getGroupId());
                DataItem dataItem = dataItemRepository.findOne(processDataDto.getApiSystem());
                if (null != dataItem) {
                    processRecordDto.setApiSystemName(dataItem.getItemName());
                }
                processRecordDto.setGroupName(publishApiGroup.getName());
                processRecordDto.setCategoryOneName(dataItemRepository.findOne(publishApiGroup.getCategoryOne()).getItemName());
                processRecordDto.setCategoryTwoName(dataItemRepository.findOne(publishApiGroup.getCategoryTwo()).getItemName());
            }
            String partition = InstancePartition.fromCode(publishApplication.getPublishApi().getPartition()).getDescription();
            processRecordDto.setPartition(partition);
            processRecordDto.setUserKey(publishApplication.getUserKey());
            processRecordDto.setApiId(publishApplication.getPublishApi().getId());
            processRecordDto.setState(publishApplication.getState());
            processRecordDto.setApiName(publishApplication.getPublishApi().getName());
            //获取api对应的发布记录
            List<ProcessRecord> processRecords = processRecordRepository.findProcessRecordByRelId(publishApplication.getPublishApi().getId(), 2);
            processRecordDto.setLastestPublishTime(CollectionUtils.isEmpty(processRecords)?null:processRecords.get(0).getCreateTime());
            //添加调用次数和最后调用时间
            int total = 0;
            String lastCalledTime = null;
            //调用次数考虑密匙重置的情况
            //查询更新密匙之前的数据
            List<PublishApplication> apps = publishApplicationRepository.findAppByApiIdAndSystem(publishApplication.getPublishApi().getId(),publishApplication.getSystem(),4);
            List<String> userKeys = new ArrayList<>();
            if(CollectionUtils.isEmpty(apps)){
                apps.stream().forEach(item->{userKeys.add(item.getUserKey());});
            }
            userKeys.add(publishApplication.getUserKey());
            Result<LinkedList<ApiCastLog>> logResult = elasticSearchService.queryApiLogForSubSystem(publishApplication.getPublishApi().getId(), userKeys);
            if(OK.equals(logResult.getCode())){
                LinkedList<ApiCastLog> logList = logResult.getData();
                if(!CollectionUtils.isEmpty(logList)){
                    total = logList.size();
                    lastCalledTime= logList.get(0).getStartTime();
                }
            }
            processRecordDto.setCalledCount(total);
            processRecordDto.setLastestCalledTime(StringUtils.isBlank(lastCalledTime)?null:new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastCalledTime));
            processRecordDtoList.add(processRecordDto);
        }
        return processRecordDtoList;
    }

    /**
     * 对查询结果分页和排序
     *
     * @param processRecordDtoList
     * @param direction
     * @param property
     * @param pageable
     * @param total
     * @return
     */
    public List<ProcessRecordDto> sortProcessRecords(List<ProcessRecordDto> processRecordDtoList, Sort.Direction direction, String property, PageRequest pageable, int total) {
        if (!CollectionUtils.isEmpty(processRecordDtoList)) {
            //排序
            if(Sort.Direction.ASC.equals(direction)){
                if(CREATE_TIME.equals(property)){
                    processRecordDtoList = processRecordDtoList.stream()
                            .sorted(Comparator.comparing(ProcessRecordDto::getCreateTime, Comparator.nullsLast(Date::compareTo)))
                            .collect(Collectors.toList());
                }else if(LASTEST_PUBLISH_TIME.equals(property)){
                    processRecordDtoList = processRecordDtoList.stream()
                            .sorted(Comparator.comparing(ProcessRecordDto::getLastestPublishTime, Comparator.nullsLast(Date::compareTo)))
                            .collect(Collectors.toList());
                }else if(LASTEST_CALLED_TIME.equals(property)){
                    processRecordDtoList = processRecordDtoList.stream()
                            .sorted(Comparator.comparing(ProcessRecordDto::getLastestCalledTime, Comparator.nullsLast(Date::compareTo)))
                            .collect(Collectors.toList());
                }
            }else if(Sort.Direction.DESC.equals(direction)){
                if(CREATE_TIME.equals(property)){
                    processRecordDtoList = processRecordDtoList.stream()
                            .sorted(Comparator.comparing(ProcessRecordDto::getCreateTime, Comparator.nullsLast(Date::compareTo)).reversed())
                            .collect(Collectors.toList());
                }else if(LASTEST_PUBLISH_TIME.equals(property)){
                    processRecordDtoList = processRecordDtoList.stream()
                            .sorted(Comparator.comparing(ProcessRecordDto::getLastestPublishTime, Comparator.nullsLast(Date::compareTo)).reversed())
                            .collect(Collectors.toList());
                }else if(LASTEST_CALLED_TIME.equals(property)){
                    processRecordDtoList = processRecordDtoList.stream()
                            .sorted(Comparator.comparing(ProcessRecordDto::getLastestCalledTime, Comparator.nullsLast(Date::compareTo)).reversed())
                            .collect(Collectors.toList());
                }
            }
            //分页
            int page = pageable.getPageNumber() + 1;
            int pageSize = pageable.getPageSize();
            List<ProcessRecordDto> newList = null;
            newList = processRecordDtoList.subList(pageSize * (page - 1), ((pageSize * page) > total ? total : (pageSize * page)));
            return newList;
        }
        return processRecordDtoList;
    }


    @Override
    public Result<List<String>> unSubscribeApi(PublishApiBatch publishApiBatch) {
        Result<List<String>> result = new Result<>(FAIL, "取消订阅APi失败", null);
        if (null == publishApiBatch || CollectionUtils.isEmpty(publishApiBatch.getIds())) {
            result.setError(FAIL, "请选择要取消订阅的APi！");
            return result;
        }
        List<Integer> recordIds = publishApiBatch.getIds();
        List<String> failMessage = new ArrayList<>();
        recordIds.forEach(recordId -> {
            Result<Boolean> oneResult = unSubscribeApiEach(recordId);
            if (oneResult.isFailure()) {
                String message = String.format("取消订阅失败, 编号[%d]", recordId);
                log.info(message);
                failMessage.add(message);
            }
        });

        int failCount = failMessage.size();
        int successCount = recordIds.size() - failCount;

        String endMessage;
        if (failCount == 0 && successCount > 0) {
            endMessage = String.format("成功取消订阅%d个API", successCount);
        } else if (successCount > 0) {
            endMessage = String.format("成功取消订阅%d个API, 未能取消订阅%d个API", successCount, failCount);
        } else {
            endMessage = String.format("失败,未能取消订阅%d个API", failCount);
        }
        result.setCode(successCount > 0 ? OK : FAIL);
        result.setMsg(endMessage);
        result.setData(failMessage);
        result.setAlert(1);
        return result;
    }

    /**
     * 单个取消订阅api
     *
     * @param id
     * @return
     */
    public Result<Boolean> unSubscribeApiEach(Integer id) {
        Result<Boolean> returnResult = new Result<>();
        ProcessRecord processRecord = processRecordRepository.findOne(id);
        if (null == processRecord) {
            returnResult.setCode(FAIL);
            returnResult.setMsg("ProcessRecord不存在");
            returnResult.setData(false);
            return returnResult;
        } else {
            PublishApplication publishApplication = publishApplicationRepository.findOne(processRecord.getRelId());
            if (null == publishApplication) {
                returnResult.setCode(FAIL);
                returnResult.setMsg("Application不存在");
                returnResult.setData(false);
                return returnResult;
            }
            PublishApi api = publishApiRepository.findOne(publishApplication.getPublishApi().getId());
            if (processRecord.getStatus() == 2) {
                if(api.getId() == null){
                    log.error("api not exit");
                }
                //如果审批通过
                if (api != null && api.getStatus() != 0) {
                    // 同时所属api没有被删除，此时才需要到3scale上去删除数据
                    Instance instance = publishApplication.getInstance();
                    if (instance == null) {
                        log.error("can not found instance");
                        throw new OperationFailed("instance not exist");
                    }

                    String user = publishApplication.getCreator();
                    if (user == null) {
                        log.error("can not found user");
                        throw new OperationFailed("user not exist");
                    }
                    //更新调用量
                    apiInvokeRecordService.saveInvokeCount(publishApplication);

                    UserInstanceRelationship userInstanceRelationship =
                            userInstanceRelationshipRepository.findByUserAndInstanceId(user, instance.getId());
                    applicationStud.applicationDelete(instance.getHost(), instance.getAccessToken(),
                            userInstanceRelationship.getAccountId().toString(),
                            publishApplication.getScaleApplicationId());
                }
                // 将本地app状态修改为删除,并解除与scale的关联
                publishApplication.setStatus(0);
                publishApplicationRepository.saveAndFlush(publishApplication);
            }
            //同时把该系统下其他人订阅的记录状态也修改为删除（0）
            List<Integer> applicationIds = new ArrayList<>();
            List<PublishApplication> subscribedApis
                    = publishApplicationRepository.findSubscribedApiBySystem(publishApplication.getSystem(),api.getId(), 2, ApiConstant.APPLICATION_TYPE_SUBSCRIBE_API);
            if(!CollectionUtils.isEmpty(subscribedApis)){
                for(PublishApplication app:subscribedApis){
                    applicationIds.add(app.getId());
                    app.setStatus(0);
                    publishApplicationRepository.saveAndFlush(app);
                }
                List<Integer> status = new ArrayList<>();
                status.add(2);
                List<ProcessRecord> prs = processRecordRepository.findSubscribeByRelIdAndStatus(applicationIds, status);
                if(!CollectionUtils.isEmpty(prs)){
                    for(ProcessRecord pr:prs){
                        pr.setStatus(0);
                        processRecordRepository.saveAndFlush(pr);
                    }
                }
            }
            // 设置所选APP记录为i删除状态
            processRecord.setStatus(0);
            processRecordRepository.saveAndFlush(processRecord);

            //取消订阅，会把告警策略数据全部重新推送到kafka-liyouzhi.ex 20-11-18
            alertPolicyService.syncAlertPolicyToKafkaAsync();
            returnResult.setCode(OK);
            returnResult.setMsg("取消订阅成功！");
            returnResult.setData(true);
            //取消订阅操作记录
            String msg = OK.equals(returnResult.getCode())?OperationType.fromCode(7).getName():OperationType.fromCode(8).getName()+returnResult.getMsg();
            String name = OperationType.fromCode(5).getName()+api.getName();
            OperationApi operationApi = new OperationApi(name, CommonBeanUtil.getLoginUserName(),new Date(),new Date(),5,msg,api);
            operationApiRepository.save(operationApi);
            return returnResult;
        }
    }
}
