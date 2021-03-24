package com.hisense.gateway.library.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.beans.CommonBeanUtil;
import com.hisense.gateway.library.config.SystemConfigProperties;
import com.hisense.gateway.library.constant.AnalyticsConstant;
import com.hisense.gateway.library.constant.GatewayConstants;
import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.constant.InstancePartition;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.TimeQuery;
import com.hisense.gateway.library.model.base.analytics.ApiTrafficStatQuery;
import com.hisense.gateway.library.model.base.monitor.*;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.library.service.ElasticSearchService;
import com.hisense.gateway.library.stud.AccountStud;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.hisense.gateway.library.constant.AnalyticsConstant.StatGranularity;
import static com.hisense.gateway.library.constant.AnalyticsConstant.StatGranularity.*;
import static com.hisense.gateway.library.constant.AnalyticsConstant.StatType.RESPONSE_TIME_PERCENTILE_FOR_APP;
import static com.hisense.gateway.library.constant.BaseConstants.TAG;
import static com.hisense.gateway.library.model.ModelConstant.*;
import static com.hisense.gateway.library.model.base.monitor.ApiLogQueryFull.ApiInfo;
import static com.hisense.gateway.library.model.base.monitor.ApiLogQueryFull.StatType.*;
import static com.hisense.gateway.library.model.base.monitor.ApiResponseStatistics.ResponseStat;

/**
 * @author guilai.ming 2020/09/20
 * <p>
 */
@Slf4j
@Service
@EnableConfigurationProperties(SystemConfigProperties.class)
public class ElasticSearchServiceImpl implements ElasticSearchService {
    @Autowired
    SystemConfigProperties properties;

    @Autowired
    InstanceRepository instanceRepository;

    @Autowired
    DataItemRepository dataItemRepository;

    @Autowired
    PublishApiRepository publishApiRepository;

 /*   @Autowired
    private RestHighLevelClient client;*/

    @Resource
    PublishApiGroupRepository publishApiGroupRepository;

    @Autowired
    PublishApplicationRepository publishApplicationRepository;

    @Autowired
    PublishApiInstanceRelationshipRepository publishApiInstanceRelationshipRepository;

    @Autowired
    UserInstanceRelationshipRepository userInstanceRelationshipRepository;

    @Autowired
    AccountStud accountStud;

    @Value("${api.config.elasticSearch.index}")
    private String INDEX_PREFIX;


    private static final String ES_MSG_NONE_INDEXES = "当前API在指定时间内,不存在调用记录,索引不存在";
    private static final String ES_MSG_NONE_RECORDS = "当前API在指定时间内,不存在调用记录";
    private static final String ES_MSG_INVALID_STAT_TYPE = "接口响应百分比查询, statType参数只能设定为6或者7";
    private static final String ES_MSG_API_NOT_EXIST = "API不存在 或者 API已删除";
    private static final String ES_MSG_SCALE_NOT_EXIST = "3scale实例不存在";
    private static final String ES_MSG_API_NOT_EXIST_ON_SCALE = "3scale实例上不存在对应的API";
    private static final String ES_MSG_SUBSCRIBED_APP_NOT_EXIST = "当前选定的订阅系统不存在";
    private static final String ES_MSG_TIMEQUERY_NOT_EXIST = "起始结束时间不能为空";
    private static final String ES_MSG_INVALID_RESP_RATE_VALUE = "响应百分比环境变量设定错误";

    /**
     * 构建起始日期之间的索引列表
     */
    private  List<String> buildIndexList(TimeQuery timeQuery) {
        return TimeUtil.getIntervalDays(timeQuery).stream().map(
                item -> String.format("%s%s", INDEX_PREFIX, item)).collect(Collectors.toList());
    }

    /**
     * 查询serviceId
     */
    private Result<Long> getScaleServiceId(PublishApi publishApi) {
        Result<Long> result = new Result<>(Result.OK, "", -1L);
        Instance instance = instanceRepository.searchInstanceByPartitionEnvironment(
                InstancePartition.fromCode(publishApi.getPartition()).getName(),
                InstanceEnvironment.fromCode(publishApi.getEnvironment()).getName());

        if (instance == null) {
            result.setMsg(ES_MSG_SCALE_NOT_EXIST);
            return result;
        }

        PublishApiInstanceRelationship relationship =
                publishApiInstanceRelationshipRepository.getByAPIidAndInstanceId(
                        publishApi.getId(), instance.getId());
        if (relationship == null) {
            result.setMsg(ES_MSG_API_NOT_EXIST_ON_SCALE);
            return result;
        }
        result.setData(relationship.getScaleApiId());
        return result;
    }

    /**
     * 修正参数,查询api\app\serviceId\构建索引, 用于接口统计查询
     */
    private Result<Boolean> preFixParameters(ApiTrafficStatQuery statQuery) {
        Result<Boolean> result = new Result<>(Result.FAIL, "", false);

        log.info("PreFixParameters StatQuery 1 {}", statQuery);

        String msg;
        if (!Result.OK.equals(msg = statQuery.isValid())) {
            log.error("{}{}", TAG, msg);
            result.setMsg(msg);
            return result;
        }

        switch (statQuery.getStatType()) {
            case RESPONSE_TIME_PERCENTILE_FOR_ALL:
            case RESPONSE_TIME_PERCENTILE_FOR_APP:
                break;

            default:
                log.error("{}{}", TAG, ES_MSG_INVALID_STAT_TYPE);
                result.setMsg(ES_MSG_INVALID_STAT_TYPE);
                return result;
        }

        PublishApi publishApi;
        if ((publishApi = publishApiRepository.findOne(statQuery.getApiId())) == null ||
                publishApi.getStatus().equals(API_DELETE)) {
            result.setCode(Result.OK);
            result.setMsg(ES_MSG_API_NOT_EXIST);
            return result;
        }

        Result<Long> serviceIdRes = getScaleServiceId(publishApi);
        if (serviceIdRes.getData() <= 0) {
            result.setMsg(serviceIdRes.getMsg());
            return result;
        }
        statQuery.setScaleServiceId(serviceIdRes.getData());

        if (statQuery.getStatType().equals(RESPONSE_TIME_PERCENTILE_FOR_APP)) {
            PublishApplication application = publishApplicationRepository.findOne(statQuery.getAppId());
            if (application == null) {
                result.setMsg(ES_MSG_SUBSCRIBED_APP_NOT_EXIST);
                return result;
            }

            statQuery.setAppUserKey(application.getUserKey());
        }

        Result<List<String>> indexListRes = preFixIndexList(statQuery.getTimeQuery());
        if (indexListRes.isFailure()) {
            result.setMsg(indexListRes.getMsg());
            return result;
        }
        statQuery.setIndexList(indexListRes.getData());

        log.info("PreFixParameters StatQuery 2 {}", statQuery);

        result.setCode(Result.OK);
        result.setData(true);
        return result;
    }


    /**
     * 构建索引列表
     */
    private Result<List<String>> preFixIndexList(TimeQuery timeQuery) {
        Result<List<String>> result = new Result<>(Result.OK, "", null);
        try {
            List<String> checkingIndex = buildIndexList(timeQuery);
            log.info("PreFixParameters index check  {}", checkingIndex);
            Iterator<String> iterator = checkingIndex.iterator();
            while (iterator.hasNext()) {
               /* String item = iterator.next();
                GetIndexRequest getIndexRequest = new GetIndexRequest(item);
                boolean flag = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
                if (!flag)iterator.remove();*/
            }
            log.info("PreFixParameters index queued  {}", checkingIndex);

            if (MiscUtil.isEmpty(checkingIndex)) {
                result.setCode(Result.OK);
                result.setMsg(ES_MSG_NONE_INDEXES);
                result.setAlert(1);
                return result;
            }
            result.setData(checkingIndex);
        }catch (Exception e){
            log.error("获取index异常：",e);
            result.setError(Result.FAIL,"获取index异常："+e.getMessage());
            return result;
        }
        return result;
    }

    /**
     * 修正参数,查询api\app\serviceId\构建索引, 用于定向查询
     */
    private Result<Boolean> preFixParameters(ApiLogQuerySpecial specialQuery) {
        Result<Boolean> result = new Result<>(Result.FAIL, "", false);

        log.info("PreFixParameters SpecialQuery 1 {}", specialQuery);
        if (!specialQuery.isValid()) {
            log.error("invalid query parameters");
            return result;
        }

        if (specialQuery.getApiId() != null) {
            PublishApi publishApi;
            if ((publishApi = publishApiRepository.findOne(specialQuery.getApiId())) == null ||
                    publishApi.getStatus().equals(API_DELETE)) {
                result.setCode(Result.OK);
                result.setMsg(ES_MSG_API_NOT_EXIST);
                return result;
            }

            Result<Long> serviceIdRes = getScaleServiceId(publishApi);
            if (serviceIdRes.getData() <= 0) {
                result.setMsg(serviceIdRes.getMsg());
                return result;
            }
            specialQuery.setScaleServiceId(serviceIdRes.getData());
        }

        if (specialQuery.getAppId() != null) {
            PublishApplication application = publishApplicationRepository.findOne(specialQuery.getAppId());
            if (application == null) {
                result.setMsg(ES_MSG_SUBSCRIBED_APP_NOT_EXIST);
                return result;
            }
            specialQuery.setAppUserKey(application.getUserKey());
        }

        Result<List<String>> indexListRes = preFixIndexList(specialQuery.getTimeQuery());
        if (indexListRes.isFailure()) {
            result.setMsg(indexListRes.getMsg());
            return result;
        }
        specialQuery.setIndexList(indexListRes.getData());

        log.info("PreFixParameters SpecialQuery 2 {}", specialQuery);

        result.setCode(Result.OK);
        result.setData(true);
        return result;
    }

    /**
     * 修正参数, 查询API列表,构建索引列表、用于分页查询
     */
    private Result<Boolean> preFixParameters(ApiLogQueryFull fullQuery) {
        Result<Boolean> result = new Result<>(Result.FAIL, "", false);
        log.info("PreFixParameters ApiInvokeQuery 1 {}", fullQuery);

        if (fullQuery.getTimeQuery() == null || fullQuery.getTimeQuery().isValid().equals(Result.FAIL)) {
            result.setError(ES_MSG_TIMEQUERY_NOT_EXIST);
            return result;
        }

        Specification<PublishApi> publishApiSpec;
        if (StringUtils.isNotBlank(fullQuery.getApiName())) {
            publishApiSpec = (root, query, builder) -> {
                List<Predicate> basicList = new LinkedList<>();
                basicList.add(builder.like(root.get("name").as(String.class), "%" + fullQuery.getApiName() + "%",
                        GatewayConstants.ESCAPECHAR));

                basicList.add(builder.equal(root.get("environment").as(Integer.class),
                        InstanceEnvironment.fromCode(fullQuery.getEnvironment()).getCode()));

                CriteriaBuilder.In<Integer> statusIn = builder.in(root.get("status").as(Integer.class));
                statusIn.value(API_INIT);
                statusIn.value(API_FOLLOWUP_PROMOTE);//3
                statusIn.value(API_COMPLETE);//4
                basicList.add(statusIn);

                return builder.and(basicList.toArray(new Predicate[0]));
            };

            log.info("{}findByPage add name like", TAG);
        } else {
            String tenantId = fullQuery.getTenantId();
            String projectId = fullQuery.getProjectId();
            String environment = fullQuery.getEnvironment();

            Set<Integer> systemIdSet = null;
            List<Integer> systemIdList = publishApiGroupRepository.findSystemByTenantProjectAndService(tenantId,
                    projectId);
            if (MiscUtil.isNotEmpty(systemIdList)) {
                systemIdSet = MiscUtil.list2Set(systemIdList);
            }

            Set<Integer> finalSystemIdSet = systemIdSet;
            final List<PublishApiGroup> defaultPublishApiGroups =
                    publishApiGroupRepository.findByTenantAndProject(tenantId, projectId);

            log.info("{}FinalSystemIdSet={}", TAG, finalSystemIdSet != null ? finalSystemIdSet : 0);
            log.info("{}FinalDefaultPublishApiGroups={}", TAG, defaultPublishApiGroups != null ?
                    defaultPublishApiGroups.stream().map(item -> String.format("[%d]: %s", item.getId(),
                            item.getName())).collect(Collectors.toList()) : 0);

            publishApiSpec = (root, query, builder) -> {
                Predicate orPredicate = null;
                CriteriaBuilder.In<Integer> groupIn = null;
                CriteriaBuilder.In<Integer> systemIn = null;
                List<Predicate> basicList = new LinkedList<>();

                basicList.add(builder.equal(root.get("environment").as(Integer.class),
                        InstanceEnvironment.fromCode(environment).getCode()));

                CriteriaBuilder.In<Integer> statusIn = builder.in(root.get("status").as(Integer.class));
                statusIn.value(API_INIT);
                statusIn.value(API_FOLLOWUP_PROMOTE);//3
                statusIn.value(API_COMPLETE);//4
                basicList.add(statusIn);

                //
                if (MiscUtil.isNotEmpty(defaultPublishApiGroups)) {
                    groupIn = builder.in(root.get("group").get("id"));
                    for (PublishApiGroup pg : defaultPublishApiGroups) {
                        groupIn.value(pg.getId());
                    }
                }

                //
                if (MiscUtil.isNotEmpty(finalSystemIdSet)) {
                    systemIn = builder.in(root/*.get("eurekaService")*/.get("systemId"));
                    for (Integer system : finalSystemIdSet) {
                        systemIn.value(system);
                    }
                }

                if (groupIn != null && systemIn != null) {
                    orPredicate = builder.or(groupIn, systemIn);
                } else if (groupIn != null) {
                    basicList.add(groupIn);
                } else if (systemIn != null) {
                    basicList.add(systemIn);
                }

                //
                if (orPredicate != null) {
                    return query.where(builder.and(basicList.toArray(new Predicate[0])), orPredicate).getRestriction();
                } else {
                    return builder.and(basicList.toArray(new Predicate[0]));
                }
            };
        }

        // 查询API
        List<PublishApi> publishApis = publishApiRepository.findAll(publishApiSpec);
        if (MiscUtil.isEmpty(publishApis)) {
            result.setError(ES_MSG_API_NOT_EXIST);
            return result;
        }

        Map<Integer, ApiInfo> apiInfos = new HashMap<>();
        for (PublishApi publishApi : publishApis) {
            long serviceId = getScaleServiceId(publishApi).getData();
            if (serviceId > 0) {
                apiInfos.put(publishApi.getId(), new ApiInfo(publishApi.getId(), serviceId, publishApi.getName(),
                        dataItemRepository.findOne(publishApi.getSystemId()).getItemName()));
            }
        }

        if (MiscUtil.isEmpty(apiInfos)) {
            result.setError(ES_MSG_API_NOT_EXIST_ON_SCALE);
            return result;
        }
        fullQuery.setApiInfos(apiInfos);

        Result<List<String>> indexListRes = preFixIndexList(fullQuery.getTimeQuery());
        if (indexListRes.isFailure()) {
            result.setError(indexListRes.getMsg());
            return result;
        }
        fullQuery.setIndexList(indexListRes.getData());

        result.setData(true);
        result.setCode(Result.OK);
        return result;
    }

    /**
     * 获取记录数量
     */
/*    private long getMatchCount(List<String> indexList,BoolQueryBuilder boolQueryBuilder) {
        long count = 0;
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(boolQueryBuilder);
            if(CollectionUtils.isEmpty(indexList)){
                return count;
            }
            String[] strings = new String[indexList.size()];
            indexList.toArray(strings);
            CountRequest countRequest = new CountRequest(strings,searchSourceBuilder);
            countRequest.source(searchSourceBuilder);
            CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
            count = countResponse.getCount();
        } catch (Exception e) {
            log.error("获取记录数量异常",e);
        }
        log.info("matched count {}", count);
        return count;
    }*/

    /**
     * 构建ES查询语句,用于多键ID
     *
     * @param indexList  索引列表
     * @param timeQuery  区间
     * @param serviceIds 主键ID
     * @param statType   统计类型
     */
   /* private BoolQueryBuilder buildSearchQuery(List<String> indexList, TimeQuery timeQuery, List<Long> serviceIds,
                                         StatType statType, String userKey) {
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
//        boolBuilder.must().add(QueryBuilders.matchAllQuery().boost(1.0f));
        boolBuilder.must().add(QueryBuilders.rangeQuery("timestamp")
                .from(timeQuery.getStart().getTime())
                .to(timeQuery.getEnd().getTime())
                .includeLower(true)
                .includeUpper(true)
                .boost(1.0f));

        if (statType.equals(STAT_OK)) {
            log.info("add condition responseCode 200");
            boolBuilder.must().add(QueryBuilders.matchPhraseQuery("responseCode", 200).slop(0).boost(1.0f));
        } else if (statType.equals(STAT_FAIL)) {
            log.info("add condition responseCode !200");
            boolBuilder.mustNot().add(QueryBuilders.matchPhraseQuery("responseCode", 200).slop(0).boost(1.0f));
        }
        if(MiscUtil.isNotEmpty(serviceIds)){
            BoolQueryBuilder nestedBoolBuilder = QueryBuilders.boolQuery();
            serviceIds.forEach(item -> {
                if (item != null) {
                    log.info("add condition serviceId {}",item);
                    nestedBoolBuilder.should().add(QueryBuilders.matchPhraseQuery("serviceId", item).slop(0).boost(1.0f));
                }
            });
            nestedBoolBuilder.adjustPureNegative(true).minimumShouldMatch(1).boost(1.0f);
            boolBuilder.must().add(nestedBoolBuilder);
        }
        if (MiscUtil.isNotEmpty(userKey)) {
            log.info("add condition userKey {}", userKey);
            boolBuilder.must().add(QueryBuilders.matchPhraseQuery("userKey", userKey).slop(0).boost(1.0f));
        }

        boolBuilder.adjustPureNegative(true).boost(1.0f);
        return boolBuilder;
    }*/

    /**
     * 构建ES查询语句,用于单键ID
     *
     * @param serviceId API-scale-id
     * @param indexList 索引列表
     * @param timeQuery 日期范围
     */
 /*   private SearchQuery buildSearchQuery(List<String> indexList, TimeQuery timeQuery, long serviceId) {
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        boolBuilder.must().add(QueryBuilders.rangeQuery("timestamp")
                .from(timeQuery.getStart().getTime())
                .to(timeQuery.getEnd().getTime())
                .includeLower(true)
                .includeUpper(true)
                .boost(1.0f));

        boolBuilder.must().add(QueryBuilders.matchPhraseQuery("serviceId", serviceId).slop(0).boost(1.0f));

        boolBuilder.filter(QueryBuilders.matchAllQuery().boost(1.0f));

        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        builder.withIndices(indexList.toArray(new String[0]));
        return builder.withTypes("doc").withQuery(boolBuilder).build();
    }*/

    /**
     * 全量升序查询,默认按照访问时间排序
     *
     * @param serviceIds API-scale-service
     * @param indexList  索引列表
     * @param timeQuery  查询区间
     * @param clazz      ES Mapping Entity
     */
/*    private <T> List<T> queryForAllList(List<String> indexList, TimeQuery timeQuery, List<Long> serviceIds,
                                        StatType statType, String userKey, Class<T> clazz) {
//        SearchQuery searchQuery = buildSearchQuery(indexList, timeQuery, serviceIds, statType, userKey);
        BoolQueryBuilder boolQueryBuilder = buildSearchQuery(indexList, timeQuery, serviceIds, statType, userKey);

        long count = getMatchCount(indexList,boolQueryBuilder);
        if (count <= 0) {
            log.info(ES_MSG_NONE_RECORDS);
            return null;
        }

        try {
            String[] indexStrings = new String[indexList.size()];
            indexList.toArray(indexStrings);

            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(boolQueryBuilder);
            sourceBuilder.sort(new FieldSortBuilder("startTime.keyword").order(SortOrder.ASC));
            log.info("ES查询条件："+sourceBuilder.toString()+",索引："+indexList);
            SearchRequest searchRequest = new SearchRequest(indexStrings,sourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
            return esResultHandle(searchResponse, clazz);
        } catch (IOException e) {
            log.error("context",e);
        }
        return null;
    }*/

    @Override
    public <T> Result<List<T>> queryForSpecial(ApiLogQuerySpecial specialQuery, Class<T> clazz) {
        Result<Boolean> booleanResult = preFixParameters(specialQuery);
        if (booleanResult.isFailure()) {
            return new Result<>(Result.FAIL, booleanResult.getMsg(), null);
        }

        log.info("QueryForList {}", specialQuery);

      /*  return new Result<List<T>>(Result.OK, "",
                queryForAllList(specialQuery.getIndexList(), specialQuery.getTimeQuery(),
                        Collections.singletonList(specialQuery.getScaleServiceId()),
                        StatType.from(specialQuery.getStatType()), specialQuery.getAppUserKey(), clazz));*/
      return null;
    }

    /**
     * ES查询结果处理
     * @param response
     * @return
     */
/*
    public static <T> LinkedList<T> esResultHandle(SearchResponse response, Class<T> clazz) {
        for(SearchHit hit : response.getHits()){
            Map<String, Object> source = hit.getSourceAsMap();
            //处理高亮片段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField nameField = highlightFields.get("title");
            if(nameField!=null){
                Text[] fragments = nameField.fragments();
                StringBuilder nameTmp = new StringBuilder();
                for(Text text:fragments){
                    nameTmp.append(text);
                }
                //将高亮片段组装到结果中去
                source.put("title", nameTmp.toString());
                log.info(source.toString());
            }
        }
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        LinkedList<T> blogList = new LinkedList<>();
        for (SearchHit hit : searchHits) {
            JSONObject jsonObject = new JSONObject(hit.getSourceAsMap());
            blogList.add(JSONObject.toJavaObject(jsonObject, clazz));
        }
        return blogList;

    }
*/

    /**
     * 根据订阅系统查询单个api的调用日志
     * @param apiId
     * @param userKeys
     * @return
     */
    @Override
    public Result<LinkedList<ApiCastLog>> queryApiLogForSubSystem(Integer apiId,List<String> userKeys){
        Result<LinkedList<ApiCastLog>> result = new Result<LinkedList<ApiCastLog>>(Result.OK, "查询成功！",null);
//        SortOrder sortOrder = SortOrder.DESC;
        String property = "startTime.keyword";
        try{
        /*    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            if (null != apiId) {
                boolQueryBuilder.must().add(QueryBuilders.matchPhraseQuery("apiId", apiId).slop(0).boost(1.0f));
            }
            if(!CollectionUtils.isEmpty(userKeys)){
                BoolQueryBuilder nestedBoolBuilder = QueryBuilders.boolQuery();
                for(String userKey:userKeys){
                   nestedBoolBuilder.should().add(QueryBuilders.matchPhraseQuery("userKey", userKey).slop(0).boost(1.0f));
                }
                boolQueryBuilder.must().add(nestedBoolBuilder);
            }
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(boolQueryBuilder);
            sourceBuilder.sort(new FieldSortBuilder(property).order(sortOrder));
            log.info("ES查询条件："+sourceBuilder.toString());
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.source(sourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
            log.info("ES查询结果："+searchResponse.toString());
            LinkedList<ApiCastLog> apiCastLogs = esResultHandle(searchResponse, ApiCastLog.class);
            result.setData(apiCastLogs);*/
        }catch (Exception e){
            result.setError(Result.FAIL, "ES查询异常："+e.getMessage());
            return result;
        }
        return result;
    }

    @Override
    public Result<Long> queryCount(List<String> indexList,TimeQuery timeQuery, List<PublishApplication> params,int statType,Boolean removeSelf) {
        if(CollectionUtils.isEmpty(indexList)){
            return new Result<>(Result.OK, "", 0l);
        }
        long count=0;
        if(MiscUtil.isNotEmpty(params)) {
            try {
             /*   BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
                boolBuilder.must().add(QueryBuilders.rangeQuery("timestamp")
                        .from(timeQuery.getStart().getTime())
                        .to(timeQuery.getEnd().getTime())
                        .includeLower(true)
                        .includeUpper(true)
                        .boost(1.0f));
                if (statType == 1) {
                    boolBuilder.must().add(QueryBuilders.matchPhraseQuery("responseCode", 200).slop(0).boost(1.0f));
                } else if (statType == 2) {
                    boolBuilder.mustNot().add(QueryBuilders.matchPhraseQuery("responseCode", 200).slop(0).boost(1.0f));
                }
                BoolQueryBuilder nestedBoolBuilder = QueryBuilders.boolQuery();
                params.forEach(item -> {
                    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                    boolQueryBuilder.must().add(QueryBuilders.matchPhraseQuery("apiId",item.getPublishApi().getId()).slop(0).boost(1.0f));
                    if (StringUtils.isNotEmpty(item.getUserKey())) {
                        if (removeSelf) {
                            boolQueryBuilder.mustNot().add(QueryBuilders.matchPhraseQuery("userKey",item.getUserKey()).slop(0).boost(1.0f));
                        } else {
                            boolQueryBuilder.must().add(QueryBuilders.matchPhraseQuery("userKey",item.getUserKey()).slop(0).boost(1.0f));
                        }
                    } else {
                        if (StringUtils.isNotEmpty(item.getAppId()) && StringUtils.isNotEmpty(item.getAppKey())) {
                            if (removeSelf) {
                                boolQueryBuilder.mustNot().add(QueryBuilders.matchPhraseQuery("appId", item.getAppId()).slop(0).boost(1.0f));
                                boolQueryBuilder.mustNot().add(QueryBuilders.matchPhraseQuery("appKey", item.getAppKey()).slop(0).boost(1.0f));
                            } else {
                                boolQueryBuilder.must().add(QueryBuilders.matchPhraseQuery("appId", item.getAppId()).slop(0).boost(1.0f));
                                boolQueryBuilder.must().add(QueryBuilders.matchPhraseQuery("appKey", item.getAppKey()).slop(0).boost(1.0f));
                            }
                        }
                    }
                    nestedBoolBuilder.should().add(boolQueryBuilder);
                });
                nestedBoolBuilder.adjustPureNegative(true).minimumShouldMatch(1).boost(1.0f);
                boolBuilder.must().add(nestedBoolBuilder);
                boolBuilder.adjustPureNegative(true).boost(1.0f);
                count = getMatchCount(indexList, boolBuilder);*/
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return new Result<>(Result.OK, "",count);
    }

    @Override
    public Result<List<ApiCastLog>> queryForApiAndRescode(ApiLogQuerySpecial specialQuery) {
        List<ApiCastLog> resultRecodes = new ArrayList<>();
        Result<List<ApiCastLog>> result = new Result<>(Result.OK,"查询接口响应状态数据正常",resultRecodes);
        //根据起止时间确认查询索引，如果不存在可用索引-则不作处理
        Result<List<String>> indexListRes = preFixIndexList(specialQuery.getTimeQuery());
        if(Result.FAIL.equals(indexListRes.getCode()) || CollectionUtils.isEmpty(indexListRes.getData())){
            result.setError(indexListRes.getMsg());
            return result;
        }
     /*   BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        boolBuilder.must().add(QueryBuilders.rangeQuery("timestamp")
                .from(specialQuery.getTimeQuery().getStart().getTime())
                .to(specialQuery.getTimeQuery().getEnd().getTime())
                .includeLower(true)
                .includeUpper(true)
                .boost(1.0f));
        boolBuilder.must().add(QueryBuilders.matchPhraseQuery("apiId", specialQuery.getApiId()).slop(0).boost(1.0f));*/
        //状态码
        AnalyticsConstant.StatType statType = AnalyticsConstant.StatType.fromCode(specialQuery.getErrorCode());
        String responseCodeLikeStr = null;
        if(statType.getCode() == 3){
            responseCodeLikeStr  = "2*";//2xx
        }else if(statType.getCode() == 4){
            responseCodeLikeStr  = "4*";//4xx
        }else if(statType.getCode() == 5){
            responseCodeLikeStr  = "5*";//5xx
        }
        /*if(StringUtils.isNotBlank(responseCodeLikeStr)){
            boolBuilder.must().add(QueryBuilders.wildcardQuery("responseCode", responseCodeLikeStr));
        }
        boolBuilder.adjustPureNegative(true).boost(1.0f);
        String[] indexStrings = new String[indexListRes.getData().size()];
        indexListRes.getData().toArray(indexStrings);
        //构造查询ES
        SortOrder sortOrder = SortOrder.DESC;*/
        String property = "startTime.keyword";
        try {
           /* SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(boolBuilder);
            sourceBuilder.sort(new FieldSortBuilder(property).order(sortOrder));
            log.info("ES查询条件："+sourceBuilder.toString()+",索引："+indexListRes.getData());
            SearchRequest searchRequest = new SearchRequest(indexStrings,sourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
            resultRecodes = esResultHandle(searchResponse, ApiCastLog.class);
            result.setData(resultRecodes);*/
        }catch (Exception e){
            log.error("查询异常",e);
            result.setError(e.getMessage());
            return result;
        }
        return result;
    }

    @Override
    public Result<List<String>> queryIndexList(TimeQuery timeQuery) {
        return preFixIndexList(timeQuery);
    }

    /**
     * 分页查询调用记录
     */
    @Override
    public Result<Page<ApiLogRecord>> queryForPage(ApiLogQueryFull invokeQuery) {
        Result<Boolean> booleanResult = preFixParameters(invokeQuery);
        if (Result.FAIL.equals(booleanResult.getCode())) {
            return new Result<>(Result.OK, booleanResult.getMsg(), null);
        }

        log.info("QueryForPage {}", invokeQuery);

        List<ApiLogRecord> apiLogRecords = new ArrayList<>();
        Sort.Direction direction = Sort.Direction.DESC;
     /*   SortOrder sortOrder = SortOrder.DESC;
        String property = "startTime.keyword";
        if (invokeQuery.getSort() != null && invokeQuery.getSort().size() > 1) {
            direction = "d".equalsIgnoreCase(invokeQuery.getSort().get(0)) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sortOrder = "d".equalsIgnoreCase(invokeQuery.getSort().get(0)) ? SortOrder.DESC : SortOrder.ASC;
        }
        Pageable pageable = PageRequest.of(
                0 != invokeQuery.getPageNum() ? invokeQuery.getPageNum() - 1 : 0, invokeQuery.getPageSize(),
                Sort.by(direction, property));

        BoolQueryBuilder boolQueryBuilder = buildSearchQuery(
                invokeQuery.getIndexList(), invokeQuery.getTimeQuery(),
                invokeQuery.getApiInfos().values().stream().map(ApiInfo::getServiceId).collect(Collectors.toList()),
                invokeQuery.getStatType(), null);
        if (MiscUtil.isNotEmpty(invokeQuery.getApiName())) {
            boolQueryBuilder.must().add(QueryBuilders.wildcardQuery("apiName.keyword", "*"+invokeQuery.getApiName()+"*"));
        }
        if (MiscUtil.isNotEmpty(invokeQuery.getRequestBody())){
            if(invokeQuery.getRequestBody().contains("&&")){
                String[] arr = invokeQuery.getRequestBody().split("&&");
                for(int i=0;i<arr.length;i++){
                    boolQueryBuilder.must().add(QueryBuilders.matchPhraseQuery("requestBody", arr[i]).slop(1));
                }
            }else if(invokeQuery.getRequestBody().contains("||")){
                String[] arr = invokeQuery.getRequestBody().replaceAll("\\|",",").split(",,");
                for(int i=0;i<arr.length;i++){
                    boolQueryBuilder.should().add(QueryBuilders.matchPhraseQuery("requestBody",arr[i]).slop(1));
                    log.info("arr:{}",arr[i]);
                }
                boolQueryBuilder.minimumShouldMatch(1);
            }else {
                boolQueryBuilder.must().add(QueryBuilders.matchPhraseQuery("requestBody", invokeQuery.getRequestBody()).slop(1));
            }
        }

        if (MiscUtil.isNotEmpty(invokeQuery.getResponseBody())){
            if(invokeQuery.getResponseBody().contains("&&")){
                String[] arr = invokeQuery.getResponseBody().split("&&");
                for(int i=0;i<arr.length;i++){
                    boolQueryBuilder.must().add(QueryBuilders.matchPhraseQuery("responseBody", arr[i]).slop(1));
                }
            }else if(invokeQuery.getResponseBody().contains("||")){
                String[] arr = invokeQuery.getResponseBody().replaceAll("\\|",",").split(",,");
                for(int i=0;i<arr.length;i++){
                    boolQueryBuilder.should().add(QueryBuilders.matchPhraseQuery("responseBody", arr[i]).slop(1));
                }
                boolQueryBuilder.minimumShouldMatch(1);
            }else {
                boolQueryBuilder.must().add(QueryBuilders.matchPhraseQuery("responseBody", invokeQuery.getResponseBody()).slop(1));
            }
        }*/
        long total = 0;
        //根据projectId拿到所有api
    /*    boolQueryBuilder = userKeyCam(boolQueryBuilder,invokeQuery,apiLogRecords,pageable);
        if(null == boolQueryBuilder){
            return new Result<>(Result.OK, "未查询到符合条件数据！", new PageImpl<>(apiLogRecords, pageable,
                    total));
        }*/
        List<ApiCastLog> apiCastLogList = new ArrayList<>();
        List<String> indexList = invokeQuery.getIndexList();
   /*     if (CollectionUtils.isEmpty(indexList)){
            return new Result<>(Result.OK, "", new PageImpl<>(apiLogRecords, pageable,
                    total));
        }*/
        String[] indexStrings = new String[indexList.size()];
        indexList.toArray(indexStrings);
        log.info("indexList {}", indexList);
        try {
          /*  SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            int from = invokeQuery.getPageNum()==0?0:(invokeQuery.getPageNum()-1)*invokeQuery.getPageSize();
            sourceBuilder.from(from);
            sourceBuilder.size(invokeQuery.getPageSize());
            //按照hashid去重
            CollapseBuilder collapse = new CollapseBuilder("id.keyword");
            sourceBuilder.collapse(collapse);
            sourceBuilder.query(boolQueryBuilder);
            sourceBuilder.sort(new FieldSortBuilder(property).order(sortOrder));
            log.info("ES查询条件："+sourceBuilder.toString()+",索引："+indexList);
            SearchRequest searchRequest = new SearchRequest(indexStrings,sourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
            total = searchResponse.getHits().getTotalHits();
            apiCastLogList = esResultHandle(searchResponse, ApiCastLog.class);*/
        }catch (Exception e){
            log.error("查询异常",e);
          /*  return new Result<>(Result.FAIL, e.getMessage(), new PageImpl<>(apiLogRecords, pageable,
                    total));*/
          return null;
        }


        if (MiscUtil.isNotEmpty(apiCastLogList)) {
            for (ApiCastLog invokeLog : apiCastLogList) {
                ApiLogRecord record = new ApiLogRecord();
                record.setHashId(invokeLog.getId());
                record.setApiId(invokeLog.getApiId());
                record.setApiName(invokeLog.getApiName());
                String ips = invokeLog.getReqHeaders().get("x-forwarded-for");
                ips=ips.substring(1,ips.length()-1);
                String clientIp=ips.split(",")[0];
                log.info("clientIp{}",clientIp);
                record.setIpList(clientIp);
                record.setServiceId(invokeLog.getServiceId());
                record.setRequestTime(invokeLog.getStartTime());
                record.setHttpMethod(invokeLog.getRequestMethod());
                record.setHttpPattern(invokeLog.getRequestUri().replaceAll("\\?user_key=.*", ""));

                DataItem item;
                ApiInfo apiInfo = invokeQuery.getApiInfos().get(invokeLog.getApiId());
                String apiSystem = apiInfo != null ? apiInfo.getSystemName() :
                        (item = publishApplicationRepository.findCreateSystemNameByApiId(invokeLog.getApiId())) != null ?
                                item.getItemName() : "none";

                String callSystem =
                        (item = publishApplicationRepository.findSubscribedSystemNameByUserKey(invokeLog.getUserKey()))
                                != null ? item.getItemName() : apiSystem;

                record.setCallSystem(callSystem);
                record.setApiSystem(apiSystem);
                record.setRequestParam(MiscUtil.getValue(invokeLog.getRequestUri(), "^.*\\?user_key=[a-z0-9]+&(\\S+)" +
                        "$"));
                record.setRequestHeader(invokeLog.getReqHeaders());
                record.setRequestBody(invokeLog.getRequestBody());
                record.setResponseBody(invokeLog.getResponseBody());
                record.setHttpStatusCode(invokeLog.getResponseCode());
                record.setResponseTime((int) (invokeLog.getResponseTime() * 1000));
                apiLogRecords.add(record);
            }
        }else{
           /* return new Result<>(Result.OK, "未查询到符合条件数据！", new PageImpl<>(apiLogRecords, pageable,
                    total));*/
        }
      /*  return new Result<>(Result.OK, "", new PageImpl<>(apiLogRecords, pageable,
                total));*/
      return null;
    }

    /**
     * 针对userKey做处理
     * @param boolQueryBuilder
     * @param invokeQuery
     * @param apiLogRecords
     * @param pageable
     * @return
     */
/*
    public BoolQueryBuilder userKeyCam(BoolQueryBuilder boolQueryBuilder,ApiLogQueryFull invokeQuery,List<ApiLogRecord> apiLogRecords,Pageable pageable){
        BoolQueryBuilder nestedBoolBuilder = QueryBuilders.boolQuery();
        int env = InstanceEnvironment.fromCode(invokeQuery.getEnvironment()).getCode();
        if(StringUtils.isNotBlank(invokeQuery.getProjectId())){
            //管理端查询，区分系统，只查询订阅api的日志
            String  projectId = invokeQuery.getProjectId();
            String tenantId = invokeQuery.getTenantId();
            Specification<PublishApiGroup> groupSpec = (root, query, builder) -> {
                List<Predicate> andList = new LinkedList<>();
                andList.add(builder.equal(root.get("tenantId").as(String.class), tenantId));
                andList.add(builder.equal(root.get("projectId").as(String.class), projectId));
                andList.add(builder.equal(root.get("status").as(Integer.class), 1));
                return builder.and(andList.toArray(new Predicate[0]));
            };

            List<PublishApiGroup> specialPublishApiGroups = publishApiGroupRepository.findAll(groupSpec);
            if (CollectionUtils.isEmpty(specialPublishApiGroups)) {
                return null;
            }
            Specification<PublishApi> spec2 = (root, query, builder) -> {
                List<Predicate> andList = new LinkedList<>();
                if (!CollectionUtils.isEmpty(specialPublishApiGroups)) {
                    CriteriaBuilder.In<Integer> groupIn = builder.in(root.get("group").get("id"));
                    for (PublishApiGroup item : specialPublishApiGroups) {
                        groupIn.value(item.getId());
                    }
                    andList.add(groupIn);
                }
                andList.add(builder.equal(root.get("environment").as(Integer.class), env));
                return builder.and(andList.toArray(new Predicate[andList.size()]));
            };
            List<PublishApi> apis = publishApiRepository.findAll(spec2);
    */
/*        if(!CollectionUtils.isEmpty(apis)){
                BoolQueryBuilder apiBoolBuilder = QueryBuilders.boolQuery();
                for(PublishApi api:apis){
                    apiBoolBuilder.should().add(QueryBuilders.matchPhraseQuery("apiId", api.getId()).slop(0).boost(1.0f));
                }
                apiBoolBuilder.adjustPureNegative(true).minimumShouldMatch(1).boost(1.0f);
                boolQueryBuilder.must().add(apiBoolBuilder);
                //查询api的订阅记录(未被订阅过也不展示)
                Specification<PublishApplication> appSpec = (root, query, builder) -> {
                    List<Predicate> andList = new LinkedList<>();
                    CriteriaBuilder.In<Integer> apiIn = builder.in(root.get("publishApi").get("id"));
                    for (PublishApi api : apis) {
                        apiIn.value(api.getId());
                    }
                    andList.add(apiIn);
                    andList.add(builder.equal(root.get("type").as(Integer.class), 1));
                    //包含取消订阅的数据
                    CriteriaBuilder.In<Integer> statusIn = builder.in(root.get("status"));
                    statusIn.value(4);//修改密匙前的数据
                    statusIn.value(2);//正常订阅的数据
                    andList.add(statusIn);
                    return builder.and(andList.toArray(new Predicate[andList.size()]));
                };
                boolQueryBuilder = userKeyHandle(appSpec,nestedBoolBuilder,boolQueryBuilder);
            }else{
                return null;
            }*//*

        }else{
            //查询api的订阅记录(未被订阅过也不展示)
            Specification<PublishApplication> appSpec = (root, query, builder) -> {
                List<Predicate> andList = new LinkedList<>();
                andList.add(builder.equal(root.get("type").as(Integer.class), 1));
                //包含取消订阅的数据
                CriteriaBuilder.In<Integer> statusIn = builder.in(root.get("status"));
                statusIn.value(4);//修改密匙前的数据
                statusIn.value(2);//正常订阅的数据
                andList.add(statusIn);
                andList.add(builder.equal(root.get("publishApi").get("status").as(Integer.class), 4));
                andList.add(builder.equal(root.get("creator").as(String.class), CommonBeanUtil.getLoginUserName()));
                andList.add(builder.equal(root.get("publishApi").get("environment").as(Integer.class), env));
                return builder.and(andList.toArray(new Predicate[andList.size()]));
            };
            boolQueryBuilder = userKeyHandle(appSpec,nestedBoolBuilder,boolQueryBuilder);
        }
        return boolQueryBuilder;
    }
*/


    /**
     * userKey和appid、appkey合并考虑（集成旧的api没有userKey的情况）
     * @param appSpec
     * @param nestedBoolBuilder
     * @param boolQueryBuilder
     * @return
     */
/*
    public BoolQueryBuilder  userKeyHandle(Specification<PublishApplication> appSpec,BoolQueryBuilder nestedBoolBuilder,BoolQueryBuilder boolQueryBuilder){
        List<PublishApplication> applications = publishApplicationRepository.findAll(appSpec);
        if(!CollectionUtils.isEmpty(applications)){
            Set<String> userKeys = new HashSet<>();
            List<Map<String,String>> keyAndIds = new ArrayList<>();
            applications.stream().forEach(app->{
                if(StringUtils.isNotBlank(app.getUserKey())){
                    userKeys.add(app.getUserKey());
                }else{
                    //获取appid和appKey
                    if(StringUtils.isNotBlank(app.getAppId()) && StringUtils.isNotBlank(app.getAppKey())){
                        Map<String,String> keyMap =  new HashMap<>();
                        keyMap.put("appId", app.getAppId());
                        keyMap.put("appKey", app.getAppKey());
                        keyAndIds.add(keyMap);
                    }
                    */
/*if(StringUtils.isNotBlank( app.getScaleApplicationId())){
                        try{
                            Instance ins = instanceRepository.findOne(app.getInstance().getId());
                            if(null == ins){
                                log.error("Instance is null,",JSONObject.toJSONString(app));
                            }
                            UserInstanceRelationship uir =
                                    userInstanceRelationshipRepository.findByUserAndInstanceId(app.getCreator(), ins.getId());
                            ApplicationXml applicationXml = accountStud.appXml(ins.getHost(),ins.getAccessToken(),  String.valueOf(uir.getAccountId()), app.getScaleApplicationId());
                            if(null != applicationXml && null != applicationXml.getApplicationId() &&  null != applicationXml.getKeys() &&
                                    !CollectionUtils.isEmpty(applicationXml.getKeys().getKey()) ){
                                Map<String,String> keyMap =  new HashMap<>();
                                keyMap.put("appId", applicationXml.getApplicationId());
                                keyMap.put("appKey", applicationXml.getKeys().getKey().get(0));
                                keyAndIds.add(keyMap);
                            }else{
                                log.info(String.format("appId or appKey is null,applicationXml:%",applicationXml));
                            }
                        }catch (Exception e){
                            log.info("get appId and appKey Exception,application",JSONObject.toJSONString(app));
                            log.error("get appId and appKey Exception:",e);
                        }
                    }else {
                        log.error("ScaleApplicationId is null :",app);
                    }*//*

                }
            });
            //有userKey的按照userkey，没有userKey的按照appId和appkey
            if(!CollectionUtils.isEmpty(userKeys) || !CollectionUtils.isEmpty(keyAndIds)){
               */
/* if(!CollectionUtils.isEmpty(userKeys)){
                    for(String userKey:userKeys){
                        nestedBoolBuilder.should().add(QueryBuilders.matchPhraseQuery("userKey", userKey).slop(0).boost(1.0f));
                    }
                }
                if(!CollectionUtils.isEmpty(keyAndIds)){
                    for(Map<String,String> keyMap:keyAndIds){
                        String appId = keyMap.get("appId");
                        String appKey = keyMap.get("appKey");
                        BoolQueryBuilder keyBuilder = QueryBuilders.boolQuery();
                        keyBuilder.must().add(QueryBuilders.matchPhraseQuery("appId", appId).slop(0).boost(1.0f));
                        keyBuilder.must().add(QueryBuilders.matchPhraseQuery("appKey", appKey).slop(0).boost(1.0f));
                        nestedBoolBuilder.should().add(keyBuilder);
                    }
                }
                nestedBoolBuilder.adjustPureNegative(true).minimumShouldMatch(1).boost(1.0f);
                boolQueryBuilder.must().add(nestedBoolBuilder);*//*

            }else{
                return null;
            }
        }else{
            return null;
        }
        return boolQueryBuilder;
    }
*/

    @Override
    public Result<List<ApiResponseTime>> queryForList(ApiTrafficStatQuery statQuery) {
        Result<Boolean> booleanResult = preFixParameters(statQuery);
        if (booleanResult.isFailure()) {
            return new Result<>(Result.FAIL, booleanResult.getMsg(), null);
        }

        log.info("QueryForList {}", statQuery);

        return null;

      /*  return new Result<>(Result.OK, "", queryForAllList(statQuery.getIndexList(), statQuery.getTimeQuery(),
                Collections.singletonList(statQuery.getScaleServiceId()), STAT_ALL, null, ApiResponseTime.class));*/
    }

    /**
     * 查询单个API的接口响应时间百分比
     */
    @Override
    public Result<ApiResponseStatistics> queryForStatistics(ApiTrafficStatQuery statQuery) {
        Result<Boolean> booleanResult = preFixParameters(statQuery);
        if (booleanResult.isFailure()) {
            return new Result<>(Result.FAIL, booleanResult.getMsg(), null);
        }

        if (MiscUtil.isEmpty(properties.getApiRespTimeRates()) ||
                properties.getApiRespTimeRates().removeIf(item -> item >= 100 || item <= 0)) {
            return new Result<>(Result.FAIL, ES_MSG_INVALID_RESP_RATE_VALUE, null);
        }

        log.info("QueryForStatistics {}", statQuery);
        Result<ApiResponseStatistics> statisticsResult = new Result<>(Result.OK, "", null);

    /*    List<ApiResponseTime> responseTimes =
                queryForAllList(statQuery.getIndexList(),
                        statQuery.getTimeQuery(),
                        Collections.singletonList(statQuery.getScaleServiceId()),
                        STAT_ALL,
                        statQuery.getAppUserKey(),
                        ApiResponseTime.class);*/
        List<ApiResponseTime> responseTimes = Lists.newArrayList();
        if (MiscUtil.isEmpty(responseTimes)) {
            log.error("QueryForStatistics fail, no record found");
            statisticsResult.setMsg("Query  no record found");
            return statisticsResult;
        }

        // build responseStat
        TimeQuery query = statQuery.getTimeQuery();

        List<TimeUnit> timeUnits = null;
        StatGranularity granularity = statQuery.getGranularity();

        if (EXACT_DURATION_HOURS.equals(granularity)) {
            timeUnits = TimeUtil.getIntervalTimes(query, TimeUtil.DEFAULT_FORMAT, Calendar.MINUTE,
                    granularity.getDefaultItemCount(), true);
        } else if (EXACT_DURATION_DAYS.equals(granularity)) {
            timeUnits = TimeUtil.getIntervalTimes(query, TimeUtil.DEFAULT_FORMAT, Calendar.HOUR_OF_DAY,
                    granularity.getDefaultItemCount(), true);
        } else if (EXACT_DURATION_MONTHS.equals(granularity)) {
            timeUnits = TimeUtil.getIntervalTimes(query, TimeUtil.DEFAULT_FORMAT, Calendar.DAY_OF_YEAR,
                    granularity.getDefaultItemCount(), true);
        }

        if (MiscUtil.isEmpty(timeUnits)) {
            log.error("invalid time unit");
            return null;
        }

        int unitIndex = 0;
        for (TimeUnit timeUnit : timeUnits) {
            if (EXACT_DURATION_HOURS.equals(granularity)) {
                timeUnit.setShowXAxis(unitIndex % 2 == 0);
            } else if (EXACT_DURATION_DAYS.equals(granularity)) {
                timeUnit.setShowXAxis(unitIndex % 4 == 0);
            } else if (EXACT_DURATION_MONTHS.equals(granularity)) {
                timeUnit.setShowXAxis(unitIndex % 6 == 0);
            }

            unitIndex++;
        }

        if (log.isDebugEnabled()) {
            timeUnits.forEach(item -> log.info("{}", item));
            responseTimes.forEach(item -> log.info("{}", item));
        }

        log.info("TimeUnits {}", timeUnits.size());
        log.info("ResponseTimes {}", responseTimes.size());

        ApiResponseStatistics statistics = new ApiResponseStatistics();
        statistics.setValues(new ArrayList<>());

        // get Array for timeStamp & respTimes
        Long[] array = responseTimes.stream().map(ApiResponseTime::getTimestamp).toArray(Long[]::new);
        Integer[] respTimes = responseTimes.stream().map(
                item -> {
                    Double  responseTime=Double.parseDouble(String.valueOf(item.getResponseTime() * 1000));
                    return responseTime.intValue();
                }).toArray(Integer[]::new);

        TimeUnit lastUnit = timeUnits.get(0);
        for (TimeUnit timeUnit : timeUnits) {
            if (timeUnit == timeUnits.get(0)) {
                continue;
            }

            ResponseStat stat = buildResponseStat(array, respTimes, lastUnit, timeUnit);
            statistics.getValues().add(stat);
            lastUnit = timeUnit;
        }

        ResponseStat responseStat = new ResponseStat();
        responseStat.setDate(lastUnit.getDate());
        responseStat.setShowX(lastUnit.isShowXAxis());
        statistics.getValues().add(responseStat);

        if (log.isDebugEnabled()) {
            log.info("{}", statistics);
        }

        statisticsResult.setCode(Result.OK);
        statisticsResult.setData(statistics);
        return statisticsResult;
    }

    @Override
    public Result<ApiCastLog> queryForSingle(ApiLogQuerySingle apiLogQuerySingle) {
        Result<ApiCastLog> result = new Result<>(Result.OK,"查询成功！",null);
        try{
            //构建索引和查询条件
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TimeUtil.DATE_FORMAT_INDEX);
            String itemIndex =simpleDateFormat.format(new SimpleDateFormat(TimeUtil.DEFAULT_FORMAT).parse(apiLogQuerySingle.getRequestTime()));
            String item = String.format("%s%s", INDEX_PREFIX, itemIndex);
          /*  GetIndexRequest getIndexRequest = new GetIndexRequest(item);
            boolean flag = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            if(!flag){
                result.setError(Result.FAIL,"index不存在！");
                return result;
            }
            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
            if (MiscUtil.isNotEmpty(apiLogQuerySingle.getHashId())) {
                boolBuilder.must().add(QueryBuilders.matchPhraseQuery("id", apiLogQuerySingle.getHashId()).slop(0).boost(1.0f));
            }
            //查询数据
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(boolBuilder);
            log.info("ES查询条件："+sourceBuilder.toString());
            SearchRequest searchRequest = new SearchRequest(item);
            searchRequest.source(sourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
            log.info("ES查询结果："+searchResponse.toString());
            if(searchResponse.getHits().getTotalHits()>0){
                List<ApiCastLog> apiCastLogList = esResultHandle(searchResponse, ApiCastLog.class);
                result.setData(apiCastLogList.get(0));
            }else{
                result.setError(Result.FAIL,"未查询到日志记录！ID："+apiLogQuerySingle.getHashId());
            }*/
            return result;
        }catch (Exception e){
            log.error("查询异常：",e);
            result.setError(Result.FAIL,String.format("查询异常！%s",e.getMessage()));
            return result;
        }
    }

    private ResponseStat buildResponseStat(Long[] tsArray, Integer[] respTimes, TimeUnit lastUnit, TimeUnit timeUnit) {
        long start = lastUnit.getTimeStamp();
        long end = timeUnit.getTimeStamp();

        ResponseStat responseStat = new ResponseStat();
        responseStat.setDate(lastUnit.getDate());
        responseStat.setShowX(lastUnit.isShowXAxis());

        if (timeUnit.getTimeStamp() < tsArray[0] || lastUnit.getTimeStamp() > tsArray[tsArray.length - 1]) {
            return responseStat;
        }

        // get boundary for current unit in responses from es
        int left, right;
        if (lastUnit.getTimeStamp() < tsArray[0]) {
            left = 0;
        } else {
            // (-(insertion point) - 1)
            // insertion point is defined as the point at which the key would be inserted into the array:
            // the index of the first element in the range greater than the key,
            // or toIndex if all elements in the range are less than the specified key
            left = Arrays.binarySearch(tsArray, 0, tsArray.length - 1, start);
            left = left < 0 ? Math.max(-left - 1, 0) : left;
        }

        if (timeUnit.getTimeStamp() > tsArray[tsArray.length - 1]) {
            right = tsArray.length - 1;
        } else {
            right = Arrays.binarySearch(tsArray, 0, tsArray.length - 1, end);
            right = right < 0 ? Math.min(-right - 2, tsArray.length - 1) : right;
        }

        int leftEnd = left, rightEnd = right;
        if (log.isDebugEnabled()) {
            log.info("TS({})[{}]", tsArray.length, Arrays.asList(tsArray));
            log.info("RS({})[{}]", tsArray.length, Arrays.asList(respTimes));
        }
        log.info("To find [{},{}],[{}, {}]",
                TimeUtil.timestampToDate(tsArray[leftEnd]),
                TimeUtil.timestampToDate(tsArray[rightEnd]),
                TimeUtil.timestampToDate(lastUnit.getTimeStamp()),
                TimeUtil.timestampToDate(timeUnit.getTimeStamp()));

        if (leftEnd > rightEnd) {
            return responseStat;
        }

        responseStat.setMap(new HashMap<>());
        Map<String, Integer> map = responseStat.getMap();

        if (leftEnd == rightEnd) {
            properties.getApiRespTimeRates().forEach(item -> {
                map.put("p" + item, respTimes[leftEnd]);
                log.info("{} {} {}", "p" + item, leftEnd, respTimes[leftEnd]);
            });
            return responseStat;
        }

        // 求平均值
        /*long allRespTime=0;
        for(int index=leftEnd; index<=rightEnd;index++){
            allRespTime+=respTimes[index];
        }
        map.put("p50",Long.valueOf(allRespTime/(rightEnd-leftEnd)).intValue());*/
        //求最大值
        long maxRespTime=0;
        for(int index=leftEnd; index<=rightEnd;index++){
            if(maxRespTime <= respTimes[index]){
                maxRespTime =  respTimes[index];
            }
        }
        map.put("p50",(int)maxRespTime );


        /*// sort responseTime arrays
        Arrays.sort(respTimes, leftEnd, rightEnd <= respTimes.length - 2 ? rightEnd + 1 : rightEnd);
        if (log.isDebugEnabled()) {
            log.info("respTimes {}", Arrays.asList(respTimes));
        }

        // 默认是将当前区间内的统计, 代给左坐标点, 如果X坐标轴间隔越大,则统计会存在左偏差.
        // build rate index
        int length = rightEnd - leftEnd + 1;
        properties.getApiRespTimeRates().forEach(item -> {
            int index = (Double.valueOf(length * item / 100.0).intValue()) + leftEnd;
            if (index > rightEnd) {
                index = rightEnd;
            }
            map.put("p" + item, respTimes[index]);
            log.info("{} {} {}", "p" + item, index, respTimes[index]);
        });*/

        return responseStat;
    }

    @Data
    private static class TimeUnit {
        private String date;
        private long timeStamp;
        private boolean showXAxis;

        public TimeUnit(String date, long timeStamp) {
            this.date = date;
            this.timeStamp = timeStamp;
        }
    }

    private static class TimeUtil {
        public static final String DATE_FORMAT = "yyyy_MM_dd";
        public static final String DATE_FORMAT_INDEX = "yyyy.MM.dd";
        public static final String DEFAULT_TZ = "Asia/Shanghai";
        public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

        private static TimeZone getDefaultTimeZone() {
            return TimeZone.getTimeZone(DEFAULT_TZ);
        }

        private static SimpleDateFormat getDefaultFormat() {
            return getDefaultFormat(DEFAULT_FORMAT);
        }

        private static SimpleDateFormat getDefaultFormat(String format) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setTimeZone(getDefaultTimeZone());
            return sdf;
        }

        private static List<String> getIntervalDays(TimeQuery timeQuery) {
            return getIntervalTimes(timeQuery, DATE_FORMAT_INDEX, Calendar.DAY_OF_YEAR, 1, false)
                    .stream().map(TimeUnit::getDate).collect(Collectors.toList());
        }

        public static String timestampToDate(long timestamp) {
            return getDefaultFormat().format(new Date(timestamp));
        }

        /**
         * 构建时间区间
         *
         * @param query    起始\结束时间
         * @param format   时间格式
         * @param field    区间单位
         * @param interval 区间长度
         */
        private static List<TimeUnit> getIntervalTimes(TimeQuery query, String format, int field, int interval,
                                                       boolean stamp) {
            List<TimeUnit> values = new ArrayList<>();
            if (query.getStart() == null || query.getEnd() == null) {
                log.error("invalid time start or end {}", query);
                return values;
            }

            DateFormat dateFormat = getDefaultFormat(format);
            try {
                TimeZone zone = getDefaultTimeZone();
                Calendar tempStart = Calendar.getInstance(zone);
                tempStart.setTime(query.getStart());

                Calendar tempEnd = Calendar.getInstance(zone);
                tempEnd.setTime(query.getEnd());
                tempEnd.add(Calendar.DATE, +1);

                while (tempStart.before(tempEnd)) {
                    values.add(new TimeUnit(dateFormat.format(tempStart.getTime()), stamp ?
                            tempStart.getTime().getTime() : 0L));
                    tempStart.add(field, interval);
                }
            } catch (Exception e) {
                log.error("context",e);
            }

            return values;
        }
    }
}
