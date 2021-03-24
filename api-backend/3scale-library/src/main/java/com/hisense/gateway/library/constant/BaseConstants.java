package com.hisense.gateway.library.constant;

public class BaseConstants {
    /**
     * log
     */
    public static final String TAG = "API-MANAGER ";

    /**
     * Common mapping
     */
    public static final String MAPPING_PREFIX = "/api/v1/tenant/{tenantId}/project/{projectId}/{environment}/";
    public static final String URL_ACCOUNTS = MAPPING_PREFIX + "accounts";
    public static final String URL_ANALYTICS = MAPPING_PREFIX + "analytics";
    public static final String URL_APPLICATIONS = MAPPING_PREFIX + "applications";
    public static final String URL_APPROVAL = MAPPING_PREFIX + "approval";
    public static final String URL_APPSYN = MAPPING_PREFIX + "appSyn";
    public static final String URL_DATAITEMS = MAPPING_PREFIX + "dataItems";
    public static final String URL_DEALDATA = MAPPING_PREFIX + "dealData";
    public static final String URL_DEBUGGING = MAPPING_PREFIX + "debugging";
    public static final String URL_LIMIT = MAPPING_PREFIX + "limits";
    public static final String URL_METRIC = MAPPING_PREFIX + "metric";
    public static final String URL_PROCESS_RECORD = MAPPING_PREFIX + "processRecord";
    public static final String URL_PUBLISH_API = MAPPING_PREFIX + "publishApi";
    public static final String URL_SUBSCRIBED_API = MAPPING_PREFIX + "subscribedApi";
    public static final String URL_PUBLISH_API_GROUP = MAPPING_PREFIX + "publishApiGroup";
    public static final String URL_SERVICES = MAPPING_PREFIX + "services";
    public static final String URL_SWAGGER_DOC = MAPPING_PREFIX + "docs";
    public static final String URL_COMMENT = MAPPING_PREFIX + "comment";
    public static final String URL_DASHBOARD = MAPPING_PREFIX + "dashboard";
    public static final String URL_SAPCONVERTINFO = MAPPING_PREFIX + "sapConvertInfo";
    public static final String URL_OPERATION = MAPPING_PREFIX + "operationApi";

    public static final String URL_ALERT_POLICY = MAPPING_PREFIX + "alertPolicy";
    public static final String URL_API_LOGS = MAPPING_PREFIX + "log";

    /**
     * Eureka
     */
    public static final String URL_EUREKA_PUSH_METADATA = "/api/v1/eureka/metadata/notification";
    public static final String URL_EUREKA_PULL_METADATA = MAPPING_PREFIX + "eureka/metadata/pullAllApis";
    public static final String URL_EUREKA_PULL_METADATA_SCHEDULE = MAPPING_PREFIX + "eureka/metadata/schedulePullApis";
    public static final String EUREKA_APPS = "eureka/apps";
    public static final String EUREKA_CONFIG = MAPPING_PREFIX + "eureka/metadata/configs";

    /**
     * nacos
     */
    public static final String NACOS_APP = "nacos/v1/ns/instance/list?serviceName=";

    /**
     * Time format
     */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_TIME_MS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public static final Integer API_PROJECT_STATUS = 0;
    public static final Integer MSG_PROJECT_STATUS = 1;
}
