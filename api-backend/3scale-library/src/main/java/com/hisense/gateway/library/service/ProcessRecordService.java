/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2020 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/3/3
 */
package com.hisense.gateway.library.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.portal.PortalProcessRecordQuery;
import com.hisense.gateway.library.model.dto.web.ProcessRecordDto;
import com.hisense.gateway.library.model.dto.web.ProcessRecordQuery;
import com.hisense.gateway.library.web.form.ApplicationSearchForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProcessRecordService {
    Page<ProcessRecordDto> findApiApplyList(String clusterId, String projectId, int isApproved, String name,
                                            String type, PageRequest pageable,String environment);

    ProcessRecordDto findApiApplyDetail(Integer id);

    Result<Boolean> approveApplication(ProcessRecordDto processRecordDto);

    Page<ProcessRecordDto> findApplicationApplyList(ApplicationSearchForm applicationSearchForm,
                                                    PageRequest pageable);

    Page<ProcessRecordDto> findApplicationApplyList(String tenantId, String projectId,String environment,
                                                    ProcessRecordQuery processRecordQuery);

    Map<String, Set<String>> findSubscribers(String tenantId, String projectId);

    Result<Boolean> approveListApplication(List<ProcessRecordDto> processRecordDto);
	
	// portal
    ProcessRecordDto findApplicationApplyDetail(Integer id);

    Page<ProcessRecordDto> findMyApplicationas(String environment,PortalProcessRecordQuery processRecordQuery);

    Result<Object> processHandle(String processId,String approveType,String remark);
}
