package com.hisense.gateway.library.model.base.alert;

import lombok.Data;

/**
 * 每条告警策略关联的API
 *
 * @author guilai.ming 2020/09/10
 */
@Data
public class AlertApiInfo {
    /**
     * API 管理平台数据库ID
     */
    private Integer id;

    /**
     * API名称
     */
    private String name;

    private String groupName;

    private Integer partition;

    private boolean isNeedLoging;

    public AlertApiInfo(Integer id, String name, String groupName, Integer partition,boolean isNeedLoging) {
        this.id = id;
        this.name = name;
        this.groupName = groupName;
        this.partition = partition;
        this.isNeedLoging = isNeedLoging;
    }
}
