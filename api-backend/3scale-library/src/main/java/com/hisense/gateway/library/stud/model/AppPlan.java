package com.hisense.gateway.library.stud.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppPlan {
    private String id;
    private String accessToken;
    private String name;
    private String type;
    private String state;
    private String setupFee;
    private String costPerMonth;
    private String trialPeriodDays;
    private String serviceId;

    public AppPlan(String id, String name) {
        this.id = id;
        this.name = name;
    }

}
