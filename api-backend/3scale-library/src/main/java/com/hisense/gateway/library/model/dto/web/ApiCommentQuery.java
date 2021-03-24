package com.hisense.gateway.library.model.dto.web;

import lombok.Data;

import java.util.List;

@Data
public class ApiCommentQuery {
    private Integer apiId;
    private Integer page;
    private Integer size;
    private List<String> sort;
}
