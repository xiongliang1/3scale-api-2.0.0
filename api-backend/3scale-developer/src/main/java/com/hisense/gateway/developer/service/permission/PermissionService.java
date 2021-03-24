/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 */

package com.hisense.gateway.developer.service.permission;


import java.util.List;

/**
 * @author wangjinshan
 * @date 2019-09-06
 */
public interface PermissionService {
    List<String> listProjectAdmin(String project);
    
    
    String getTenantName(String tenantId);
    
    String getProjectName(String projectId);

}
