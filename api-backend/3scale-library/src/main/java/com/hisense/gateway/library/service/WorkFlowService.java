package com.hisense.gateway.library.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.buz.WorkFlowDto;
import com.hisense.gateway.library.model.dto.web.FlowResponseDto;
import com.hisense.gateway.library.model.dto.web.QueryParamsDto;

import java.util.Map;

public interface WorkFlowService {

    /**
     * 启动流程
     * @param workFlowDto
     * @return
     */
    Result<Object> startProcess(WorkFlowDto workFlowDto);

    /**
     * 查询某人发起的流程
     * @return
     */
    Result<FlowResponseDto> queryPersonStartProcessInstWithBizInfo(QueryParamsDto queryParamsDto);

    /**
     * 获取流程图相关数据信息
     */
     Result<Object> getProcessGraph(QueryParamsDto queryParamsDto);
}
