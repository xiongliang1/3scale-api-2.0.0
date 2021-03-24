/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/4/26 @author peiyun
 */
package com.hisense.gateway.developer.constant;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "audience")
@Component
public class Audience {

    private String clientId;
    private String base64Secret;
    private String name;
    private int expiresSecond;//毫秒
}
