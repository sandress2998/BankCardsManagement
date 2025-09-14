package com.example.bankcards.service;

import com.example.bankcards.entity.CardEncryptionKey;
import com.example.bankcards.repository.CardEncryptionKeyRepository;
import com.example.bankcards.service.impl.CardSecurityServiceImpl;
import com.example.bankcards.util.security.EncryptionAES;
import com.example.bankcards.util.security.HmacUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardSecurityServiceImplTest {

    @Mock
    CardEncryptionKeyRepository cardEncryptionKeyRepository;

    CardSecurityServiceImpl service;

    String masterKeyStr = Base64.getEncoder().encodeToString("0123456789abcdef".getBytes());
    String hmacKeyStr = Base64.getEncoder().encodeToString("abcdef9876543210".getBytes());
    MockEnvironment env = new MockEnvironment()
        .withProperty("security.card.number.key", masterKeyStr)
        .withProperty("security.card.number.hmac", hmacKeyStr);

    @BeforeEach
    void setUp() {
        service = new CardSecurityServiceImpl(env, cardEncryptionKeyRepository);
    }

    // saveEncryptedKey - успешное сохранение
    @Test
    void saveEncryptedKey_shouldReturnSavedEntity() {
        CardEncryptionKey key = new CardEncryptionKey();
        when(cardEncryptionKeyRepository.save(key)).thenReturn(key);
        assertEquals(key, service.saveEncryptedKey(key));
        verify(cardEncryptionKeyRepository).save(key);
    }

    // deleteEncryptedKey
    @Test
    void deleteEncryptedKey_shouldInvokeDeleteById() {
        UUID id = UUID.randomUUID();
        doNothing().when(cardEncryptionKeyRepository).deleteById(id);
        service.deleteEncryptedKey(id);
        verify(cardEncryptionKeyRepository).deleteById(id);
    }

    // generateKey - успешное создание ключа
    @Test
    void generateKey_shouldReturnSecretKey() {
        SecretKey key = service.generateKey();
        assertNotNull(key);
    }

    // generateKey - имитация ошибки генерации и выброса исключения
    @Test
    void generateKey_shouldThrowRuntimeExceptionOnError() {
        try (MockedStatic<EncryptionAES> mocked = mockStatic(EncryptionAES.class)) {
            mocked.when(EncryptionAES::generateAESKey).thenThrow(new RuntimeException("Error"));
            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.generateKey());
            assertEquals("Internal Error", ex.getMessage());
        }
    }

    // encryptNumber - успешное шифрование
    @Test
    void encryptNumber_shouldReturnEncryptedString() throws Exception {
        SecretKey key = EncryptionAES.generateAESKey();
        try (MockedStatic<EncryptionAES> mocked = mockStatic(EncryptionAES.class)) {
            mocked.when(() -> EncryptionAES.encryptAES("card123", key)).thenReturn("encrypted123");
            String result = service.encryptNumber("card123", key);
            assertEquals("encrypted123", result);
        }
    }

    // encryptNumber - ошибка шифрования
    @Test
    void encryptNumber_shouldThrowRuntimeExceptionOnError() {
        SecretKey key = new SecretKeySpec(new byte[16], "AES");
        try (MockedStatic<EncryptionAES> mocked = mockStatic(EncryptionAES.class)) {
            mocked.when(() -> EncryptionAES.encryptAES(anyString(), any())).thenThrow(new Exception("Error"));
            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.encryptNumber("data", key));
            assertEquals("Internal Error", ex.getMessage());
        }
    }

    // decryptNumber - успешное расшифрование
    @Test
    void decryptNumber_shouldReturnDecryptedString() throws Exception {
        SecretKey key = EncryptionAES.generateAESKey();
        try (MockedStatic<EncryptionAES> mocked = mockStatic(EncryptionAES.class)) {
            mocked.when(() -> EncryptionAES.decryptAES("encrypted123", key)).thenReturn("card123");
            String result = service.decryptNumber("encrypted123", key);
            assertEquals("card123", result);
        }
    }

    // decryptNumber - ошибка расшифрования
    @Test
    void decryptNumber_shouldThrowRuntimeExceptionOnError() {
        SecretKey key = new SecretKeySpec(new byte[16], "AES");
        try (MockedStatic<EncryptionAES> mocked = mockStatic(EncryptionAES.class)) {
            mocked.when(() -> EncryptionAES.decryptAES(anyString(), any())).thenThrow(new Exception("Error"));
            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.decryptNumber("data", key));
            assertEquals("Internal Error", ex.getMessage());
        }
    }

    // encryptKey - успешное шифрование ключа
    @Test
    void encryptKey_shouldReturnEncryptedKey() throws Exception {
        SecretKey key = EncryptionAES.generateAESKey();
        try (MockedStatic<EncryptionAES> mocked = mockStatic(EncryptionAES.class)) {
            mocked.when(() -> EncryptionAES.encryptKey(key, service.masterKey)).thenReturn("encryptedKeyString");
            String result = service.encryptKey(key);
            assertEquals("encryptedKeyString", result);
        }
    }

    // encryptKey - ошибка шифрования ключа
    @Test
    void encryptKey_shouldThrowRuntimeExceptionOnError() {
        SecretKey key = new SecretKeySpec(new byte[16], "AES");
        try (MockedStatic<EncryptionAES> mocked = mockStatic(EncryptionAES.class)) {
            mocked.when(() -> EncryptionAES.encryptKey(any(), any())).thenThrow(new Exception("Error"));
            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.encryptKey(key));
            assertEquals("Internal Error", ex.getMessage());
        }
    }

    // decryptKey - успешное расшифрование ключа
    @Test
    void decryptKey_shouldReturnSecretKey() {
        try (MockedStatic<EncryptionAES> mocked = mockStatic(EncryptionAES.class)) {
            SecretKey mockKey = new SecretKeySpec(new byte[16], "AES");
            mocked.when(() -> EncryptionAES.decryptKey("encryptedKeyString", service.masterKey)).thenReturn(mockKey);
            SecretKey result = service.decryptKey("encryptedKeyString");
            assertNotNull(result);
            assertEquals(mockKey, result);
        }
    }

    // decryptKey - ошибка расшифрования ключа
    @Test
    void decryptKey_shouldThrowRuntimeExceptionOnError() {
        try (MockedStatic<EncryptionAES> mocked = mockStatic(EncryptionAES.class)) {
            mocked.when(() -> EncryptionAES.decryptKey(anyString(), any())).thenThrow(new Exception("Error"));
            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.decryptKey("data"));
            assertEquals("Internal Error", ex.getMessage());
        }
    }

    // calculateHmac - успешный расчет
    @Test
    void calculateHmac_shouldReturnHmacString() {
        try (MockedStatic<HmacUtils> mocked = mockStatic(HmacUtils.class)) {
            mocked.when(() -> HmacUtils.calculateHMAC(anyString(), anyString())).thenReturn("hmacValue");
            String hmac = service.calculateHmac("cardData");
            assertEquals("hmacValue", hmac);
        }
    }

    // calculateHmac - ошибка при вычислении
    @Test
    void calculateHmac_shouldThrowRuntimeExceptionOnError() {
        try (MockedStatic<HmacUtils> mocked = mockStatic(HmacUtils.class)) {
            mocked.when(() -> HmacUtils.calculateHMAC(anyString(), anyString())).thenThrow(new Exception("Error"));
            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.calculateHmac("data"));
            assertEquals("Internal Error", ex.getMessage());
        }
    }
}

