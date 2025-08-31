package com.example.bankcards.service.impl;

import com.example.bankcards.entity.CardEncryptionKey;
import com.example.bankcards.repository.CardEncryptionKeyRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardSecurityService;
import com.example.bankcards.util.EncryptionAES;
import com.example.bankcards.util.HmacUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class CardSecurityServiceImpl implements CardSecurityService {
    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardEncryptionKeyRepository cardEncryptionKeyRepository;

    // Мастер-ключ из настроек
    private SecretKeySpec masterKey;

    public CardSecurityServiceImpl(@Value("${security.card-number.master-key}") String masterKeyStr) throws Exception {
        // Инициализируем мастер-ключ из строки base64 (если строка другая — трансформировать)
        byte[] decodedKey = Base64.getDecoder().decode(masterKeyStr);
        this.masterKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    @Transactional
    @Override
    public CardEncryptionKey saveEncryptedKey(CardEncryptionKey cardEncryptionKey) {
        return cardEncryptionKeyRepository.save(cardEncryptionKey);
    }

    @Override
    public SecretKey generateKey() throws Exception {
        return EncryptionAES.generateAESKey();
    }

    @Override
    public String encryptNumber(String cardNumber, SecretKey key) throws Exception {
        return EncryptionAES.encryptAES(cardNumber, key);
    }

    @Override
    public String decryptNumber(String encryptedCardNumber, SecretKey key) throws Exception {
        return EncryptionAES.decryptAES(encryptedCardNumber, key);
    }

    @Override
    public String encryptKey(SecretKey key) throws Exception {
        return EncryptionAES.encryptKey(key, masterKey);
    }

    @Override
    public SecretKey decryptKey(String encryptedKey) throws Exception {
        return EncryptionAES.decryptKey(encryptedKey, masterKey);
    }

    @Override
    public String calculateHmac(String cardNumber, SecretKey key) throws Exception {
        // Получаем байты ключа
        byte[] keyBytes = key.getEncoded();
        // Преобразуем байты ключа в строку Base64
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);
        // Вызываем существующий метод, передавая строковый ключ
        return HmacUtils.calculateHMAC(cardNumber, base64Key);
    }
}
