package com.hisense.gateway.library.utils;

import org.apache.commons.lang3.RandomStringUtils;

public class ScaleUtils {
    public static String systemName(String apiName, String suffix) {
        return apiName.toLowerCase() + "-" +
                suffix.toLowerCase() + "-" +
                RandomStringUtils.random(6, true, true).toLowerCase();
    }

    public static String planName(String apiName, String suffix) {
        return apiName.toLowerCase() + "-planName-" +
                suffix.toLowerCase() + "-" +
                RandomStringUtils.random(6, true, true).toLowerCase();
    }

    public static String planSystemName(String apiName, String suffix) {
        return apiName.toLowerCase() + "-plansSystemName-" +
                suffix.toLowerCase() + "-" +
                RandomStringUtils.random(6, true, true).toLowerCase();
    }

    public static String applicationName(String apiName, String suffix) {
        return apiName.toLowerCase() + "-applicationName-" +
                suffix.toLowerCase() + "-" +
                RandomStringUtils.random(6, true, true).toLowerCase();
    }

    public static String applicationDesc(String apiName, String suffix) {
        return apiName.toLowerCase() + "-applicationDesc-" +
                suffix.toLowerCase() + "-" +
                RandomStringUtils.random(6, true, true).toLowerCase();
    }
}
