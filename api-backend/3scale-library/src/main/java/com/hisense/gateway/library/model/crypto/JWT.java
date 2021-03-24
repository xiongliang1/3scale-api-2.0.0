package com.hisense.gateway.library.model.crypto;
/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2020 TenxCloud. All Rights Reserved.
 *
 * @author douyanwei
 * @date 2020/2/19
 */
import com.hisense.gateway.library.exception.NotSignedByApiServer;
import com.hisense.gateway.library.exception.UnknownTokenFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public final class JWT {
    private static final byte[] privateKey;

    static {
        String base64Content = "MIIJKQIBAAKCAgEA2XUrxPC2ZzfLXiGz3J+BJmfpzNWyRNhUEdljCb9QfMz/54t1" +
                "Eri+R3XEvPE+h7n7izBYohv/qXstwM55SBrLqhHVx8qD9SB5FfvkFjU1Q8K+d/1C" +
                "KPQpGZnckCtr9c+LjXVB6h1yXP5lBcmbR2SNBAhN+zRdOAoxyw2CgycCo0kyAotT" +
                "a9EiKYVLoP4TyDJJIebRp2rRVENBl647jC2Lp13YEXeZhxe6gHw2yEJcg0e8qn3A" +
                "hw/842CNbhCVO5kvEhFnCrfJ7w/K7FpS4sQQzI4BocwrvDJxbwBH7RS9Q4+bax5k" +
                "RQqB5C6bGWS59ePmqSJ/HjPBPaWcXT/FaFCqZX6umCiAkVWi2F6PK/Un3mkbCa3u" +
                "AnqjPO0urgjel3W7kGbHZc1qnSPypUPhmuoLcLCZvzfpY6sl6rUyn8Pbbx3AaEbn" +
                "Taxn2wvWVYAik9eeSO3mXOqdZOyFxL1oQe+dv5BjfC/KaeTA5LDPQZ+GpqUq/1ya" +
                "lkeEUU8UsrOuSnmekxYQkHEOJ+LAK/aiCsVkbe05Ues4lAFB8g8FZ9Fo/yoCSAzs" +
                "AMC4MPFqtNE+GeZPNAsGaNh3GUDWET7IRmu/scZI5L/eBkAyPwsKohkZOpAzfLEx" +
                "5OiewUQ9J5zUBXZuKaZL/5HYyqf6/stItzU7UtP62X8cLi8NnC1Vq+4c9mMCAwEA" +
                "AQKCAgEAkgCrrIT50v+RGdiDDKRDcGfggFkSYkrk1z8f0dGT1tdFEk9+AV3s08ns" +
                "l/dZxoNGssN5Hw6xbzd3FrcKkzD6gWuMH6KHSrPM2MfQ8mAzLRW6EJIIM7sLRVca" +
                "0el1iQsaZZXO9cNjn7BmX6ZnDV4jmAuDYCBeXlvp5q1hbXFpwfxJCZBGKGO4Diyj" +
                "BKrS2V154Ls7FK8RcQPfLFodPRbvZyYJBmFIwX1bCR1dIsP7nWEy+T2JYKWJY5jW" +
                "HIohyGwnQHhSuM2BVXNDCcHzWLHPnafSzLFqw+cSZjbIFBQSpyPqc9dp9zkA0RXB" +
                "qSEKAmBL5E93De4t1pg1Dh7dChbu8A0BjeT9YOX3eLdSnu5A9tJXu8lubyzRBjNE" +
                "360tFwC8c4IAs0KuEq3W1tzv4AEPqyx/k0V/o1d8do9WL7NqtiEk/EuMr5pkPOmb" +
                "yUQOT9qY/B1bMk8jHllRE6E68BdhFa6pbgVrpsFehXslukN/QsgCK4yEsS22aE0R" +
                "K4TLmRaYT8JU9hlbY86dRhqNKM+m5aH4Aa1owUj6nZQq3zYgY+0SoCdlJdGSD0gG" +
                "LN5E4i2tlPvxBo/z7ObV9Cc+zI1OwcH5WvcBYINYDPLN9Fr9iZzLC0zI1H7eL0ut" +
                "0eI+4BfytTk8Ff8EA2mLrfYy1fyNZqBol7lyzIg6iTBDaLzUCTECggEBAPjFmarO" +
                "dsj2KDWr1VOobwaoY54Q/AavRARnGi24QcprHLQAJsKIUbSmMoIV/qRBUhyw88VB" +
                "LCRW92kK4GJJIOwkr8l/zNIJRGRg/CK4E0S3vu4GA1x300VAole/nk/jUKQGCUL9" +
                "27XJrrOKNz1Equn98OSeRB9j7AJ0aPy5BafI2PWoUoMPfpWfULeK8eBAxiYMewwk" +
                "VpBzJr/FoZD4QgFKXhk6PoxdqdEaRQxySRWNCwgjVsSxYKVxO47bd3lJjGgXF2sd" +
                "hfGnurBZt0jL7CmmdcI4t8+05svZrw02L+GPmp6dnpmVudlKdTbJPjRdW/SVAFuc" +
                "vwtXrbD3glY5pccCggEBAN/GpsnePTRwGKsM43RLDlKM0SGMbpAoORFNC/X6PtQ9" +
                "nLNM+y+ZUYVT+/TZXsKY7vOKOFFxcS/vt3Sez6CH1fkiEj7eHMFadjnYQp4mDJUh" +
                "NuT3eOCCLnqImuVhyuGjb0kVo+Uw7NLKcWFshj16ybCO/3dSLAAf/QWjk9XMVarP" +
                "0GHXysqOE+6aqvVwjqgZcXrteP8z/Xrij2pVsDUn6+o7E9c/snUAxpNxO5pKuaI0" +
                "cYpLC6YILhbwLgICo22wY4W3Cd7GNJ+F8UK9QZChnbJmrMG7gqtD2wqNKiiKue4T" +
                "n3jGG9W6DRwYsVExDsWTC8WPJ5ZULBZ8dDY5OXmPeoUCggEAXHIB0sl6tt9Sve8n" +
                "DTmQWKcGrdyd61YCLqipv8ezGyeGuRU9UhkaU8lXB6Roxl1HyEWxsOGxJ6fxtOVH" +
                "0P5f76EKehS15m9vLOYljDlfX6/wkb9GTHxy1E9ahMU+bW2JsApWMsDnfrx94VZB" +
                "hNEZum6VsD9oDUoykA72XMPc6CbpCREN6Io/fhaABlTp4W3wtH760t5GFNPV2Hn2" +
                "ukqnLJeYNEPCrqK30m6yrhdiNVH+gX2wZtOLmK9ldIb19Opx9NRv7WxBNDYiWBpe" +
                "0/yDvE6RgCVXmSYehi5UsNIsJOQaj0r/fw92ytqyiDNsnET9QPyF74VmMS7Z6uNv" +
                "Wd9+TQKCAQBVFD0ToShaCIiIeCT+cQ7n+dwFSlQ7AN/5oPZ8NgGvRiGO1iTmSv+A" +
                "lpbD1+U8TVMESzfwVxY2qIhykXLVUO/cgcS4HFCIfvFWOs/ROxwrku5BDYnqqfQr" +
                "6EYkEhNFyJKmEdE3cWuJFSkYZl9/fnCybRvZ7OcHwSG9BB1P+xlTESHkIVxbuLsB" +
                "S9LV8E58wPexShpnxQeJshvezOdqvlvmuUFo5DHgZEQbiMClf+WmMxQ8BR5PqOqF" +
                "FBoZ75DdQmQEUbwx89/MCuvYeQY1jAzd6EWkfrtGjEz6bQNrWJsqVlGaZI/uqYcU" +
                "eJrqCKHaIncmTLA7apM8lWLFvuoIOrHVAoIBAQDzs6q8kcDRwJhvGCbCf2wGpRTZ" +
                "Jza0O/toOSgI7fqrOopub/XZ/mNCsa/fFskbkJJwBj34Mt3wb59P9zFVIRyX+amh" +
                "YREfbwk/PF8fEW6OH0FFtP6Y5D0K8MV/0qls7lVTPXeKT7UXA1nQHbBG0Bb1G2Kq" +
                "hhovPyYZzh0xOmiXC0z8pBn1BLMnbC7/PUkDiVBmxJYAilD11xJDx+LLEuCzC8vL" +
                "J2FMsw2T/L4egMn2Ae17AuVXOxgTkkfIf1NI4AtoznnFmhtG4+ztFyoOAOUUDE2L" +
                "8kjUZ1BV35VkZFSz5NPbdelAp4HzkhdbUvfA9MotTtYtN8mgTPJ2neq8Lx4Z";
        privateKey = Base64.getDecoder().decode(base64Content);
    }

    private static byte[] signature(String content) throws NoSuchAlgorithmException, InvalidKeyException {
        Key key = new SecretKeySpec(privateKey, "HmacSHA512");
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(key);
        return mac.doFinal(content.getBytes());
    }

    private JWT() {
    }

    public static Claims validate(String rawToken)
            throws UnknownTokenFormat,
            InvalidKeyException,
            NoSuchAlgorithmException,
            NotSignedByApiServer,
            IOException {
        String[] parts = rawToken.split("\\.");
        if (parts.length != 3) throw new UnknownTokenFormat();
        String content = String.join(".", parts[0], parts[1]);
        byte[] expect = signature(content);
        byte[] got = Base64.getUrlDecoder().decode(parts[2]);
        if (!Arrays.equals(expect, got)) throw new NotSignedByApiServer();
        return Claims.parse(parts[1]);
    }
}
