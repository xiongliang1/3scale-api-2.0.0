/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/11/25 @author peiyun
 */
package com.hisense.gateway.library.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;

@Slf4j
public class AesUtils {

    private static final String ALGORITHMSTR = "DES/CBC/PKCS5Padding";

    private final static String DES = "DES";

    public final static String UTF = "UTF-8";
    //接口认证集成方式统一加解密密钥
    private final static String LOGIN_KEY = "loginKey";


    /**
     * aes解密
     *
     * @param encrypt 内容
     * @return
     * @throws Exception
     */
    public static String aesDecrypt(String encrypt) {
        try {
            return decrypt(encrypt, LOGIN_KEY);
        } catch (Exception e) {
            log.error("aesDecrypt excepetion:",e);
            return "";
        }
    }

    /**
     * aes加密
     *
     * @param content
     * @return
     * @throws Exception
     */
    public static String aesEncrypt(String content) {
        try {
            return encrypt(content, LOGIN_KEY);
        } catch (Exception e) {
            log.error("aesEncrypt exception:",e);
            return "";
        }
    }

    /**
     * @param src
     * @param key
     * @return
     * @throws Exception
     */
    private static byte[] encrypt(byte[] src, byte[] key) throws Exception {
        // DES算法要求有一个可信任的随机数源
        SecureRandom sr = new SecureRandom();

        // 从原始密匙数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密匙工厂，然后用它把DESKeySpec转换成一个SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);

        IvParameterSpec iv = new IvParameterSpec(key);
        // 用密匙初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, securekey, iv);

        // 执行加密操作
        return cipher.doFinal(src);
    }

    /**
     * 字符串加密（aes算法）
     *
     * @param content 需要加密的内容
     * @param key     加密的密钥
     * @return 加密后的结果
     */
    public final static String encrypt(String content, String key) {

        try {
            return byte2String(encrypt(content.getBytes(UTF), key.getBytes(UTF)));
        } catch (Exception e) {
            log.error("AES 加密处理错误", e);
            return "";
        }
    }

    public static String byte2String(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * @param src 数据源
     * @param key 密钥，长度必须是8的倍数
     * @return
     * @throws Exception
     */
    private static byte[] decrypt(byte[] src, byte[] key) throws Exception {
        // DES算法要求有一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        // 从原始密匙数据创建一个DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);
        // 创建一个密匙工厂，然后用它把DESKeySpec对象转换成一个SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);
        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);

        IvParameterSpec iv = new IvParameterSpec(key);
        // 用密匙初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, iv);
        // 正式执行解密操作
        return cipher.doFinal(src);
    }

    /**
     * 字符串解密
     *
     * @param comtent ：要解密的字符串
     * @param key     ：解密的密钥
     * @return 解密后的结果
     */
    public final static String decrypt(String comtent, String key) {
        try {
            return new String(decrypt(String2byte(comtent.getBytes(UTF)), key.getBytes(UTF)));
        } catch (Exception e) {
            log.error("AES 解密处理错误: {}", e);
            return "";
        }
    }

    private static byte[] String2byte(byte[] b) {
        if ((b.length % 2) != 0)
            throw new IllegalArgumentException("长度不是偶数");
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }

    public static byte[] String2byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    public static void main(String[] args) {
        String param = "Hisense@2018";
        String result = aesEncrypt(param);
        log.info("加密结果:" + result);
        String decryptResult = aesDecrypt(result);
        log.info("解密结果:" + decryptResult);
    }
}
