package com.hisense.gateway.management.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.constant.AnalyticsConstant;
import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.DashboardModels;
import com.hisense.gateway.library.model.base.TimeQuery;
import com.hisense.gateway.library.model.base.analytics.ApiTrafficStatQuery;
import com.hisense.gateway.library.model.base.monitor.ApiLogQuerySpecial;
import com.hisense.gateway.library.model.base.monitor.ApiLogRecord;
import com.hisense.gateway.library.model.base.portal.PublishApiVO;
import com.hisense.gateway.library.model.dto.buz.SystemInfoDto;
import com.hisense.gateway.library.model.pojo.base.DataItem;
import com.hisense.gateway.library.model.pojo.base.PublishApi;
import com.hisense.gateway.library.model.pojo.base.PublishApiGroup;
import com.hisense.gateway.library.model.pojo.base.PublishApplication;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.library.service.AnalyticsService;
import com.hisense.gateway.library.service.ElasticSearchService;
import com.hisense.gateway.library.stud.model.AnalyticsDto;
import com.hisense.gateway.library.utils.DateUtil;
import com.hisense.gateway.management.service.DashboardService;
import com.hisense.gateway.management.service.SystemInfoService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hisense.gateway.library.model.base.DashboardModels.*;
import static com.hisense.gateway.library.model.base.DashboardQueries.*;

/**
 * 2020/10/20 guilai.ming
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {
    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    AnalyticsService analyticsService;

    @Autowired
    PublishApiRepository publishApiRepository;

    @Autowired
    SystemInfoService systemInfoService;

    @Autowired
    ElasticSearchService elasticSearchService;

    @Autowired
    DataItemRepository dataItemRepository;

    @Autowired
    PublishApplicationRepository publishApplicationRepository;

    @Autowired
    ApiInvokeRecordRepository apiInvokeRecordRepository;


    @Override
    public Result<List<PublishApiVO>> hitApiSubscribeCount(String environment,String projectId) {
        List<String> projects = getProjects(environment, projectId);
        Specification<PublishApi> apiSpec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            if (!CollectionUtils.isEmpty(projects)) {
                CriteriaBuilder.In<String> in = builder.in(root.get("group").get("projectId"));
                projects.forEach(v->in.value(v));
                andList.add(in);
            }
            if (MiscUtil.isNotEmpty(environment)) {
                andList.add(builder.equal(root.get("environment").as(Integer.class), InstanceEnvironment.fromCode(environment).getCode()));
            }
            andList.add(builder.equal(root.get("isOnline").as(Integer.class), 1));
            andList.add(builder.equal(root.get("status").as(Integer.class), 4));
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApi> publishApis=publishApiRepository.findAll(apiSpec);
        if (MiscUtil.isEmpty(publishApis)) {
            return new Result<>(Result.FAIL,
                    projectId != null ? "指定系统下不存在API" : "当前管理端不存在已发布的API", null);
        }

        List<PublishApiVO> infos = publishApis.stream().map(item -> new PublishApiVO(
                item.getId(),
                publishApiRepository.findOne(item.getId()).getName(),
                publishApiRepository.findOne(item.getId()).getGroup().getProjectId(),
                (long)publishApplicationRepository.subscribeCount(item.getId()))
        ).sorted(Comparator.comparing(PublishApiVO::getSubscribeCount)).limit(5).sorted(Comparator.comparing(PublishApiVO::getSubscribeCount).reversed()).collect(Collectors.toList());

        return new Result<>(Result.OK, "", infos);
    }

    @Override
    public Result<List<PublishApiVO>> hotApiSubscribeCount(String environment, String projectId) {

        List<String> projects = getProjects(environment, projectId);
        Specification<PublishApi> apiSpec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            if (MiscUtil.isNotEmpty(environment)) {
                andList.add(builder.equal(root.get("environment").as(Integer.class), InstanceEnvironment.fromCode(environment).getCode()));
            }
            if (!CollectionUtils.isEmpty(projects)) {
                CriteriaBuilder.In<String> in = builder.in(root.get("group").get("projectId"));
                projects.forEach(v->in.value(v));
                andList.add(in);
            }
            andList.add(builder.equal(root.get("isOnline").as(Integer.class), 1));
            andList.add(builder.equal(root.get("status").as(Integer.class), 4));
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApi> publishApis=publishApiRepository.findAll(apiSpec);
        if (MiscUtil.isEmpty(publishApis)) {
            return new Result<>(Result.FAIL,
                    projectId != null ? "指定系统下不存在API" : "当前管理端不存在已发布的API", null);
        }

        List<PublishApiVO> infos = publishApis.stream().map(item -> new PublishApiVO(
                item.getId(),
                publishApiRepository.findOne(item.getId()).getName(),
                publishApiRepository.findOne(item.getId()).getGroup().getProjectId(),
                (long)publishApplicationRepository.subscribeCount(item.getId()))
        ).sorted(Comparator.comparing(PublishApiVO::getSubscribeCount).reversed()).limit(5).collect(Collectors.toList());
        return new Result<>(Result.OK, "", infos);
    }


    @Override
    public Result<List<ApiProjectInfo>> getTopPublishApiCountProjects(String environment,String projectId) {
        long start = System.currentTimeMillis();
        List<String> projects = getProjects(environment, projectId);
        long end = System.currentTimeMillis();
        log.info("==================getTopPublishApiCountProjects1："+(end-start));
        Map<String, Object> params = new HashMap<>();
        StringBuilder dataSql = new StringBuilder();
        dataSql.append("with tab as( ");
        dataSql.append("SELECT pag.project_id, ");
        dataSql.append("count(DISTINCT api.id) AS API_COUNT ");
        dataSql.append("FROM publish_api api ");
        dataSql.append("INNER JOIN publish_api_group pag ON api.group_id = pag.id ");
        dataSql.append("WHERE api.status = 4 ");
        dataSql.append("AND api.environment =:environment ");
        dataSql.append("AND pag.project_id IN (:projects) ");
        dataSql.append("GROUP BY pag.project_id) ");
        dataSql.append("SELECT * FROM ( ");
        dataSql.append("SELECT si.id as SYSTEM_ID, ");
        dataSql.append("si.name as SYSTEM_NAME, ");
        dataSql.append("nvl(tab.api_count,0) api_count, ");
        dataSql.append("row_number() over(ORDER BY nvl(tab.api_count,0) DESC) as rank, ");
        dataSql.append("ROUND(ratio_to_report(nvl(tab.api_count,0)) over ()*100,1) AS ratio ");
        dataSql.append("FROM SYSTEM_INFO si ");
        dataSql.append("LEFT JOIN tab on si.id = tab.project_id ");
        dataSql.append("WHERE 1=1 ");
        dataSql.append("AND si.id in (:projects) ");
        dataSql.append(")t WHERE t.rank <=5");
        params.put("environment", InstanceEnvironment.fromCode(environment).getCode());
        params.put("projects",projects);
        Query dataQuery = entityManager.createNativeQuery(dataSql.toString());
        dataQuery.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        setParameters(dataQuery,params);
        List<Map<String, Object>> mapList = dataQuery.getResultList();
        List<ApiProjectInfo> apiProjectInfos = new ArrayList();
        if(!CollectionUtils.isEmpty(mapList)){
            for(Map<String, Object> map:mapList){
                ApiProjectInfo info = new ApiProjectInfo();
                info.setSystemId(Integer.parseInt(String.valueOf(map.get("SYSTEM_ID"))));
                info.setSystemName(String.valueOf(map.get("SYSTEM_NAME")));
                info.setApiCount(Integer.parseInt(map.get("API_COUNT").toString()));
                info.setPercentage(map.get("RATIO")+"%");
                apiProjectInfos.add(info);
            }
        }
        long end2 = System.currentTimeMillis();
        log.info("==================getTopPublishApiCountProjects2："+(end2-end));
        return new Result<>(Result.OK, "", apiProjectInfos);
    }

    @Override
    public Result<List<ApiProjectInfo>> getTopSubscribeApiSystem(String environment, String project) {
        long start = System.currentTimeMillis();
        List<String> projects = getProjects(environment, project);
        long end = System.currentTimeMillis();
        log.info("==================getTopSubscribeApiSystem1："+(end-start));
        Map<String, Object> params = new HashMap<>();
        StringBuilder dataSql = new StringBuilder();
        dataSql.append("with tab as( ");
        dataSql.append("SELECT pag.project_id, ");
        dataSql.append("app.api_id,app.system ");
        dataSql.append("FROM publish_application app ");
        dataSql.append("INNER JOIN publish_api api ON app.api_id = api.id ");
        dataSql.append("INNER JOIN publish_api_group pag ON api.group_id = pag.id ");
        dataSql.append("WHERE app.type = 1 ");
        dataSql.append("AND app.status = 2 AND api.status = 4 ");
        dataSql.append("AND api.environment = :environment ");
        dataSql.append(" AND pag.project_id IN (:projects) ");
        dataSql.append("GROUP BY pag.project_id,app.api_id,app.system) ");
        dataSql.append("SELECT * FROM ( ");
        dataSql.append("SELECT si.id as SYSTEM_ID, ");
        dataSql.append("max(si.name) as SYSTEM_NAME, ");
        dataSql.append("count(1) as api_count, ");
        dataSql.append("row_number() over(ORDER BY count(1) DESC) as rank, ");
        dataSql.append("ROUND(ratio_to_report(count(1)) over()*100,1) AS ratio ");
        dataSql.append("FROM SYSTEM_INFO si ");
        dataSql.append("LEFT JOIN tab on si.id = tab.project_id ");
        dataSql.append("WHERE 1=1  ");
        dataSql.append("AND si.id in (:projects) ");
        dataSql.append("GROUP BY si.id )t WHERE t.rank <=5 ");
        params.put("environment", InstanceEnvironment.fromCode(environment).getCode());
        params.put("projects",projects);
        Query dataQuery = entityManager.createNativeQuery(dataSql.toString());
        dataQuery.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        setParameters(dataQuery,params);
        List<Map<String, Object>> mapList = dataQuery.getResultList();
        List<ApiProjectInfo> apiProjectInfos = new ArrayList();
        if(!CollectionUtils.isEmpty(mapList)){
            for(Map<String, Object> map:mapList){
                ApiProjectInfo info = new ApiProjectInfo();
                info.setSystemName(String.valueOf(map.get("SYSTEM_NAME")));
                info.setSystemId(Integer.parseInt( map.get("SYSTEM_ID").toString()));
                info.setApiCount(Integer.parseInt(map.get("API_COUNT").toString()));
                info.setPercentage(map.get("RATIO").toString()+"%");
                apiProjectInfos.add(info);
            }
        }
        long end2 = System.currentTimeMillis();
        log.info("==================getTopSubscribeApiSystem2："+(end2-end));
        return new Result<>(Result.OK, "", apiProjectInfos);
    }

    @Override
    public Result<Map<String,Object>> getTopSubscribeApiCount(String environment,String project,String value) {
        List<String> projects = getProjects(environment, project);
        List<SubscribeApiProjectInfo> list = getSubscribeApiProjectInfos(environment, projects);
        List<String> api = new ArrayList<>();
        List<Integer> count = new ArrayList<>();
        List<SubscribeApiProjectInfo> collect;
        if("top".equals(value)){
             collect = list.stream().sorted(Comparator.comparing(SubscribeApiProjectInfo::getSubscribeCount).reversed()).limit(5).collect(Collectors.toList());
        }else{
            collect = list.stream().sorted(Comparator.comparing(SubscribeApiProjectInfo::getSubscribeCount)).limit(5).collect(Collectors.toList());

        }
        Collections.reverse(collect);
        for(SubscribeApiProjectInfo info : collect){
            api.add(info.getApiName());
            count.add(info.getSubscribeCount());
        }
        Map<String,Object> map = new HashMap<>();
        map.put("api",api);
        map.put("count",count);
        return new Result<>(Result.OK, "", map);
    }

    @Override
    public Result<Map<String,Object>> getTopInvokeCount(String environment,String project) {
        List<String> projects = getProjects(environment, project);
        Map<String,List<ApiInvokeInfo>> invokeCountApiProjectCount = getInvokeCountApiProjectCount(environment, projects);
        List<ApiInvokeInfo> today = invokeCountApiProjectCount.get("today");
        List<ApiInvokeInfo> total = invokeCountApiProjectCount.get("total");
        List<ApiInvokeInfo> collect = today.stream().sorted(Comparator.comparing(ApiInvokeInfo::getInvokeCount).reversed()).limit(5).collect(Collectors.toList());
        List<ApiInvokeInfo> collect2 = total.stream().sorted(Comparator.comparing(ApiInvokeInfo::getInvokeCount).reversed()).limit(5).collect(Collectors.toList());
        Collections.reverse(collect);
        Collections.reverse(collect2);
        List<String> todayApi = new ArrayList<>();
        List<Integer> todayCount = new ArrayList<>();
        List<String> totalApi = new ArrayList<>();
        List<Integer> totalCount = new ArrayList<>();
        for(ApiInvokeInfo info : collect){
            todayApi.add(info.getApiName());
            todayCount.add(info.getInvokeCount());
        }
        for(ApiInvokeInfo info : collect2){
            totalApi.add(info.getApiName());
            totalCount.add(info.getInvokeCount());
        }
        Map<String,Object> result = new HashMap<>();
        result.put("todayApi",todayApi);
        result.put("todayCount",todayCount);
        result.put("totalApi",totalApi);
        result.put("totalCount",totalCount);
        return new Result<>(Result.OK, "",result);
    }

    @Override
    public Result<ApiReleaseStatisticsVO> getReleaseStatisticsInfos(String environment, String project) {
        long start = System.currentTimeMillis();
        List<String> projects = getProjects(environment, project);
        long end = System.currentTimeMillis();
        log.info("==================getReleaseStatisticsInfos1："+(end-start));
        ApiReleaseStatisticsVO vo = new ApiReleaseStatisticsVO();
        try {
            FutureTask<List<ApiDetailVO>> future1 = null;
            FutureTask<Map<String,Object>> future2 = null;
            FutureTask<Map<String,List<ApiInvokeInfo>>> future4 = null;
            future1 = new FutureTask<>((Callable)()-> {
                return getApiCount(environment, projects,Arrays.asList(4));//发布api数量
            });
            new Thread(future1).start();
            future2 = new FutureTask<>((Callable)()-> {
                    return getApiSubscribedCount(environment, projects);//累计被订阅
            });
            new Thread(future2).start();
            future4 = new FutureTask<>((Callable)()-> {
                return getInvokeCountApiProjectCount(environment, projects);//累计访问
            });
            new Thread(future4).start();

            List<ApiDetailVO> apiDetailVOs = future1.get();
            Map<String,Object> apiSubscribed = future2.get();
            Map<String,List<ApiInvokeInfo>> totalSum = future4.get();
            vo.setApiProjectCount(projects.size());
            vo.setApiCount(apiDetailVOs.size());
            vo.setApiDetails(apiDetailVOs);
            List<ApiSubscribedVO> apiSubscribedCount = (List<ApiSubscribedVO>) apiSubscribed.get("arrays");
            vo.setApiSubscribedCount(apiSubscribedCount.size());
            vo.setApiSubscribedVOs(apiSubscribedCount);
            long daySuccess = (long) apiSubscribed.get("successCount");
            long dayFail = (long) apiSubscribed.get("failCount");
            vo.setApiUseSuccessDayount(daySuccess);//今日调用成功
            vo.setApiUseFailDayount(dayFail);//今日调用失败
            List<ApiInvokeInfo> today = totalSum.get("today");
            vo.setApiUseDayCount(today.stream().mapToInt(m -> m.getInvokeCount()).sum());
            List<ApiInvokeInfo> total = totalSum.get("total");
            vo.setApiUseTotalCount(total.stream().mapToInt(m -> m.getInvokeCount()).sum());
        } catch (Exception e) {
            log.error("context",e);
        }
        long end2 = System.currentTimeMillis();
        log.info("==================getReleaseStatisticsInfos2："+(end2-end));
        return new Result<>(Result.OK, "",vo);
    }

    @Override
    public Result<List<ApiDetailVO>> unreleasedApiProjectInfos(@PathVariable String environment,String projectId) {
        List<String> projects = getProjects(environment, projectId);
        List<ApiDetailVO> apiCount = getApiCount(environment, projects, Arrays.asList(1, 2, 5, 6));
        return new Result<>(Result.OK, "",apiCount);
    }

    @Override
    public Result<Map<String, Object>> topApiBarChart(String environment, String project) {
        long start = System.currentTimeMillis();
        List<String> projects = getProjects(environment, project);
        long end = System.currentTimeMillis();
        log.info("==================topApiBarChart1："+(end-start));
        Map<String,Object> result = new HashMap<>();
        try {
        FutureTask<List<SubscribeApiProjectInfo>> future1 = null;
        FutureTask<Map<String,List<ApiInvokeInfo>>> future2 = null;
//        FutureTask<List<ApiInvokeInfo> > future3 = null;
        future1 = new FutureTask<>((Callable)()-> {
            return getSubscribeApiProjectInfos(environment, projects);//API订阅数量
        });
            new Thread(future1).start();
        future2 = new FutureTask<>((Callable)()-> {
            return getInvokeCountApiProjectCount(environment, projects);//API累计访问量
        });
            new Thread(future2).start();
//        future3 = new FutureTask<>((Callable)()-> {
//            return getInvokeCountApiProjectCount(environment, getStartTime(), projects);//今日API访问量
//        });
//            new Thread(future3).start();
        List<SubscribeApiProjectInfo> subscribeApiProjectInfos = future1.get();
            Map<String,List<ApiInvokeInfo>> invokeCountApiTotal = future2.get();
//        List<ApiInvokeInfo> invokeCountApiToday = future3.get();
//        List<SubscribeApiProjectInfo> subscribeApiProjectInfos = getSubscribeApiProjectInfos(environment, projects);
        //API订阅数量TOP5
        List<String> subscribeApiTop = new ArrayList<>();
        List<Integer> subscribeApiTopCount = new ArrayList<>();
        List<SubscribeApiProjectInfo> desc = subscribeApiProjectInfos
                .stream().sorted(Comparator.comparing(SubscribeApiProjectInfo::getSubscribeCount).reversed())
                .limit(5).collect(Collectors.toList());
        Collections.reverse(desc);
        for(SubscribeApiProjectInfo info : desc){
            subscribeApiTop.add(info.getApiName());
            subscribeApiTopCount.add(info.getSubscribeCount());
        }
        //API订阅数量BOTTOM5
        List<String> subscribeApiBottom = new ArrayList<>();
        List<Integer> subscribeApiBottomCount = new ArrayList<>();
        List<SubscribeApiProjectInfo> asc = subscribeApiProjectInfos
                .stream().sorted(Comparator.comparing(SubscribeApiProjectInfo::getSubscribeCount))
                .limit(5).collect(Collectors.toList());
        Collections.reverse(asc);
        for(SubscribeApiProjectInfo info : asc){
            subscribeApiBottom.add(info.getApiName());
            subscribeApiBottomCount.add(info.getSubscribeCount());
        }
        //API累计访问量TOP5
//        List<ApiInvokeInfo> invokeCountApiTotal = getInvokeCountApiProjectCount(environment, getInitTime(), projects);
            List<ApiInvokeInfo> total = invokeCountApiTotal.get("total");
            List<ApiInvokeInfo> totalInvokeCount = total.stream().sorted(Comparator.comparing(ApiInvokeInfo::getInvokeCount).reversed()).limit(5).collect(Collectors.toList());
        Collections.reverse(totalInvokeCount);
        List<String> totalInvokeApi = new ArrayList<>();
        List<Integer> totalInvokeApiCount = new ArrayList<>();
        for(ApiInvokeInfo info : totalInvokeCount){
            totalInvokeApi.add(info.getApiName());
            totalInvokeApiCount.add(info.getInvokeCount());
        }
        //今日API访问量TOP5
//        List<ApiInvokeInfo> invokeCountApiToday = getInvokeCountApiProjectCount(environment, getStartTime(), projects);
            List<ApiInvokeInfo> today = invokeCountApiTotal.get("today");
            List<ApiInvokeInfo> todyayInvokeCount = today.stream().sorted(Comparator.comparing(ApiInvokeInfo::getInvokeCount).reversed()).limit(5).collect(Collectors.toList());
        Collections.reverse(todyayInvokeCount);
        List<String> todayInvokeApi = new ArrayList<>();
        List<Integer> todayInvokeApiCount = new ArrayList<>();
        for(ApiInvokeInfo info : todyayInvokeCount){
            todayInvokeApi.add(info.getApiName());
            todayInvokeApiCount.add(info.getInvokeCount());
        }
            result.put("subscribeApiTop",subscribeApiTop);//API订阅数量TOP5
            result.put("subscribeApiTopCount",subscribeApiTopCount);//API订阅数量TOP5
            result.put("subscribeApiBottom",subscribeApiBottom);//API订阅数量BOTTOM5
            result.put("subscribeApiBottomCount",subscribeApiBottomCount);//API订阅数量BOTTOM5
            result.put("totalInvokeApi",totalInvokeApi);//API累计访问量TOP5
            result.put("totalInvokeApiCount",totalInvokeApiCount);//API累计访问量TOP5
            result.put("todayInvokeApi",todayInvokeApi);//今日API访问量TOP5
            result.put("todayInvokeApiCount",todayInvokeApiCount);//今日API访问量TOP5
        } catch (Exception e) {
            log.error("context",e);
        }
        long end2 = System.currentTimeMillis();
        log.info("==================topApiBarChart2："+(end2-end));
        return new Result<>(Result.OK, "",result);
    }

    /**
     * 获取api订阅列表
     * @param environment 平台环境
     * @param projects 项目
     * @return result
     */
    private List<SubscribeApiProjectInfo> getSubscribeApiProjectInfos(String environment, List<String> projects){
        Map<String, Object> paramMap = new HashMap<>();
        StringBuilder dataSql = new StringBuilder();
        dataSql.append("SELECT api.id AS api_id, ");
        dataSql.append("max(api.name) AS api_name, ");
        dataSql.append("si.id AS api_system_id, ");
        dataSql.append("max(si.name) as SYSTEM_NAME, ");
        dataSql.append("count(DISTINCT app.system) AS app_count ");
        dataSql.append("FROM publish_application app ");
        dataSql.append("INNER JOIN publish_api api ON app.api_id = api.id ");
        dataSql.append("INNER JOIN publish_api_group pag ON api.group_id = pag.id ");
        dataSql.append("INNER JOIN SYSTEM_INFO si on si.id = pag.project_id ");
        dataSql.append("WHERE app.type = 1");//订阅API
//        dataSql.append("AND app.user_key IS NOT NULL ");//有效的订阅
        dataSql.append("AND app.status = 2 ");//已审批通过的订阅
        dataSql.append("AND api.status = 4 ");//API_COMPLETE,已发布的API
        dataSql.append("AND api.environment = :environment ");
        paramMap.put("environment", InstanceEnvironment.fromCode(environment).getCode());
        dataSql.append("AND pag.project_id IN (:projects) ");
        paramMap.put("projects",projects);
        dataSql.append("GROUP BY api.id,si.id");
        Query query = this.entityManager.createNativeQuery(dataSql.toString()).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        this.setParameters(query, paramMap);
        List<Map<String,Object>> resultList = query.getResultList();
        List<SubscribeApiProjectInfo> list = new ArrayList<>();
        for(Map<String,Object> map :resultList){
            SubscribeApiProjectInfo info = new SubscribeApiProjectInfo();
            info.setApiName(map.get("API_NAME")+"-"+map.get("SYSTEM_NAME"));
            info.setSubscribeCount(Integer.parseInt(map.get("APP_COUNT").toString()));
            list.add(info);
        }
        return list;
    }

    private List<String> getProjects(String environment, String project){
        List<String> projects = new ArrayList<>();
        if("undefined".equals(project)){
            List<SystemInfoDto> messageUserSystemInfos = systemInfoService.getSystemInfosOfUser(environment);
            projects = messageUserSystemInfos.stream().map(m -> m.getId().toString()).collect(Collectors.toList());
        }else{
            projects.add( project);
        }
        return projects;
    }

    /**
     * 发布api数量
     * @param environment 运行环境
     * @param projects 项目
     * @return map
     */
    private List<ApiDetailVO> getApiCount(String environment,List<String> projects,List<Integer> status){
        Map<String, Object> paramMap = new HashMap<>();
        StringBuilder dataSql = new StringBuilder();
        dataSql.append("SELECT api.id as API_ID, ");
        dataSql.append("api.name as API_NAME, ");
        dataSql.append("si.id as API_SYSTEM_ID, ");
        dataSql.append("si.name as API_SYSTEM, ");
        dataSql.append("(SELECT max(amr.pattern) FROM API_MAPPING_RULE amr WHERE api.id =amr.api_id) as PATTERN, ");
        dataSql.append("api.HOST, ");
        dataSql.append("api.CREATOR ");
        dataSql.append("FROM publish_api api ");
        dataSql.append("INNER JOIN publish_api_group pag ON api.group_id = pag.id ");
        dataSql.append("INNER join SYSTEM_INFO si on pag.project_id = si.id ");
        dataSql.append("WHERE 1=1 ");
        dataSql.append("and api.status in(:status) ");
        paramMap.put("status", status);
        dataSql.append("AND api.environment = :environment ");
        paramMap.put("environment", InstanceEnvironment.fromCode(environment).getCode());
        dataSql.append("AND pag.project_id IN (:projects) ");
        paramMap.put("projects",projects);
        Query query = this.entityManager.createNativeQuery(dataSql.toString()).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        this.setParameters(query, paramMap);
        List<Map<String,Object>> resultList = query.getResultList();
        List<ApiDetailVO> list = new ArrayList<>();
        for (Map<String,Object> map : resultList){
            ApiDetailVO vo = new ApiDetailVO();
            vo.setApiId(String.valueOf(map.get("API_ID")));
            vo.setApiName(String.valueOf(map.get("API_NAME")));
            vo.setApiSystemId(String.valueOf(map.get("API_SYSTEM_ID")));
            vo.setApiSystem(String.valueOf(map.get("API_SYSTEM")));
            vo.setPattern(String.valueOf(map.get("PATTERN")));
            vo.setHost(String.valueOf(map.get("HOST")));
            vo.setCreator(String.valueOf(map.get("CREATOR")));
            list.add(vo);
        }
        return list;
    }

    /**
     * 累计被订阅
     * @param environment 运行环境
     * @param projects 项目
     * @return map
     */
    private Map<String,Object> getApiSubscribedCount(String environment,List<String> projects){
        Map<String, Object> paramMap = new HashMap<>();
        StringBuilder dataSql = new StringBuilder();
        dataSql.append("SELECT api.id as api_id, ");
        dataSql.append("max(app.id) as app_id, ");
        dataSql.append("max(api.name) as api_name, ");
        dataSql.append("si.id as api_system_id, ");
        dataSql.append("max(si.name) as API_SYSTEM, ");
        dataSql.append("app.system as app_system_id, ");
        dataSql.append("(SELECT di.ITEM_NAME FROM DATA_ITEM di WHERE di.id = app.system and di.group_key='system') as APP_SYSTEM, ");
        dataSql.append("max(app.creator)as creator ");
        dataSql.append("FROM publish_application app ");
        dataSql.append("INNER JOIN publish_api api ON app.api_id = api.id ");
        dataSql.append("INNER JOIN publish_api_group pag ON api.group_id = pag.id ");
        dataSql.append("INNER JOIN SYSTEM_INFO si on si.id = pag.project_id ");
        dataSql.append("WHERE app.type = 1 ");//订阅API
//        dataSql.append("AND app.user_key IS NOT NULL ");//有效的订阅
        dataSql.append("AND app.status = 2 ");//已审批通过的订阅
        dataSql.append("AND api.status = 4 ");//API_COMPLETE,已发布的API
         dataSql.append("AND api.environment = :environment ");
        paramMap.put("environment", InstanceEnvironment.fromCode(environment).getCode());
        dataSql.append("AND pag.project_id IN (:projects) ");
        paramMap.put("projects",projects);
        dataSql.append("GROUP BY api.id,si.id,app.system order by api.id");
        Query query = this.entityManager.createNativeQuery(dataSql.toString()).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        this.setParameters(query, paramMap);
        List<Map<String,Object>> resultList = query.getResultList();
        List<ApiSubscribedVO> list = new ArrayList<>();
        Set<Integer> apiIds = new HashSet<>();
        for(Map<String,Object> map :resultList){
            ApiSubscribedVO vo = new ApiSubscribedVO();
            vo.setApiId(String.valueOf(map.get("API_ID")));
            vo.setAppId(String.valueOf(map.get("APP_ID")));
            vo.setApiName(String.valueOf(map.get("API_NAME")));
            vo.setApiSystemId(String.valueOf(map.get("API_SYSTEM_ID")));
            vo.setApiSystem(String.valueOf(map.get("API_SYSTEM")));
            vo.setAppSystemId(String.valueOf(map.get("APP_SYSTEM_ID")));
            vo.setAppSystem(String.valueOf(map.get("APP_SYSTEM")));
            vo.setSubscriber(String.valueOf(map.get("CREATOR")));
            apiIds.add(Integer.valueOf(vo.getApiId()));
            list.add(vo);
        }
        List apids = new ArrayList<>(apiIds);
        List<PublishApplication> publishApplications = publishApplicationRepository.queryAppByApiIds(apids);
        TimeQuery timeQuery = new TimeQuery();
        timeQuery.setStart(DateUtil.getStartTime(0));
        timeQuery.setEnd(DateUtil.getEndTime(0));
        long start = System.currentTimeMillis();
        Result<List<String>> listResult = elasticSearchService.queryIndexList(timeQuery);
        Result<Long> success = elasticSearchService.queryCount(listResult.getData(),timeQuery,publishApplications ,1,true);
        Result<Long> fail = elasticSearchService.queryCount(listResult.getData(),timeQuery,publishApplications, 2,true);
        long end = System.currentTimeMillis();
        log.info("==================getApiSubscribedCount："+(end-start));
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("arrays",list);
        resultMap.put("successCount",success.getData());
        resultMap.put("failCount",fail.getData());
        return resultMap;
    }



    /**
     * 待处理申请
     * @param environment 运行环境
     * @param projects 项目
     * @return
     */
    private Integer getApplicationApplyCount(String environment,List<String> projects){
        Map<String, Object> paramMap = new HashMap<>();
        StringBuilder dataSql = new StringBuilder();
        dataSql.append(
                "SELECT \n" +
                    "count(DISTINCT app.api_id||app.SYSTEM) count\n" +
                    "FROM \n" +
                    "publish_application app \n" +
                    "INNER JOIN publish_api api ON app.api_id = api.id\n" +
                    "INNER JOIN publish_api_group pag ON api.group_id = pag.id\n" +
                    "WHERE app.status = 1 and(( app.type=0 and api.status in (2,3)) or(app.type=1 and api.status=4)) ");
        dataSql.append("AND api.environment = :environment ");
        paramMap.put("environment", InstanceEnvironment.fromCode(environment).getCode());
        dataSql.append("AND pag.project_id IN (:projects) ");
        paramMap.put("projects",projects);
        Query countQuery = this.entityManager.createNativeQuery(dataSql.toString());
        this.setParameters(countQuery, paramMap);
        BigDecimal count = (BigDecimal) countQuery.getSingleResult();
        long total = count.longValue();
        return Integer.parseInt(String.valueOf(total));
    }

    /**
     * 累计访问量
     * @param environment 运行环境
     * @param projects 项目
     * @return 累计访问量
     */
    private Map<String,List<ApiInvokeInfo>> getInvokeCountApiProjectCount(String environment,List<String> projects) {
        List<ApiInvokeInfo> apiInvokeInfos = querySubscribedApiList(environment, projects);
        List<Integer> apiIds = apiInvokeInfos.stream().map(v -> v.getApiId()).distinct().collect(Collectors.toList());
        LocalDate now = LocalDate.now();
        String toDay = now.format(DateTimeFormatter.ISO_DATE);
        Map<Integer,Integer> todayApiInvokeData = new HashMap<>();
        Map<Integer,Integer> totalApiInvokeData = new HashMap<>();
        List<JSONObject> todayData = apiInvokeRecordRepository.queryApiInvokeValue(toDay, apiIds);
        for(JSONObject obj : todayData){
            Integer api_id = Integer.valueOf(String.valueOf(obj.get("API_ID")));
            Integer total = Integer.valueOf(String.valueOf(obj.get("TOTAL")));
            todayApiInvokeData.put(api_id,total);
        }
        List<JSONObject> totalData = apiInvokeRecordRepository.queryApiInvokeTotalValue(apiIds);
        for (JSONObject obj : totalData){
            Integer api_id = Integer.valueOf(String.valueOf(obj.get("API_ID")));
            Integer total = Integer.valueOf(String.valueOf(obj.get("TOTAL")));
            totalApiInvokeData.put(api_id,total);
        }

        List<ApiInvokeInfo> todayInfos = new ArrayList<>();
        for (ApiInvokeInfo info :apiInvokeInfos) {
            ApiInvokeInfo todayInfo = new ApiInvokeInfo();
            BeanUtils.copyProperties(info,todayInfo);
            todayInfo.setInvokeCount(todayApiInvokeData.get(info.getApiId())==null?0:todayApiInvokeData.get(info.getApiId()));
            info.setInvokeCount(totalApiInvokeData.get(info.getApiId())==null?0:totalApiInvokeData.get(info.getApiId()));
            todayInfos.add(todayInfo);
        }
        Map<String,List<ApiInvokeInfo>> result = new HashMap<>();
        result.put("today",todayInfos);
        result.put("total",apiInvokeInfos);
        return result;
    }

    private List<ApiInvokeInfo> querySubscribedApiList(String environment, List<String> project) {
        Map<String, Object> params = new HashMap<>();

        StringBuilder dataSql = new StringBuilder();
        dataSql.append("SELECT api.id as API_ID, ");
        dataSql.append("max(api.name) as api_name, ");
        dataSql.append("si.id as system_id, ");
        dataSql.append("max(si.name) as SYSTEM_NAME ");
        dataSql.append("FROM publish_application app ");
        dataSql.append("INNER JOIN publish_api api ON app.api_id = api.id ");
        dataSql.append("INNER JOIN publish_api_group pag ON api.group_id = pag.id ");
        dataSql.append("INNER JOIN SYSTEM_INFO si on si.id = pag.project_id ");
        dataSql.append("WHERE 1=1 ");
        dataSql.append("AND app.type = 1 ");// 订阅API
//        dataSql.append("AND app.user_key IS NOT NULL ");// 有效的订阅
        dataSql.append("AND app.status = 2 ");// 已审批通过的订阅
        dataSql.append("AND api.status = 4 ");// API_COMPLETE,已发布的API
        if (MiscUtil.isNotEmpty(environment)) {
            dataSql.append("AND api.environment = :environment ");
            params.put("environment", InstanceEnvironment.fromCode(environment).getCode());
        }
        if (project != null) {
            dataSql.append("AND pag.project_id in(:project) ");
            params.put("project", project);
        }
        dataSql.append("GROUP BY api.id,si.id");

        Query dataQuery = entityManager.createNativeQuery(dataSql.toString());
        dataQuery.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        setParameters(dataQuery,params);
        List<Map<String, Object>> mapList = dataQuery.getResultList();
        List<ApiInvokeInfo> apiInvokeInfos = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            ApiInvokeInfo info = new ApiInvokeInfo();
            info.setApiId(Integer.parseInt(map.get("API_ID").toString()));
            info.setApiName(map.get("API_NAME").toString());
            info.setApiName(map.get("API_NAME")+"-"+map.get("SYSTEM_NAME"));
            apiInvokeInfos.add(info);

        }
        return apiInvokeInfos;
    }

    /**
     * 给hql参数设置值
     *
     * @param query  查询
     * @param params 参数
     */
    private void setParameters(Query query, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
    }


}
