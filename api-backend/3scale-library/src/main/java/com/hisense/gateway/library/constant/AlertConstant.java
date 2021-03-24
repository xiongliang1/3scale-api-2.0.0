package com.hisense.gateway.library.constant;

/**
 * @author guilai.ming 2020/09/10
 */
public class AlertConstant {
    public static final String DTO_INVALID_NAME = "参数错误，策略名称无效";

    public static final String DTO_INVALID_MSG_SEND_INTERVAL = "参数错误, 请指定告警发送间隔";

    public static final String DTO_INVALID_MSG_SEND_TYPES = "参数错误, 请指定告警方式";

    public static final String DTO_INVALID_TRIGGER_RST = "参数错误, 选择超时告警时，必须指定超时阈值";

    public static final String DTO_INVALID_TRIGGER_METHOD = "参数错误，触发方式未指定,请重新设定(状态码,或者响应时间),至少一种";

    public static final String DTO_DUP_TRIGGER_METHODS = "参数错误，触发方式中存在重复, 请从(状态码、或者响应时间、或者状态码+响应时间)三种组合中选取";

    /**
     * 触发方式:
     * byHttpCode: 按照HTTP错误码
     * byResponseTime: 按照接口调用时间阈值触发
     */
    public enum TriggerType {
        BY_HTTP_CODE(0, "byHttpCode", "调用失败告警"),
        BY_RESPONSE_TIME(1, "byResponseTime", "响应超时告警");

        private final Integer code;
        private final String key;
        private final String description;

        TriggerType(Integer code, String key, String description) {
            this.code = code;
            this.key = key;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getKey() {
            return key;
        }

        public String getDescription() {
            return description;
        }

        public static TriggerType fromCode(int code) {
            if (code == BY_RESPONSE_TIME.getCode()) {
                return BY_RESPONSE_TIME;
            }
            return BY_HTTP_CODE;
        }

        @Override
        public String toString() {
            return String.valueOf(code);
        }
    }

    /**
     * 告警级别
     */
    public enum AlertLevel {
        LEVEL_PRIMARY(0, "primary", "主要告警"),
        LEVEL_SECOND(1, "second", "次要告警");

        private final Integer code;
        private final String key;
        private final String description;

        AlertLevel(Integer code, String key, String description) {
            this.code = code;
            this.key = key;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getKey() {
            return key;
        }

        public String getDescription() {
            return description;
        }

        public static AlertLevel fromCode(int code) {
            if (code == LEVEL_PRIMARY.getCode()) {
                return LEVEL_PRIMARY;
            }
            return LEVEL_SECOND;
        }

        @Override
        public String toString() {
            return String.valueOf(code);
        }
    }

    public static final int STATUS_DELETE = 0;
    public static final int STATUS_INIT = 1;
}
