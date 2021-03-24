/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.developer.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.developer.config.TaskExecutorPoolConfig;
import com.hisense.gateway.developer.service.ApplicationService;
import com.hisense.gateway.developer.service.permission.PermissionService;
import com.hisense.gateway.developer.service.thread.PublishApiApplicationThread;
import com.hisense.gateway.developer.utils.RestProxyPortal;
import com.hisense.gateway.library.beans.CommonBeanUtil;
import com.hisense.gateway.library.constant.ApiConstant;
import com.hisense.gateway.library.constant.BpmConstant;
import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.constant.OperationType;
import com.hisense.gateway.library.exception.BadScaleRequest;
import com.hisense.gateway.library.exception.NotExist;
import com.hisense.gateway.library.exception.OperationFailed;
import com.hisense.gateway.library.model.ModelConstant;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.portal.SubscribeApiResultDto;
import com.hisense.gateway.library.model.dto.buz.MessageDto;
import com.hisense.gateway.library.model.dto.buz.ProcessDataDto;
import com.hisense.gateway.library.model.dto.buz.WorkFlowDto;
import com.hisense.gateway.library.model.dto.web.ProcessRecordDto;
import com.hisense.gateway.library.model.dto.web.PublishApiBatch;
import com.hisense.gateway.library.model.dto.web.SubscribedApi;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.library.service.ApiInvokeRecordService;
import com.hisense.gateway.library.service.DebuggingService;
import com.hisense.gateway.library.service.WorkFlowService;
import com.hisense.gateway.library.service.impl.AlertPolicyServiceImpl;
import com.hisense.gateway.library.stud.AccountStud;
import com.hisense.gateway.library.stud.ApplicationStud;
import com.hisense.gateway.library.stud.ServiceStud;
import com.hisense.gateway.library.stud.model.*;
import com.hisense.gateway.library.web.form.ApplicationForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.hisense.gateway.library.model.ModelConstant.API_COMPLETE;
import static com.hisense.gateway.library.model.Result.*;

/**
 * ApplicationServiceImp
 *
 * @author wangjinshan
 * @version v1.0
 * @date 2019-11-20 10:35
 */
@Slf4j
@Service
public class ApplicationServiceImpl implements ApplicationService {

	@Autowired
	ServiceStud serviceStud;

	@Autowired
	PermissionService permissionService;

	@Autowired
	WorkFlowService workFlowService;

	@Autowired
	DataItemRepository dataItemRepository;

	@Autowired
	PublishApiRepository publishApiRepository;

	@Autowired
	PublishApiGroupRepository publishApiGroupRepository;

	@Autowired
	ProcessRecordRepository processRecordRepository;

	@Autowired
	DomainRepository domainRepository;

	@Autowired
	PublishApplicationRepository publishApplicationRepository;

	@Autowired
	OperationApiRepository operationApiRepository;

	@Resource
	RestProxyPortal restProxy;

	@Autowired
	ApplicationStud applicationStud;

	@Autowired
	AccountStud accountStud;

	@Autowired
	InstanceRepository instanceRepository;

	@Autowired
	PublishApiPlanRepository publishApiPlanRepository;

	@Autowired
	PublishApiInstanceRelationshipRepository publishApiInstanceRelationshipRepository;

	@Autowired
	UserInstanceRelationshipRepository userInstanceRelationshipRepository;

	@Autowired
	SystemInfoRepository systemInfoRepository;

	@Autowired
	AlertPolicyServiceImpl alertPolicyService;

	@Autowired
	DebuggingService debuggingService;

	@Autowired
	ApiDocFileRepository apiDocFileRepository;

    @Autowired
    ApiInvokeRecordService apiInvokeRecordService;

	@Value("${flow.app.link}")
	private String link;

	@Value("${flow.app.im-link}")
	private String imLink;

	//订阅api的线程池管理
	private static ExecutorService executor =TaskExecutorPoolConfig.executor(10,30,100,30);

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<List<Object>> subscribeApi(String environment,List<SubscribedApi> applications) {
		List<Object> failMessage = new ArrayList<>();
		String user = CommonBeanUtil.getLoginUserName();
		log.info(String.format("开始订阅,环境=%s,入参=%s",environment,applications));
		List<Future<Result<SubscribeApiResultDto>>> subResList = new ArrayList<>();
		for(SubscribedApi application : applications) {
			if(null == application.getId()){
				log.error("can not choose api");
				failMessage.add("请选择需要订阅的API!");
				continue;
			}
			if(CollectionUtils.isEmpty(application.getSystem())){
				log.error("can not choose system");
				failMessage.add("请选择订阅系统!");
				continue;
			}
			PublishApi api =
					publishApiRepository.getOne(application.getId());
			if (null == api) {
				log.error("can not found api");
				failMessage.add(String.format("API【%d】不存在!",application.getId()));
				continue;
			}else if(!API_COMPLETE.equals(api.getStatus())){
			    log.info("API 未发布");
                failMessage.add(String.format("API【%d】未发布!",application.getId()));
                continue;
            }
			List<Integer> subsrcibSystems = application.getSystem();
			log.info(String.format("开始处理api=%s的订阅",api.getName()));
			for(Integer subsrcibSystem:subsrcibSystems){
				DataItem systemItem = dataItemRepository.getOne(subsrcibSystem);
				String systemName = systemItem==null?subsrcibSystem+"":systemItem.getItemName();
				log.info(String.format("订阅系统=%s,api=%s",systemName,api.getName()));
				StringBuffer buffers = new StringBuffer();
				//异步线程处理订阅逻辑
				if(application.getUserName()==null){
					application.setUserName(user);
				}
				PublishApplication app = publishApplicationRepository.findByApiIdAndSystemId(api.getId(), api.getSystemId());
				application.setIns(app.getInstance());
				UserInstanceRelationship userInsRel=null;
				synchronized(application.getUserName()+app.getInstance().getId()){
					userInsRel = getUserAccount(application.getUserName(), app.getInstance(), buffers);
				}
				PublishApiApplicationThread publishApiApplicationThread = new PublishApiApplicationThread(environment, api, subsrcibSystem,
						buffers, systemName, application,userInsRel);
				Future<Result<SubscribeApiResultDto>> future = executor.submit(publishApiApplicationThread);
				subResList.add(future);
			}
		}
		//多线程订阅结果解析
		return subscribeResultHandle(subResList);
	}

	/**
	 * 订阅结果处理
	 * @param list
	 * @return
	 */
	public Result<List<Object>> subscribeResultHandle(List<Future<Result<SubscribeApiResultDto>>> list){
		Result<List<Object>> result = new Result<>();
        //消息返回正确结果
		List<Object> messageResult = new ArrayList<>();
		StringBuilder failMessageStr = new StringBuilder();
		int failCount = 0;
		int successCount = 0;
		//确保每个线程都执行完
		while(list.size()>0){
			Iterator<Future<Result<SubscribeApiResultDto>>> it = list.iterator();
			while(it.hasNext()){
				Future<Result<SubscribeApiResultDto>> future = it.next();
				if(!future.isDone()){
					continue;
				}else{
					it.remove();
				}
				StringBuffer buffers = new StringBuffer();
				SubscribedApi application = new SubscribedApi();
				try {
					Result<SubscribeApiResultDto> itemResult = future.get();
					SubscribeApiResultDto subResult = itemResult.getData();
					PublishApi api = subResult.getApi();
					Integer subsrcibSystem = subResult.getSubsrcibSystem();
					buffers = subResult.getBuffer();
					application = subResult.getApplication();
					boolean isubResult = subResult.isSubResult();
					if (!isubResult) {
						String message = String.format((failCount+1)+"、失败原因：%s",buffers.toString());
						failMessageStr.append(message);
						failCount+=1;
					}else{
						successCount+=1;
						if(application.getMessageStu()!=null && "message".equals(application.getMessageStu())){
							PublishApplication app = publishApplicationRepository.findByApiIdAndSystemId(api.getId(),api.getSystemId());
							//判断是否存在发布记录
							if(app.getId()==null){
							    log.error("未查询到发布信息！");
                            }
							Instance instance = app.getInstance();
							if(instance.getId()==null){
							    log.error("instance not exist");
                            }
							String partitionStr = instance.getClusterPartition();
							String env = instance.getClusterName();
							Result<List<Map<String,Object>>> path = debuggingService.getPath( partitionStr,env,api.getId());
							Result<Map<String,String>> userKey = debuggingService.getApiByUserKey(instance.getClusterPartition(),env,api.getId(),subsrcibSystem);
							List<ApiDocFile> picFiles = apiDocFileRepository.findApiDocByTypeAndApiId(api.getId(), "1");
							List<ApiDocFile> attFiles = apiDocFileRepository.findApiDocByTypeAndApiId(api.getId(), "2");
							MessageDto messageDto = new MessageDto();
							messageDto.setApiId(api.getId());
							messageDto.setApiName(api.getName());
							if(picFiles!=null && picFiles.size()>0){
								List<Integer> picIds = picFiles.stream().map(ApiDocFile::getId).collect(Collectors.toList());
								messageDto.setPicFiles(picIds);
							}
							if(attFiles!=null && attFiles.size()>0){
								List<Integer> attIds = attFiles.stream().map(ApiDocFile::getId).collect(Collectors.toList());
								messageDto.setAttFiles(attIds);
							}

                            if(path.getCode().equals(OK)){
                                messageDto.setPath(path.getData());
                            }
                            if(userKey.getCode().equals(OK)){
                                if(userKey.getData().get("userKey")!=null){
                                    messageDto.setUserKey(userKey.getData().get("userKey"));
                                }
                                if(userKey.getData().get("authAppId")!=null){
                                    messageDto.setAppId(userKey.getData().get("authAppId"));
                                }
                                if(userKey.getData().get("authAppKey")!=null){
                                    messageDto.setAppKey(userKey.getData().get("authAppKey"));
                                }
                            }

							messageResult.add(messageDto);
						}
					}
				} catch (Exception e) {
					log.error("订阅结果获取异常！",e);
					buffers.append("订阅结果获取异常！"+e.getMessage());
					String message = String.format((failCount+1)+"、失败原因：%s",buffers.toString());
					failMessageStr.append(message);
					failCount+=1;
				}
			}
		}
		String endMessage;
		String resultCode;
		if (failCount == 0 && successCount > 0) {
			endMessage = String.format("成功订阅%d个API", successCount);
			resultCode = OK;
		} else if (successCount > 0) {
			endMessage = String.format("成功订阅%d个API, 未能订阅%d个API!%s",  successCount,failCount, failMessageStr.toString());
			resultCode = OTHER;
		} else {
			endMessage = String.format("失败,未能订阅%d个API!%s",  failCount,failMessageStr.toString());
			resultCode = FAIL;
		}
		log.info(String.format("订阅结束,成功订阅%d个API, 订阅失败%d个API",successCount,failCount));
		result.setAlert(1);
		result.setCode(resultCode);
		result.setMsg(endMessage);
		result.setData(messageResult);
		return result;
	}

	/**
	 * 为单个系统订阅单个api
	 * 1、同一个人为同一个系统订阅同一个api不需要记录，其他都要记录（生成pa、pr）
	 * @param api
	 * @param subsrcibSystem
	 * @param buffer
	 */
    @Transactional(rollbackFor = Exception.class)
	public  boolean subscribeApiItem(String environment,PublishApi api,Integer subsrcibSystem,StringBuffer buffer,
												 String systemName,SubscribedApi application,UserInstanceRelationship userInsRel){
		boolean subscribeFlag = true;
		List<PublishApplication> applicationByApiId = publishApplicationRepository.findApplicationByApiId(api.getId());
		String user =  application.getUserName();
		Instance ins=null;
		PublishApiPlan publishApiPlan=null;
		for (PublishApplication publishApplication : applicationByApiId) {
			if(null != publishApplication.getInstance() && null != publishApplication.getApiPlan()){
				ins = publishApplication.getInstance();
				publishApiPlan = publishApplication.getApiPlan();
				break;
			}
		}
		if(null == ins || null == publishApiPlan){
			//throw new OperationFailed("instance or apiplan is null!");
			buffer.append(String.format("API【%s】对应的instance或者apiplan不存在!",api.getName()));
			return false;
		}

		/**
		 *1、如果无需鉴权，则创建pr和pa
		 *2、如果无需订阅（无需审批），则创建pr和pa、3scale
		 *3、需要审批，则创建pr和pa、3scale并发送审批邮件
		 **/
		String scaleApplicationId = "";
		String userKey = "";
		Integer status = 1;//
		String state = "";

		//根据当前登陆人、订阅系统、api判断是否有过订阅（同一个人为同一个系统订阅同一个api，包含流程中、以已通过的）
		String msg = "";
		List<PublishApplication> subRecordsBySystemAndPers = publishApplicationRepository.findSubRecordsBySystemAndPer(subsrcibSystem, api.getId(), user);
		if(!CollectionUtils.isEmpty(subRecordsBySystemAndPers) && subRecordsBySystemAndPers.size()>0){
			msg = String.format("您已经为系统【%s】订阅过API【%s】，不能重复订阅!",systemName,api.getName());
			log.info(msg);
			buffer.append(msg);
			return false;
		}
		boolean isSubscribed = false;
		boolean isNeedStartProcess = true;
		//自己订阅自己系统的接口也按照正常订阅流程处理
		//判断当前用户是否已经订阅过或者在订阅流程中
		List<PublishApplication> apps =
				publishApplicationRepository.findSubscribedApiBySystem(subsrcibSystem,api.getId(),2,1);
		if(!CollectionUtils.isEmpty(apps)) {
			msg = String.format("API【%s】已经为系统【%s】订阅过，订阅成功!",api.getName(),systemName);
			log.info(msg);
			buffer.append(msg);
			isSubscribed = true;
			status = 2;
			subscribeFlag = true;
			isNeedStartProcess = false;
			state = "live";
		}else{
			List<PublishApplication> apps2 =
					publishApplicationRepository.findSubscribedApiBySystem(subsrcibSystem,api.getId(),1,1);
			if(!CollectionUtils.isEmpty(apps2)) {
				msg = String.format("API【%s】正在为系统【%s】申请订阅，等待审批！",api.getName(),systemName);
				log.info(msg);
				buffer.append(msg);
				status = 1;
				subscribeFlag = true;
				isNeedStartProcess = false;
			}
		}
		//查询订阅记录
		List<PublishApplication> subscribedApis
				= publishApplicationRepository.findSubscribedApiBySystem(subsrcibSystem,api.getId(),2,1);

		//先创建pa、pr
		PublishApplication app = new PublishApplication();
		if(application.getMessageStu()!=null && application.getMessageStu().equalsIgnoreCase("message")){
			app.setStatus(2);
		}else {
			app.setStatus(status);
		}
		System.out.println(app.getStatus());
		app.setInstance(ins);
		app.setApiPlan(publishApiPlan);
		app.setCreateTime(new Date());
		app.setCreator(user);
		app.setDescription(applicationByApiId.get(0).getDescription());
		app.setName(applicationByApiId.get(0).getName());
		app.setPublishApi(api);
		app.setSystem(subsrcibSystem);
		app.setScaleApplicationId(scaleApplicationId);
		app.setUserKey(userKey);
		app.setType(ApiConstant.APPLICATION_TYPE_SUBSCRIBE_API);
		app = publishApplicationRepository.saveAndFlush(app);

		ProcessRecord pr = new ProcessRecord();
		pr.setCreateTime(new Date());
		pr.setCreator(user);
		pr.setType("application");
		if(application.getMessageStu()!=null && application.getMessageStu().equalsIgnoreCase("message")){
			pr.setStatus(2);
		}else {
			pr.setStatus(status);
		}
		pr.setRelId(app.getId());
		pr.setRemark(application.getDescription());
		ProcessDataDto processDataDto = new ProcessDataDto();
		processDataDto.setGroupId(api.getApiGroup().getId());
		processDataDto.setApiSystem(api.getGroup().getSystem());
		processDataDto.setAppSystem(subsrcibSystem);
		List<String> clusterPartitions = new ArrayList<>();
		clusterPartitions.add(ins.getClusterPartition());
		processDataDto.setClusterPartitions(clusterPartitions);
		pr.setExtVar(JSONObject.toJSONString(processDataDto));
		List<ApiInstance> apiInstances =
				publishApiInstanceRelationshipRepository.getApiInstancesPortal(api.getId());
		ProxyConfigDto proxyConfigDto = null;
		for (ApiInstance apiInstance:apiInstances) {
			if(proxyConfigDto == null){
				proxyConfigDto = serviceStud.latestPromote(apiInstance.getHost(),
						apiInstance.getAccessToken(),apiInstance.getId(),
						ModelConstant.ENV_SANDBOX);
				log.info("*********proxyConfigDto,{}",proxyConfigDto);
				if(null!=proxyConfigDto && null!=proxyConfigDto.getProxyConfig()
						&& null!=proxyConfigDto.getProxyConfig().getVersion()){
					serviceStud.configPromote(apiInstance.getHost(),
							apiInstance.getAccessToken(),
							apiInstance.getId().toString(),
							ModelConstant.ENV_SANDBOX,proxyConfigDto.getProxyConfig().getVersion(),
							ModelConstant.ENV_PROD);
				}
				pr.setData(JSONObject.toJSON(proxyConfigDto).toString());
			}
		}
		pr = processRecordRepository.saveAndFlush(pr);

		if(!api.isNeedSubscribe() || isSubscribed || (application.getMessageStu()!=null && application.getMessageStu().equalsIgnoreCase("message"))){
			//创建3scale-application
			/**
			 * 以系统为单位，一个系统可以通过一个userkey调用多个API
			 * 根据订阅者所在系统查询
			 */
			Application scaleApplication = new Application();
			AppPlan plan = new AppPlan();
			scaleApplication.setName(applicationByApiId.get(0).getName());
			scaleApplication.setDescription(applicationByApiId.get(0).getDescription());
			plan.setId(publishApiPlan.getScalePlanId().toString());
			scaleApplication.setPlan(plan);
			scaleApplication.setAccessToken(ins.getAccessToken());

			if(!CollectionUtils.isEmpty(subscribedApis)){
				scaleApplicationId = subscribedApis.get(0).getScaleApplicationId();
				userKey = subscribedApis.get(0).getUserKey();
			}else{
				List<PublishApplication> subscribedsBySystem =
						publishApplicationRepository.findSubscribedsBySystem(app.getSystem(), ApiConstant.APPLICATION_TYPE_SUBSCRIBE_API,app.getPublishApi().getEnvironment());

				//如果该系统有订阅过api（包含成功失败），则就用原来的useray
				if(!CollectionUtils.isEmpty(subscribedsBySystem)){
					userKey = subscribedsBySystem.get(0).getUserKey();
					scaleApplication.setUserKey(userKey);

				}
				Result<Application> appRlt =
						accountStud.createApplication(ins.getHost(), userInsRel.getAccountId().toString(),scaleApplication);
				log.info("********appRlt,{}", JSONObject.toJSON(appRlt).toString());

				if (appRlt == null || "1".equals(appRlt.getCode()) || null == appRlt.getData()) {
					throw new BadScaleRequest("fail to invole createApplication of 3scale");
				}
				Application applicationNew = appRlt.getData();
				scaleApplicationId = applicationNew.getId();
				userKey = applicationNew.getUserKey();
			}
			status = 2;
		}
		if(application.getMessageStu()!=null && application.getMessageStu().equalsIgnoreCase("message")){
			app.setStatus(2);
		}else {
			app.setStatus(status);
		}
		app.setScaleApplicationId(scaleApplicationId);
		app.setUserKey(userKey);
		app.setState(state);
		publishApplicationRepository.saveAndFlush(app);

		if(application.getMessageStu()!=null && application.getMessageStu().equalsIgnoreCase("message")){
			pr.setStatus(2);
		}else {
			pr.setStatus(status);
		}
		processRecordRepository.saveAndFlush(pr);

		Integer systemId = Integer.parseInt(api.getApiGroup().getProjectId());
		SystemInfo systemInfo = systemInfoRepository.findOne(systemId);
		if(systemInfo.getId()!=null){
			log.info("项目开发人员："+systemInfo.getDevApiDevName()+"租户管理人员："+systemInfo.getDevApiTenantName());
		}

		//重复订阅，或者无需订阅的不需要启动流程 -- liyouzhi.ex-2020/10/31
		if(api.isNeedSubscribe() && isNeedStartProcess && application.getMessageStu()==null){
			Map<String,Object> map =new HashMap<>();
			map.put("isSubscribeApprove",1);
			if(InstanceEnvironment.fromCode(environment).getCode()==0){ //当前为测试环境
				if(systemInfo.getApiAdminName()==null){
					throw new NotExist("项目管理员为空，流程启动失败");
				}
				map.put("projectManager",systemInfo.getApiAdminName());
			}else { //当前为生产环境
				if(systemInfo.getPrdApiAdminName()==null){
					throw new NotExist("项目管理员为空，流程启动失败");
				}
				map.put("projectManager",systemInfo.getPrdApiAdminName());
			}
			//map.put("projectManager","xueyukun");
			//api为高密级
			if(api.getSecretLevel()==2){
				map.put("isHighSecurity",1);
				if(InstanceEnvironment.fromCode(environment).getCode()==0){//当前为测试环境
					if(systemInfo.getDevApiTenantName()==null){
						throw new NotExist("租户管理员为空，流程启动失败");
					}
					map.put("tenantManager",systemInfo.getDevApiTenantName());
				}else {
					if(systemInfo.getPrdApiTenantName()==null){
						throw new NotExist("租户管理员为空，流程启动失败");
					}
					map.put("tenantManager",systemInfo.getPrdApiTenantName());
				}
			}else {
				map.put("isHighSecurity",0);
			}
			//map.put("tenantManager","xueyukun");
			map.put("link",link);
			map.put("im_link",imLink);
			WorkFlowDto flowDto = new WorkFlowDto();
			flowDto.setUserID(user);
			flowDto.setProcessDefName(BpmConstant.SUBSCRIBE_API);
			flowDto.setProcessInstName("订阅API");
			flowDto.setRelaDatas(map);
			flowDto.setUserName(user);
			Map map1 = new HashMap();
			map1.put("theme",String.format("%s-订阅-%s的API-%s",systemName,dataItemRepository.findOne(api.getSystemId()).getItemName(),api.getName()));
			flowDto.setBizInfo(map1);
			Result<Object> result = workFlowService.startProcess(flowDto);
			if(Result.OK.equals(result.getCode()) && null != result.getData()){
				JSONObject resultObj = (JSONObject)result.getData();
				if(null != resultObj){
					pr.setProcessInstID(String.valueOf(resultObj.get("processInstID")));
					processRecordRepository.saveAndFlush(pr);
				}
			}else {
				subscribeFlag=false;
				buffer.append(result.getMsg());
			}
		}
		return subscribeFlag;
	}

	/**
	 * 同步方法获取3scale中用户信息，避免并发操作引起重复创建用户
	 * @param user
	 * @param ins
	 * @param buffer
	 * @return
	 */
	public   UserInstanceRelationship getUserAccount(String user,Instance ins,StringBuffer buffer){
		UserInstanceRelationship userInsRel =
					userInstanceRelationshipRepository.findByUserAndInstanceId(user,ins.getId());
		Long accountId = getAccountId(user, ins);
		if (userInsRel == null) {
			userInsRel = new UserInstanceRelationship();
			userInsRel.setInstanceId(ins.getId());
			userInsRel.setUserName(user);
			if(accountId != null){
				userInsRel.setAccountId(accountId);
				userInsRel=userInstanceRelationshipRepository.saveAndFlush(userInsRel);
			}else {
				buffer.append("用户在3scale中对应的accountId创建失败！");
				return null;
			}
		}
		if (null == userInsRel.getAccountId()) {
			if(accountId != null){
				userInsRel.setAccountId(accountId);
				userInsRel=userInstanceRelationshipRepository.saveAndFlush(userInsRel);
			}else {
				buffer.append("用户在3scale中对应的accountId创建失败！");
				return null;
			}
		}
		return userInsRel;
	}
	private Long getAccountId(String user, Instance ins) {
		Account account = new Account();
		account.setAccessToken(ins.getAccessToken());
		account.setUsername(user);
		AccountDto accountFind =
				accountStud.accountFind(ins.getHost(), account);
		if (null == accountFind || null == accountFind.getAccount()) {
			account.setEmail(user + "@hisense.com");
			account.setPassword("123456");
			account.setOrgName("hisense" + RandomStringUtils.random(6, true, true).toLowerCase());
			Result<AccountDto> accountDtoResult = accountStud.signUp(ins.getHost(), account);
			AccountDto accountDto = accountDtoResult.getData();
			return accountDto.getAccount().getId();
		}
		return accountFind.getAccount().getId();
	}

	@Override
	public ApplicationDtos getApplicationListByAccount(String domainName,
			ApplicationForm applicationForm) {
		Domain domain = 
    			domainRepository.searchDomainByName(domainName);
    	
    	if (null == domain) {
    		log.error("can not found domain,domain is {}",domainName);
    		throw new OperationFailed("domain not exist");
    	}
    	applicationForm.setAccessToken(domain.getAccessToken());
    	ApplicationDtos applicationDtos = null;
    	applicationDtos = applicationStud.getApplicationListByAccount(domain.getHost(), 
    			applicationForm);
    	return applicationDtos;
	}

	@Override
	public ApplicationXmlDtos findApplicationListByAccount(String domainName, ApplicationForm applicationForm) {
		Domain domain =
				domainRepository.searchDomainByName(domainName);

		if (null == domain) {
			log.error("can not found domain,domain is {}",domainName);
			throw new OperationFailed("domain not exist");
		}
		applicationForm.setAccessToken(domain.getAccessToken());
		ApplicationXmlDtos applicationXmlDtos = null;
		applicationXmlDtos = applicationStud.findApplicationListByAccount(domain.getHost(),
				applicationForm);
		return applicationXmlDtos;
	}

	@Override
	public Page<ProcessRecordDto> applicationList(String environment, String apiName, List<Integer> system,
												  PageRequest pageable) {
		Integer envCode = InstanceEnvironment.fromCode(environment).getCode();
		String user = CommonBeanUtil.getLoginUserName();
		Specification<PublishApplication> appSpec = (root, query, builder) -> {
			List<Predicate> andList = new LinkedList<>();
			if(user != null){
				andList.add(builder.equal(root.get("creator").as(String.class), user));
			}
			andList.add(builder.equal(root.get("status").as(Integer.class),  2 ));
			andList.add(builder.equal(root.get("type").as(Integer.class),  1 ));
			andList.add(builder.equal(root.get("publishApi").get("environment").as(Integer.class), envCode));
			//按照apiName模糊查询
			if(StringUtils.isNotBlank(apiName)){
				andList.add(builder.like(root.get("publishApi").get("name").as(String.class),  "%" + apiName + "%") );
			}
			return builder.and(andList.toArray(new Predicate[andList.size()]));
		};
		List<PublishApplication> publishApplicationList = publishApplicationRepository.findAll(appSpec);
		if (CollectionUtils.isEmpty(publishApplicationList)) {
			throw new NotExist("app not exist");
		}
		//查询订阅记录
		Specification<ProcessRecord> spec4 = (root, query, builder) -> {
			List<Predicate> andList = new LinkedList<>();
			CriteriaBuilder.In<Integer> in = builder.in(root.get("relId"));
			for (PublishApplication publishApplication : publishApplicationList) {
				in.value(publishApplication.getId());
			}
			andList.add(in);
			if(user != null){
				log.info("current login user "+user);
				andList.add(builder.equal(root.get("creator").as(String.class), user));
			}
			andList.add(builder.equal(root.get("type").as(String.class), "application"));
			return builder.and(andList.toArray(new Predicate[andList.size()]));
		};
		Page<ProcessRecord> p = processRecordRepository.findAll(spec4, pageable);
		List<ProcessRecord> processRecords = p.getContent();
		if (CollectionUtils.isEmpty(processRecords)) {
			throw new NotExist("ProcessRecord not exist");
		}

		List<ProcessRecordDto> processRecordDtoList = new ArrayList<>();
		for (ProcessRecord processRecord : processRecords) {
			ProcessRecordDto processRecordDto = new ProcessRecordDto(processRecord);
			PublishApplication publishApplication = publishApplicationRepository.findOne(processRecord.getRelId());
			ProcessDataDto processDataDto = JSONObject.parseObject(processRecord.getExtVar(), ProcessDataDto.class);
			log.info("processDataDto={}",processDataDto);
			if (null != processDataDto && processDataDto.isValid()) {
				PublishApiGroup publishApiGroup = publishApiGroupRepository.findOne(processDataDto.getGroupId());
				DataItem dataItem = dataItemRepository.findOne(processDataDto.getAppSystem());
				if (dataItem != null) {
					processRecordDto.setAppSystemName(dataItem.getItemName());
				}
				processRecordDto.setGroupName(publishApiGroup.getName());
			}

			processRecordDto.setSystem(publishApplication.getSystem());
			processRecordDto.setApiId(publishApplication.getPublishApi().getId());
			processRecordDto.setState(publishApplication.getState());
			processRecordDto.setApiName(publishApplication.getPublishApi().getName());
			processRecordDtoList.add(processRecordDto);
		}

		Page<ProcessRecordDto> data = new PageImpl<>(processRecordDtoList, pageable, p.getTotalElements());
		return data;
	}

    @Override
    public Result<List<String>> unSubscribeApi(PublishApiBatch publishApiBatch) {
        Result<List<String>> result = new Result<>(FAIL, "取消订阅APi失败", null);
        if (null == publishApiBatch || CollectionUtils.isEmpty(publishApiBatch.getIds())) {
            result.setError(FAIL, "请选择要取消订阅的APi！");
            return result;
        }
        List<Integer> recordIds = publishApiBatch.getIds();
        List<String> failMessage = new ArrayList<>();
        recordIds.forEach(recordId -> {
            Result<Boolean> oneResult = unSubscribeApiEach(recordId);
            if (oneResult.isFailure()) {
                String message = String.format("取消订阅失败, 编号[%d]", recordId);
                log.info(message);
                failMessage.add(message);
            }
        });

        int failCount = failMessage.size();
        int successCount = recordIds.size() - failCount;

        String endMessage;
		String errorCode = FAIL;
        if (failCount == 0 && successCount > 0) {
            endMessage = String.format("成功取消订阅%d个API", successCount);
			errorCode = OK;
        } else if (successCount > 0) {
            endMessage = String.format("成功取消订阅%d个API, 未能取消订阅%d个API", successCount, failCount);
			errorCode = OTHER;
        } else {
            endMessage = String.format("失败,未能取消订阅%d个API", failCount);
        }
        result.setCode(errorCode);
        result.setMsg(endMessage);
        result.setData(failMessage);
        return result;
    }

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<Object> modifyAuthSecret(Integer prId) {
		Result<Object> result = new Result<>(OK,"重置密匙成功！",null);
		result.setAlert(1);
		if(null == prId){
			result.setError("重置密匙失败：processRecord id 不能为空!");
			return result;
		}
		ProcessRecord pr = processRecordRepository.findOne(prId);
		if(null == pr.getRelId()){
			result.setError("重置密匙失败：processRecord 不存在!");
			return result;
		}
		PublishApplication app = publishApplicationRepository.findOne(pr.getRelId());
		if(null == pr.getRelId()){
			result.setError("重置密匙失败：application 不存在!");
			return result;
		}
		String scaleApplicationId = app.getScaleApplicationId();
		String user = CommonBeanUtil.getLoginUserName();
		Instance instance = app.getInstance();
		Application scaleApplication = new Application();
		scaleApplication.setAccessToken(instance.getAccessToken());
		scaleApplication.setId(scaleApplicationId);

		UserInstanceRelationship userInsRel = userInstanceRelationshipRepository.findByUserAndInstanceId(user,instance.getId());
		Result<Application> applicationResult = accountStud.readApplication(instance.getHost(), userInsRel.getAccountId().toString(), scaleApplication);
		log.info("********read application,{}", JSONObject.toJSON(applicationResult).toString());
		if (applicationResult == null || "1".equals(applicationResult.getCode()) || null == applicationResult.getData()) {
			result.setError("重置密匙失败：Application of 3scale is not exist!");
			return result;
		}

        //更新重置密钥前的调用量
        apiInvokeRecordService.saveInvokeCount(app);

		Application application = applicationResult.getData();
		String userKey = application.getUserKey();
		String appId =application.getApplicationId();
		String appKey =application.getKeys()==null?"":application.getKeys().getKey()==null?"":application.getKeys().getKey().get(0);
		//1、修改3scale的application的userKey或者appkey
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		String newScaleApplicationId=null;
		if(StringUtils.isNotBlank(application.getUserKey())){
			//修改userkey（删除原来的application，创建新的application）
			application.setAccessToken(instance.getAccessToken());
			application.setUserKey(uuid);
			Result<Application> newApplicationResult = accountStud.createApplication(instance.getHost(), userInsRel.getAccountId().toString(), application);
			log.info("createApplication applicationResult:"+ JSON.toJSONString(newApplicationResult.getData()));
			Application applicationRes = newApplicationResult.getData();
			if(FAIL.equals(newApplicationResult.getCode()) || null==applicationRes){
				log.error("3scale create error:"+applicationResult.getMsg());
				result.setError("重置密匙失败："+applicationResult.getMsg());
				return result;
			}
			newScaleApplicationId = applicationRes.getId();
			userKey = applicationRes.getUserKey();
			applicationStud.applicationDelete(instance.getHost(), instance.getAccessToken(),
					userInsRel.getAccountId().toString(),scaleApplicationId);
		}else if(null != application.getKeys() && !CollectionUtils.isEmpty(application.getKeys().getKey())){
			//删除旧的key，使用新的key
			Result<Application> newAppRel = accountStud.createNewKey(instance.getHost(), instance.getAccessToken(),
					userInsRel.getAccountId().toString(), scaleApplicationId, uuid, executor);
			if(FAIL.equals(newAppRel.getCode())){
				log.error("3scale appKey update error:"+newAppRel.getMsg());
				result.setError("重置密匙失败："+newAppRel.getMsg());
				return result;
			}else {
				if(null != newAppRel.getData()){
					appId =newAppRel.getData().getApplicationId();
					appKey =newAppRel.getData().getKeys()==null?"":newAppRel.getData().getKeys().getKey()==null?"":newAppRel.getData().getKeys().getKey().get(0);
				}
			}
			newScaleApplicationId = scaleApplicationId;
		}

		//2、修改使用该application的数据（为了调用历史数据统计，保留原application和record）
		Specification<PublishApplication> appSpec = (root, query, builder) -> {
			List<Predicate> andList = new LinkedList<>();
			andList.add(builder.equal(root.get("scaleApplicationId").as(String.class), scaleApplicationId));
			andList.add(builder.equal(root.get("type").as(Integer.class), app.getType()));
			andList.add(builder.equal(root.get("instance").get("id").as(Integer.class), app.getInstance().getId()));
			return builder.and(andList.toArray(new Predicate[andList.size()]));
		};
		List<PublishApplication> publishApplicationList = publishApplicationRepository.findAll(appSpec);
		if (!CollectionUtils.isEmpty(publishApplicationList)) {
			String finalNewScaleApplicationId = newScaleApplicationId;
			String finalUserKey = userKey;
			String finalAppId = appId;
			String finalAppKey = appKey;
			publishApplicationList.stream().forEach(item->{
				//更新3scale的applicationid和userkey;同时生成一条历史记录
				ProcessRecord oldRecd = processRecordRepository.findByApiId(item.getId());
				//1、生成历史纪录(record和application)

				PublishApplication newApp = new PublishApplication();
				BeanUtils.copyProperties(item,newApp);
				newApp.setStatus(4);
				newApp.setId(null);
				newApp.setCreateTime(new Date());
				newApp.setUpdateTime(new Date());
				newApp = publishApplicationRepository.saveAndFlush(newApp);

				ProcessRecord newRecd = new ProcessRecord();
				BeanUtils.copyProperties(oldRecd,newRecd);
				newRecd.setStatus(4);
				newRecd.setId(null);
				newRecd.setCreateTime(new Date());
				newRecd.setUpdateTime(new Date());
				newRecd.setRelId(newApp.getId());
				processRecordRepository.saveAndFlush(newRecd);
				//2、更新原数据
				item.setUpdateTime(new Date());
				item.setUserKey(finalUserKey);
				item.setAppId(finalAppId);
				item.setAppKey(finalAppKey);
				if(!scaleApplicationId.equals(finalNewScaleApplicationId)){
					item.setScaleApplicationId(finalNewScaleApplicationId);
				}
				publishApplicationRepository.saveAndFlush(item);
				//异步更新appId、appKey
				apiInvokeRecordService.updateApiIdAndAppKey(item.getId());

			});
		}
		result.setData(userKey);
		return result;
	}

    /**
     * 单个取消订阅api
     *
     * @param id
     * @return
     */
    public Result<Boolean> unSubscribeApiEach(Integer id) {
        Result<Boolean> returnResult = new Result<>();
        ProcessRecord processRecord = processRecordRepository.findOne(id);
        if (null == processRecord.getId()) {
            returnResult.setCode(FAIL);
            returnResult.setMsg("ProcessRecord不存在");
            returnResult.setData(false);
            return returnResult;
        } else {
            PublishApplication publishApplication = publishApplicationRepository.findOne(processRecord.getRelId());
            if (null == publishApplication.getId()) {
                returnResult.setCode(FAIL);
                returnResult.setMsg("Application不存在");
                returnResult.setData(false);
                return returnResult;
            }
			PublishApi api = publishApiRepository.findOne(publishApplication.getPublishApi().getId());
            if (processRecord.getStatus() == 2) {
            	if(api.getId() == null){
            		log.error("api not exit");
				}
                //如果审批通过
				if (api != null && api.getStatus() != 0) {
                    // 同时所属api没有被删除，此时才需要到3scale上去删除数据
                    Instance instance = publishApplication.getInstance();
                    if (instance == null) {
                        log.error("can not found instance");
                        throw new OperationFailed("instance not exist");
                    }

                    String user = publishApplication.getCreator();
                    if (user == null) {
                        log.error("can not found user");
                        throw new OperationFailed("user not exist");
                    }
                    apiInvokeRecordService.saveInvokeCount(publishApplication);
                    UserInstanceRelationship userInstanceRelationship =
                            userInstanceRelationshipRepository.findByUserAndInstanceId(user, instance.getId());
                    applicationStud.applicationDelete(instance.getHost(), instance.getAccessToken(),
                            userInstanceRelationship.getAccountId().toString(),
                            publishApplication.getScaleApplicationId());
                }
                // 将本地app状态修改为删除,并解除与scale的关联
                publishApplication.setStatus(0);
                publishApplicationRepository.saveAndFlush(publishApplication);
            }

			//同时把该系统下其他人订阅（包含修改密匙之前的数据）的记录状态也修改为删除（0）
			List<Integer> applicationIds = new ArrayList<>();
			List<Integer> status = new ArrayList<>();
			status.add(2);
			status.add(4);
			List<PublishApplication> subscribedApis
					= publishApplicationRepository.findSubscribedApiBySystemAndStatus(publishApplication.getSystem(),api.getId(), status,ApiConstant.APPLICATION_TYPE_SUBSCRIBE_API);
			if(!CollectionUtils.isEmpty(subscribedApis)){
				for(PublishApplication app:subscribedApis){
					applicationIds.add(app.getId());
					app.setStatus(0);
					publishApplicationRepository.saveAndFlush(app);
				}
				List<ProcessRecord> prs = processRecordRepository.findSubscribeByRelIdAndStatus(applicationIds, status);
				if(!CollectionUtils.isEmpty(prs)){
					for(ProcessRecord pr:prs){
						pr.setStatus(0);
						processRecordRepository.saveAndFlush(pr);
					}
				}
			}
            // 设置所选APP记录为i删除状态
            processRecord.setStatus(0);
            processRecordRepository.saveAndFlush(processRecord);

			//取消订阅，会把告警策略数据全部重新推送到kafka-liyouzhi.ex 20-11-18
			alertPolicyService.syncAlertPolicyToKafkaAsync();
            returnResult.setCode(OK);
            returnResult.setMsg("取消订阅成功！");
            returnResult.setData(true);
			//取消订阅操作记录
			String msg = OK.equals(returnResult.getCode())?OperationType.fromCode(7).getName():OperationType.fromCode(8).getName()+returnResult.getMsg();
			String name = OperationType.fromCode(5).getName()+api.getName();
			OperationApi operationApi = new OperationApi(name,CommonBeanUtil.getLoginUserName(),new Date(),new Date(),5,msg,api);
			operationApiRepository.save(operationApi);
            return returnResult;
        }
    }

}
