package com.hisense.gateway.library.stud;

import com.hisense.gateway.library.stud.model.AppPlanDto;

public interface ApplicationPlanStud {
    AppPlanDto createApplicationPlan(String host, String accessToken, String serviceId, String name, String systemName);

    void updateApplicationPlan(String host, String accessToken, String serviceId, String scalePlanId, Integer stateEvent);
}
