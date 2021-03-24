/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.model.dto.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * UserInfo
 *
 * @author huhu
 * @version v1.0
 * @date 2019/2/25 13:53
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo {
    /**
     * username
     */
    @JsonProperty("username")
    @Setter(AccessLevel.NONE)
    private String username;

    /**
     * password
     */
    @JsonProperty("password")
    @Setter(AccessLevel.NONE)
    private String password;

    /**
     * displayName
     */
    @Setter(AccessLevel.NONE)
    private String displayName;

    /**
     * namespace
     */
    @Setter(AccessLevel.NONE)
    private String namespace;

    /**
     * email
     */
    @Setter(AccessLevel.NONE)
    private String email;

    /**
     * phone
     */
    @Setter(AccessLevel.NONE)
    private String phone;

    /**
     * role
     */
    @Setter(AccessLevel.NONE)
    private Integer role;

    /**
     * globalRoles
     */
    @Setter(AccessLevel.NONE)
    private List<String> globalRoles;

    /**
     * avatar
     */
    @Setter(AccessLevel.NONE)
    private String avatar;

    /**
     * balance
     */
    @Setter(AccessLevel.NONE)
    private Integer balance;

    /**
     * teamCount
     */
    @Setter(AccessLevel.NONE)
    private Integer teamCount;

    /**
     * envEdition
     */
    @JsonProperty("env_edition")
    @Setter(AccessLevel.NONE)
    private Integer envEdition;

    /**
     * migrated
     */
    @Setter(AccessLevel.NONE)
    private Integer migrated;

    /**
     * isPasswordSet
     */
    @Setter(AccessLevel.NONE)
    private Boolean isPasswordSet;

    /**
     * type
     */
    @Setter(AccessLevel.NONE)
    private Integer type;

    /**
     * comment
     */
    @Setter(AccessLevel.NONE)
    private String comment;

    /**
     * creationTime
     */
    @Setter(AccessLevel.NONE)
    private Date creationTime;

    private String uid;

    private String mobile;

    private String url;

    private String code;
}
