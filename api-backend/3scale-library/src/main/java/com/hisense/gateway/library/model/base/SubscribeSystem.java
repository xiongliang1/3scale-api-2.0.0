package com.hisense.gateway.library.model.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author guilai.ming 2020/09/10
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscribeSystem {
    /**
     * 3scale上当前订阅对应的application的Id
     */
    private Integer appId;
    /**
     * publish_application表的Id
     */
    private Integer applicationId;

    /**
     * 订阅系统名称
     */
    private String systemName;

    /**
     * 订阅系统对应的DataItem id
     */
    private String systemId;

    /**
     * 3scale-user key
     */
    private String userKey;

    /**
     * 订阅者
     */
    private String users;

    public SubscribeSystem() {
    }

    public SubscribeSystem(Integer appId,Integer applicationId,String system) {
        this.appId = appId;
        this.applicationId = applicationId;
        this.systemName = system;
    }
}
