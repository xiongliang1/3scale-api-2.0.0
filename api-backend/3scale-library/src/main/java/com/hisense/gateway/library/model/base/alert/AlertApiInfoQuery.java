package com.hisense.gateway.library.model.base.alert;

import lombok.Data;

import java.util.List;

/**
 * 每条告警策略关联的API
 *
 * @author guilai.ming 2020/09/10
 */
@Data
public class AlertApiInfoQuery {
    /**
     * API名称
     */
    private String name;
    private List<Integer> groupIds;// 分组id
    private List<Integer> partitions;// 发布环境: 0-内网,1-外网
    private boolean bind;// 已关联列表, 未关联列表
}
