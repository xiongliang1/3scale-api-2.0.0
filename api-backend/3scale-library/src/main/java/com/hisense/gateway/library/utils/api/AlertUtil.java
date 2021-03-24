package com.hisense.gateway.library.utils.api;

import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.alert.AlertTriggerMethod;
import com.hisense.gateway.library.model.dto.web.AlertPolicyDto;
import com.hisense.gateway.library.model.pojo.base.AlertPolicy;
import com.hisense.gateway.library.utils.CommonUtil;
import com.hisense.gateway.library.utils.meta.GlobalSettings;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hisense.gateway.library.constant.AlertConstant.*;
import static com.hisense.gateway.library.constant.AlertConstant.TriggerType.BY_RESPONSE_TIME;

/**
 * @author guilai.ming 2020/09/10
 */
public class AlertUtil {
    public static Result<Boolean> isAlertPolicyValid(AlertPolicyDto alertPolicyDto) {
        Result<Boolean> result = new Result<>(Result.FAIL, "", false);
        if (MiscUtil.isEmpty(alertPolicyDto.getName())) {
            result.setMsg(DTO_INVALID_NAME);
            return result;
        }

        if (MiscUtil.isEmpty(alertPolicyDto.getTriggerMethods())) {
            result.setMsg(DTO_INVALID_TRIGGER_METHOD);
            return result;
        }

        Set<String> triggerMethodTypes =
                alertPolicyDto.getTriggerMethods().stream().map(item -> item.getTriggerTypeInner().getKey()).collect(Collectors.toSet());
        if (MiscUtil.isEmpty(triggerMethodTypes)) {
            result.setMsg(DTO_INVALID_TRIGGER_METHOD);
            return result;
        }

        if (triggerMethodTypes.size() < alertPolicyDto.getTriggerMethods().size()) {
            result.setMsg(DTO_DUP_TRIGGER_METHODS);
            return result;
        }

        for (AlertTriggerMethod method : alertPolicyDto.getTriggerMethods()) {
            if (method.getTriggerTypeInner() == BY_RESPONSE_TIME && method.getResponseTime() == null) {
                result.setMsg(DTO_INVALID_TRIGGER_RST);
                return result;
            }
        }

        if (alertPolicyDto.getMsgSendInterval() == null) {
            result.setMsg(DTO_INVALID_MSG_SEND_INTERVAL);
            return result;
        }

        if (MiscUtil.isEmpty(alertPolicyDto.getMsgSendTypes())) {
            result.setMsg(DTO_INVALID_MSG_SEND_TYPES);
            return result;
        }

        result.setCode(Result.OK);
        result.setMsg("");
        result.setData(true);
        return result;
    }

    public static AlertPolicy buildAlertPolicy(AlertPolicyDto dto) {
        AlertPolicy policy = new AlertPolicy();

        policy.setName(dto.getName());
        policy.setTriggerMethods(String.format("{\"triggerMethods\":%s}", MiscUtil.toJson(dto.getTriggerMethods())));
        policy.setMsgReceivers(CommonUtil.encodeStrListWithComma(dto.getMsgReceivers()));
        policy.setMsgSendInterval(dto.getMsgSendInterval());
        policy.setMsgSendTypes(CommonUtil.encodeIntListWithComma(dto.getMsgSendTypes()));
        policy.setCreateTime(dto.getCreateTime() == null ? new Date() : dto.getCreateTime());
        policy.setUpdateTime(policy.getCreateTime());
        policy.setStatus(STATUS_INIT);
        policy.setEnable(1);
        policy.setCreator(GlobalSettings.getVisitor().getUsername());
        return policy;
    }

    public static void updateAlertPolicy(AlertPolicy policy, AlertPolicyDto dto) {
        policy.setName(dto.getName());
        policy.setTriggerMethods(String.format("{\"triggerMethods\":%s}", MiscUtil.toJson(dto.getTriggerMethods())));
        policy.setMsgReceivers(CommonUtil.encodeStrListWithComma(dto.getMsgReceivers()));
        policy.setMsgSendInterval(dto.getMsgSendInterval());
        policy.setMsgSendTypes(CommonUtil.encodeIntListWithComma(dto.getMsgSendTypes()));
        policy.setUpdateTime(dto.getCreateTime() == null ? new Date() : dto.getCreateTime());
    }

    public static void updatePolicyBindApis(AlertPolicy policy, List<Integer> apiIds) {
        List<Integer> oldApiIds = CommonUtil.decodeIntListWithComma(policy.getApiIds());

    }

    public static List<AlertPolicy> buildAlertPolicies(List<AlertPolicyDto> dtos) {
        return dtos.stream().map(AlertUtil::buildAlertPolicy).collect(Collectors.toList());
    }
}
