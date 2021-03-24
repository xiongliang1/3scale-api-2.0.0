package com.hisense.gateway.library.constant;

public class BpmConstant {

    /**
     * esbUrl
     */
    public static final String ESB_URL = "http://10.19.34.80:9090/";

    /**
     * appkey
     */
    public static final String APP_KEY = "N4dLEpIoenUFgAgmekVqPQ==";

    /**
     * 启动流程 OperationCode
     */
    public static final String CREATE_AND_START = "com.hisense.bpm.rest.createAndStartProcessInstance";
    public static final String QUERY_START_PROCESS = "com.hisense.bpm.rest.queryPersonStartProcessInstWithBizInfo";
    public static final String GET_PROCESS_GRAPH = "com.hisense.bpm.rest.getProcessGraph";

    public static final String SUBSCRIBE_API = "com.hisense.bpm.hip.subscribeApi";

    public static final String PUBLISH_API="com.hisense.bpm.hip.publishApi";

    public static final String LINK="http://hiit.hisense.com";
    public static final String IM_LINK="https://kk-proxy.hisense.com/hiphichat";
}
