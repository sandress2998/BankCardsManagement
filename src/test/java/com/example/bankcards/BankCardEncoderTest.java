package com.example.bankcards;

import com.example.bankcards.util.EncryptionAES;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;

public class BankCardEncoderTest {
    @Test
    void test() throws Exception {


        SecretKey key = EncryptionAES.generateAESKey(); // 128 бит
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println(base64Key);
    }

}
