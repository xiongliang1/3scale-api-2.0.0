package com.hisense.gateway.library.model.dto.web;

import lombok.Data;

/**
 * 接口返回分页信息
 */
@Data
public class ReturnPageCond {
    private  int begin;
    private  int length;
    private  boolean isCount=true;
    private  int count;
    private  boolean last;
    private  int beginIndex;
    private  int currentPage;
    private  boolean first;
    private  int totalPage;
}
