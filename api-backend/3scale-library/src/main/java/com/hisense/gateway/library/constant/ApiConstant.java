/*
 * @author guilai.ming
 * @date 2020/8/4
 */
package com.hisense.gateway.library.constant;

import com.hisense.api.library.model.DefineMode;
import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.model.dto.web.PublishApiDto;
import com.hisense.gateway.library.model.pojo.base.Instance;
import com.hisense.gateway.library.model.pojo.base.PublishApi;
import com.hisense.gateway.library.model.pojo.base.PublishApiGroup;
import lombok.Data;

import java.util.*;

import static com.hisense.api.library.model.DefineMode.*;
import static com.hisense.gateway.library.model.ModelConstant.*;
import static com.hisense.gateway.library.constant.ApiConstant.ApiSetOps.*;
import static com.hisense.gateway.library.constant.ApiConstant.ApiSetZone.*;

public class ApiConstant {
    /**
     * API创建方式:0 自动创建, 1-手动创建
     */
    public static final int API_SRC_EUREKA_AUTO = 0;
    public static final int API_SRC_USER_CREATE = 1;
    /**
     * application类型：0-创建api；1-订阅api
     */
    public static  final int APPLICATION_TYPE_CREATE_API = 0;
    public static  final int APPLICATION_TYPE_SUBSCRIBE_API = 1;

    private static final String INTERSECTION_STATUS_1 = "交集外,未入库的,新建API";
    private static final String INTERSECTION_STATUS_2 = "交集外,已入库的,未上线的,未发布的,在发布流程中的,不做处理";
    private static final String INTERSECTION_STATUS_3 = "交集外,已入库的,未上线的,未发布的,不在发布流程中的,删除";
    private static final String INTERSECTION_STATUS_4 = "交集外,已入库的,未上线的,已发布的,邮件通知";
    private static final String INTERSECTION_STATUS_5 = "交集外,已入库的,已上线的,邮件通知";
    private static final String INTERSECTION_STATUS_6 = "交集内,已入库的,未上线的,未发布的,在发布流程中的,不做处理";
    private static final String INTERSECTION_STATUS_7 = "交集内,已入库的,未上线的,未发布的,不在发布流程中的,更新不影响发布的字段";
    private static final String INTERSECTION_STATUS_8 = "交集内,已入库的,未上线的,已发布的,更新不影响发布的字段";
    private static final String INTERSECTION_STATUS_9 = "交集内,已入库的,已上线的,更新不影响发布的字段";

    public enum ApiSetOps {
        API_OPS_NEW,// 新建API
        API_OPS_BREAK,// 忽略
        API_OPS_DELETE,// 删除
        API_OPS_MAIL_NOTIFY,// 邮件通知
        API_OPS_UPDATE_FIELDS;// 更新不影响发布的字段
    }

    /**
     * 自动扫描上的API列表与已入库的API列表求交集,对应的区域
     */
    public enum ApiSetZone {
        ZONE_A,// A区域: 新增、未入库
        ZONE_B,// B区域: 新增、已入库
        ZONE_C;// C区域: 缺失、已入库
    }

    /**
     * 同service下已入库的API列表,基于唯一性hash,与已扫描上来的API列表求交集状态
     */
    public enum ApiSetPartStatus {
        OUT_UNSAVED_NEW(1,
                INTERSECTION_STATUS_1,
                API_OPS_NEW,
                ZONE_A),

        OUT_SAVED_OFFLINE_NOPUBLISHED_INAPPROVE(2,
                INTERSECTION_STATUS_2,
                API_OPS_BREAK,
                ZONE_C,
                API_FIRST_PROMOTE, API_FOLLOWUP_PROMOTE),

        OUT_SAVED_OFFLINE_NOPUBLISHED_NOAPPROVE(3,
                INTERSECTION_STATUS_3,
                API_OPS_DELETE,
                ZONE_C,
                API_DELETE, API_INIT, API_FIRST_PROMOTE_REJECT, API_FOLLOWUP_PROMOTE_REJECT),

        OUT_SAVED_OFFLINE_PUBLISHED(4,
                INTERSECTION_STATUS_4,
                API_OPS_MAIL_NOTIFY,
                ZONE_C,
                API_COMPLETE),

        OUT_SAVED_ONLINE(5,
                INTERSECTION_STATUS_5,
                API_OPS_MAIL_NOTIFY,
                ZONE_C,
                API_COMPLETE),

        IN_SAVED_OFFLINE_NOPUBLISHED_INAPPROVE(6,
                INTERSECTION_STATUS_6,
                API_OPS_BREAK,
                ZONE_B,
                API_FIRST_PROMOTE, API_FOLLOWUP_PROMOTE),

        IN_SAVED_OFFLINE_NOPUBLISHED_NOAPPROVE(7,
                INTERSECTION_STATUS_7,
                API_OPS_UPDATE_FIELDS,
                ZONE_B,
                API_DELETE, API_INIT, API_FIRST_PROMOTE_REJECT, API_FOLLOWUP_PROMOTE_REJECT),

        IN_SAVED_OFFLINE_PUBLISHED(8,
                INTERSECTION_STATUS_8,
                API_OPS_UPDATE_FIELDS,
                ZONE_B,
                API_COMPLETE),

        IN_SAVED_ONLINE(9,
                INTERSECTION_STATUS_9,
                API_OPS_UPDATE_FIELDS,
                ZONE_B,
                API_COMPLETE);

        private final Integer code;
        private final String description;
        private final ApiSetOps apiOps;
        private final ApiSetZone setZone;
        private final List<Integer> apiStatus = new ArrayList<>();

        ApiSetPartStatus(Integer code, String description, ApiSetOps apiOps, ApiSetZone setZone, Integer... apiStatus) {
            this.code = code;
            this.description = description;
            this.apiOps = apiOps;
            this.setZone = setZone;
            this.apiStatus.addAll(Arrays.asList(apiStatus));
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public List<Integer> getApiStatus() {
            return apiStatus;
        }

        public ApiSetOps getApiOps() {
            return apiOps;
        }

        public ApiSetZone getSetZone() {
            return setZone;
        }

        public static ApiSetPartStatus from(int code) {
            if (OUT_UNSAVED_NEW.getCode().equals(code)) {
                return OUT_UNSAVED_NEW;
            } else if (OUT_SAVED_OFFLINE_NOPUBLISHED_INAPPROVE.getCode().equals(code)) {
                return OUT_SAVED_OFFLINE_NOPUBLISHED_INAPPROVE;
            } else if (OUT_SAVED_OFFLINE_NOPUBLISHED_NOAPPROVE.getCode().equals(code)) {
                return OUT_SAVED_OFFLINE_NOPUBLISHED_NOAPPROVE;
            } else if (OUT_SAVED_OFFLINE_PUBLISHED.getCode().equals(code)) {
                return OUT_SAVED_OFFLINE_PUBLISHED;
            } else if (OUT_SAVED_ONLINE.getCode().equals(code)) {
                return OUT_SAVED_ONLINE;
            } else if (IN_SAVED_OFFLINE_NOPUBLISHED_INAPPROVE.getCode().equals(code)) {
                return IN_SAVED_OFFLINE_NOPUBLISHED_INAPPROVE;
            } else if (IN_SAVED_OFFLINE_NOPUBLISHED_NOAPPROVE.getCode().equals(code)) {
                return IN_SAVED_OFFLINE_NOPUBLISHED_NOAPPROVE;
            } else if (IN_SAVED_OFFLINE_PUBLISHED.getCode().equals(code)) {
                return IN_SAVED_OFFLINE_PUBLISHED;
            } else if (IN_SAVED_ONLINE.getCode().equals(code)) {
                return IN_SAVED_ONLINE;
            } else {
                return OUT_UNSAVED_NEW;
            }
        }

        public static ApiSetPartStatus from(boolean inSet, boolean saved, boolean onLine, Integer apiStatus) {
            if (!inSet) {
                if (!saved) {// A
                    return OUT_UNSAVED_NEW;
                } else {// C
                    if (onLine) {
                        return OUT_SAVED_ONLINE;
                    } else {
                        if (OUT_SAVED_OFFLINE_NOPUBLISHED_INAPPROVE.getApiStatus().contains(apiStatus)) {
                            return OUT_SAVED_OFFLINE_NOPUBLISHED_INAPPROVE;
                        } else if (OUT_SAVED_OFFLINE_NOPUBLISHED_NOAPPROVE.getApiStatus().contains(apiStatus)) {
                            return OUT_SAVED_OFFLINE_NOPUBLISHED_NOAPPROVE;
                        } else {
                            return OUT_SAVED_OFFLINE_PUBLISHED;
                        }
                    }
                }
            } else {// B
                if (onLine) {
                    return IN_SAVED_ONLINE;
                } else {
                    if (IN_SAVED_OFFLINE_NOPUBLISHED_INAPPROVE.getApiStatus().contains(apiStatus)) {
                        return IN_SAVED_OFFLINE_NOPUBLISHED_INAPPROVE;
                    } else if (IN_SAVED_OFFLINE_NOPUBLISHED_NOAPPROVE.getApiStatus().contains(apiStatus)) {
                        return IN_SAVED_OFFLINE_NOPUBLISHED_NOAPPROVE;
                    } else {
                        return IN_SAVED_OFFLINE_PUBLISHED;
                    }
                }
            }
        }
    }

    @Data
    public static class ApiSetPart {
        /**
         * 待创建的API
         */
        private final Set<PublishApiDto> setOpsNewList = new HashSet<>();

        /**
         * 待删除的API
         */
        private final Set<PublishApi> setOpsDeleteList = new HashSet<>();

        /**
         * 待邮件通知
         */
        private final Map<PublishApi, Set<PublishApiDto>> setOpsNotifyList = new HashMap<>();

        /**
         * 待更新
         */
        private final Map<PublishApi, Set<PublishApiDto>> setOpsUpdateList = new HashMap<>();

        @Override
        public String toString() {
            return "ApiSetPart{" +
                    "NewList=" + setOpsNewList.size() +
                    ", DeleteList=" + setOpsDeleteList.size() +
                    ", NotifyList=" + setOpsNotifyList.size() +
                    ", UpdateList=" + setOpsUpdateList.size() +
                    '}';
        }
    }

    @Data
    public static class ApiValidateStatus {
        private String apiName;
        private PublishApiGroup group;
        private List<Instance> instances;
        private String tenantId;
        private String projectId;
    }

    /**
     * API DTO 构造场景
     */
    public enum ApiDtoBuildType {
        API_DTO_BUILD_QUERY_LIST,
        API_DTO_BUILD_QUERY_DETAIL
    }

    /**
     * 评论记录对应的类型
     */
    public enum ApiCommentType  {
        API_COMMENT(1, "评论"),
        API_COMMENT_REPLY(2, "回复");

        private final int type;
        private final String description;

        ApiCommentType(int type, String description) {
            this.type = type;
            this.description = description;
        }

        public int getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }
        public static ApiCommentType from(Integer type) {
            if (API_COMMENT.getType()==type) {
                return API_COMMENT;
            } else if (API_COMMENT_REPLY.getType()==type) {
                return API_COMMENT_REPLY;
            } else {
                return null;
            }
        }
    }

    /**
     * 批处理操作类型
     */
    public enum ApiBatchOPS {
        DELETE("删除", "delete"),
        PROMOTE("发布", "promote"),
        SET_GROUP("设置分组", "setGroup"),
        OFF_LINE("下线", "getSubscribeStatus"),
        DELETE_FILE("删除API文档", "deleteApiDoc");

        private final String name;
        private final String logKey;

        ApiBatchOPS(String name, String logKey) {
            this.name = name;
            this.logKey = logKey;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * API HTTP参数传递方式
     */
    public enum ApiHttpParamType {
        FORM("form"),
        BODY("body"),
        PATH("path"),
        QUERY("query"),
        HEADER("header");

        private final String name;

        ApiHttpParamType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static ApiHttpParamType from(String paramType) {
            if (FORM.toString().equals(paramType)) {
                return FORM;
            } else if (BODY.getName().equals(paramType)) {
                return BODY;
            } else if (PATH.getName().equals(paramType)) {
                return PATH;
            } else if (QUERY.getName().equals(paramType)) {
                return QUERY;
            } else if (HEADER.getName().equals(paramType)) {
                return HEADER;
            } else {
                return QUERY;
            }
        }
    }

    /**
     * API 参数数据类型,约定为14种
     */
    public enum ApiParamDataType {
        API_PARAM_BYTE("byte", "0xFF", "java.lang.Byte", PRIMITIVE),
        API_PARAM_CHAR("char", "'A'", "java.lang.Character", PRIMITIVE),
        API_PARAM_SHORT("short", "10", "java.lang.Short", PRIMITIVE),
        API_PARAM_INT("int", "10", "java.lang.Integer", PRIMITIVE),
        API_PARAM_LONG("long", "10L", "java.lang.Long", PRIMITIVE),
        API_PARAM_FLOAT("float", "10.0", "java.lang.Float", PRIMITIVE),
        API_PARAM_DOUBLE("double", "10.0", "java.lang.Double", PRIMITIVE),
        API_PARAM_BOOLEAN("boolean", "true", "java.lang.Boolean", PRIMITIVE),
        API_PARAM_STRING("string", "\"This is string\"", "java.lang.String", PRIMITIVE),
        API_PARAM_OBJECT("object", "{int a;int b}", "", DEFINED),
        API_PARAM_ARRAY("Array", "[1,2,3,3]", "", PRIMITIVE_ARRAY, DEFINED_ARRAY),
        API_PARAM_LIST("List", "[1,2,3,3]", "", PRIMITIVE_LIST, DEFINED_LIST),
        API_PARAM_SET("Set", "[1,2,3,4]", "", PRIMITIVE_SET, DEFINED_SET),
        API_PARAM_MAP("Map", "{\"key1\":\"value1\"}", "", PRIMITIVE_MAP, DEFINED_MAP);

        private final String dataType;
        private final String wrapperType;
        private List<DefineMode> defineModes;
        private final String defaultValue;

        ApiParamDataType(String dataType, String wrapperType, String defaultValue, DefineMode... defineModes) {
            this.dataType = dataType;
            this.wrapperType = wrapperType;
            this.defaultValue = defaultValue;
            if (MiscUtil.isNotEmpty(defineModes)) {
                this.defineModes = new ArrayList<>();
                this.defineModes.addAll(Arrays.asList(defineModes));
            }
        }

        public String getDataType() {
            return dataType;
        }

        public String getWrapperType() {
            return wrapperType;
        }

        public List<DefineMode> getDefineModes() {
            return defineModes;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        /**
         * 获取参数数据类型
         *
         * @param defineMode API扫描时生成的定义模式
         * @param dataType   调用者须确保非空
         * @return 参数数据类型
         */
        public static ApiParamDataType from(DefineMode defineMode, String dataType) {
            switch (defineMode) {
                case DEFINED:
                    return API_PARAM_OBJECT;

                case DEFINED_ARRAY:
                case PRIMITIVE_ARRAY:
                    return API_PARAM_ARRAY;

                case DEFINED_LIST:
                case PRIMITIVE_LIST:
                    return API_PARAM_LIST;

                case DEFINED_SET:
                case PRIMITIVE_SET:
                    return API_PARAM_SET;

                case DEFINED_MAP:
                case PRIMITIVE_MAP:
                    return API_PARAM_MAP;

                case PRIMITIVE:
                    break;
            }

            // for PRIMITIVE
            if (dataType.matches(buildRegex(API_PARAM_BYTE))) {
                return API_PARAM_BYTE;
            } else if (dataType.matches(buildRegex(API_PARAM_CHAR))) {
                return API_PARAM_CHAR;
            } else if (dataType.matches(buildRegex(API_PARAM_SHORT))) {
                return API_PARAM_SHORT;
            } else if (dataType.matches(buildRegex(API_PARAM_INT))) {
                return API_PARAM_INT;
            } else if (dataType.matches(buildRegex(API_PARAM_LONG))) {
                return API_PARAM_LONG;
            } else if (dataType.matches(buildRegex(API_PARAM_FLOAT))) {
                return API_PARAM_FLOAT;
            } else if (dataType.matches(buildRegex(API_PARAM_DOUBLE))) {
                return API_PARAM_DOUBLE;
            } else if (dataType.matches(buildRegex(API_PARAM_BOOLEAN))) {
                return API_PARAM_BOOLEAN;
            } else if (dataType.matches(buildRegex(API_PARAM_STRING))) {
                return API_PARAM_STRING;
            } else {
                return API_PARAM_OBJECT;
            }
        }

        private static String buildRegex(ApiParamDataType type) {
            return String.format("%s|%s", type.getDataType(), type.getWrapperType());
        }
    }
}
