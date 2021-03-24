/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/17 @author wangjinshan
 */
package com.hisense.gateway.library.model.pojo.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.hibernate.annotations.ManyToAny;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "process_record")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessRecord implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String type;
    /**
     * 审批状态: 0-删除,1-初始,2-完成审批,3-审批不通过，4-重置密匙前的数据
     */
    private Integer status;
    private Integer relId;
    private String remark;//暂放订阅申请说明

    //@Column(columnDefinition = "text default null")
    @Column(columnDefinition = "clob default null")
    private String extVar;

    @Column(columnDefinition = "clob default null")
    private String extVar2;//暂放审批意见

    @Column(columnDefinition = "clob default null")
    private String data;

    String creator;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

    String updater;

    @Transient
    Integer version;

    @Column(columnDefinition = "varchar2(512) default null")
    private String processInstID;// 流程实例ID
}
