package com.hisense.gateway.library.model.base.eureka;

import lombok.Data;

import java.util.Map;

@Data
public class EurekaInstance {
    private String instanceId;
    private Map<String, String> metadata;
}
