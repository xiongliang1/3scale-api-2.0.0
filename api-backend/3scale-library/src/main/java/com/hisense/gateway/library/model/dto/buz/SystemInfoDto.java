/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/18 @author zhangjian
 */

package com.hisense.gateway.library.model.dto.buz;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SystemInfoDto {
    // 系统ID
    private Integer id;

    /**
     * 系统管理员
     */
    private String apiAdminName;

    //开发员
    private String apiDevName;

    //租户管理员
    private String apiTenantName;

    private String msgDevName;

    private String name; // 系统名称
    private String code;
    private String slmid;
    private boolean isDevApiCreated;// 管理端申请创建测试系统时,置为true
    private boolean isPrdApiCreated;

    private List<LdapUserDto> adminNames;
    private List<LdapUserDto> devNames;
    private List<LdapUserDto> tenantNames;
    private List<LdapUserDto> msgDevNames;

}
