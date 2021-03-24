package com.hisense.gateway.management.xxljob;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.repository.PublishApplicationRepository;
import com.hisense.gateway.library.stud.AccountStud;
import com.hisense.gateway.library.stud.model.ApplicationXml;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @ClassName: GainAppidAndAppkeyJobHandler
 * @Description: TOOD
 * @Author: zhouguokai.ex
 * @createDate: 2021/01/15
 * @version: 1.0
 */
@JobHandler(value = "gainAppidAndAppkeyJobHandler")
@Component
public class GainAppidAndAppkeyJobHandler extends IJobHandler{

    @Autowired
    PublishApplicationRepository publishApplicationRepository;

    @Autowired
    AccountStud accountStud;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        XxlJobLogger.log("XXL-JOB,同步AppIdAndAppKey-开始。。。。。");
        getAppIdAndAppKeyHistoryJob();
        XxlJobLogger.log("XXL-JOB,同步AppIdAndAppKey-结束。。。。。");
        return SUCCESS;
    }

    public void getAppIdAndAppKeyHistoryJob(){
        List<JSONObject> jsonObjects = publishApplicationRepository.queryDataList();
        if(!CollectionUtils.isEmpty(jsonObjects)){
            for (JSONObject obj : jsonObjects){
                if(StringUtils.isEmpty(obj.get("APP_ID"))&&StringUtils.isEmpty(obj.get("APP_KEY"))){
                    ApplicationXml applicationXml = accountStud.appXml2(
                            String.valueOf(obj.get("HOST")),
                            String.valueOf(obj.get("ACCESS_TOKEN")),
                            String.valueOf(obj.get("ACCOUNT_ID")),
                            String.valueOf(obj.get("SCALE_APPLICATION_ID")));
                    if(null != applicationXml){
                        XxlJobLogger.log("********read application,{}", JSONObject.toJSON(applicationXml).toString());
                        if(applicationXml.getApplicationId()!=null
                                && null != applicationXml.getKeys()
                                && !CollectionUtils.isEmpty(applicationXml.getKeys().getKey())){
                            publishApplicationRepository.updateAppIdAndAppKey(
                                    Integer.valueOf(obj.get("ID").toString()),
                                    applicationXml.getApplicationId(),
                                    applicationXml.getKeys().getKey().get(0));
                        }
                    }


                }
            }
        }
    }

    @Scheduled(cron = "0 31 16 * * ?")
    public void test(){
        getAppIdAndAppKeyHistoryJob();
    }
}
