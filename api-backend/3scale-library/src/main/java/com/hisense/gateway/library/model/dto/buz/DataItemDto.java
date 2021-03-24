/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/18 @author peiyun
 */
package com.hisense.gateway.library.model.dto.buz;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class DataItemDto implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;

    private Integer id;
    /**
     * 字典组key
     */
    private String groupKey;
    /**
     * 字典组名称
     */
    private String groupName;
    /**
     * 字典key
     */
    private String itemKey;
    /**
     * 字典名称
     */
    private String itemName;
    /**
     * 父字典ID
     */
    private Integer parentId;
    /**
     * 状态：0 废弃,1有效
     */
    private Integer status=1;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;

    public DataItemDto() {
    }

    public DataItemDto( String groupKey, String groupName, String itemKey, String itemName, Integer parentId) {
        this.groupKey = groupKey;
        this.groupName = groupName;
        this.itemKey = itemKey;
        this.itemName = itemName;
        this.parentId = parentId;

    }
}
