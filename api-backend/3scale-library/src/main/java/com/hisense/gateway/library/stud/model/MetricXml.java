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

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "metric")
@XmlType(propOrder = {
        "id",
        "name",
        "systemName",
        "friendlyName",
        "description",
        "unit"
})
public class MetricXml implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;

    @XmlElement(name = "system_name")
    private String systemName;

    @XmlElement(name = "friendly_name")
    private String friendlyName;

    private String description;

    private String unit;
}
