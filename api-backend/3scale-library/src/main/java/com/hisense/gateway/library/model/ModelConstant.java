package com.hisense.gateway.library.model;

public class ModelConstant {
    /**
     * proxyType枚举值,设置为 directive时,route会被直接代理到host
     */
    public static final int ProxyType_Directive = 0;

    /**
     * proxyType枚举值，设置为load-balance时,route会被代理到upstream 以及用于lb的target
     */
    public static final int ProxyType_LoadBalance = 1;

    /**
     * 当APIgroup为直连类型时，target需要区分地址类型
     * 当为domain（域名）时,创建的原生service为ExternalName类型
     * 当未ip地址的时候,创建的原生service为headless类型的
     */
    public static final int TargetType_Domain = 0;
    public static final int TargetType_Ip = 1;
    public static final int TargetType_K8s = 2;

    /**
     * 市场标志环境的标志
     */
    public static final String Env_Public_Mark = "public";

    // env中resource正在运行
    public static final int Status_Running = 0;
    public static final int Status_Stop = 1;
    public static final int Status_Deleted = 2;

    public static final String Protocol_HTTP = "1";
    public static final String Protocol_HTTPS = "2";
    public static final String ENV_SANDBOX = "sandbox";
    public static final String ENV_PROD = "production";

    public static final Integer API_DELETE = 0;
    public static final Integer API_INIT = 1;// API 完成创建
    public static final Integer API_FIRST_PROMOTE = 2;// API第一次发布流程中,不允许订阅
    public static final Integer API_FOLLOWUP_PROMOTE = 3;// API发布流程中,允许订阅
    public static final Integer API_COMPLETE = 4;//api 完成审批、允许订阅
    public static final Integer API_FIRST_PROMOTE_REJECT = 5;//API 第一次发布审批不通过，不允许订阅
    public static final Integer API_FOLLOWUP_PROMOTE_REJECT = 6;//API后续发布审批不通过，允许订阅

    /**
     * 集群分类
     */
    public static final String CLUSTER_PARTITION_INNER = "inner";
    public static final String CLUSTER_PARTITION_OUTER = "outer";

    public static final String API_AUTH = "auth";
    public static final String API_NOAUTH = "noauth";
    public static final String API_ANONYMOUS_POLICY_NAME = "default_credentials";

    public static final Integer ALLOW_STATUS = 2;//审批通过
    public static final Integer NO_ALLOW_STATUS = 3;// 审批不通过

    public static final String PROJECT_ADMIN_ROLE_ID = "RID-LFJKCKtKzCrd";
}
