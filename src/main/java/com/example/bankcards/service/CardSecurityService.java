package com.example.bankcards.service;

import com.example.bankcards.entity.CardEncryptionKey;

import javax.crypto.SecretKey;
import java.util.UUID;


public interface CardSecurityService {

    /**
     * Сохраняет зашифрованный ключ карты в базе данных.
     *
     * @param cardEncryptionKey объект зашифрованного ключа
     * @return сохранённый объект CardEncryptionKey с установленным идентификатором
     * @throws org.springframework.dao.DataAccessException если произошла ошибка при сохранении ключа в базе данных
     */
    CardEncryptionKey saveEncryptedKey(CardEncryptionKey cardEncryptionKey);

    /**
     * Удаляет зашифрованный ключ карты из базы по идентификатору.
     *
     * @param id UUID ключа для удаления
     * @throws org.springframework.dao.DataAccessException если произошла ошибка при удалении из базы данных
     */
    void deleteEncryptedKey(UUID id);

    /**
     * Генерирует новый случайный AES-ключ.
     *
     * Нюансы:
     * - Используется для шифрования номеров карт.
     *
     * @return новый AES-ключ
     * @throws RuntimeException при внутренней ошибке генерации ключа (например, криптографическая ошибка)
     */
    SecretKey generateKey();

    /**
     * Шифрует номер карты с помощью указанного AES-ключа.
     *
     * Нюансы:
     * - В случае ошибки выбрасывает RuntimeException с сообщением "Internal Error".
     *
     * @param data номер карты в открытом виде
     * @param key AES-ключ для шифрования
     * @return зашифрованный номер карты в виде строки
     * @throws RuntimeException при ошибке шифрования (например, неверный ключ, сбой алгоритма)
     */
    String encryptNumber(String data, SecretKey key);

    /**
     * Расшифровывает зашифрованный номер карты с помощью указанного AES-ключа.
     *
     * Нюансы:
     * - В случае ошибки выбрасывает RuntimeException с сообщением "Internal Error".
     *
     * @param encryptedData зашифрованный номер карты
     * @param key AES-ключ для расшифровки
     * @return расшифрованный номер карты в виде строки
     * @throws RuntimeException при ошибке расшифрования (например, неверный ключ, некорректные данные)
     */
    String decryptNumber(String encryptedData, SecretKey key);

    /**
     * Шифрует AES-ключ для хранения с помощью мастер-ключа.
     *
     * Нюансы:
     * - Используется для защиты ключей шифрования карт.
     * - В случае ошибки выбрасывает RuntimeException с сообщением "Internal Error".
     *
     * @param key AES-ключ для шифрования
     * @return зашифрованный ключ в виде строки
     * @throws RuntimeException при ошибке шифрования ключа
     */
    String encryptKey(SecretKey key);

    /**
     * Расшифровывает зашифрованный AES-ключ с помощью мастер-ключа.
     *
     * Нюансы:
     * - В случае ошибки выбрасывает RuntimeException с сообщением "Internal Error".
     *
     * @param encryptedKey зашифрованный ключ в виде строки
     * @return расшифрованный AES-ключ
     * @throws RuntimeException при ошибке расшифрования ключа (например, неверное шифрование или ключ)
     */
    SecretKey decryptKey(String encryptedKey);

    /**
     * Вычисляет HMAC (хэш с ключом) от переданных данных.
     *
     * Нюансы:
     * - Использует HMAC-SHA256 с предварительно загруженным ключом hmacKey.
     *
     * @param data строка для хэширования
     * @return HMAC-хэш в виде строки
     * @throws RuntimeException при ошибке хэширования (например, ошибка алгоритма или некорректный ключ)
     */
    String calculateHmac(String data);
}



/*
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
*/