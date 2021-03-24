package com.hisense.gateway.management.xxljob;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.constant.AnalyticsConstant;
import com.hisense.gateway.library.model.base.TimeQuery;
import com.hisense.gateway.library.model.base.analytics.ApiTrafficStatQuery;
import com.hisense.gateway.library.model.pojo.base.ApiInvokeRecord;
import com.hisense.gateway.library.repository.ApiInvokeRecordRepository;
import com.hisense.gateway.library.stud.AnalyticsStud;
import com.hisense.gateway.library.stud.model.AnalyticsDto;
import com.hisense.gateway.library.utils.DateUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.hisense.gateway.library.constant.AnalyticsConstant.StatType.INVOKE_ALL;
import static com.hisense.gateway.library.constant.AnalyticsConstant.StatType.INVOKE_BY_SINGLE_SUB_SYSTEM;

/**
 * @ClassName: ApiInvokeJobHandler
 * @Description: TOOD
 * @Author: zhouguokai.ex
 * @createDate: 2020/11/20
 * @version: 1.0
 */
@JobHandler(value = "apiInvokeJobHandler")
@Component
public class ApiInvokeRecordJobHandler extends IJobHandler{

    public final static int BATCH_NUMBER = 1000;

    @Autowired
    ApiInvokeRecordRepository apiInvokeRecordRepository;

    @Autowired
    AnalyticsStud analyticsStud;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        XxlJobLogger.log("XXL-JOB,同步3scale-API调用量-开始。。。。。");
            apiInvokeRecordHistoryJob(s);
        XxlJobLogger.log("XXL-JOB,同步3scale-API调用量-结束。。。。。");
        return SUCCESS;
    }

    public void apiInvokeRecordHistoryJob(String s){
        LocalDateTime now = LocalDateTime.now();
        String startDate=now.format(DateTimeFormatter.ISO_DATE);
        String endDate=now.format(DateTimeFormatter.ISO_DATE);
        if(!StringUtils.isEmpty(s)&&s.split("~").length==2){
            String[] split = s.split("~");
            startDate = split[0];
            endDate = split[1];
        }else if(0==now.getHour()){
            startDate = now.minusDays(1).format(DateTimeFormatter.ISO_DATE);
        }
        List<String> betweenDate = DateUtil.getBetweenDate(startDate, endDate);
        List<JSONObject> jsonObjects = apiInvokeRecordRepository.queryApiInvokeHistiryList();
        if(!CollectionUtils.isEmpty(jsonObjects)) {
            TimeQuery timeQuery = new TimeQuery();
            List<ApiInvokeRecord> apiInvokes = new ArrayList<>();
            Date startTime = DateUtil.asDate(startDate+" 00:00:00");
            Date endTime = DateUtil.asDate(endDate+" 23:59:59");
            timeQuery.setStart(startTime);
            timeQuery.setEnd(endTime);
            ApiTrafficStatQuery statQuery = new ApiTrafficStatQuery();
            statQuery.setTimeQuery(timeQuery);
            for (JSONObject obj : jsonObjects) {
                Integer scaleApiId = Integer.valueOf(String.valueOf(obj.get("SCALE_API_ID")));
                Integer apiId = Integer.valueOf(String.valueOf(obj.get("API_ID")));
                String host = obj.getString("HOST");
                if(obj.get("SCALE_APPLICATION_ID")!=null){
                    Integer scaleApplicationId = Integer.valueOf(String.valueOf(obj.get("SCALE_APPLICATION_ID")));
                    Integer system = Integer.valueOf(String.valueOf(obj.get("SYSTEM")));
                    String accessToken = obj.getString("ACCESS_TOKEN");
                    statQuery.setApiId(scaleApiId);
                    statQuery.setGranularity(AnalyticsConstant.StatGranularity.EXACT_DURATION_DAYS.getCode());
                    statQuery.setStatType(INVOKE_BY_SINGLE_SUB_SYSTEM.getCode());
                    statQuery.setAppId(scaleApplicationId);
                    AnalyticsDto analyticsDto = analyticsStud.serviceAnalytics2(host, accessToken, statQuery);
                    List<String> values = analyticsDto.getValues();
                    if (values!=null&&betweenDate.size()==values.size()) {
                        for(int i=0;i<betweenDate.size();i++){
                            apiInvokeRecordRepository.deleteInfoByInvokeDay(betweenDate.get(i), apiId,scaleApplicationId);
                            if(Integer.valueOf(values.get(i))>0){
                                ApiInvokeRecord apiInvoke = new ApiInvokeRecord();
                                apiInvoke.setApiId(apiId);
                                apiInvoke.setInvokeDay(betweenDate.get(i));
                                apiInvoke.setScaleApiId(scaleApiId);
                                apiInvoke.setScaleApplicationId(scaleApplicationId);
                                apiInvoke.setSystem(system);
                                apiInvoke.setInvokeCount(Integer.valueOf(values.get(i)));
                                apiInvoke.setUpdateTime(new Date());
                                apiInvokeRecordRepository.save(apiInvoke);
                                XxlJobLogger.log(JSON.toJSONString(apiInvoke));
//                                apiInvokes.add(apiInvoke);
                            }
//                            if (apiInvokes.size() >= BATCH_NUMBER) {
//                                apiInvokeRecordRepository.saveAll(apiInvokes);
//                                apiInvokes.clear();
//                            }
                        }
                    }
                }
            }
//            if (apiInvokes.size() > 0) {
//                apiInvokeRecordRepository.saveAll(apiInvokes);
//                apiInvokes.clear();
//            }
        }
    }

//        @Scheduled(cron = "0 42 10 * * ?")
    public void apiInvokeRecordJob(){
        String s = "2020-01-01~2021-01-12";
        apiInvokeRecordHistoryJob(s);
    }
}
