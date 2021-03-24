package com.hisense.gateway.library.model.base;

import lombok.Data;
import sun.net.www.content.text.plain;

@Data
public class PolicyHeader {
    private String op;//操作（add/set/push/delete）
    private String header;//header名称
    private String valueType;//header值得类型（plain/liquid）
    private String value;//header值
}
