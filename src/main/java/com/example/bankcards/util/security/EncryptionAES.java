package com.example.bankcards.util.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionAES {
    // Генерация ключа AES
    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        return keyGen.generateKey();
    }

    // Шифрование данных с использованием ключа
    public static String encryptAES(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Дешифрование данных с использованием ключа
    public static String decryptAES(String encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }

    // Шифрование ключа — зашифровывает сериализованный ключ otherKey основным ключом masterKey
    public static String encryptKey(SecretKey otherKey, SecretKey masterKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, masterKey);
        byte[] encrypted = cipher.doFinal(otherKey.getEncoded());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // Дешифрование ключа — расшифровывает строку и восстанавливает SecretKey
    public static SecretKey decryptKey(String encryptedKey, SecretKey masterKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, masterKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedKey);
        byte[] decrypted = cipher.doFinal(decodedBytes);
        return new SecretKeySpec(decrypted, "AES");
    }
}
