package com.hisense.gateway.library.model.dto.web;

import lombok.Data;

import java.util.List;

@Data
public class ProcessRecordQuery {
    private List<Integer> apiSystem;//订阅系统
    private String apiName;//API名称
    private List<String> creators;//订阅人
    private List<String> updaters;//审批人
    private List<Integer> status;//审批状态: 0:删除 1:待审批 2:审批通过 3:审批不通过
    private String startDate;//开始时间
    private String endDate;//结束时间
    private List<String> state;//suspended:停用, live:启用
    private Integer page = 1;
    private Integer size = 10;
    private String[] sort;

    public void setPage(Integer page) {
        if (page < 1) {
            page = 1;
        }
        this.page = page;
    }
}
