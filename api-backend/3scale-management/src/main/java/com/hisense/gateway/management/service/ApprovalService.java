package com.hisense.gateway.management.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.ApprovalApiDto;
import com.hisense.gateway.library.model.dto.web.ApprovalApiResDto;
import com.hisense.gateway.library.model.dto.web.PublishApiDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ApprovalService {
    Result<Page<ApprovalApiResDto>> searchReadyApprovalApi(ApprovalApiDto approvalApiDto, String authorization);

    Result<Page<ApprovalApiResDto>> searchWaitApprovalApi(ApprovalApiDto approvalApiDto, String authorization);

    Result<Boolean> approvalApi(ApprovalApiDto approvalApiDto);

    Result<ApprovalApiResDto> getApprovalRecord(Integer processRecordId, Integer type, String authorization);

    Result<PublishApiDto> getApprovalRecordDetails(Integer processRecordId);

    Result<List<Map<String, String>>> findTenantList(String authorization);

    Result<List<Map<String, String>>> findProjectList(String authorization, String tenantId);
}
