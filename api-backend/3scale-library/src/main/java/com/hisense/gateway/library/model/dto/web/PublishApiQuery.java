package com.hisense.gateway.library.model.dto.web;

import com.hisense.gateway.library.model.base.TimeQuery;
import lombok.Data;

import java.util.List;

@Data
public class PublishApiQuery {
    private Integer pageNum;
    private Integer pageSize;
    private String name;
    private TimeQuery timeQuery;
    private List<Integer> groupIds;
    private List<Integer> partitions;
    /*private List<String> creators;*/
    private List<Integer> categoryOne;//一级类目
    private List<Integer> categoryTwo;// 二级类目
    private List<String> sort;
    private Integer system;
    private boolean published;
    private List<String> users;//创建者
}
