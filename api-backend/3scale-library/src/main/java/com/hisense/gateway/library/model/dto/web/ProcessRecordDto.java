/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @author douyanwei
 * @date 2020/3/3
 */
package com.hisense.gateway.library.model.dto.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hisense.gateway.library.model.pojo.base.ProcessRecord;
import lombok.Data;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessRecordDto {
    private static final long serialVersionUID = 5105223900137785597L;
    private Integer id;
    private String type;
    private Integer status;//审批状态 0:删除 1:待审批 2审批通过 3审批不通过
    private Integer relId;
    private String remark;//补充说明
    private String extVar;
    private String extVar2;
    private String processInstID;// 流程实例ID


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;//申请时间

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;//审批时间

    private String apiName;//api名称

    private String userKey;
    private Integer apiId;
    private Integer system;
    private String apiSystemName;//api所属系统
    private Integer systemId;//订阅系统ID
    private String appSystemName;//订阅系统
    private String groupName;//api所属分组
    private String categoryOneName;//一级类目
    private String categoryTwoName;//二级类目
    private String creator;//订阅人姓名
    private String updater;//审批人姓名
    private String partition;//发布环境
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date lastestPublishTime;//最新发布时间
    private Integer calledCount;//调用次数
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date lastestCalledTime;//最后调用时间

    private String data;
    private String state;//是否禁用 live:启用 suspended:禁用

    private PublishApiDto publishApiDto;
    private PublishApplicationDto publishApplicationDto;

    public ProcessRecordDto(ProcessRecord processRecord) {
        this.id = processRecord.getId();
        this.type = processRecord.getType();
        this.status = processRecord.getStatus();
        this.relId = processRecord.getRelId();
        this.remark = processRecord.getRemark();
        this.extVar = processRecord.getExtVar();
        this.creator = processRecord.getCreator();
        this.createTime = processRecord.getCreateTime();
        this.updateTime = processRecord.getUpdateTime();
        this.updater = processRecord.getUpdater();
    }

    public ProcessRecordDto() {
    }

    public boolean isValid(){
        return system !=null;
    }
}
