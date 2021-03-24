/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 */

package com.hisense.gateway.developer.web.controller;

import java.util.List;

import com.hisense.gateway.library.model.dto.portal.UserDto;
import com.hisense.gateway.library.model.pojo.portal.User;
import com.hisense.gateway.developer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/api/v1/domains/{domain}/users")
@RestController
public class UserController {

    @Autowired
    UserService userService;


    @GetMapping("/")
    public List<User> getUsetList(@PathVariable String domain) {
        return userService.getUserList();
    }
    
    @GetMapping("/signUp/{name}")
    public void test(@PathVariable String domain,@PathVariable String name) {
    	UserDto userDto = new UserDto();
    	userDto.setDescription("for test");
    	userDto.setEmail(name + "@qq.com");
    	userDto.setName(name);
    	userDto.setOrgName("test");
    	userDto.setPhone("131321332131");
    	userDto.setPwd("123456");   	
        userService.signUp( userDto);
    }

}
