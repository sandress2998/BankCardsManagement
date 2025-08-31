package com.example.bankcards.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class HmacUtils {
    private static final String HMAC_ALGO = "HmacSHA256";

    // Вычисление HMAC для данных с использованием секретного ключа
    public static String calculateHMAC(String data, String secretKey) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), HMAC_ALGO);
        Mac mac = Mac.getInstance(HMAC_ALGO);
        mac.init(keySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes());

        return Base64.getEncoder().encodeToString(rawHmac);
    }

    // Проверка соответствия HMAC
    public static boolean verifyHMAC(String data, String secretKey, String hmacToVerify) throws Exception {
        String calculatedHmac = calculateHMAC(data, secretKey);
        return calculatedHmac.equals(hmacToVerify);
    }
}
