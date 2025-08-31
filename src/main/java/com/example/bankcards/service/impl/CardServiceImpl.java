package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardActionRequest;
import com.example.bankcards.dto.CardBalanceResponse;
import com.example.bankcards.dto.CardInfoResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardEncryptionKey;
import com.example.bankcards.entity.CardHash;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardHashRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardSecurityService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardUtil;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CardServiceImpl implements CardService {
    @Value("${card.months-until-expires}")
    private int monthsQuantityUntilExpiresDefault;

    @Value("${security.card-number.hmac}")
    private String hmacKeyStr;

    private SecretKey hmacKey;

    @Autowired
    CardRepository cardRepository;

    @Autowired
    CardHashRepository cardHashRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CardSecurityService cardSecurityService;

    public LocalDate setValidityPeriod(Integer monthsQuantity) {
        if (monthsQuantity == null) {
            monthsQuantity = monthsQuantityUntilExpiresDefault;
        }

        LocalDate today = LocalDate.now();
        LocalDate targetMonth = today.plusMonths(monthsQuantity);

        return targetMonth.with(TemporalAdjusters.lastDayOfMonth());
    }

    @PostConstruct
    public void init() {
        try {
            // Предполагается, что hmacKeyStr — это Base64 строка
            byte[] keyBytes = Base64.getDecoder().decode(hmacKeyStr);
            this.hmacKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Failed to decode HMAC key from config", e);
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public CardInfoResponse create(UUID ownerId, Integer monthsQuantityUntilExpires) throws Exception {
        System.out.println("Create method started");
        User owner = userRepository.findUserById(ownerId);

        if (owner == null) {
            throw new NotFoundException("User not found");
        }

        // генерируем случайный номер карты (через Card.getRandomNumber())
        // через hmac(CardSecurityService) проверяем, нет ли у нас такого номера (сравниваем хэш)
        // продолжаем так делать около 100 раз, иначе выдаем ошибку RuntimeException
        System.out.println("\n-1\n");
        String cardNum = generateUniqueCardNumber();
        System.out.println("\n0\n");
        // если все норм, то генерируем ключ AES(CardSecurityService) и шифруем номер карты
        SecretKey cardSecretKey = cardSecurityService.generateKey();

        System.out.println("\n1\n");
        String encryptedCardNumber = cardSecurityService.encryptNumber(cardNum, cardSecretKey);
        System.out.println("\n2\n");
        // шифруем ключ
        CardEncryptionKey encryptedSecretKey = new CardEncryptionKey(cardSecurityService.encryptKey(cardSecretKey));
        System.out.println("\n3\n");
        Card newCard = new Card(owner, encryptedCardNumber, setValidityPeriod(monthsQuantityUntilExpires));
        System.out.println("\n4\n");
        newCard.setEncryptionKey(encryptedSecretKey);
        System.out.println("\n5\n");
        encryptedSecretKey.setCard(newCard);
        System.out.println("\n6\n");

        Card receivedCard = cardRepository.save(newCard);
        cardSecurityService.saveEncryptedKey(encryptedSecretKey);

        return mapToCardInfoResponse(receivedCard);
    }

    private CardInfoResponse mapToCardInfoResponse(Card card) throws Exception {
        SecretKey cardKey = cardSecurityService.decryptKey(card.getEncryptionKey().getEncryptedKey());
        String plainNumber = cardSecurityService.decryptNumber(card.getEncryptedNumber(), cardKey);

        String maskedNumber = CardUtil.maskCardNumber(plainNumber);
        String expiryDate = CardUtil.convertValidationDate(card.getValidityPeriod());

        return new CardInfoResponse(
            maskedNumber,
            expiryDate,
            card.getStatus(),
            card.getBalance(),
            card.getOwner().getLogin()
        );
    }

    private String generateUniqueCardNumber() throws Exception {
        StringBuilder resultNumber;
        String resultHash;

        int attempt = 0;
        do {
            resultNumber = new StringBuilder();

            for (int i = 0; i < 4; i++) {
                int randomNumber = ThreadLocalRandom.current().nextInt(0, 10000);
                resultNumber.append(String.format("%04d", randomNumber));
            }
            resultHash = cardSecurityService.calculateHmac(resultNumber.toString(), hmacKey);

            if (!cardHashRepository.existsByHmacHash(resultHash)) {
                cardHashRepository.save(new CardHash(resultHash));
                return resultNumber.toString();
            }
            ++attempt;
        } while (attempt < 100);

        throw new RuntimeException("Too much attempts to generate card number. Something went wrong.");
    }


    @Override
    public void activate(CardActionRequest request) {

    }

    @Override
    public void block(CardActionRequest request) {

    }

    @Override
    public void delete(CardActionRequest request) {

    }

    @Override
    public CardBalanceResponse getBalance(CardActionRequest request) {
        return null;
    }

    @Override
    public List<Card> getAllCards(UUID ownerId) {
        return List.of();
    }

    @Override
    public void doMoneyTransfer(String cardNumberFrom, String cardNumberTo) {

    }
}
/* Тактика сохранения:
1) Сначала генерируем номер и ключ
2) шифруем номер
3) шифруем ключ (с помощью master key)
4) сохраняем в таблицу CardEncryptionKeyRepository
5) сохраняем в таблицу Card
 */



/*
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public CardInfoResponse create(UUID ownerId) throws Exception {
        // 1) Генерация номера карты и AES-ключа
        String cardNumber = Card.getRandomNumber(); // метод генерации номера

        if (cardRepository.exists())
        SecretKey cardAesKey = cardSecurityService.generateAESKey();

        // 2) Шифруем номер карты ключом карты
        String encryptedCardNumber = cardSecurityService.encryptNumber(cardNumber, cardAesKey);

        // 3) Шифруем ключ карты мастер-ключом
        String encryptedCardKey = cardSecurityService.encryptKey(cardAesKey);

        // 4) Сохраняем в таблицу CardEncryptionKey
        CardEncryptionKey cardKeyEntity = new CardEncryptionKey();
        cardKeyEntity.setEncryptedKey(encryptedCardKey);
        cardKeyEntity.setCard(null); // заполним позже после создания карты
        cardSecurityService.save(cardKeyEntity);

        // 5) Сохраняем карту с зашифрованным номером и связываем с CardEncryptionKey
        Card cardEntity = new Card();
        cardEntity.setOwner(userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found")));
        cardEntity.setEncryptedNumber(encryptedCardNumber);
        cardEntity.setEncryptionKey(cardKeyEntity);

        Card savedCard = cardRepository.save(cardEntity);

        // Обновляем связь ключа с картой
        cardKeyEntity.setCard(savedCard);
        cardSecurityService.save(cardKeyEntity);

        return mapToCardInfoResponse(savedCard);
    }

    @Transactional
    @Override
    public void activate(CardActionRequest request) {
        cardRepository.updateCardStatus();
    }

    @Transactional
    @Override
    public void block(CardActionRequest request) {
        cardRepository.updateCardStatus(request.number());
    }

    @Transactional
    @Override
    public void delete(CardActionRequest request) {


        cardRepository.delete(request.number());
    }

    private boolean isBalanceUpdateCorrect(Card card, long sum) {
        long newBalance;
        long currentBalance = card.getBalance();
        if (currentBalance > Long.MAX_VALUE - sum) {
            throw new IllegalArgumentException("Balance is too high");
        }
        newBalance = currentBalance + sum;
        if (newBalance < 0) {
            throw new IllegalArgumentException("Not enough balance");
        }
        return true;
    }

    private Authentication getAuthData() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        return auth;
    }


    private static final DateTimeFormatter CARD_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");

    private CardInfoResponse mapToCardInfoResponse(Card card) throws Exception {
        SecretKey cardKey = cardSecurityService.decryptKey(card.getEncryptionKey().getEncryptedKey());
        String plainNumber = cardSecurityService.decryptNumber(card.getEncryptedNumber(), cardKey);
        String maskedNumber = "**** **** **** " + plainNumber.substring(plainNumber.length() - 4);

        // Преобразование срока действия карты в LocalDate (предполагается, что getExpiryDate() возвращает LocalDate)
        LocalDate expiryDate = card.getValidityPeriod();

        return new CardInfoResponse(
                maskedNumber,
                expiryDate,
                card.getStatus(),
                card.getBalance()
        );

    }
*/