/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/2/24
 */
package com.hisense.gateway.library.stud;

import com.hisense.gateway.library.stud.model.Limit;
import com.hisense.gateway.library.stud.model.LimitDto;

public interface LimitStud {
    Limit limitCreate(String host, String accessToken, Limit limit);

    Limit limitUpdate(String host, String accessToken, Limit limit);

    void limitDelete(String host, String accessToken, Limit limit);

    LimitDto limitsListPerApplicationPlan(String host, String accessToken, String scalePlanId);
}
