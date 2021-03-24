package com.hisense.gateway.library.model.dto.web;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QueryParamsDto {
    private  String userID;//用户ID
    private  String personID;//人员ID
    private  String permission;//执行权限-执行权限ALL(全部)|PUBLIC(公共)|PRIVATE(个人)(PUBLIC，PRIVATE可以形成组合)用逗号隔开
    private  String scope;//任务来源-任务来源（ALL：全部；AGENT：代理；DELEG：代办；HELP：协办）。AGENT、DELEG、HELP可以形成组合，用半角逗号","隔开。
    private  String tableName;//表名称
    private  String wiSql;//工作项条件-工作项查询条件
    private  String bizSql;//业务条件-业务查询条件
    private List<String> wiBindList;//工作项条件
    private  List<String> bizBindList;//业务条件
    private  PageCond pageCond;//分页属性
    private  String tenantID;//租户ID
    private long processInstID;//流程实例ID
    private Map<String,String> condition;//扩展条件
    private long processDefID;//流程定义ID
    private String processDefName;//流程定义名称
    private List<String> procBindList;//流程实例条件
    private int index;//当前页码
    private int size;//每页条数
}
