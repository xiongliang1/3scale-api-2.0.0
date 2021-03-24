package com.hisense.gateway.library.model.dto.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hisense.gateway.library.model.base.alert.AlertTriggerMethod;
import lombok.Data;

import java.util.Date;
import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.DATE_TIME_FORMAT;

/**
 * 告警策略的具体内容
 *
 * @author guilai.ming 2020/09/010
 */
@Data
public class AlertPolicyDto {
    private Integer id;
    /**
     * 告警策略名称
     */
    private String name;

    /**
     * 告警触发方式,最多支持两种
     */
    private List<AlertTriggerMethod> triggerMethods;

    /**
     * 告警消息的接收人
     */
    private List<String> msgReceivers;

    /**
     * 告警消息发送间隔
     */
    private Integer msgSendInterval;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date createTime;

    /**
     * 告警策略绑定的API,逗号分隔
     */
    private String bindApis;

    /**
     * 告警策略的状态
     */
    @JsonIgnore
    private Integer status;

    /**
     * 策略创建者
     */
    private String creator;

    /**
     * 策略是否已开启（生效） or 关闭(不生效)
     */
    private boolean enable;

    /**
     * 告警方式
     */
    private List<Integer> msgSendTypes;
}
