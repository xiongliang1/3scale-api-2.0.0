/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.model.crypto;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public final class Cryptor {
    private static final char[] secret = "dazyunsecretkeysforuserstenx20141019generatedKey".toCharArray();

    private static Key key(char[] secret, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(secret, salt, 2145, 32 * 8);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private static byte[] concat(byte[]... arrays) {
        int length = Arrays.stream(arrays).reduce(0,
                (total, array) -> total + array.length,
                (a, b) -> a + b);
        byte[] result = new byte[length];
        int index = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, index, array.length);
            index += array.length;
        }
        return result;
    }

    private Cryptor() {
    }

    public static byte[] aeadEncrypt(byte[] content)
            throws InvalidKeySpecException,
            NoSuchAlgorithmException,
            NoSuchProviderException,
            NoSuchPaddingException,
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            BadPaddingException,
            IllegalBlockSizeException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        byte[] salt = new byte[64];
        random.nextBytes(salt);
        Key key = key(secret, salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "SunJCE");

        byte[] nonce = new byte[12];
        random.nextBytes(nonce);
        GCMParameterSpec spec = new GCMParameterSpec(16 * 8, nonce);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        byte[] iv = cipher.getIV();
        byte[] withTag = cipher.doFinal(content);
        int length = withTag.length;
        int withoutTagLength = length - 16;
        byte[] withoutTag = new byte[withoutTagLength];

        byte[] tag = new byte[16];
        System.arraycopy(withTag, 0, withoutTag, 0, withoutTagLength);
        System.arraycopy(withTag, withoutTagLength, tag, 0, 16);
        return concat(salt, iv, tag, withoutTag);
    }

    public static byte[] aeadDecrypt(byte[] encrypted)
            throws InvalidKeySpecException,
            NoSuchAlgorithmException,
            NoSuchProviderException,
            NoSuchPaddingException,
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            BadPaddingException,
            IllegalBlockSizeException {
        int length = encrypted.length;
        if (length <= 92) throw new IllegalArgumentException("encrypted");
        int contentLength = length - 92;
        byte[] salt = new byte[64];
        byte[] iv = new byte[12];
        byte[] content = new byte[contentLength + 16];

        System.arraycopy(encrypted, 0, salt, 0, 64);
        System.arraycopy(encrypted, 64, iv, 0, 12);
        System.arraycopy(encrypted, 92, content, 0, contentLength);
        System.arraycopy(encrypted, 76, content, contentLength, 16);

        Key key = key(secret, salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "SunJCE");
        AlgorithmParameterSpec spec = new GCMParameterSpec(16 * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        return cipher.doFinal(content);
    }
}
