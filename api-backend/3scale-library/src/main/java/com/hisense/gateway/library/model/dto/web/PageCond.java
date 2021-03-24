package com.hisense.gateway.library.model.dto.web;

import lombok.Data;

@Data
public class PageCond {
    private  int begin = 0;
    private  int length = 10;
    private  int isCount= 0;
}
