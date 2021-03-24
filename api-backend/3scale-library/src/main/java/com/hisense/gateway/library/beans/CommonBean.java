package com.hisense.gateway.library.beans;

/**
 * management 和 developer 各自有单独的实现例程汇总
 */
public interface CommonBean {
    /**
     * 获取登录用户名，注意：门户端此方法不能在异步线程中调用
     * @return
     */
    String getLoginUserName();
}
