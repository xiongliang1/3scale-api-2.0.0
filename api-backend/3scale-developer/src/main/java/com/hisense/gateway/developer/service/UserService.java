/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.developer.service;

import java.util.List;

import com.hisense.gateway.library.model.dto.portal.UserDto;
import com.hisense.gateway.library.model.pojo.portal.User;

/**
 * UserService
 *
 * @author wangjinshan
 * @version v1.0
 * @date 2019-11-20 10:34
 */
public interface UserService {
	
	List<User> getUserList();
	
	public boolean signUp(UserDto userDto);
}
