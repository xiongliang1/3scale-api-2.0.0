/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020-02-19 @author jinshan
 */
package com.hisense.gateway.library.model.pojo.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.persistence.*;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

/**
 * Domain
 *
 * @author wangjinshan
 * @version v1.0
 * @date 2020-02-19 11:39
 */
@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "gw_instance")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Instance implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    /**
     * 实例名称
     */
    String instanceName;
    /**
     * 集群Id
     */
    String clusterId;
    /**
     * 集群名称(测试集群或生产集群)
     */
    String clusterName;
    /**
     * 环境划分(内网或外网)
     */
    String clusterPartition;
    /**
     * 租户Id
     */
    String tenantId;

    String host;

    /**
     * 网关实例接口访问凭证
     */
    String accessToken;

    String sandbox;
    String requestSandbox;
    String production;
    String requestProduction;
    /**
     * 实例状态(0废弃  1有效)
     */
    Integer status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",timezone = "GMT+8")
    private Date createTime;

    /**
     * 创建人(网关实例部署人)
     */
    Integer createId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",timezone = "GMT+8")
    private Date updateTime;

    //@JsonIgnore
    //@OneToMany(mappedBy = "instance",cascade = CascadeType.ALL,orphanRemoval = true)
    //private Set<PublishApiInstanceRelationship> apiInstances;

    public Instance(String clusterId, String clusterName, String clusterPartition, String host,
                    String accessToken, Integer status, Date createTime, Integer createId, Date updateTime) {
        this.clusterId = clusterId;
        this.clusterName = clusterName;
        this.clusterPartition = clusterPartition;
        this.host = host;
        this.accessToken = accessToken;
        this.status = status;
        this.createTime = createTime;
        this.createId = createId;
        this.updateTime = updateTime;
    }
}
