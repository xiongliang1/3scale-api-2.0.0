/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.stud;

import com.hisense.gateway.library.stud.model.ApplicationDtos;
import com.hisense.gateway.library.stud.model.ApplicationSynDtos;
import com.hisense.gateway.library.stud.model.ApplicationXml;
import com.hisense.gateway.library.stud.model.ApplicationXmlDtos;
import com.hisense.gateway.library.web.form.ApplicationForm;

public interface ApplicationStud {
    ApplicationDtos getApplicationListByAccount(String host, ApplicationForm applicationForm);

    ApplicationXmlDtos findApplicationListByAccount(String host, ApplicationForm applicationForm);

    ApplicationXml applicationRead(String host, String accessToken, String accountId, String scaleApplicationId);

    ApplicationXml applicationSuspend(String host, String accessToken, String accountId, String scaleApplicationId);

    ApplicationXml applicationResume(String host, String accessToken, String accountId, String scaleApplicationId);

    Boolean applicationDelete(String host, String accessToken, String accountId, String scaleApplicationId);

    ApplicationDtos getAllApplicationList(String host, String accessToken);

    ApplicationSynDtos getAllApplicationList2(String host, String accessToken);
}
