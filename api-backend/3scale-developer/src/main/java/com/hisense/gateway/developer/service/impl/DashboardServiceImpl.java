package com.hisense.gateway.developer.service.impl;

import com.hisense.gateway.developer.service.DashboardService;
import com.hisense.gateway.library.beans.CommonBeanUtil;
import com.hisense.gateway.library.constant.AnalyticsConstant;
import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.DashboardModels;
import com.hisense.gateway.library.model.base.DashboardModels.ApiMarketOverview;
import com.hisense.gateway.library.model.base.TimeQuery;
import com.hisense.gateway.library.model.base.analytics.ApiTrafficStatQuery;
import com.hisense.gateway.library.model.dto.web.ProcessRecordQuery;
import com.hisense.gateway.library.model.pojo.base.PublishApplication;
import com.hisense.gateway.library.repository.ApiInvokeRecordRepository;
import com.hisense.gateway.library.repository.PublishApplicationRepository;
import com.hisense.gateway.library.service.ElasticSearchService;
import com.hisense.gateway.library.stud.AnalyticsStud;
import com.hisense.gateway.library.stud.model.AnalyticsDto;
import com.hisense.gateway.library.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.hisense.gateway.library.constant.AnalyticsConstant.StatType;
import static com.hisense.gateway.library.constant.AnalyticsConstant.StatType.INVOKE_BY_SINGLE_SUB_SYSTEM;

/**
 * @ClassName: DashboradServiceImpl
 * @Description: TOOD
 * @Author: zhouguokai.ex
 * @createDate: 2020/12/24
 * @version: 1.0
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    AnalyticsStud analyticsStud;
    @Autowired
    ElasticSearchService elasticSearchService;

    @Autowired
    ApiInvokeRecordRepository apiInvokeRecordRepository;

    @Autowired
    PublishApplicationRepository publishApplicationRepository;

    @Override
    public Page<ApiMarketOverview> apiMarketOverview(String environment, ProcessRecordQuery param) {
        StringBuilder dataSql = new StringBuilder();
        StringBuilder countSql = new StringBuilder("SELECT count(1) FROM ");
        dataSql.append("SELECT\n");
        dataSql.append("api.id api_id,\n");
        dataSql.append("max(api.NAME) api_name,\n");
        dataSql.append("si.id as api_system_id,\n");
        dataSql.append("max(si.name) as API_SYSTEM,\n");
        dataSql.append("app.system as app_system_id,\n");
        dataSql.append("(SELECT max(di.ITEM_NAME) FROM DATA_ITEM di WHERE di.id = app.system and di.group_key='system') as APP_SYSTEM,\n");
        dataSql.append("nvl(sum(ai.INVOKE_COUNT),0) INVOKE_total_COUNT,\n");
        dataSql.append("nvl(sum(air.INVOKE_COUNT),0) INVOKE_day_COUNT\n");
        dataSql.append("FROM PUBLISH_APPLICATION app\n");
        dataSql.append("INNER JOIN PUBLISH_API api ON app.API_ID = api.id\n");
        dataSql.append("INNER JOIN publish_api_group pag ON api.group_id = pag.id\n");
        dataSql.append("INNER JOIN SYSTEM_INFO si on si.id = pag.project_id\n");
        dataSql.append("LEFT JOIN (SELECT api_id,SYSTEM,sum(INVOKE_COUNT) INVOKE_COUNT FROM API_INVOKE_RECORD GROUP BY api_id,SYSTEM) ai ON ai.api_id=app.API_ID AND  ai.SYSTEM =app.system\n");
        dataSql.append("LEFT JOIN (SELECT api_id,SYSTEM,sum(INVOKE_COUNT) INVOKE_COUNT FROM API_INVOKE_RECORD WHERE INVOKE_DAY=to_char(sysdate,'yyyy-mm-dd') GROUP BY api_id,SYSTEM) air ON air.api_id=app.API_ID AND  air.SYSTEM =app.system\n");
        dataSql.append("WHERE 1=1\n");
        dataSql.append("AND app.STATUS =2\n");
        dataSql.append("AND app.type = 1\n");
        dataSql.append("AND api.STATUS = 4\n");
        dataSql.append("AND app.creator = :creator \n");
        dataSql.append("AND api.environment = :environment \n");
        Map<String, Object> paramMap = new HashMap<>();
        if(StringUtils.isNotEmpty(param.getApiName())){
            dataSql.append("AND api.NAME like :apiName \n");
            paramMap.put("apiName", "%" + param.getApiName() + "%");
        }
        dataSql.append("GROUP BY api.id,si.id,app.system order by INVOKE_TOTAL_COUNT desc");
        String user = CommonBeanUtil.getLoginUserName();
        paramMap.put("creator",user);
        paramMap.put("environment", InstanceEnvironment.fromCode(environment).getCode());
        Query query = this.entityManager.createNativeQuery(dataSql.toString()).unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        this.setParameters(query, paramMap);
        countSql.append("(").append(dataSql).append(")tab");
        Query countQuery = entityManager.createNativeQuery(countSql.toString());
        this.setParameters(countQuery, paramMap);
        PageRequest pageable = PageRequest.of(param.getPage() - 1, param.getSize());
        query.setFirstResult(new Long(pageable.getOffset()).intValue());
        query.setMaxResults(pageable.getPageSize());

        BigDecimal count = (BigDecimal) countQuery.getSingleResult();
        long total = count.longValue();
        List<Map<String,Object>> resultList = total > pageable.getOffset() ? query.getResultList() : Collections.emptyList();
        TimeQuery timeQuery = new TimeQuery();
        timeQuery.setStart(DateUtil.getStartTime(0));
        timeQuery.setEnd(DateUtil.getEndTime(0));
        Result<List<String>> listResult = elasticSearchService.queryIndexList(timeQuery);
        List<ApiMarketOverview> list = new ArrayList<>();
        List<String> apiIds = new ArrayList<>();
        List<String> appSystemId = new ArrayList<>();
        for(Map<String,Object> map :resultList){
            ApiMarketOverview overview = new ApiMarketOverview();
            overview.setApiId(String.valueOf(map.get("API_ID")));
            overview.setAppSystemId(String.valueOf(map.get("APP_SYSTEM_ID")));
            overview.setApiName(String.valueOf(map.get("API_NAME")));
            overview.setApiSystem(String.valueOf(map.get("API_SYSTEM")));
            overview.setAppSystem(String.valueOf(map.get("APP_SYSTEM")));
            overview.setApiInvokeTotalCount(Integer.valueOf(map.get("INVOKE_TOTAL_COUNT").toString()));
            overview.setApiInvokeDayCount(Integer.valueOf(map.get("INVOKE_DAY_COUNT").toString()));
            apiIds.add(String.valueOf(map.get("API_ID")));
            appSystemId.add(String.valueOf(map.get("APP_SYSTEM_ID")));
            List<PublishApplication> publishApplications = publishApplicationRepository.queryAppByApiIdAndSystem(Integer.valueOf(String.valueOf(map.get("APP_SYSTEM_ID"))),Integer.valueOf(String.valueOf(map.get("API_ID"))));
            Result<Long> success = elasticSearchService.queryCount(listResult.getData(),timeQuery,publishApplications,1,false);
            Result<Long> fail = elasticSearchService.queryCount(listResult.getData(),timeQuery,publishApplications,2,false);
            overview.setApiInvokeSuccessDayount(Integer.valueOf(success.getData().toString()));
            overview.setApiInvokeFailDayount(Integer.valueOf(fail.getData().toString()));
            list.add(overview);
        }
        return new PageImpl(list, pageable, total);
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
