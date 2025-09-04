package com.example.bankcards.service.impl;

import com.example.bankcards.entity.CardEncryptionKey;
import com.example.bankcards.repository.CardEncryptionKeyRepository;
import com.example.bankcards.service.CardSecurityService;
import com.example.bankcards.util.EncryptionAES;
import com.example.bankcards.util.HmacUtils;
import jakarta.transaction.Transactional;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.UUID;

import static org.apache.tomcat.util.codec.binary.Base64.decodeBase64;

@Service
public class CardSecurityServiceImpl implements CardSecurityService {

    private final CardEncryptionKeyRepository cardEncryptionKeyRepository;

    public final SecretKeySpec masterKey; // Мастер-ключ из настроек
    public final SecretKey hmacKey;

    public CardSecurityServiceImpl(
            Environment env,
           CardEncryptionKeyRepository cardEncryptionKeyRepository
    ) {
        this.cardEncryptionKeyRepository = cardEncryptionKeyRepository;

        String masterKeyStr = env.getRequiredProperty("security.card.number.key");
        String hmacKeyStr   = env.getRequiredProperty("security.card.number.hmac");

        byte[] masterKeyBytes = decodeBase64(masterKeyStr);
        byte[] hmacKeyBytes   = decodeBase64(hmacKeyStr);

        this.masterKey = new SecretKeySpec(masterKeyBytes, "AES");
        this.hmacKey   = new SecretKeySpec(hmacKeyBytes, "HmacSHA256");
    }

    @Transactional
    @Override
    public CardEncryptionKey saveEncryptedKey(CardEncryptionKey cardEncryptionKey) {
        return cardEncryptionKeyRepository.save(cardEncryptionKey);
    }

    @Transactional
    @Override
    public void deleteEncryptedKey(UUID id) {
        cardEncryptionKeyRepository.deleteById(id);
    }

    @Override
    public SecretKey generateKey() {
        try {
            return EncryptionAES.generateAESKey();
        } catch (Exception e) {
            throw new RuntimeException("Internal Error");
        }
    }

    @Override
    public String encryptNumber(String cardNumber, SecretKey key) {
        try {
            return EncryptionAES.encryptAES(cardNumber, key);
        } catch (Exception e) {
            throw new RuntimeException("Internal Error");
        }
    }

    @Override
    public String decryptNumber(String encryptedCardNumber, SecretKey key) {
        try {
            return EncryptionAES.decryptAES(encryptedCardNumber, key);
        } catch (Exception e) {
            throw new RuntimeException("Internal Error");
        }
    }

    @Override
    public String encryptKey(SecretKey key) {
        try {
            return EncryptionAES.encryptKey(key, masterKey);
        } catch (Exception e) {
            throw new RuntimeException("Internal Error");
        }
    }

    @Override
    public SecretKey decryptKey(String encryptedKey) {
        try {
            return EncryptionAES.decryptKey(encryptedKey, masterKey);
        } catch (Exception e) {
            throw new RuntimeException("Internal Error");
        }
    }

    @Override
    public String calculateHmac(String cardNumber) {
        try {
            // Получаем байты ключа
            byte[] keyBytes = hmacKey.getEncoded();
            // Преобразуем байты ключа в строку Base64
            String base64Key = Base64.getEncoder().encodeToString(keyBytes);
            // Вызываем существующий метод, передавая строковый ключ
            return HmacUtils.calculateHMAC(cardNumber, base64Key);
        } catch (Exception e) {
            throw new RuntimeException("Internal Error");
        }
    }
}
