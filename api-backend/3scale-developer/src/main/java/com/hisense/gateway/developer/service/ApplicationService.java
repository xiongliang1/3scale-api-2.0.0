/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */

package com.hisense.gateway.developer.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.ProcessRecordDto;
import com.hisense.gateway.library.model.dto.web.PublishApiBatch;
import com.hisense.gateway.library.model.dto.web.SubscribedApi;
import com.hisense.gateway.library.model.pojo.base.Instance;
import com.hisense.gateway.library.model.pojo.base.PublishApplication;
import com.hisense.gateway.library.stud.model.Application;
import com.hisense.gateway.library.stud.model.*;
import com.hisense.gateway.library.web.form.ApplicationForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * ApplicationService
 *
 * @author wangjinshan
 * @version v1.0
 * @date 2019-11-20 10:34
 */
public interface ApplicationService {

    Result<List<Object>> subscribeApi(String environment,List<SubscribedApi> applications);

    ApplicationDtos getApplicationListByAccount(String host,
												ApplicationForm applicationForm);

    ApplicationXmlDtos findApplicationListByAccount(String domainName,
                                                    ApplicationForm applicationForm);

    Page<ProcessRecordDto> applicationList(String environment, String apiName, List<Integer> system, PageRequest pageable);

    /**
     * 取消订阅Api
     * @param publishApiBatch
     * @return
     */
    Result<List<String>> unSubscribeApi( PublishApiBatch publishApiBatch);

    /**
     * 重置密匙
     * @param prId
     * @return
     */
    Result<Object> modifyAuthSecret(Integer prId);

}
