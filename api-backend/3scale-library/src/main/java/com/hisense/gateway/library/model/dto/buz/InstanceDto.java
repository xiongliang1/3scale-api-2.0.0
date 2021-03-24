/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @author douyanwei
 * 2020/2/19
 */
package com.hisense.gateway.library.model.dto.buz;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
public class InstanceDto {
    private static final long serialVersionUID = 5105223900137785597L;

    Integer id;
    /**
     * 实例名称
     */
    String instanceName;
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
    /**
     * 网关实例访问地址
     */
    String host;
    /**
     * 网关实例接口访问凭证
     */
    String accessToken;
    /**
     * 实例状态(0废弃,1有效)
     */
    Integer status;
    /**
     * 创建时间
     */
    Date createTime;
    /**
     * 创建人(网关实例部署人)
     */
    String createId;
    /**
     * 更新时间
     */
    Date updateTime;
}

