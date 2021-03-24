package com.hisense.gateway.developer.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.ApprovalApiResDto;
import com.hisense.gateway.library.model.dto.web.FlowResponseDto;
import com.hisense.gateway.library.model.dto.web.QueryParamsDto;

public interface ApprovalService {

    Result<ApprovalApiResDto> getApprovalRecord(Integer id);

    /**
     * 查询某人发起的流程
     * @param queryParamsDto
     * @return
     */
    Result<FlowResponseDto> queryPersonStartProcessInstWithBizInfo(QueryParamsDto queryParamsDto);

    /**
     * 获取流程图相关数据信息
     */
    Result<Object> getProcessGraph(QueryParamsDto queryParamsDto);

}
