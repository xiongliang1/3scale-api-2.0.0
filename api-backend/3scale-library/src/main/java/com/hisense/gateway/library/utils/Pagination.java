/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2019/11/25
 */
package com.hisense.gateway.library.utils;

import lombok.Data;

@Data
public class Pagination {

    private int pageNum;
    private int pageSize;
    private int totalRecord;
    private int totalPage;
    private int fromIndex;
    private int toIndex;

    public Pagination(int pageNum, int pageSize, int totalRecord) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalRecord = totalRecord;
        fromIndex = (pageNum-1)*pageSize;
        toIndex = pageNum*pageSize > totalRecord ? totalRecord : pageNum*pageSize;
        if (totalRecord % pageSize == 0) {
            this.totalPage = totalRecord / pageSize;
        } else {
            this.totalPage = totalRecord / pageSize + 1;
        }
    }
}
