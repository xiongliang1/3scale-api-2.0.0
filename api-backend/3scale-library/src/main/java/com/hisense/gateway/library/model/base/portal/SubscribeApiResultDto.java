package com.hisense.gateway.library.model.base.portal;

import com.hisense.gateway.library.model.dto.web.SubscribedApi;
import com.hisense.gateway.library.model.pojo.base.PublishApi;
import lombok.Data;

@Data
public class SubscribeApiResultDto {
    private  String environment;
    private PublishApi api;
    private  Integer subsrcibSystem;
    private  StringBuffer buffer;
    private  String systemName;
    private SubscribedApi application;
    private boolean subResult;

    public SubscribeApiResultDto(String environment,PublishApi api,Integer subsrcibSystem,
                                 StringBuffer buffer,String systemName,SubscribedApi application,boolean subResult){
        this.api = api;
        this.environment = environment;
        this.subsrcibSystem = subsrcibSystem;
        this.buffer = buffer;
        this.systemName = systemName;
        this.application = application;
        this.subResult = subResult;
    }
}
