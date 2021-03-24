/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 */

package com.hisense.gateway.library.constant;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wangjunxia
 * @date 2019-09-06
 */
public class PermissionCodeConstant {
    public static final String ALL_PRIVILEGES = "SYSTEM_ALL_PRIVILEGES";
    public static final String RESOURCE_READ_ONLY = "SYSTEM_RESOURCE_READ_ONLY";

    /**
     * 查看API分组
     */
    public static final String SCALE_API_GROUP = "SCALE_API_GROUP";

    /**
     * 增加、编辑、删除API分组
     */
    public static final String SCALE_API_GROUP_OPERATION = "SCALE_API_GROUP_OPERATION";

    /**
     * 查看API(列表、详情、监控、历史、调试)
     */
    public static final String SCALE_API_MANAGER = "SCALE_API_MANAGER";

    /**
     * 查看API-我订阅的(我订阅的，取消订阅)
     */
    public static final String SUBSCRIBED_API_MANAGER = "SUBSCRIBED_API_MANAGER";

    /**
    /**
     * 增加、编辑、删除、发布、下线API
     */
    public static final String SCALE_API_MANAGER_OPERATION = "SCALE_API_MANAGER_OPERATION";

    /**
     * 查看流控管理(列表,详情)
     */
    public static final String SCALE_LIMIT = "SCALE_LIMIT";

    /**
     * 增加、编辑、删除流控、绑定API
     */
    public static final String SCALE_LIMIT_OPERATION = "SCALE_LIMIT_OPERATION";

    /**
     * 查看API发布申请(列表,详情)
     */
    public static final String SCALE_API_PROMOTE_APPLY = "SCALE_API_PROMOTE_APPLY";

    /**
     * 查看API订购管理(待办,已办,列表,详情)
     */
    public static final String SCALE_APPLICATION = "SCALE_APPLICATION";

    /**
     * 审批,禁用,启用,删除API订购
     */
    public static final String SCALE_APPLICATION_OPERATION = "SCALE_APPLICATION_OPERATION";

    /**
     * 查看API发布审批(待办,已办,列表,详情)
     */
    public static final String SCALE_API_PROMOTE_APPROVE = "SCALE_API_PROMOTE_APPROVE";

    /**
     * 审批API发布
     */
    public static final String SCALE_API_PROMOTE_APPROVE_OPERATION = "SCALE_API_PROMOTE_APPROVE_OPERATION";

    /**
     * 读取操作 permission code 集合
     */
    protected static  final Set<String> readOnlySet = new HashSet<>();

    static {
        readOnlySet.add(SCALE_API_GROUP);
        readOnlySet.add(SCALE_API_MANAGER);
        readOnlySet.add(SCALE_API_PROMOTE_APPROVE);
        readOnlySet.add(SCALE_API_PROMOTE_APPLY);
        readOnlySet.add(SCALE_LIMIT);
        readOnlySet.add(SCALE_APPLICATION);
    }

    public static Set<String> getReadOnlySet(){
        return readOnlySet;
    }
}
