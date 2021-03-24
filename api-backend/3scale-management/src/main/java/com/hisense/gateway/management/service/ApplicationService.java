/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.management.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.ProcessRecordDto;
import com.hisense.gateway.library.model.dto.web.ProcessRecordQuery;
import com.hisense.gateway.library.model.dto.web.PublishApplicationDto;
import com.hisense.gateway.library.model.pojo.base.PublishApplication;
import com.hisense.gateway.library.model.pojo.base.TemporaryApplication;
import com.hisense.gateway.library.stud.model.ApplicationDtos;
import com.hisense.gateway.library.stud.model.ApplicationXmlDtos;
import com.hisense.gateway.library.web.form.ApplicationForm;
import com.hisense.gateway.library.web.form.ApplicationSearchForm;
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
    ApplicationDtos getApplicationListByAccount(String host, ApplicationForm applicationForm);

    ApplicationXmlDtos findApplicationListByAccount(String domainName, ApplicationForm applicationForm);

    List<TemporaryApplication> findAllTemporaryApplication(ApplicationSearchForm applicationSearchForm);

    Page<PublishApplication> findBackLogApplication(ApplicationSearchForm applicationSearchForm, PageRequest pageable);

    PublishApplicationDto getPublishApplication(Integer id);

    Page<ProcessRecordDto> findApprovalCompleteApp(ApplicationSearchForm applicationSearchForm, PageRequest pageable);

    Result<Boolean> applicationSuspend(Integer id);

    Result<Boolean> applicationResume(Integer id);

    Boolean applicationDelete(Integer id);

    Page<ProcessRecordDto> findApprovalCompleteApp(String tenantId, String projectId,String environment, ProcessRecordQuery processRecordQuery);

    Result<List<String>> applicationSuspendList(List<Integer> ids);

    Result<List<String>> applicationResumeList(List<Integer> ids);

    Boolean applicationDeleteList(List<Integer> ids);
}
