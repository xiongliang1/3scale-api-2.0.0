/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.PromoteRequestInfo;
/**
 * MailService
 *
 * @author zhangjian
 * @version v1.0
 * @date 2019-11-20 10:34
 */
public interface MailService {

    /**
     * 接口升级不兼容提醒
     * @param promoteRequestInfo
     */
    void NotCompatibleSendMail(PromoteRequestInfo promoteRequestInfo);

    /**
     * 接口下线提醒
     * @param apiId
     */
    void ApiOfflineSendMail(Integer apiId);

    /**
     * 接口修改提醒
     * @param apiId
     */
    void ApiUpdateSendMail(Integer apiId);

    /**
     * 订阅审批通过提醒
     * @param recordId
     */
    void ApiApprovalSendMail(Integer recordId);

    /**
     * 订阅审批拒绝提醒
     * @param recordId
     */
    void ApiRefuseSendMail(Integer recordId);

    /**
     * 发布审批拒绝提醒
     * @param recordId
     */
    void PromoteRejectSendMail(Integer recordId);

    /**
     * 接口订阅审批催办
     * @param recordId 订阅记录Id
     * @auth  liyouzhi.ex
     * @date 20200924
     */
    Result<String> ApiApprovalUrgeSendMail(Integer recordId);

    /**
     * 通知管理员添加系统
     * @param systemName
     */
    void createSystemSendMail(String systemName, String userName);

    Result<String> SystemSendEmail(String systemName,String user,Integer type);

}

