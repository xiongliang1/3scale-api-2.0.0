package com.hisense.gateway.library.model.base.meta;

import lombok.Data;

import java.io.Serializable;

/**
 * 三级嵌套类型,无此类型嵌套
 */
@Data
public class NestedTypeThird implements Serializable {
    private String name;// 名称
    private String dataType;// 类型
    private String description;
    private String defaultValue;
}
