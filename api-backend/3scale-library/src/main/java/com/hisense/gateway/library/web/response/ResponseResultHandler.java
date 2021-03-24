package com.hisense.gateway.library.web.response;

import java.util.HashMap;
import java.util.Map;

/**
 * @author weiwei@tenxcloud.com
 * @date   2017-07-31
 */
public class ResponseResultHandler {
    private static final String RES_KEY = "message";
    private static final String _SUCCESS_ = "success";
    private static final String _ERROR_ = "error";
    private static final String OK_KEY = "data";
    private static final String ERROR_KEY = "error";

    public static Map<String, Object> handleResult(Object result) {
        return handleResult(result, null);
    }

    public static Map<String, Object> handleResult(Object result, Map<String, Object> others) {
        Map<String, Object> resMap = new HashMap<>();
        resMap.put(OK_KEY, result);
        resMap.put(RES_KEY, _SUCCESS_);
        if (null != others) {
            resMap.putAll(others);
        }
        return resMap;
    }

    public static Map<String, Object> handleError(ResponseError error) {
        return handleError(error, null);
    }

    public static Map<String, Object> handleError(ResponseError error, Map<String, Object> others) {
        Map<String, Object> resMap = new HashMap<>();
        resMap.put(ERROR_KEY, error);
        resMap.put(RES_KEY, _ERROR_);
        if (null != others) {
            resMap.putAll(others);
        }
        return resMap;
    }
}
