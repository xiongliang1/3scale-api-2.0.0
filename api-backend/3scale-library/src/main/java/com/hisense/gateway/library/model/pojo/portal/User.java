/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */

package com.hisense.gateway.library.model.pojo.portal;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

import static com.hisense.gateway.library.constant.BaseConstants.DATE_TIME_FORMAT;

/**
 * User
 *
 * @author wangjinshan
 * @version v1.0
 * @date 2019-11-20 11:39
 */
@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "gw_user")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    Integer paasUserId;

    //@Column(columnDefinition = "datetime(0) default null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    //@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date createTime;

    @Column(columnDefinition = "varchar(1024) default null")
    String description;
    /**
     * �û�������֯��������
     */
    String org;
    /**
     * �ʼ�
     */
    String email;
    /**
     * �û���
     */
    String name;
    /**
     * ����
     */
    String pwd;
    /**
     * �绰
     */
    String phone;
    /**
     * �û���ɫ
     */
    Integer role;

    //@Column(columnDefinition = "tinyint(1) default 1")
    Integer status;//0-����,1-��Ч
    Integer type;//�û�����
    Integer domain;

    public User(Date createTime, String description, String email, String name, String phone, Integer role,
                Integer status, Integer type) {
        this.createTime = createTime;
        this.description = description;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.type = type;
    }

    public User(Integer id) {
        this.id = id;
    }
}
