/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.PromoteRequestInfo;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.library.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.API_PROJECT_STATUS;
import static com.hisense.gateway.library.constant.BaseConstants.MSG_PROJECT_STATUS;

/**
 * UserServiceImp
 *
 * @author wangjinshan
 * @version v1.0
 * @date 2019-11-20 10:35
 */
@Slf4j
@Service
public class MailServiceImpl implements MailService {

    @Autowired
    PublishApiRepository publishApiRepository;

    @Autowired
    PublishApplicationRepository publishApplicationRepository;

    @Autowired
    ProcessRecordRepository processRecordRepository;

    @Autowired
    DataItemRepository dataItemRepository;

    @Autowired
    PublishApiInstanceRelationshipRepository publishApiInstanceRelationshipRepository;

/*
    @Autowired
    MessageService messageService;
*/

    @Value("${project.admin}")
    private String admin;
//
//    @Value("${pangea.message.secret-key}")
//    private String secretKey;

    /**
     * 接口升级不兼容发送通知邮件
     * @param promoteRequestInfo
     */
    @Override
    @Async
    public void NotCompatibleSendMail(PromoteRequestInfo promoteRequestInfo) {

        PublishApi publishApi = publishApiRepository.findOne(promoteRequestInfo.getId());

        //所有订阅者
        List<String> subscribeUsers = getSubscribeUsers(promoteRequestInfo.getId());

        DataItem item = dataItemRepository.findOne(publishApi.getSystemId());
        String publishSystem = null;
        if (item != null){
            publishSystem = item.getItemName();
        }
        String content = String.format("您订阅的系统【%s】发布的API：【%s】进行了升级，逻辑上不兼容，需要修改代码。",publishSystem, publishApi.getName());
        String subject = "融合集成平台-接口升级不兼容提醒";
//        sendMail(content, subscribeUsers, subject);

    }

    /**
     * 接口下线发送通知邮件
     * @param apiId
     */
    @Override
    @Async
    public void ApiOfflineSendMail(Integer apiId) {

        PublishApi publishApi = publishApiRepository.findOne(apiId);

        //所有订阅者
        List<String> subscribeUsers = getSubscribeUsers(apiId);
        //api创建者
        subscribeUsers.add(publishApi.getCreator());
        System.out.println("=============="+subscribeUsers);

        DataItem item = dataItemRepository.findOne(publishApi.getSystemId());
        String publishSystem = null;
        if (item != null){
            publishSystem = item.getItemName();
        }
        String content = String.format("系统【%s】发布的API：【%s】已下线，请系统进行相应的调整，谢谢！",publishSystem, publishApi.getName());
        String subject = "融合集成平台-接口下线提醒";
//        sendMail(content, subscribeUsers, subject);

    }

    /**
     * 接口修改发送邮件通知
     * @param apiId
     */
    @Override
    public void ApiUpdateSendMail(Integer apiId) {
        PublishApi publishApi = publishApiRepository.findOne(apiId);

        //所有订阅者
        List<String> subscribeUsers = getSubscribeUsers(apiId);
        //api创建者
        subscribeUsers.add(publishApi.getCreator());

        DataItem item = dataItemRepository.findOne(publishApi.getSystemId());
        String publishSystem = null;
        if (item != null){
            publishSystem = item.getItemName();
        }
        String content = String.format("您订阅的系统【%s】发布的API：【%s】已修改，请系统进行相应的调整，谢谢！",publishSystem, publishApi.getName());
        String subject = "融合集成平台-接口修改提醒";
//        sendMail(content, subscribeUsers, subject);
    }

    /**
     * 订阅审批通过发送邮件通知
     * @param recordId
     */
    @Override
    public void ApiApprovalSendMail(Integer recordId) {
        //查询订阅记录
        ProcessRecord record = processRecordRepository.findOne(recordId);
        if(null == record || null == record.getRelId()){
            log.error("record {} or pa  not exist", recordId);
        }else {
            //审核通过
            if(2 == record.getStatus()){
                //查询对应的pa（及api信息）
                PublishApplication pa = publishApplicationRepository.findOne(record.getRelId());
                //查询订阅系统名称
                DataItem item = dataItemRepository.findOne(pa.getSystem());
                String subscribeSystem = null;
                if (item != null){
                    subscribeSystem = item.getItemName();
                }
                //所有订阅者
                List<String> publishUsers = new ArrayList<>();
                publishUsers.add(pa.getCreator());
                String content = String.format("系统【%s】已订阅发布的API：【%s】，审批通过，谢谢！",subscribeSystem, pa.getPublishApi().getName());
                String subject = "融合集成平台-接口订阅审批通过";
//                sendMail(content, publishUsers, subject);
            }
        }
    }

    /**
     * 审批拒绝发送邮件通知
     * @param recordId
     */
    @Override
    public void ApiRefuseSendMail(Integer recordId) {
        //查询订阅记录
        ProcessRecord record = processRecordRepository.findOne(recordId);
        if(null == record || null == record.getRelId()){
            log.error("record {} or pa  not exist", recordId);
        }else {
            //审核拒绝
            if(3 == record.getStatus()){
                //查询对应的pa（及api信息）
                PublishApplication pa = publishApplicationRepository.findOne(record.getRelId());
                //查询订阅系统名称
                DataItem item = dataItemRepository.findOne(pa.getSystem());
                String subscribeSystem = null;
                if (item != null){
                    subscribeSystem = item.getItemName();
                }
                //所有订阅者
                List<String> publishUsers = new ArrayList<>();
                publishUsers.add(pa.getCreator());
                String content = String.format("系统【%s】已订阅发布的API：【%s】，审批被拒绝，谢谢！",subscribeSystem, pa.getPublishApi().getName());
                String subject = "融合集成平台-接口订阅审批拒绝";
//                sendMail(content, publishUsers, subject);
            }
        }
    }

    /**
     * 发布审批拒绝发送邮件
     * @param recordId
     */
    @Override
    public void PromoteRejectSendMail(Integer recordId) {
        //查询订阅记录
        ProcessRecord record = processRecordRepository.findOne(recordId);
        if(null == record || null == record.getRelId()){
            log.error("record {} or pa  not exist", recordId);
        }else {
            //审核拒绝
            if(3 == record.getStatus()){
                //查询对应api
                PublishApi api = publishApiRepository.findOne(record.getRelId());
                //api创建人
                List<String> publishUsers = new ArrayList<>();
                publishUsers.add(api.getCreator());
                DataItem dataItem = dataItemRepository.findOne(api.getGroup().getSystem());
                String content = String.format("系统【%s】发布的API：【%s】，审批被拒绝，谢谢！",dataItem.getItemName(), api.getName());
                String subject = "融合集成平台-接口发布审批拒绝";
              /*  SendResultDTO resultDTO =  sendMail(content, publishUsers, subject);
                if(null == resultDTO){
                    log.error("发布审批拒绝信息发送失败！");
                }*/
            }
        }
    }

    @Override
    public Result<String> ApiApprovalUrgeSendMail(Integer recordId) {
        Result<String> result = new Result<>(Result.OK,"催办成功！");
        try{
            //查询订阅记录
            ProcessRecord record = processRecordRepository.findOne(recordId);
            if(null == record || null == record.getRelId()){
                log.error("record {} or pa  not exist", recordId);
                result.setError(Result.FAIL,"催办异常：订阅记录不存在！");
                return result;
            }else{
                if(2 == record.getStatus()){
                    result.setError(Result.FAIL,"该订阅已审批，无需催办！");
                    return result;
                }
                //查询对应的pa（及api信息）
                PublishApplication pa = publishApplicationRepository.findOne(record.getRelId());
                //查询订阅系统名称
                DataItem item = dataItemRepository.findOne(pa.getSystem());
                String subscribeSystem = null;
                if (item != null){
                    subscribeSystem = item.getItemName();
                }
                //系统发布者
                List<String> publishUsers = new ArrayList<>();
                publishUsers.add(pa.getPublishApi().getCreator());
                //组装邮件内容和标题
                String content = String.format("系统【%s】已订阅发布的API：【%s】，请您及时审批，谢谢！",subscribeSystem, pa.getPublishApi().getName());
                String subject = "融合集成平台-接口订阅审批催办";

                return result;
            }
        }catch (Exception e){
            log.error("催办异常：",e);
            result.setError(Result.FAIL,"催办异常：请联系管理员或者IT人员处理！");
            return result;
        }/*  SendResultDTO resultDTO =  sendMail(content, publishUsers, subject);
                if(null == resultDTO){
                    result.setError(Result.FAIL,"催办消息发送失败！");
                }
                if(null != resultDTO && !CollectionUtils.isEmpty(resultDTO.getData())){
                    JSONObject jsonObj=new JSONObject(resultDTO.getData());
                    result.setData(jsonObj.toString());
                }*/
    }

    /**
     * 通知管理员添加系统
     * @param systemName
     */
    @Override
    public void createSystemSendMail(String systemName,String userName) {
        String content = String.format("%s为【%s】系统订阅时，未找到该系统，请管理员及时添加，谢谢！",userName,systemName);
        String subject = "融合集成平台-通知管理员添加系统";
        List<String> user = new ArrayList<>();
        user.add(admin);
//        sendMail(content,user,subject);
    }

    @Override
    public Result<String> SystemSendEmail(String systemName, String user,Integer type) {
        String name = null;
        if (API_PROJECT_STATUS.equals(type)){
            name = "服务管理";
        }else if (MSG_PROJECT_STATUS.equals(type)){
            name = "消息管理";
        }
        String content = String.format("【%s】在%s中申请创建系统【%s】时，未找到该系统，请管理员及时添加，谢谢！",user,name,systemName);
        return sendMailBySystem(content);
    }

    private Result<String> sendMailBySystem(String content){
        Result<String> result = new Result<>(Result.OK,"邮件发送成功，等待管理员通知");
        result.setAlert(1);
        String subject = "融合集成平台-通知管理员添加系统";
        List<String> user = new ArrayList<>();
        user.add(admin);
      /*  SendResultDTO resultDTO = sendMail(content, user, subject);
        if (null == resultDTO){
            result.setError(Result.FAIL,"通知管理员添加系统消息发送失败！");
        }
        if(null != resultDTO && !CollectionUtils.isEmpty(resultDTO.getData())){
            JSONObject jsonObj=new JSONObject(resultDTO.getData());
            result.setData(jsonObj.toString());
        }*/
        return result;
    }

    /**
     * 根据APIID获取该API的订阅用户信息列表
     * @param apiId
     * @return
     */
    private List<String> getSubscribeUsers(Integer apiId) {

        PublishApi publishApi = publishApiRepository.findOne(apiId);

        List<PublishApplication> subscribedAppsForApi =
                publishApplicationRepository.findByApiIdAndStatusAndType(apiId, Arrays.asList(1,2),1);
        //所有订阅者
        List<String> subscribeUsers = new ArrayList<>();
        for (PublishApplication pa : subscribedAppsForApi) {
            if (pa.getSystem() == null || pa.getUserKey() == null) {
                log.error("pa {} has invalid system", pa);
                continue;
            }

            if (pa.getInstance() == null || pa.getInstance().getId() == null) {
                log.error("pa {} has invalid instance", pa);
                continue;
            }

            DataItem item = dataItemRepository.findOne(pa.getSystem());
            if (item == null) {
                log.error("pa {} has no found system", pa);
                continue;
            }

            PublishApiInstanceRelationship relationship =
                    publishApiInstanceRelationshipRepository.getByAPIidAndInstanceId(apiId, pa.getInstance().getId());

            if (relationship == null) {
                log.error("pa {} has no found valid instance", pa);
                continue;
            }

            subscribeUsers.add(pa.getCreator());
        }
        log.info("api: {}" + publishApi.getName() + "update inflect users is : {}" + subscribeUsers.toString());
        return subscribeUsers;
    }
    /**
     * 发送邮件
     * @param content 邮件内容
     * @param users 发送人
     * @param subject 主题
     */
  /*  public SendResultDTO sendMail(String content, List<String> users, String subject){
        //临时添加
        if (!CollectionUtils.isEmpty(users)){
            for (int i=0;i<users.size();i++){
                if("hicloud".equals(users.get(i))){
                    users.set(i,"liuyanan5");
                }
            }
        }
       // if (!users.isEmpty()){
        SendMessageDTO messageDto =  new SendMessageDTO();
        messageDto.setSysCode("S0052");
        messageDto.setMsgName("融合集成平台消息");
//        messageDto.setSecretKey(secretKey);
        messageDto.setSendType("1");
        messageDto.setUserName("融合集成平台");
        messageDto.setContentEmail(content);
        messageDto.setSubject(subject);
        messageDto.setUser(users);
        log.info("send mail params is : {}" ,messageDto.toString());
        SendResultDTO resultDto = messageService.sendCommonMessage(messageDto);
        log.info("send mail result is : {}" ,resultDto.toString());

        return  resultDto;
    }*/

}
