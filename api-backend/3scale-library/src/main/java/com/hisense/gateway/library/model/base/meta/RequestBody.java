package com.hisense.gateway.library.model.base.meta;

import com.hisense.api.library.model.DefineMode;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 父级类型
 */
@Data
public class RequestBody implements Serializable {
    private String name;// 参数名称
    private String value;// 参数描述
    private boolean required;// 是否必填
    private String paramType;// 参数传递方式
    private String dataType;// 普通类型
    private List<NestedTypeFirst> object;// 一级嵌套对象类型
    //private DefineMode defineMode;// 参数定义方式
    //private boolean hasSubField;// 其数据类型是否有子成员
    private String defaultValue;// 默认值
}
