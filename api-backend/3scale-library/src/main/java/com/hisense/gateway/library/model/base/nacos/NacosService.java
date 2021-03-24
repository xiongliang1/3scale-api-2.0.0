package com.hisense.gateway.library.model.base.nacos;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class NacosService {
    private List<NacosInstance>  hosts;
    private String dom;
    private String name;
    private long cacheMillis;
    private long lastRefTime;
    private String checksum;
    private boolean useSpecifiedURL;
    private String clusters;
    private String env;
    private Map<String, String> metadata;
}
