/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2019/12/5
 */
package com.hisense.gateway.library.stud.model;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "metrics")
@XmlType(propOrder = {
        "metric",
        "method"
})
public class MetricXmls implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "metric")
    private List<MetricXml> metric;

    @XmlElement(name = "method")
    private List<MethodXml> method;
}
