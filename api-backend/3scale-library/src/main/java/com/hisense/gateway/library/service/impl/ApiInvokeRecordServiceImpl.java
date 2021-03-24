package com.hisense.gateway.library.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.constant.AnalyticsConstant;
import com.hisense.gateway.library.model.base.TimeQuery;
import com.hisense.gateway.library.model.base.analytics.ApiTrafficStatQuery;
import com.hisense.gateway.library.model.pojo.base.ApiInvokeRecord;
import com.hisense.gateway.library.model.pojo.base.Instance;
import com.hisense.gateway.library.model.pojo.base.PublishApiInstanceRelationship;
import com.hisense.gateway.library.model.pojo.base.PublishApplication;
import com.hisense.gateway.library.repository.ApiInvokeRecordRepository;
import com.hisense.gateway.library.repository.PublishApiInstanceRelationshipRepository;
import com.hisense.gateway.library.repository.PublishApplicationRepository;
import com.hisense.gateway.library.service.ApiInvokeRecordService;
import com.hisense.gateway.library.stud.AccountStud;
import com.hisense.gateway.library.stud.AnalyticsStud;
import com.hisense.gateway.library.stud.model.AnalyticsDto;
import com.hisense.gateway.library.stud.model.ApplicationXml;
import com.hisense.gateway.library.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static com.hisense.gateway.library.constant.AnalyticsConstant.StatType.INVOKE_BY_SINGLE_SUB_SYSTEM;

/**
 * @ClassName: ApiInvokeRecordServiceImpl
 * @Description: TOOD
 * @Author: zhouguokai.ex
 * @createDate: 2021/1/18
 * @version: 1.0
 */
@Slf4j
@Service
public class ApiInvokeRecordServiceImpl implements ApiInvokeRecordService {

    @Autowired
    PublishApiInstanceRelationshipRepository publishApiInstanceRelationshipRepository;

    @Autowired
    AnalyticsStud analyticsStud;

    @Autowired
    ApiInvokeRecordRepository apiInvokeRecordRepository;

    @Autowired
    PublishApplicationRepository publishApplicationRepository;

    @Autowired
    AccountStud accountStud;

    @Async
    @Transactional
    @Override
    public void saveInvokeCount(PublishApplication app) {
        Instance instance = app.getInstance();
        PublishApiInstanceRelationship scaleApi = publishApiInstanceRelationshipRepository.getByAPIidAndInstanceId(app.getPublishApi().getId(), instance.getId());
        if(scaleApi!=null){
            Integer scaleAppId = Integer.valueOf(app.getScaleApplicationId());
            TimeQuery timeQuery = new TimeQuery();
            String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            timeQuery.setStart(DateUtil.asDate(today+" 00:00:00"));
            timeQuery.setEnd(DateUtil.asDate(today+" 23:59:59"));
            ApiTrafficStatQuery statQuery = new ApiTrafficStatQuery();
            statQuery.setTimeQuery(timeQuery);
            statQuery.setApiId(Integer.valueOf(scaleApi.getScaleApiId().toString()));
            statQuery.setGranularity(AnalyticsConstant.StatGranularity.EXACT_DURATION_DAYS.getCode());
            statQuery.setStatType(INVOKE_BY_SINGLE_SUB_SYSTEM.getCode());
            statQuery.setAppId(scaleAppId);
            AnalyticsDto analyticsDto = analyticsStud.serviceAnalytics(instance.getHost(), instance.getAccessToken(), statQuery);
            if(analyticsDto!=null&&isInteger(analyticsDto.getTotal())&&!"0".equals(analyticsDto.getTotal())){
                apiInvokeRecordRepository.deleteInfoByInvokeDay(LocalDate.now().toString(), app.getPublishApi().getId(),scaleAppId);
                ApiInvokeRecord apiInvoke = new ApiInvokeRecord();
                apiInvoke.setInvokeDay(LocalDate.now().toString());
                apiInvoke.setApiId(app.getPublishApi().getId());
                apiInvoke.setScaleApiId(Integer.valueOf(scaleApi.getScaleApiId().toString()));
                apiInvoke.setScaleApplicationId(scaleAppId);
                apiInvoke.setSystem(app.getSystem());
                apiInvoke.setInvokeCount(Integer.valueOf(analyticsDto.getTotal()));
                apiInvoke.setUpdateTime(new Date());
                apiInvokeRecordRepository.save(apiInvoke);
            }
        }
    }

    @Async
    @Transactional
    @Override
    public void updateApiIdAndAppKey(Integer id) {
        JSONObject obj = publishApplicationRepository.queryApplicationById(id);
        if(obj!=null && obj.get("APP_ID")!=null&&obj.get("APP_KEY")!=null){
            ApplicationXml applicationXml = accountStud.appXml(
                    String.valueOf(obj.get("HOST")),
                    String.valueOf(obj.get("ACCESS_TOKEN")),
                    String.valueOf(obj.get("ACCOUNT_ID")),
                    String.valueOf(obj.get("SCALE_APPLICATION_ID")));
            if(null != applicationXml){
                log.info("********read application,{}"+ JSONObject.toJSON(applicationXml).toString());
                if(applicationXml.getApplicationId()!=null
                        && null != applicationXml.getKeys()
                        && !CollectionUtils.isEmpty(applicationXml.getKeys().getKey())){
                    publishApplicationRepository.updateAppIdAndAppKey(id,
                            applicationXml.getApplicationId(),
                            applicationXml.getKeys().getKey().get(0));
                }
            }
        }
    }

    @Async
    @Transactional
    @Override
    public void updateApiIdAndAppKeyByApiId(Integer apiId) {
        List<JSONObject> jsonObjects = publishApplicationRepository.queryApplicationByApiId(apiId);
        for(JSONObject obj : jsonObjects){
            if(obj!=null && obj.get("APP_ID")!=null&&obj.get("APP_KEY")!=null){
                ApplicationXml applicationXml = accountStud.appXml(
                        String.valueOf(obj.get("HOST")),
                        String.valueOf(obj.get("ACCESS_TOKEN")),
                        String.valueOf(obj.get("ACCOUNT_ID")),
                        String.valueOf(obj.get("SCALE_APPLICATION_ID")));
                if(null != applicationXml){
                    log.info("********read application,{}"+ JSONObject.toJSON(applicationXml).toString());
                    if(applicationXml.getKeys()!=null
                            && !CollectionUtils.isEmpty(applicationXml.getKeys().getKey())
                            &&applicationXml.getApplicationId()!=null){
                        publishApplicationRepository.updateAppIdAndAppKey(Integer.valueOf(String.valueOf(obj.get("APP_ID"))),
                                applicationXml.getApplicationId(),
                                applicationXml.getKeys().getKey().get(0));
                    }
                }
            }
        }
    }

    @Async
    @Transactional
    @Override
    public void updateApiIdAndAppKey(Integer id, String appId, String appKey) {
        publishApplicationRepository.updateAppIdAndAppKey(id, appId, appKey);
    }

    private boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }
}
