package com.hisense.gateway.library.model.dto.web;

import lombok.Data;

@Data
public class ProcessInst {
    private String processInstID; //流程实例ID String
    private String processInstName; //流程实例名称 String
    private String processInstDesc; //流程实例描述 String
    private String creator; //创建者
    private String owner; //所有者
    private String currentState; //当前状态  1-未启动 2-运行 3-挂起 7-完成 8-终止
    private String priority; //优先级  30-极低 40-低 50-中低 60-普通 70-中高 80-高  目前未使用，无功能含义
    private String relateData; //相关数据大字段
    private String relateDataVChr; //相关数据字符串
    private String limitNum; //限制时间数
    private String limitNumDesc; //限制事件描述
    private String createTime; //创建时时间  yyyyMMddHHmmssSSS
    private String startTime; //启动时间  yyyyMMddHHmmssSSS
    private String endTime; //结束时间
    private String finalTime; //超时时间  yyyyMMddHHmmssSSS
    private String remindTime; //提醒时间  yyyyMMddHHmmssSSS
    private String parentProcID; //父流程实例id
    private String parentActID; //父活动实例id
    private String processDefID; //流程定义ID
    private String isTimeOut; //是否超时   Y-是 N-否
    private String timeOutNum; //超市数字
    private String timeOutNumDesc; //超时时间描述
    private String updateVersion; //更新版本号   流程实例的操作都会更新流程实例版本号。
    private String processDefName; //流程定义名称
    private String catalogUUID; //业务目录编号
    private String catalogName; //业务目录名称
}
