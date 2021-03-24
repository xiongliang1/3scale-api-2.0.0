/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2019/11/25
 */
package com.hisense.gateway.management.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.stud.model.Application;
import com.hisense.gateway.library.stud.model.ApplicationDtos;
import com.hisense.gateway.library.stud.model.ApplicationSearchForm;

import java.text.ParseException;

public interface AccountsService {
    ApplicationDtos getApplicationList(String domain, String userName, int pageNum, int pageSize);

    ApplicationDtos getApplicationListBySearch(String domain, String userName, ApplicationSearchForm form) throws ParseException;

    Application getApplication(String domain, String userName, String id);

    String deleteApplication(String domain, String userName, String id);

    Result<Application> updateApplication(String domain, String userName, Application application);

    Result<Application> createApplication(String domain, String userName, Application application);

    Result<Application> createNewKey(String domain, String userName, String applicationId);

    String deleteKey(String domain, String userName, String applicationId, String key);
}
