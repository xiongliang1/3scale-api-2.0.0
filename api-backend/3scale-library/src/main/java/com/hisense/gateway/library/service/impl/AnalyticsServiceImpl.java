/*
 * 2020-09-06 @author guilai.ming
 */
package com.hisense.gateway.library.service.impl;

import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.constant.AnalyticsConstant;
import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.constant.InstancePartition;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.SubscribeSystem;
import com.hisense.gateway.library.model.base.TimeQuery;
import com.hisense.gateway.library.model.base.analytics.ApiTrafficStatQuery;
import com.hisense.gateway.library.model.base.monitor.ApiCastLog;
import com.hisense.gateway.library.model.base.monitor.ApiLogQuerySpecial;
import com.hisense.gateway.library.model.base.monitor.ApiResponseStatistics;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.library.service.AnalyticsService;
import com.hisense.gateway.library.service.ElasticSearchService;
import com.hisense.gateway.library.stud.AnalyticsStud;
import com.hisense.gateway.library.stud.model.AnalyticsDto;
import com.hisense.gateway.library.stud.model.AnalyticsPeriod;
import com.hisense.gateway.library.stud.model.Metric;
import com.hisense.gateway.library.utils.DateUtil;
import com.hisense.gateway.library.utils.api.AnalyticsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hisense.gateway.library.constant.AnalyticsConstant.StatType.*;
import static com.hisense.gateway.library.constant.BaseConstants.TAG;
import static com.hisense.gateway.library.model.ModelConstant.API_DELETE;

@Slf4j
@Service
public class AnalyticsServiceImpl implements AnalyticsService {
    @Autowired
    AnalyticsStud analyticsStud;

    @Autowired
    InstanceRepository instanceRepository;

    @Autowired
    DataItemRepository dataItemRepository;

    @Autowired
    PublishApiRepository publishApiRepository;

    @Autowired
    ProcessRecordRepository processRecordRepository;

    @Autowired
    PublishApplicationRepository publishApplicationRepository;

    @Autowired
    PublishApiInstanceRelationshipRepository publishApiInstanceRelationshipRepository;

    @Autowired
    ElasticSearchService elasticSearchService;

    @Override
    public Result<AnalyticsDto> getServiceAnalytics(ApiTrafficStatQuery statQuery) {
        Result<AnalyticsDto> result = new Result<>(Result.FAIL, "", null);

        log.info("statQuery1 {}", statQuery);

        String msg;
        if (!Result.OK.equals(msg = statQuery.isValid())) {
            log.error("{}{}", TAG, msg);
            result.setMsg(msg);
            return result;
        }

        PublishApi publishApi;
        if ((publishApi = publishApiRepository.findOne(statQuery.getApiId())) == null ||
                publishApi.getStatus().equals(API_DELETE)) {
            result.setCode(Result.OK);
            result.setMsg("API不存在 或者 API已删除");
            return result;
        }

        String partition = InstancePartition.fromCode(publishApi.getPartition()).getName();

        Instance instance = instanceRepository.searchInstanceByPartitionEnvironment(partition,statQuery.getEnvironment());
        if (instance == null) {
            result.setMsg("3scale实例不存在");
            return result;
        }

        PublishApiInstanceRelationship relationship =
                publishApiInstanceRelationshipRepository.getByAPIidAndInstanceId(
                        statQuery.getApiId(), instance.getId());
        if (relationship == null) {
            result.setMsg("3scale实例上不存在对应的API");
            return result;
        }

        statQuery.setApiId(Math.toIntExact(relationship.getScaleApiId()));
        AnalyticsUtil.fixTimeQuery(statQuery, false);

        AnalyticsDto analyticsDto;
        if (statQuery.getStatType().equals(INVOKE_BY_ALL_SUB_SYSTEM)) {
            PublishApplication defaultApp = publishApplicationRepository.findByPublishApiId(publishApi.getId(), instance.getId());

            if (defaultApp == null) {
                result.setCode(Result.OK);
                result.setMsg("当前API默认的APP不存在");
                return result;
            }

            statQuery.setStatType(INVOKE_ALL.getCode());
            analyticsDto = analyticsStud.serviceAnalytics(instance.getHost(), instance.getAccessToken(), statQuery);

            if (analyticsDto == null) {
                result.setCode(Result.OK);
                result.setMsg("当前3scale实例上此API无调用记录");
                return result;
            }

            statQuery.setAppId(Integer.parseInt(defaultApp.getScaleApplicationId()));
            statQuery.setStatType(INVOKE_BY_SINGLE_SUB_SYSTEM.getCode());
            AnalyticsDto analyticsSubDto = analyticsStud.serviceAnalytics(instance.getHost(),
                    instance.getAccessToken(), statQuery);
            analyticsDto = analyticsDto.subtraction(analyticsSubDto);
        } else if(statQuery.getStatType().equals(INVOKE_BY_SINGLE_SUB_SYSTEM)){
            analyticsDto = analyticsStud.serviceAnalytics(instance.getHost(), instance.getAccessToken(), statQuery);
        } else {
            //3scale暂不支持查询接口异常，改用ES查处接口异常
            statQuery.setApiId(publishApi.getId());
            analyticsDto = respExceptionStatics(statQuery);
        }

        if (analyticsDto == null) {
            result.setCode(Result.OK);
            result.setMsg("当前3scale实例上此API无调用记录");
            return result;
        }

        result.setCode(Result.OK);
        result.setMsg("");
        result.setData(analyticsDto);
        return result;
    }

    /**
     * API监控数据下载
     * @param statQuery
     * @return
     * @auth liyouzhi.ex-20200915
     */
    @Override
    public XSSFWorkbook downloadApiStatisticsData(ApiTrafficStatQuery statQuery) {
        if(statQuery.getStatType().getCode()== RESPONSE_TIME_PERCENTILE_FOR_ALL.getCode() ||
                statQuery.getStatType().getCode()==RESPONSE_TIME_PERCENTILE_FOR_APP.getCode()){
            return apiResponseStatistics(statQuery);
        }else{
            return publicAnalyTicsData(statQuery);
        }

    }

    /**
     * 网络延迟百分比数据下载
     * @param statQuery
     * @return
     */
    public XSSFWorkbook apiResponseStatistics(ApiTrafficStatQuery statQuery){
        XSSFWorkbook workbook= new XSSFWorkbook();
        Result<ApiResponseStatistics> result = elasticSearchService.queryForStatistics(statQuery);
        if(Result.OK.equals(result.getCode())) {
            ApiResponseStatistics resultData = result.getData();
            if(null != resultData && null != resultData.getValues()){
                //结果集
                List<ApiResponseStatistics.ResponseStat> values = resultData.getValues();
                AnalyticsPeriod period = resultData.getPeriod();
                String util = period.getGranularity();
                XSSFSheet sheet = workbook.createSheet(statQuery.getStatType().getName());
                sheet.setDefaultColumnWidth(20);
                //声明表头
                XSSFRow headrow = sheet.createRow(0);
                //创建一个单元格
                XSSFCell cell0 = headrow.createCell(0);
                cell0.setCellValue("统计频次（"+util+")");
                if(!CollectionUtils.isEmpty(values.get(0).getMap())){
                    List<String> keys =  new ArrayList<>(values.get(0).getMap().keySet());
                    for(int i=0;i<keys.size();i++ ){
                        XSSFCell cell1 = headrow.createCell(1+i);
                        cell1.setCellValue(keys.get(i));
                    }
                }

                for (int i = 0; i < values.size(); i++) {
                    XSSFRow row = sheet.createRow(i + 1);
                    ApiResponseStatistics.ResponseStat responseStat = values.get(i);
                    String date = responseStat.getDate();
                    Map<String, Integer> map = responseStat.getMap();
                    XSSFCell cellZreo = row.createCell(0);
                    cellZreo.setCellValue(date);
                    if(!CollectionUtils.isEmpty(map)){
                        for(int j = 0; j < map.size(); j++){
                            XSSFCell cellOne = row.createCell(1+j);
                            cellOne.setCellValue(map.get(headrow.getCell(1+j).getStringCellValue()));
                        }
                    }
                }
            }
        }
        return workbook;
    }

    /**
     * 总量和响应状态数据下载
     * @param statQuery
     * @return
     */
    public XSSFWorkbook publicAnalyTicsData(ApiTrafficStatQuery statQuery){
        XSSFWorkbook workbook= new XSSFWorkbook();
        Result<AnalyticsDto> ResultData = getServiceAnalytics(statQuery);
        if(Result.OK.equals(ResultData.getCode())) {
            AnalyticsDto resultData = ResultData.getData();
            if(null != resultData && null != resultData.getValues()){
                //结果集
                List<String> values = resultData.getValues();
                AnalyticsPeriod period = resultData.getPeriod();
                String util = period.getGranularity();
                XSSFSheet sheet = workbook.createSheet(statQuery.getStatType().getName());
                sheet.setDefaultColumnWidth(20);
                //声明表头
                XSSFRow headrow = sheet.createRow(0);
                //创建一个单元格
                XSSFCell cell0 = headrow.createCell(0);
                cell0.setCellValue("统计频次（"+util+")");
                XSSFCell cell1 = headrow.createCell(1);
                cell1.setCellValue("数量");

                List<String> times = getTimes(period);
                for (int i = 0; i < values.size(); i++) {
                    XSSFRow row = sheet.createRow(i + 1);
                    XSSFCell cellZreo = row.createCell(0);
                    cellZreo.setCellValue(times.get(i));
                    XSSFCell cellOne = row.createCell(1);
                    cellOne.setCellValue(values.get(i));
                }
            }
        }
        return workbook;
    }

    /**
     * 根据条件获取时间集合
     * @param period
     * @return
     */
    public  List<String> getTimes(AnalyticsPeriod period){
        List<String> times = new ArrayList<>();
        if(null != period && StringUtils.isNotBlank(period.getGranularity())){
            String since = DateUtil.formatUtcTime(period.getSince());
            String entail = DateUtil.formatUtcTime(period.getUntil());
            switch (period.getGranularity()){
                case "month":
                    times =  DateUtil.getMonths(since,entail);
                    break;
                case "day":
                    times =  DateUtil.getDays(since,entail);
                    break;
                case "hour":
                    times =  DateUtil.getHours(since,entail);
                    break;
            }
        }
        return times;
    }

    @Override
    public Result<List<SubscribeSystem>> getSubscribeSystems(Integer apiId) {
        Result<List<SubscribeSystem>> result = new Result<>(Result.FAIL, "", null);

        PublishApi publishApi;
        if ((publishApi = publishApiRepository.findOne(apiId)) == null || publishApi.getStatus().equals(API_DELETE)) {
            log.error("API不存在 或者 API已删除");
            return result;
        }

        List<PublishApplication> subscribedApps =
                publishApplicationRepository.findSubscribedAppByApiId(apiId);
        if (MiscUtil.isEmpty(subscribedApps)) {
            log.info("{}当前API未被订阅过", TAG);
            result.setCode(Result.OK);
            return result;
        }

        result.setCode(Result.OK);
        result.setData(subscribedApps.stream()
                .map(item -> new SubscribeSystem(Integer.parseInt(item.getScaleApplicationId()),item.getId(),
                        dataItemRepository.findOne(item.getSystem()).getItemName())).collect(Collectors.toList()));
        return result;
    }

    @Override
    public Result<SubscribeSystem> getSubscribeSystems(Integer apiId, Integer recordId) {
        Result<SubscribeSystem> result = new Result<>(Result.FAIL, "", null);

        PublishApi publishApi;
        if ((publishApi = publishApiRepository.findOne(apiId)) == null || publishApi.getStatus().equals(API_DELETE)) {
            log.error("API不存在 或者 API已删除");
            return result;
        }

        ProcessRecord processRecord = processRecordRepository.findOne(recordId);
        if(processRecord == null || processRecord.getStatus().equals(API_DELETE)){
            log.error("API订阅记录不存在 或 API订阅记录已删除");
            return result;
        }

        Integer appId = processRecord.getRelId();

        PublishApplication subscribedApp = publishApplicationRepository.findOne(appId);

        if (subscribedApp.getId() == null) {
            log.info("{}当前API未被订阅过", TAG);
            result.setCode(Result.OK);
            return result;
        }
        SubscribeSystem subscribeSystem = new SubscribeSystem(Integer.parseInt(subscribedApp.getScaleApplicationId()),subscribedApp.getId(),
                dataItemRepository.findOne(subscribedApp.getSystem()).getItemName());

        result.setCode(Result.OK);
        result.setData(subscribeSystem);
        return result;
    }

    /**
     * 网络异常数据查询
     * @param statQuery
     * @return
     */
    public AnalyticsDto respExceptionStatics(ApiTrafficStatQuery statQuery){
        AnalyticsPeriod period = new AnalyticsPeriod();
        String total = null;
        String granularity = statQuery.getGranularity().getGranularity();
        List<String> values = new ArrayList<>();
        AnalyticsDto analyticsDto = new AnalyticsDto();
        period.setSince(AnalyticsUtil.formatTime(statQuery.getTimeQuery().getStart()));
        period.setUntil(AnalyticsUtil.formatTime(statQuery.getTimeQuery().getEnd()));
        period.setTimezone("Asia/Shanghai");
        period.setGranularity(granularity);

        ApiLogQuerySpecial specialQuery = new ApiLogQuerySpecial();
        specialQuery.setApiId(statQuery.getApiId());
        specialQuery.setErrorCode(statQuery.getStatType().getCode());
        specialQuery.setTimeQuery(statQuery.getTimeQuery());
        log.info("ES search condition:{}",specialQuery);
        Result<List<ApiCastLog>> listResult = elasticSearchService.queryForApiAndRescode(specialQuery);
        if(Result.FAIL.equals(listResult.getCode()) || CollectionUtils.isEmpty(listResult.getData())){
            log.error(String.format("ES search: result=%s",listResult.getCode()));
            return analyticsDto;
        }
        //查询结果处理(hour/day/month)
        List<String> times = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime = dateFormat.format(statQuery.getTimeQuery().getStart());
        String endTime = dateFormat.format(statQuery.getTimeQuery().getEnd());
        if("hour".equals(granularity)){
            times = DateUtil.getHours(startTime,endTime);
        }else if("day".equals(granularity)){
            times = DateUtil.getDays(startTime,endTime);
        }else if("month".equals(granularity)){
            times = DateUtil.getMonths(startTime,endTime);
        }

        if(CollectionUtils.isEmpty(times)){
            log.error(String.format("time is null,timeQuery=%s",statQuery.getTimeQuery()));
            return analyticsDto;
        }
        List<ApiCastLog> logList = listResult.getData();
        values = apiLogStatistics(logList,granularity,times,values);
        total = String.valueOf(logList.size());
        analyticsDto.setPeriod(period);
        analyticsDto.setTotal(total);
        analyticsDto.setValues(values);
        return analyticsDto;
    }

    /**
     * 网络异常数据统计
     * @param logList
     * @param granularity
     * @param times
     * @param values
     * @return
     */
    public List<String> apiLogStatistics(List<ApiCastLog> logList,String granularity,List<String> times,List<String> values){
        //将查询的日志数据转换成map结构，方便后面统计使用
        Map<String,List<ApiCastLog>> logMap = new HashMap<>();
        for(ApiCastLog log:logList){
            String itemStartTime = log.getStartTime();
            if("hour".equals(granularity)){
                itemStartTime = itemStartTime.substring(0,13);
            }else if("day".equals(granularity)){
                itemStartTime = itemStartTime.substring(0,10);
            }else if("month".equals(granularity)){
                itemStartTime = itemStartTime.substring(0,7);
            }
            List<ApiCastLog> itemLogList = null;
            if(logMap.containsKey(itemStartTime)){
                itemLogList = logMap.get(itemStartTime);
            }else{
                itemLogList = new ArrayList<>();
            }
            itemLogList.add(log);
            logMap.put(itemStartTime,itemLogList);
        }
        //日志数据统计和分析
        for(String time:times){
            List<ApiCastLog> itemLogList = logMap.get(time);
            String count = CollectionUtils.isEmpty(itemLogList)?"0":String.valueOf(itemLogList.size());
            values.add(count);
        }
        return values;
    }

}
