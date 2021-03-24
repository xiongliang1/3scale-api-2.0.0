/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.stud.impl;

import com.hisense.gateway.library.stud.ApplicationStud;
import com.hisense.gateway.library.stud.model.ApplicationDtos;
import com.hisense.gateway.library.stud.model.ApplicationSynDtos;
import com.hisense.gateway.library.stud.model.ApplicationXml;
import com.hisense.gateway.library.stud.model.ApplicationXmlDtos;
import com.hisense.gateway.library.utils.XmlUtils;
import com.hisense.gateway.library.web.form.ApplicationForm;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.hisense.gateway.library.utils.HttpUtil;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ApplicationStudImpl implements ApplicationStud {

	private static final String PATH_APPLICATION_LIST ="/admin/api/accounts/%s/applications.json?access_token=%s";

	private static final String PATH_APPLICATION_LIST_XML ="/admin/api/accounts/%s/applications.xml?access_token=%s";

	private static final String PATH_APPLICATION_XML ="/admin/api/accounts/%s/applications/%s.xml?access_token=%s";

	private static final String PATH_APPLICATION_SUSPEND_XML ="/admin/api/accounts/%s/applications/%s/suspend.xml?access_token=%s";

	private static final String PATH_APPLICATION_RESUME_XML ="/admin/api/accounts/%s/applications/%s/resume.xml?access_token=%s";

	private static final String PATH_ALLAPPLICATION_LIST ="/admin/api/applications.json?access_token=%s&page=1&per_page=500";

	@Override
	public ApplicationDtos getApplicationListByAccount(String host, ApplicationForm applicationForm) {
		log.info("******start to invoke getApplicationListByAccount,host is {},applicationForm is {}",
    			host,applicationForm);
		ApplicationDtos o =  null;
		try {
			String rlt = 
					HttpUtil.sendGet(host + String.format(PATH_APPLICATION_LIST,
							applicationForm.getUserAccountId(),applicationForm.getAccessToken()));
			log.info("******end to invoke getApplicationListByAccount,"
					+ "host is {},applicationForm is {},rlt is {}",
	    			host,applicationForm,rlt);
			if(null==rlt) {
				return o;
			}else {
				o = JSON.parseObject(rlt, ApplicationDtos.class);
			}
		} catch (Exception e) {
			log.error("******fail to invoke getApplicationListByAccount,"
					+ "host is {},applicationForm is {},e is {}",
	    			host,applicationForm,e);
		}
		return o;
	}

	@Override
	public ApplicationXmlDtos findApplicationListByAccount(String host, ApplicationForm applicationForm) {
		log.info("******start to invoke findApplicationListByAccount,host is {},applicationForm is {}",
				host,applicationForm);
		ApplicationXmlDtos o =  null;
		try {
			String rlt =
					HttpUtil.sendGet(host + String.format(PATH_APPLICATION_LIST_XML,
							applicationForm.getUserAccountId(),applicationForm.getAccessToken()));
			log.info("******end to invoke findApplicationListByAccount,"
							+ "host is {},applicationForm is {},rlt is {}",
					host,applicationForm,rlt);
			if(null==rlt) {
				return o;
			}else {
				o = (ApplicationXmlDtos)
						XmlUtils.xmlStrToObject(ApplicationXmlDtos.class, rlt);
			}
		} catch (Exception e) {
			log.error("******fail to invoke findApplicationListByAccount,"
							+ "host is {},applicationForm is {},e is {}",
					host,applicationForm,e);
		}
		return o;
	}

	@Override
	public ApplicationXml applicationRead(String host, String accessToken, String accountId, String scaleApplicationId) {
		log.info("******start to invoke applicationRead,host is {},accountId is {}, scaleApplicationId is {}",
				host,accountId,scaleApplicationId);
		ApplicationXml o =  null;
		try {
			String rlt =
					HttpUtil.sendGet(host + String.format(PATH_APPLICATION_XML,
							accountId, scaleApplicationId, accessToken));
			log.info("******end to invoke applicationRead,host is {},accountId is {},scaleApplicationId is {},rlt is {}",
					host,accountId, scaleApplicationId,rlt);
			if(null==rlt) {
				return o;
			}else {
				o = (ApplicationXml)
						XmlUtils.xmlStrToObject(ApplicationXml.class, rlt);
			}
		} catch (Exception e) {
			log.error("******fail to invoke applicationRead,"
							+ "host is {},accountId is {},scaleApplicationId is {},e is {}",
					host,accountId, scaleApplicationId,e);
		}
		return o;
	}

	@Override
	public ApplicationXml applicationSuspend(String host, String accessToken, String accountId, String scaleApplicationId) {
		log.info("******start to invoke applicationSuspend,host is {},accountId is {}, scaleApplicationId is {}",
				host,accountId,scaleApplicationId);
		ApplicationXml o =  null;
		try {
			String rlt =
					HttpUtil.sendPut(host + String.format(PATH_APPLICATION_SUSPEND_XML,
							accountId, scaleApplicationId, accessToken), new HashMap<>());
			log.info("******end to invoke applicationSuspend,host is {},accountId is {},scaleApplicationId is {},rlt is {}",
					host,accountId, scaleApplicationId,rlt);
			if(null==rlt) {
				return o;
			}else {
				o = (ApplicationXml)
						XmlUtils.xmlStrToObject(ApplicationXml.class, rlt);
			}
		} catch (Exception e) {
			log.error("******fail to invoke applicationSuspend,"
							+ "host is {},accountId is {},scaleApplicationId is {},e is {}",
					host,accountId, scaleApplicationId,e);
		}
		return o;
	}

	@Override
	public ApplicationXml applicationResume(String host, String accessToken, String accountId, String scaleApplicationId) {
		log.info("******start to invoke applicationResume,host is {},accountId is {}, scaleApplicationId is {}",
				host,accountId,scaleApplicationId);
		ApplicationXml o =  null;
		try {
			String rlt =
					HttpUtil.sendPut(host + String.format(PATH_APPLICATION_RESUME_XML,
							accountId, scaleApplicationId, accessToken), new HashMap<>());
			log.info("******end to invoke applicationResume,host is {},accountId is {},scaleApplicationId is {},rlt is {}",
					host,accountId, scaleApplicationId,rlt);
			if(null==rlt) {
				return o;
			}else {
				o = (ApplicationXml)
						XmlUtils.xmlStrToObject(ApplicationXml.class, rlt);
			}
		} catch (Exception e) {
			log.error("******fail to invoke applicationResume,"
							+ "host is {},accountId is {},scaleApplicationId is {},e is {}",
					host,accountId, scaleApplicationId,e);
		}
		return o;
	}

	@Override
	public Boolean applicationDelete(String host, String accessToken, String accountId, String scaleApplicationId) {
		log.info("******start to invoke applicationDelete,host is {},accountId is {}, scaleApplicationId is {}",
				host,accountId,scaleApplicationId);
		Map<String, Object> rlt = new HashMap<>();
		try {
			rlt =
					HttpUtil.sendDelAndGetCode(host + String.format(PATH_APPLICATION_XML,
							accountId, scaleApplicationId, accessToken),null);
			log.info("******end to invoke applicationDelete,host is {},accountId is {},scaleApplicationId is {},rlt is {}",
					host,accountId, scaleApplicationId,rlt);
		} catch (Exception e) {
			log.error("******fail to invoke applicationDelete,"
							+ "host is {},accountId is {},scaleApplicationId is {},e is {}",
					host,accountId, scaleApplicationId,e);
		}
		if (200 != (Integer) rlt.get("code")) {
			return true;
		}
		return false;
	}

	@Override
	public ApplicationDtos getAllApplicationList(String host, String accessToken) {
		log.info("******start to invoke getAllApplicationList,host is {},accessToken is {}",
    			host,accessToken);
		ApplicationDtos o =  null;
		try {
			String rlt = 
					HttpUtil.sendGet(host + String.format(PATH_ALLAPPLICATION_LIST,
							accessToken));
			log.info("******end to invoke getAllApplicationList,"
					+ "host is {},accessToken is {},rlt is {}",
	    			host,accessToken,rlt);
			if(null==rlt) {
				return o;
			}else {
				o = JSON.parseObject(rlt, ApplicationDtos.class);
			}
		} catch (Exception e) {
			log.error("******fail to invoke getAllApplicationList,"
					+ "host is {},accessToken is {},e is {}",
	    			host,accessToken,e);
		}
		return o;
	}

	@Override
	public ApplicationSynDtos getAllApplicationList2(String host, String accessToken) {
		log.info("******start to invoke getAllApplicationList2,host is {},accessToken is {}",
    			host,accessToken);
		ApplicationSynDtos o =  null;
		try {
			String rlt = 
					HttpUtil.sendGet(host + String.format(PATH_ALLAPPLICATION_LIST,
							accessToken));
			log.info("******end to invoke getAllApplicationList2,"
					+ "host is {},accessToken is {},rlt is {}",
	    			host,accessToken,rlt);
			if(null==rlt) {
				return o;
			}else {
				o = JSON.parseObject(rlt, ApplicationSynDtos.class);
			}
		} catch (Exception e) {
			log.error("******fail to invoke getAllApplicationList2,"
					+ "host is {},accessToken is {},e is {}",
	    			host,accessToken,e);
		}
		return o;
	}

}
