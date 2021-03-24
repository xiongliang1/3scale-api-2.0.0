package com.hisense.gateway.developer.service.thread;

import com.hisense.gateway.developer.service.impl.ApplicationServiceImpl;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.portal.SubscribeApiResultDto;
import com.hisense.gateway.library.model.dto.web.SubscribedApi;
import com.hisense.gateway.library.model.pojo.base.Instance;
import com.hisense.gateway.library.model.pojo.base.PublishApi;
import com.hisense.gateway.library.model.pojo.base.UserInstanceRelationship;
import com.hisense.gateway.library.utils.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * 为单个系统订阅单个api
 * @author liyouzhi.ex
 */
@Slf4j
public class PublishApiApplicationThread implements Callable<Result<SubscribeApiResultDto>> {
    private  String environment;
    private  PublishApi api;
    private  Integer subsrcibSystem;
    private  StringBuffer buffer;
    private  String systemName;
    private  SubscribedApi application;
    private UserInstanceRelationship userInsRel;
    public PublishApiApplicationThread(){}
    public PublishApiApplicationThread(String environment,PublishApi api,Integer subsrcibSystem,
                                       StringBuffer buffer,String systemName,SubscribedApi application,
                                       UserInstanceRelationship userInsRel){
        this.api = api;
        this.environment = environment;
        this.subsrcibSystem = subsrcibSystem;
        this.buffer = buffer;
        this.systemName = systemName;
        this.application = application;
        this.userInsRel = userInsRel;
    }
    @Override
    public Result<SubscribeApiResultDto> call() throws Exception {
        Result<SubscribeApiResultDto> result = new Result<>(Result.OK,"",null);
        long time1 = System.currentTimeMillis();
        String user =  application.getUserName();
        Instance ins = application.getIns();
        ApplicationServiceImpl applicationService = SpringBeanUtil.getBean(ApplicationServiceImpl.class);
        boolean subResult = applicationService.subscribeApiItem(environment, api, subsrcibSystem, buffer, systemName, application,userInsRel);
        SubscribeApiResultDto subscribeApiResultDto =
                new SubscribeApiResultDto( environment, api, subsrcibSystem,buffer, systemName, application,subResult);
        result.setData(subscribeApiResultDto);
        long time2 = System.currentTimeMillis();
        log.info(String.format("订阅完成，系统=%s,api=%s,订阅线程%S处理时间(ms)=%s",systemName,api.getName(),Thread.currentThread().getName(),(time2-time1)));
        return result;
    }
}
