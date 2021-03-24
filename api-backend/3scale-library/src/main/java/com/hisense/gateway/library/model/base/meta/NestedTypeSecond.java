package com.hisense.gateway.library.model.base.meta;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 二级嵌套类型
 */
@Data
public class NestedTypeSecond implements Serializable {
    private String name;// 名称
    private String dataType;// 类型
    private List<NestedTypeThird> object;// 三级嵌套对象类型
    private String description;
    private String defaultValue;
}
