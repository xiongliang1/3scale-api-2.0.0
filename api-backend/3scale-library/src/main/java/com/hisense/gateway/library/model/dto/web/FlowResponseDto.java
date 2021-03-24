package com.hisense.gateway.library.model.dto.web;

import lombok.Data;

import java.util.List;

@Data
public class FlowResponseDto {
    private  ReturnPageCond pageCond;
    private List<ProcessInst> processInsts;
}
