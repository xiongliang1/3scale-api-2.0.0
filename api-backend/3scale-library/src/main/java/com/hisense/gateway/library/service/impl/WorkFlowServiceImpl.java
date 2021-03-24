package com.hisense.gateway.library.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.constant.BpmConstant;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.buz.WorkFlowDto;
import com.hisense.gateway.library.model.dto.web.FlowResponseDto;
import com.hisense.gateway.library.model.dto.web.PageCond;
import com.hisense.gateway.library.model.dto.web.QueryParamsDto;
import com.hisense.gateway.library.service.WorkFlowService;
import com.hisense.gateway.library.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class WorkFlowServiceImpl implements WorkFlowService {

    private String bpmSucCode = "00000000";

    @Value("${flow.app.key}")
    private String appKey;

    @Value("${flow.app.operationCode}")
    private String operationCode;

    @Value("${flow.bpm.apiBasic-url}")
    private String apiBasicUrl;

    @Value("${flow.bpm.service-url}")
    private String bpmServiceUrl;

    @Override
    public Result<Object> startProcess(WorkFlowDto workFlowDto) {

        Result<Object> result = new Result<>(Result.OK,"启动流程成功！",null);
        WorkFlowDto flowDto = new WorkFlowDto();
        flowDto.setTenantID("HXJT");
        flowDto.setUserID(workFlowDto.getUserID());
        flowDto.setProcessDefName(workFlowDto.getProcessDefName());
        flowDto.setProcessInstName(workFlowDto.getProcessInstName());
        flowDto.setRelaDatas(workFlowDto.getRelaDatas());
        flowDto.setTableName("bizinfo");
        flowDto.setBizInfo(workFlowDto.getBizInfo());
        flowDto.setFinishFirstWorkItem(true);
        flowDto.setLog(true);
        flowDto.setUserName(workFlowDto.getUserName());
        Map<String,String> header= new HashMap<>() ;
        header.put("appKey",appKey);
        header.put("OperationCode",operationCode);
        String url = bpmServiceUrl + "com.hisense.bpm.createAndStartProcessInstance";
        String response = null;
        try {
            response = HttpUtil.sendPostAndHeader(url, JSONObject.toJSONString(flowDto), header);
            JSONObject resultObj = JSONObject.parseObject(response);
            String code  = (String) resultObj.get("code");
            if(bpmSucCode.equals(code)){
                JSONObject data = JSONObject.parseObject((String)resultObj.get("data"));
                result.setData(data==null?new String[]{}:data);
            }else{
                result.setError(Result.FAIL,(String)resultObj.get("message"));
            }
        } catch (Exception e) {
            log.error("启动流程异常：",e);
        }
        return result;
    }

    @Override
    public Result<FlowResponseDto> queryPersonStartProcessInstWithBizInfo(QueryParamsDto queryParamsDto) {

        Result<FlowResponseDto> result = new Result<>(Result.OK, "我发起的流程查询完成!", null);
        Map<String,Object> params = new HashMap<>();
        params.put("userID",queryParamsDto.getUserID());
        params.put("tenantID",queryParamsDto.getTenantID());
        params.put("personID",queryParamsDto.getPersonID());
        params.put("tableName",queryParamsDto.getTableName());
        params.put("procBindList",queryParamsDto.getProcBindList()==null?new ArrayList<>():queryParamsDto.getProcBindList());
        params.put("bizBindList",queryParamsDto.getBizBindList()==null?new ArrayList<>():queryParamsDto.getBizBindList());
        params.put("pageCond",queryParamsDto.getPageCond());
        String url = BpmConstant.ESB_URL + "com.hisense.bpm.queryPersonStartProcessInstWithBizInfo";
        Map<String, String> header = new HashMap<>();
        header.put("appKey", BpmConstant.APP_KEY);
        header.put("OperationCode", BpmConstant.QUERY_START_PROCESS);

        try {
            Result<Object> objectResult = HttpUtil.sendPostAndHeader1(url, params, header);
            if(Result.OK.equals(objectResult.getCode())){
                LinkedHashMap<String,String> resultMap = (LinkedHashMap<String, String>) objectResult.getData();
                FlowResponseDto flowResponseVo = new FlowResponseDto();
                BeanUtils.copyProperties(resultMap.get("data"),flowResponseVo);
                result.setData(flowResponseVo);
            }else{
                result.setError(objectResult.getCode(),objectResult.getMsg());
            }
        } catch (Exception e) {
            log.error("我发起的流程查询异常：",e);
            result.setError(Result.FAIL,e.getMessage());
        }
        return result;
    }

    @Override
    public Result<Object> getProcessGraph(QueryParamsDto queryParamsDto){
        Result<Object> result = new Result<>(Result.OK, "获取流程图相关数据信息完成!", null);
        Map<String,Object> params = new HashMap<>();
        params.put("userID",queryParamsDto.getUserID());
        params.put("processInstID",queryParamsDto.getProcessInstID());
        params.put("processDefID",queryParamsDto.getProcessDefID());
        params.put("processDefName",queryParamsDto.getProcessDefName());
        params.put("tenantID",queryParamsDto.getTenantID());
        String url = BpmConstant.ESB_URL + "com.hisense.bpm.getProcessGraph";
        Map<String, String> header = new HashMap<>();
        header.put("appKey", BpmConstant.APP_KEY);
        header.put("OperationCode", BpmConstant.GET_PROCESS_GRAPH);
        try {
            result.setData(HttpUtil.sendPostAndHeader1(url, params, header));
        }catch (Exception e){
            log.error("获取流程图相关数据信息异常：",e);
            result.setError(Result.FAIL,e.getMessage());
        }
        return result;
    }


}
