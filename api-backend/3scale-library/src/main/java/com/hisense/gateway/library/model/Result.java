package com.hisense.gateway.library.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Result<T> implements Serializable {
    public static final String OK = "0";
    public static final String FAIL = "1";
    public static final String OTHER = "2";
    private static final String FAIL_MSG = "fail to invoke";

    private String code;
    private String msg;
    private Integer alert;
    private T data;

    public Result() {
        this.code = OK;
    }

    public Result(T data) {
        this.code = OK;
        this.setData(data);
    }

    public Result(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result(String code, String msg, T data, Integer alert) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.alert = alert;
    }

    public Result<T> setError(String msg) {
        this.setCode(FAIL);
        this.setMsg(msg);
        return this;
    }

    public Result<T> setError(String code, String msg) {
        this.setCode(code);
        this.setMsg(msg);
        return this;
    }

    public static Result instance() {
        return new Result();
    }

    public static <T> Result instance(T data) {
        return new Result(data);
    }

    public static <T> Result instance(String code, String msg) {
        return new Result(code, msg);
    }

    public static <T> Result instance(String code, String msg, T data) {
        return new Result(code, msg, data);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getAlert() {
        return alert;
    }

    public void setAlert(Integer alert) {
        this.alert = alert;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Map<String, Object> toJsonMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("data", this.data);
        map.put("msg", this.msg);
        map.put("code", this.code);
        return map;
    }

    public boolean isSuccess() {
        return OK.equals(this.code);
    }

    public boolean isFailure() {
        return !isSuccess();
    }


}