/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @author douyanwei
 * @date 2020/2/24
 */
package com.hisense.gateway.library.stud.model;

import lombok.Data;

import javax.xml.bind.annotation.*;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "limit")
@XmlType(propOrder = {
        "id",
        "metricId",
        "planId",
        "period",
        "value"
})
public class Limit {
    private String id;

    @XmlElement(name = "metric_id")
    private String metricId;

    @XmlElement(name = "plan_id")
    private String planId;

    private String period;

    private String value;
}
