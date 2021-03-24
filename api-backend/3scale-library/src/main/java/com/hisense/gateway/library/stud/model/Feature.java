/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/11/25 @author peiyun
 */
package com.hisense.gateway.library.stud.model;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "feature")
@XmlType(propOrder = {
        "id",
        "name",
        "systemName",
        "serviceId",
        "scope",
        "visible",
        "description"
})
public class Feature implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;

    @XmlElement(name = "system_name")
    private String systemName;

    @XmlElement(name = "service_id")
    private String serviceId;

    private String scope;
    private Boolean visible;
    private String description;
}
