package com.hisense.gateway.library.model.dto.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hisense.gateway.library.model.base.TimeQuery;
import com.hisense.gateway.library.model.pojo.base.PublishApi;
import lombok.Data;

import java.util.Date;
import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.DATE_TIME_FORMAT;

@Data
public class OperationApiQuery {

    private Integer pageNum;
    private Integer pageSize;
    private TimeQuery timeQuery;
    private List<String> sort;
    private String name;
    private String creator;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FORMAT,timezone = "GMT+8")
    private Date createTime;
    private Integer apiId;
    private  String apiName;
    private PublishApi publishApi;
    private String userName;
    private String projectId;
}
