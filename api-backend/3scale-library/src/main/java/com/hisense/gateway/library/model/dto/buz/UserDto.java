/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author wuhuhu
 */
package com.hisense.gateway.library.model.dto.buz;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * OverviewDto
 *
 * @author  wangjinshan
 * @version v1.0
 * @date 2019-11-21 15:39
 */
@Getter
@Setter
public class UserDto {
    private static final long serialVersionUID = 5105223900137785597L;

    Integer id;
    Date createTime;
    String description;
    /**
     * 用户所属组织机构名称
     */
    String org;
    String email;
    String name;
    String phone;
    /**
     * 用户角色
     */
    Integer role;
    /**
     * 用户状态: 0废弃  1有效
     */
    Integer status;
    /**
     * 用户类型
     */
    Integer type;
}
