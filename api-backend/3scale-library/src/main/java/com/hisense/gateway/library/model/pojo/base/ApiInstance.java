/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020-02-19 @author jinshan
 */
package com.hisense.gateway.library.model.pojo.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * Domain
 *
 * @author wangjinshan
 * @version v1.0
 * @date 2020-02-19 11:39
 */
@Data
public class ApiInstance implements Serializable {
    Long id;
    /**
     * 租户ID
     */
    String tenantId;
    /**
     * 集群名称(测试集群或生产集群)
     */
    String clusterName;
    /**
     * 环境划分(内网或外网)
     */
    String clusterPartition;

    String host;
    /**
     * 网关实例接口访问凭证
     */
    String accessToken;
    String requestProduction;
    String requestSandbox;

    public ApiInstance(Long id, String tenantId, String clusterPartition, String host,
                       String accessToken) {
        this.id = id;
        this.tenantId = tenantId;
        this.clusterPartition = clusterPartition;
        this.host = host;
        this.accessToken = accessToken;
    }

    public ApiInstance(Long id, String tenantId, String clusterPartition, String host,
                       String accessToken, String requestProduction) {
        this.id = id;
        this.tenantId = tenantId;
        this.clusterPartition = clusterPartition;
        this.host = host;
        this.accessToken = accessToken;
        this.requestProduction = requestProduction;
    }

    public ApiInstance(Long id, String tenantId, String clusterPartition, String host, String accessToken, String requestProduction, String requestSandbox) {
        this.id = id;
        this.tenantId = tenantId;
        this.clusterPartition = clusterPartition;
        this.host = host;
        this.accessToken = accessToken;
        this.requestProduction = requestProduction;
        this.requestSandbox = requestSandbox;
    }
}
