package com.hisense.gateway.library.model.base.alert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import static com.hisense.gateway.library.constant.AlertConstant.*;

/**
 * 告警触发方式
 *
 * @author guilai.ming 2020/09/10
 */
@Data
public class AlertTriggerMethod {
    /**
     * 触发方式:
     * byHttpCode: 按照HTTP错误码
     * byResponseTime: 按照接口调用时间阈值触发
     */
    private Integer triggerType;

    /**
     * 告警级别
     */
    private Integer alertLevel;

    private String httpCodes;

    private Integer responseTime;

    @JsonIgnore
    public TriggerType getTriggerTypeInner() {
        return TriggerType.fromCode(triggerType);
    }

    @JsonIgnore
    public AlertLevel getAlertLevelInner() {
        return AlertLevel.fromCode(alertLevel);
    }
}
