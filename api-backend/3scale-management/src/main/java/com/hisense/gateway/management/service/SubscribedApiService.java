package com.hisense.gateway.management.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.ProcessRecordDto;
import com.hisense.gateway.library.model.dto.web.PublishApiBatch;
import com.hisense.gateway.library.model.dto.web.SubscribedApiQuery;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SubscribedApiService {

    /**
     * 分页查询(我的订阅列表)
     */
    Page<ProcessRecordDto> findByPage(String tenantId, String projectId, String environment, SubscribedApiQuery apiQuery);

    /**
     * 取消订阅Api
     * @param publishApiBatch
     * @return
     */
    Result<List<String>> unSubscribeApi( PublishApiBatch publishApiBatch);
}
