package com.hisense.gateway.library.model.base.alert;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 同步到kafka的告警策略记录
 *
 * @author guilai.ming 2020/09/10
 */
@Data
public class AlertSyncRecord {
    /**
     * 3scale实例的唯一标识
     */
    private Integer instanceId;

    /**
     * API在3scale内部的ID
     */
    private Long scaleServiceId;

    /**
     * 订阅系统的UserKey, 或者发布系统的默认userKey
     */
    private String subscribeSystemUserKey;

    /**
     * 告警消息的接收人( 发布者, 订阅者, 多个订阅者 需在此列出)
     */
    private String msgReceivers;

    /**
     * 告警方式: 0-短信,1-邮箱,2-信鸿公众号,3-微信公众号
     */
    private List<Integer> msgSendTypes;

    /**
     * 触发方式: errorCode, responseTime;
     */
    private List<TriggerMethod> triggerMethods;

    /**
     * 告警策略名称
     */
    private String policyName;

    /**
     * 策略创建时间
     */
    private Date policyCreateTime;

    /**
     * 策略更新时间
     */
    private Date policyUpdateTime;

    /**
     * 告警消息发送间隔
     */
    private Integer msgSendInterval;

    /**
     * API发布系统：单值
     */
    private String apiPublishSystem;

    /**
     * API订阅系统: 单值
     */
    private String apiSubscribeSystem;

    @Data
    public static final class TriggerMethod {
        /**
         * 触发方式: 0-errorCode, 1-responseTime;
         */
        private Integer triggerType;

        /**
         * 告警级别: 0-主要告警,1-次要告警
         */
        private Integer alertLevel;

        /**
         * errorCode 方式下, 支持的HTTP错误响应码,多个则逗号隔开
         */
        private String httpErrorCodes;

        /**
         * responseTime方式下, 接口调用时间阈值
         */
        private Integer responseTime;

        public TriggerMethod(AlertTriggerMethod method) {
            this.triggerType = method.getTriggerTypeInner().getCode();
            this.alertLevel = method.getAlertLevelInner().getCode();
            if (this.triggerType == 0) {
                this.httpErrorCodes = method.getHttpCodes() != null ? method.getHttpCodes() : "4XX,5XX";
            }
            this.responseTime = method.getResponseTime();
        }
    }
}
