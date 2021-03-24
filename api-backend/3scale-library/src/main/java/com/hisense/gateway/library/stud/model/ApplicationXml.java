/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @author douyanwei
 * 2019/11/25
 */
package com.hisense.gateway.library.stud.model;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "application")
@XmlType(propOrder = {
        "id",
        "createdAt",
        "state",
        "userAccountId",
        "serviceId",
        "userKey",
        "applicationId",
        "keys",
        "providerVerificationKey",
        "plan",
        "name",
        "description"
})
public class ApplicationXml implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    @XmlElement(name = "created_at")
    private String createdAt;

    @XmlElement(name = "state")
    private String state;

    @XmlElement(name = "user_account_id")
    private String userAccountId;

    @XmlElement(name = "service_id")
    private String serviceId;

    @XmlElement(name = "user_key")
    private String userKey;

    @XmlElement(name = "application_id")
    private String applicationId;

    @XmlElement(name = "keys")
    private Keys keys;

    @XmlElement(name = "provider_verification_key")
    private String providerVerificationKey;

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "description")
    private String description;

    @XmlElement(name = "plan")
    private PlanXml plan;
}
