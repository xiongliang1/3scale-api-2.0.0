/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/11/25 @author peiyun
 */
package com.hisense.gateway.developer.web.interceptor;

public final class UserNameHolder implements AutoCloseable {

    private static final ThreadLocal<String> USERNAME_THREAD_LOCAL = new ThreadLocal<>();

    public static String get() {
        return USERNAME_THREAD_LOCAL.get();
    }

    public static void init(String userName) {
        USERNAME_THREAD_LOCAL.set(userName);
    }

    public static void release() {
        USERNAME_THREAD_LOCAL.remove();
    }

    @Override
    public void close() {
        USERNAME_THREAD_LOCAL.remove();
    }
}
