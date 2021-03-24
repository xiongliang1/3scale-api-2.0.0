package com.hisense.gateway.library.model.dto.web;

import lombok.Data;

import java.util.List;

/**
 * 我的订阅查询入参
 */
@Data
public class SubscribedApiQuery {
    private Integer page;
    private Integer size;
    private String name;//api名称
    private List<Integer> categoryOnes;//一级类别
    private List<Integer> categoryTwos;//二级类别
    private List<String> partitions;//发布环境
    private String publishTimeSort;//最新发布时间排序
    private String callTimeSort;//最新调用时间排序

    @Override
    public String toString() {
        return "SubscribedApiQuery{" +
                "page=" + page +
                ", size=" + size +
                ", name='" + name + '\'' +
                ", categoryOnes=" + categoryOnes +
                ", categoryTwos=" + categoryTwos +
                ", partitions=" + partitions +
                ", publishTimeSort=" + publishTimeSort +
                ", callTimeSort=" + callTimeSort +
                '}';
    }
}
