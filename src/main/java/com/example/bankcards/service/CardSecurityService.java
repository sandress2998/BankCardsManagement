package com.example.bankcards.service;

import com.example.bankcards.entity.CardEncryptionKey;

import javax.crypto.SecretKey;
import java.util.UUID;

public interface CardSecurityService {
    // database
    CardEncryptionKey saveEncryptedKey(CardEncryptionKey cardEncryptionKey);
    void deleteEncryptedKey(UUID id);

    // encryption
    SecretKey generateKey();
    String encryptNumber(String data, SecretKey key);
    String decryptNumber(String encryptedData, SecretKey key);
    String encryptKey(SecretKey key);
    SecretKey decryptKey(String encryptedKey);

    // hash
    String calculateHmac(String data);
}
