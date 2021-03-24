/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.model.dto.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hisense.gateway.library.model.crypto.Cryptor;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * 登陆信息
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class LoginInfo {
    // @Setter(AccessLevel.NONE)// close for debug,mingguilai.ex
    private String username;

    //@Setter(AccessLevel.NONE) // close for debug,mingguilai.ex
    private String token;

    //@Setter(AccessLevel.NONE) // close for debug,mingguilai.ex
    private Integer userId;

    // @Setter(AccessLevel.NONE)
    private Integer id;
    private Integer role;
    private String tenantId;
    private String clusterId;
    private String projectId;

    public LoginInfo() {
    }

    public static LoginInfo parse(String base64)
            throws NoSuchPaddingException,
            InvalidKeyException,
            NoSuchAlgorithmException,
            IllegalBlockSizeException,
            BadPaddingException,
            NoSuchProviderException,
            InvalidAlgorithmParameterException,
            InvalidKeySpecException,
            IOException {
        byte[] encrypted = Base64.getDecoder().decode(base64);
        byte[] decrypted = Cryptor.aeadDecrypt(encrypted);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(decrypted, LoginInfo.class);
    }
}
