package com.hisense.gateway.library.model.base.eureka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hisense.api.library.utils.MiscUtil;
import lombok.Data;

@Data
public class EurekaPullConfig {
    private Integer groupId;
    private String eurekaUrl;
    private boolean scheduleEnable;
    private String type;//注册中心类型
    private String serviceName;//nacos注册中心对应的服务名称

    @JsonIgnore
    private final Object lock = new Object();

    @JsonIgnore
    private boolean scheduled = false;

    public boolean isValid() {
        return MiscUtil.isNotEmpty(eurekaUrl) &&
                groupId != null && groupId >= 0;
    }
}
