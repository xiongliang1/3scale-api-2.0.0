/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @author douyanwei
 * 2020/2/19
 */
package com.hisense.gateway.library.model.dto.buz;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiInstanceDto implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;

    /** 实例名称 */
    String instanceName;

    Integer id;
    /**
     * 集群ID
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
    String requestProduction;
    String requestSandbox;

    public ApiInstanceDto() {
    }

    public ApiInstanceDto(String clusterId, String clusterPartition) {
        this.clusterId = clusterId;
        this.clusterPartition = clusterPartition;
    }

    public ApiInstanceDto(String clusterId, String clusterPartition, String requestProduction) {
        this.clusterId = clusterId;
        this.clusterPartition = clusterPartition;
        this.requestProduction = requestProduction;
    }

    public ApiInstanceDto(String clusterId, String clusterPartition, String requestProduction, String requestSandbox) {
        this.clusterId = clusterId;
        this.clusterPartition = clusterPartition;
        this.requestProduction = requestProduction;
        this.requestSandbox = requestSandbox;
    }
}
