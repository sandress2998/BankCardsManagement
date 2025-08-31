package com.example.bankcards.service;

import com.example.bankcards.entity.CardEncryptionKey;

import javax.crypto.SecretKey;
import java.util.UUID;

public interface CardSecurityService {
    // database
    CardEncryptionKey saveEncryptedKey(CardEncryptionKey cardEncryptionKey);
    void deleteEncryptedKey(UUID id);

    // encryption
    SecretKey generateKey() throws Exception;
    String encryptNumber(String data, SecretKey key) throws Exception;
    String decryptNumber(String encryptedData, SecretKey key) throws Exception;
    String encryptKey(SecretKey key) throws Exception;
    SecretKey decryptKey(String encryptedKey) throws Exception;

    // hash
    String calculateHmac(String data) throws Exception;
}
