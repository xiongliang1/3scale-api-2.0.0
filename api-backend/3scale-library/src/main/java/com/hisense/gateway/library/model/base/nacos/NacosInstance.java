package com.hisense.gateway.library.model.base.nacos;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class NacosInstance {
    private Map<String, String> metadata;
    private boolean valid;
    private boolean marked;
    private String instanceId;
    private String ip;
    private int port;
    private double weight;
    private boolean healthy;
    private boolean enabled;
    private boolean ephemeral;
    private String clusterName;
    private String serviceName;
}
