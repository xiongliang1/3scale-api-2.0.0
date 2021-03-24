/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */

package com.hisense.gateway.developer.service.impl;

import java.util.Date;
import java.util.List;

import com.hisense.gateway.developer.utils.RestProxyPortal;
import com.hisense.gateway.library.exception.OperationFailed;
import com.hisense.gateway.library.model.dto.portal.UserDto;
import com.hisense.gateway.library.repository.DomainRepository;
//import com.hisense.gateway.library.repository.UserRepository;
import com.hisense.gateway.developer.service.UserService;
import com.hisense.gateway.library.stud.model.AccountDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.pojo.base.Instance;
import com.hisense.gateway.library.model.pojo.portal.User;
import com.hisense.gateway.library.model.pojo.base.UserInstanceRelationship;
import com.hisense.gateway.library.repository.InstanceRepository;
import com.hisense.gateway.library.repository.UserInstanceRelationshipRepository;
import com.hisense.gateway.library.stud.AccountStud;
import com.hisense.gateway.library.stud.model.Account;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * UserServiceImp
 *
 * @author wangjinshan
 * @version v1.0
 * @date 2019-11-20 10:35
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    //@Autowired
	//UserRepository userRepository;
    
    @Autowired
    DomainRepository domainRepository;
    
    @Resource
    RestProxyPortal restProxy;
    
    @Autowired
    AccountStud accountStud;
    
    @Autowired
    InstanceRepository instanceRepository;
    
    @Autowired
    UserInstanceRelationshipRepository userInstanceRelationshipRepository;
    
    public List<User> getUserList(){
		//return userRepository.findAll();
		//TODO
		return null;
    }  
    
    public boolean signUp(UserDto userDto){
    	/*Instance ins = instanceRepository.searchInstanceByName(domainName);
        if (null == ins) {
            log.error("can not found Instance,Instance is {}", domainName);
            throw new OperationFailed("Instance not exist");
        }
        User user = userRepository.searchUserByName(userDto.getName());
        if(null==user) {
        	user = new User();
//			user.setDomain(domain.getId());
			user.setEmail(userDto.getEmail());
			user.setName(userDto.getName());
			user.setPhone(userDto.getPhone());
			user.setOrg(userDto.getOrgName());
			user.setCreateTime(new Date());
			user.setStatus(1);
			userRepository.save(user);
        }
        
        UserInstanceRelationship userInstanceRelationship = 
        		userInstanceRelationshipRepository.findByUserIdAndInstanceId(user.getId(), ins.getId());
        if(null==userInstanceRelationship) {
        	userInstanceRelationship = new UserInstanceRelationship();
        	userInstanceRelationship.setInstanceId(ins.getId());
        	userInstanceRelationship.setUserId(user.getId());
        	userInstanceRelationshipRepository.saveAndFlush(userInstanceRelationship);
        }
        if(null == userInstanceRelationship.getAccountId()) {
        	Account account = new Account();
        	account.setAccessToken(ins.getAccessToken());
        	account.setUsername(userDto.getName());
        	AccountDto accountFind =
        			accountStud.accountFind(ins.getHost(), account);
        	
        	if(null == accountFind || null==accountFind.getAccount()||
        			null==accountFind.getAccount().getId()) {
        		account.setEmail(userDto.getEmail());
        		account.setOrgName(userDto.getOrgName());
        		account.setPassword(userDto.getPwd());
        		Result<AccountDto> rlt = accountStud.signUp(ins.getHost(), account);
        		if(null!=rlt && "1".equals(rlt.getCode()) && null!=rlt.getData()) {
        			userInstanceRelationship.setAccountId(rlt.getData().getAccount().getId());
        			userInstanceRelationshipRepository.saveAndFlush(userInstanceRelationship);
        		}
        	}else {
        		userInstanceRelationship.setAccountId(accountFind.getAccount().getId());
    			userInstanceRelationshipRepository.saveAndFlush(userInstanceRelationship);
        	}
        	
        }
    	
		return true;*/
    	//TODO
		return true;
    }
}
