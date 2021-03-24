package com.hisense.gateway.library.model.pojo.buz;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
public class Plugin {
    private Long id;
    private String name;
    private String ingressClass;
    private String type;
    private Long createTime;
    private String description;
    private Map<String, Object> config;

    private Boolean disabled = false;
    private Boolean global = false;

    /**
     * 某些plugin需要再project-NameSpace中创建
     * 某系则需要再env.namespace中创建,用于共享
     * 默认使用project namespace
     */
    private Boolean projectNamespace = true;

    public Plugin(String type) {
        this.type = type;
        this.projectNamespace = true;
    }

    public Plugin(String type, Boolean projectNamespace) {
        this(type);
        this.projectNamespace = projectNamespace;
    }

    public Plugin(String type, Map<String, Object> config, String name) {
        this(type);
        this.config = config;
        this.name = name;
    }
}
