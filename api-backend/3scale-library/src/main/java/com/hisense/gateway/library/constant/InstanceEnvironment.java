package com.hisense.gateway.library.constant;

public enum InstanceEnvironment {
    ENVIRONMENT_STAGING(0, "测试集群"),
    ENVIRONMENT_PRODUCTION(1, "生产集群");

    private int code;
    private String name;

    private static final String STAGING = "staging";
    public final String PRODUCTION = "production";

    InstanceEnvironment(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static InstanceEnvironment fromCode(Integer codeArg) {
        if (ENVIRONMENT_STAGING.code == codeArg) {
            return ENVIRONMENT_STAGING;
        }

        if (ENVIRONMENT_PRODUCTION.code == codeArg) {
            return ENVIRONMENT_PRODUCTION;
        }

        return ENVIRONMENT_STAGING;
    }

    public static InstanceEnvironment fromCode(String code) {
        if (STAGING.equals(code)) {
            return ENVIRONMENT_STAGING;
        }

        return ENVIRONMENT_PRODUCTION;
    }
}
