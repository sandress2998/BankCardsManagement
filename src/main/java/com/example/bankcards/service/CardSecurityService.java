package com.example.bankcards.service;

import com.example.bankcards.entity.CardEncryptionKey;

import javax.crypto.SecretKey;

public interface CardSecurityService {
    CardEncryptionKey saveEncryptedKey(CardEncryptionKey cardEncryptionKey);

    // encryption
    SecretKey generateKey() throws Exception;
    String encryptNumber(String data, SecretKey key) throws Exception;
    String decryptNumber(String encryptedData, SecretKey key) throws Exception;
    String encryptKey(SecretKey key) throws Exception;
    SecretKey decryptKey(String encryptedKey) throws Exception;

    // hash
    String calculateHmac(String data, SecretKey key) throws Exception;
}
