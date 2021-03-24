/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.stud;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.stud.model.*;

import java.util.concurrent.ExecutorService;

public interface AccountStud {
    Result<AccountDto> signUp(String host, Account account);

    AccountDto accountFind(String host, Account account);

    ApplicationDtos appDtoList(String host, String accessToken, String accountId);

    ApplicationXmlDtos appXmlDtoList(String host, String accessToken, String accountId);

    ApplicationDto appDto(String host, String accessToken, String accountId, String id);

    ApplicationXml appXml(String host, String accessToken, String accountId, String id);

    String delAppDto(String host, String accessToken, String accountId, String id);

    Result<Application> readApplication(String host, String accountId, Application application);
    Result<Application> updateApplication(String host, String accountId, Application application);

    Result<Application> createApplication(String host, String accountId, Application application);

    ApplicationXml addAppAndReturnKey(String host, String accessToken, String accountId, String appPlanId,
                                      String appName, String appDesc);

    Result<Application> createNewKey(String host, String accessToken, String accountId, String applicationId,
                                     String key, ExecutorService executorService);

    String deleteKey(String host, String accessToken, String accountId, String applicationId, String key);

    AccountSynDto accountFindById(String host, Account account);

    AccountSyn accountUserFindById(String host, Account account);

    ApplicationXml appXml2(String host, String accessToken, String accountId, String id);
}
