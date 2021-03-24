package com.hisense.gateway.developer.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.constant.BpmConstant;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.ApprovalApiResDto;
import com.hisense.gateway.library.model.dto.web.FlowResponseDto;
import com.hisense.gateway.library.model.dto.web.PageCond;
import com.hisense.gateway.library.model.dto.web.QueryParamsDto;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.developer.service.ApprovalService;
import com.hisense.gateway.library.service.WorkFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ApprovalServiceImpl implements ApprovalService {

    @Autowired
    WorkFlowService workFlowService;

    @Autowired
    ProcessRecordRepository processRecordRepository;

    @Autowired
    PublishApiRepository publishApiRepository;

    @Autowired
    PublishApiGroupRepository publishApiGroupRepository;

    @Autowired
    DataItemRepository dataItemRepository;

    @Override
    public Result<ApprovalApiResDto> getApprovalRecord(Integer id) {
        Result<ApprovalApiResDto> returnResult = new Result<>();

        ApprovalApiResDto approvalApiResDto = new ApprovalApiResDto();
        //查询processRecord基本信息
        ProcessRecord processRecord = processRecordRepository.findOne(id);
        if (null == processRecord) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("查询失败,记录不存在");
            returnResult.setData(null);
            return returnResult;
        }
        //申请基本信息
        approvalApiResDto.setCreateTime(processRecord.getCreateTime());
        approvalApiResDto.setCreator(processRecord.getCreator());

        //查询api基本信息
        PublishApi publishApi = publishApiRepository.findOne(processRecord.getRelId());

        if(publishApi != null){
            approvalApiResDto.setApiId(publishApi.getId());
            approvalApiResDto.setApiName(publishApi.getName());
        }

        //由于分组会更改，所以从ext_var中查
        String extVar = processRecord.getExtVar();
        JSONObject extVarJson = JSONObject.parseObject(extVar);
        Integer groupId = extVarJson.getInteger("groupId");
        PublishApiGroup publishApiGroup = publishApiGroupRepository.findOne(groupId);
        approvalApiResDto.setApiGroupName(publishApiGroup.getName());
        String tenantId = publishApiGroup.getTenantId();
        String tenantName = "hicloud";//permissionService.getTenantNameById(tenantId);
        approvalApiResDto.setTenantName(tenantName);
        String projectId = publishApiGroup.getProjectId();
        String projectName = "hicloud";//permissionService.getProjectNameById(projectId);
        approvalApiResDto.setProjectName(projectName);

        List<Integer> dataItemIds = new ArrayList<>(3);
        dataItemIds.add(publishApiGroup.getCategoryOne());
        dataItemIds.add(publishApiGroup.getCategoryTwo());
        dataItemIds.add(publishApiGroup.getSystem());

        List<DataItem> dataItemList = dataItemRepository.findByIds(dataItemIds);
        for (DataItem dataItem : dataItemList) {
            if (dataItem.getId().equals(publishApiGroup.getSystem())) {
                approvalApiResDto.setSystemName(dataItem.getItemName());
            } else if (dataItem.getId().equals(publishApiGroup.getCategoryOne())) {
                approvalApiResDto.setCategoryOneName(dataItem.getItemName());
            } else if (dataItem.getId().equals(publishApiGroup.getCategoryTwo())) {
                approvalApiResDto.setCategoryTwoName(dataItem.getItemName());
            }
        }

        if (2 == processRecord.getStatus()) {// 已办理，发布审批
            approvalApiResDto.setStatus(processRecord.getStatus());
            approvalApiResDto.setUpdateTime(processRecord.getUpdateTime());
            if (null != processRecord.getUpdater()) {
                approvalApiResDto.setUpdater(processRecord.getUpdater());
            }
            approvalApiResDto.setRemark(processRecord.getRemark());
        }
        approvalApiResDto.setExt_var2(processRecord.getExtVar2());

        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(approvalApiResDto);
        return returnResult;
    }

    @Override
    public Result<FlowResponseDto> queryPersonStartProcessInstWithBizInfo(QueryParamsDto queryParamsDto) {
        queryParamsDto.setTenantID("HXJT");
        queryParamsDto.setUserID("sysadmin");
        queryParamsDto.setPersonID("xueyukun");//CommonBeanUtil.getLoginUserName();
        queryParamsDto.setTableName("bizinfo");
        List<String> list = new ArrayList<>();
        list.add("processDefName@like@%com.hisense.bpm.hip%");
        list.add("currentState@=@2");

        queryParamsDto.setProcBindList(list);
        PageCond pageCond = new PageCond();
        pageCond.setBegin((queryParamsDto.getIndex() - 1) * queryParamsDto.getSize());
        pageCond.setLength(queryParamsDto.getSize());
        pageCond.setIsCount(1);
        queryParamsDto.setPageCond(pageCond);
        return workFlowService.queryPersonStartProcessInstWithBizInfo(queryParamsDto);
    }

    @Override
    public Result<Object> getProcessGraph(QueryParamsDto queryParamsDto) {
        queryParamsDto.setTenantID("HXJT");
        queryParamsDto.setUserID("sysadmin");
        return workFlowService.getProcessGraph(queryParamsDto);
    }

}
