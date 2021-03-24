/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.model.dto.web;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
public final class Namespace {
    public enum Type {
        USER, TEAM, PROJECT, ONBEHALFUSER
    }

    @Setter(AccessLevel.NONE)
    private Type type;

    @Setter(AccessLevel.NONE)
    private String namespace;

    private Namespace(String namespace, Type type) {
        this.namespace = namespace;
        this.type = type;
    }

    private static final Map<String, Type> typeMapping;

    static {
        typeMapping = new HashMap<>();
        typeMapping.put("teamspace", Type.PROJECT);
        typeMapping.put("project", Type.PROJECT);
        typeMapping.put("onbehalfuser", Type.ONBEHALFUSER);
    }

    public static Namespace parse(Headers headers, LoginInfo info) {
        Set<Map.Entry<String, Type>> entries = typeMapping.entrySet();
        for (Map.Entry<String, Type> entry : entries) {
            String space = headers.getHeader(entry.getKey());
            if (space != null && space.length() > 0) {
                return new Namespace(space, entry.getValue());
            }
        }
        return new Namespace(info.getUsername(), Type.USER);
    }

    public interface Headers {
        String getHeader(String key);
    }
}
