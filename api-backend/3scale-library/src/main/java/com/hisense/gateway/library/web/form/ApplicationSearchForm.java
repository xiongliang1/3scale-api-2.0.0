package com.hisense.gateway.library.web.form;

import lombok.Data;

@Data
public class ApplicationSearchForm {
    private Integer groupId;// API分组
    private String applicationName;// 订阅API名称
    private Integer categoryOne;//一级类目
    private Integer categoryTwo;//二级类目
    private Integer system;// 所属系统
    private Integer status;//订阅状态
    private Integer pageNum;//页码
    private Integer pageSize;//每页数量
    private Integer appSystem;//订阅系统
    private Integer apiSystem;//API所属系统
    private String apiName;//API名称
    private String name;//API名称
    private String tenantId;//
    private String projectId;
}
