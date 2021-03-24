package com.hisense.gateway.library.model.base.meta;

import lombok.Data;

import java.util.List;

@Data
public class ResponseBody {
    private String dataType;// 普通类型
    private List<NestedTypeFirst> object;// 一级嵌套对象类型
    private String description;
    private String defaultValue;
}
