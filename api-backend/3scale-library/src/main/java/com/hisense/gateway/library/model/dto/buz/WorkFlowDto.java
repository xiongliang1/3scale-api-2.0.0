package com.hisense.gateway.library.model.dto.buz;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class WorkFlowDto {
    private String userID;  //用户id
    private String processDefName; //流程定义名称
    private String processInstName; //流程实例名称
    private String processInstDesc; //流程实例描述
    private Map<String, Object> relaDatas; //相关数据
    private String tenantID;   //租户id
    private String tableName;  //业务表
    private Map bizInfo;  //业务数据
    private Boolean finishFirstWorkItem; //是否完成第一个工作项
    private Boolean log;  //是否记录操作
    private String userName; //用户名称
    private String msg;  //操作意见

    private String personID;  //指定人员
    private List<String> procBindList; //流程实例条件
    private List<String> bizBindList; //业务条件
    private Map pageCond; //分页属性
}
