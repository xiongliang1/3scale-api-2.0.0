/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.model.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.io.IOException;
import java.util.Base64;

@Data
public final class Claims {
    @Setter(AccessLevel.NONE)
    private String encrypted;

    @JsonProperty("exp")
    @Setter(AccessLevel.NONE)
    private Long expiresAt;

    @JsonProperty("iss")
    @Setter(AccessLevel.NONE)
    private String issuedBy;

    private Claims() {
    }

    public static Claims parse(String base64) throws IOException {
        byte[] content = Base64.getUrlDecoder().decode(base64);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(content, Claims.class);
    }

    public boolean expired() {
        return expiresAt * 1000 - System.currentTimeMillis() < 0;
    }
}
