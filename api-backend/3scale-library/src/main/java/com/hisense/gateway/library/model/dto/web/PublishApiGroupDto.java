/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/18 @author peiyun
 */
package com.hisense.gateway.library.model.dto.web;

import com.hisense.gateway.library.model.base.TimeQuery;
import com.hisense.gateway.library.model.pojo.base.PublishApiGroup;
import lombok.Data;

@Data
public class PublishApiGroupDto {
    private Integer id;
    private String name;
    private Integer categoryOne;//一级类目
    private Integer categoryTwo;// 二级类目
    private Integer system;//所属系统
    private String projectId;//所属项目ID
    private String systemName;
    private String categoryOneName;
    private String categoryTwoName;
    private Integer pageNum;
    private Integer pageSize;
    private String[] sort;
    private String description;
    private TimeQuery timeQuery;

    public PublishApiGroupDto() {
    }

    public PublishApiGroupDto(PublishApiGroup publishApiGroup) {
        this.categoryOne = publishApiGroup.getCategoryOne();
        this.categoryTwo = publishApiGroup.getCategoryTwo();
        this.id = publishApiGroup.getId();
        this.name = publishApiGroup.getName();
        this.projectId = publishApiGroup.getProjectId();
        this.system = publishApiGroup.getSystem();
    }
}
