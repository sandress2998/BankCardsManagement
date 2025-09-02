package com.example.bankcards.service.impl;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.*;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.CardBlockingRequestRepository;
import com.example.bankcards.repository.CardHashRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardSecurityService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import com.example.bankcards.util.CardUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CardServiceImpl implements CardService {
    private final UserService userService;
    @Value("${card.months-until-expires}")
    private int monthsQuantityUntilExpiresDefault;

    CardRepository cardRepository;
    CardHashRepository cardHashRepository;
    UserRepository userRepository;
    CardSecurityService cardSecurityService;
    CardBlockingRequestRepository cardBlockingRequestRepository;

    public CardServiceImpl(
        CardRepository cardRepository,
        CardSecurityService cardSecurityService,
        UserRepository userRepository,
        CardHashRepository cardHashRepository,
        CardBlockingRequestRepository cardBlockingRequestRepository,
        UserService userService) {
        this.cardRepository = cardRepository;
        this.cardSecurityService = cardSecurityService;
        this.userRepository = userRepository;
        this.cardHashRepository = cardHashRepository;
        this.cardBlockingRequestRepository = cardBlockingRequestRepository;
        this.userService = userService;
    }

    public LocalDate setValidityPeriod(Integer monthsQuantity) {
        if (monthsQuantity == null) {
            monthsQuantity = monthsQuantityUntilExpiresDefault;
        }

        LocalDate today = LocalDate.now();
        LocalDate targetMonth = today.plusMonths(monthsQuantity);

        return targetMonth.with(TemporalAdjusters.lastDayOfMonth());
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public CardInfoResponse create(UUID ownerId, Integer monthsQuantityUntilExpires) {
        User owner = userRepository.findUserById(ownerId);

        if (owner == null) {
            throw new NotFoundException("User not found");
        }

        // генерируем случайный номер карты (через Card.getRandomNumber())
        // через hmac(CardSecurityService) проверяем, нет ли у нас такого номера (сравниваем хэш)
        // продолжаем так делать около 100 раз, иначе выдаем ошибку RuntimeException
        String cardNum = generateUniqueCardNumber();

        // если все норм, то генерируем ключ AES(CardSecurityService) и шифруем номер карты
        SecretKey cardSecretKey = cardSecurityService.generateKey();
        String encryptedCardNumber = cardSecurityService.encryptNumber(cardNum, cardSecretKey);

        // шифруем ключ
        CardEncryptionKey encryptedSecretKey = new CardEncryptionKey(cardSecurityService.encryptKey(cardSecretKey));
        Card newCard = new Card(owner, encryptedCardNumber, setValidityPeriod(monthsQuantityUntilExpires));

        newCard.setEncryptionKey(encryptedSecretKey);
        encryptedSecretKey.setCard(newCard);

        // сохраняем в базу данных
        Card receivedCard = cardRepository.save(newCard);
        cardSecurityService.saveEncryptedKey(encryptedSecretKey);

        return mapToCardInfoResponse(receivedCard);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public void updateCard(Card.CardAction cardAction, UUID cardId) {
        Card card = getCardById(cardId);

        switch (cardAction) {
            case ACTIVATE -> cardRepository.updateCardStatus(card.getId(), Card.Status.ACTIVE);
            case BLOCK -> cardRepository.updateCardStatus(card.getId(), Card.Status.BLOCKED);
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public void delete(UUID cardId)  {
        Card card = getCardById(cardId);
        String number = getCardNumber(card);

        // надо удалить хэш из CardHash
        String hashedKey = cardSecurityService.calculateHmac(number);
        cardHashRepository.deleteByHmacHash(hashedKey);
        // надо удалить карту из CardEncryptionKey
        cardSecurityService.deleteEncryptedKey(card.getEncryptionKey().getId());
        // если есть, надо удалить из CardBlockRequest
        cardBlockingRequestRepository.deleteByCardId(card.getId());
        // надо удалить карту из Card
        cardRepository.delete(card);
    }

    /** Функция, которая позволяет узнать данные всех карточек пользователя */
    @Override
    public List<CardInfoResponse> getCardsInfo(
        UUID id,
        Card.Status status,
        int page,
        int size
    ) {
        boolean isAdmin = isCurrentUserIsAdmin();
        UUID ownerId;

        if (isAdmin && id == null) {
            ownerId = UUID.fromString(getAuthData().getName());
        } else if (isAdmin) {
            ownerId = id;
            if (userRepository.findUserById(ownerId) == null) {
                throw new NotFoundException("User not found");
            }
        } else {
            ownerId = UUID.fromString(getAuthData().getName());
        }

        List<Card> cards;
        List<CardInfoResponse> cardInfoResponses = new ArrayList<>();

        Pageable pageable = PageRequest.of(page, size);

        // если указан какой-то параметр для фильтра, то фильтруем.
        if (status == null) {
            cards = cardRepository.findByOwnerId(ownerId, pageable);
        } else {
            cards = cardRepository.findByOwnerIdAndStatus(ownerId, status, pageable);
        }

        for (Card card : cards) {
            cardInfoResponses.add(mapToCardInfoResponse(card));
        }
        return cardInfoResponses;
    }

    /** Перевод денег из одной карты на другую */
    @Transactional
    @Override
    public void doMoneyTransfer(CardTransferMoney body) {
        if (body.amount() < 0) {
            throw new BadRequestException("Amount cannot be negative");
        }

        Card cardFrom = getCardByNumber(body.from());
        Card cardTo = getCardByNumber(body.to());

        checkCardAvailable(cardFrom);
        checkCardAvailable(cardTo);

        validateBalanceUpdateCorrect(cardFrom, -body.amount());
        validateBalanceUpdateCorrect(cardFrom, body.amount());

        cardRepository.transferMoney(cardFrom.getId(), cardTo.getId(), body.amount());
    }

    /** Позволяет узнать почти все данные карты, в том числе номер карты. Тестовый метод (созданный для удобства тестирования и проверки) */
    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<CardFullInfoResponse> getAllCards() {
        List<Card> cards = cardRepository.findAll();
        ArrayList<CardFullInfoResponse> cardsFullInfo = new ArrayList<>();

        for (Card card : cards) {
            // На самом деле здесь будет не encrypted number, а decrypted number (ради теста)
            card.setEncryptedNumber(getCardNumber(card));
            cardsFullInfo.add(mapToCardFullInfo(card));
        }
        return cardsFullInfo;
    }

    @Transactional
    @Override
    public CardBalanceResponse updateCardBalanceAction(Card.BalanceAction action, CardBalanceRequest body) {
        Card card = getCardByNumber(body.cardNumber());

        switch (action) {
            case WITHDRAW_MONEY -> {
                return withdrawMoney(card, body.amount());
            }
            case DEPOSIT_MONEY -> {
                return depositMoney(card, body.amount());
            }
            default -> throw new BadRequestException("Invalid action");
        }
    }

    @Override
    public CardBalanceResponse getBalance(CardNumberBody body) {
        Card card = getCardByNumber(body.number());

        return new CardBalanceResponse(card.getBalance());
    }

    @Override
    @Transactional
    public void submitCardBlocking(CardNumberBody body) {
        Card card = getCardByNumber(body.number());

        checkCardAvailable(card);

        cardBlockingRequestRepository.save(new CardBlockingRequest(card));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<CardBlockingResponse> getBlockRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<CardBlockingRequest> requests = cardBlockingRequestRepository.findAll(pageable);
        List<CardBlockingResponse> result = new ArrayList<>();
        for (CardBlockingRequest request: requests) {
            result.add(new CardBlockingResponse(request.getCard().getId()));
        }

        return result;
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public void processBlockRequest(UUID cardId) {
        if (cardId == null) {
            throw new BadRequestException("Card ID cannot be null");
        }
        if (!cardRepository.existsCardById(cardId)) {
            throw new NotFoundException("Card not found");
        }

        cardRepository.updateCardStatus(cardId, Card.Status.BLOCKED);
        cardBlockingRequestRepository.deleteById(cardId);
    }

    private CardBalanceResponse withdrawMoney(Card card, double amount) {
        if (amount < 0) {
            throw new BadRequestException("Amount cannot be negative");
        }

        checkCardAvailable(card);
        validateBalanceUpdateCorrect(card, -amount);
        double newBalance = card.getBalance() - amount;
        cardRepository.updateCardBalance(card.getId(), newBalance);
        return new CardBalanceResponse(newBalance);
    }

    private CardBalanceResponse depositMoney(Card card, double amount) {
        if (amount < 0) {
            throw new BadRequestException("Amount cannot be negative");
        }

        checkCardAvailable(card);
        validateBalanceUpdateCorrect(card, amount);
        double newBalance = card.getBalance() + amount;
        cardRepository.updateCardBalance(card.getId(), newBalance);
        return new CardBalanceResponse(newBalance);
    }

    ///  Функции, работающие с зашифрованными данными

    /** Функция, расшифровывающая номер карты по переданному объекту Card */
    private String getCardNumber(Card card) {
        try {
            CardEncryptionKey encryptionKey = card.getEncryptionKey();
            SecretKey decryptedKey = cardSecurityService.decryptKey(encryptionKey.getEncryptedKey());
            return cardSecurityService.decryptNumber(card.getEncryptedNumber(), decryptedKey);
        } catch (Exception e) {
            throw new RuntimeException("Internal error");
        }
    }

    /** Функция, по номеру карты определяющая объект Card (извлекается из базы данных) */
    private Card getCardByNumber(String cardNumber) {
        // сначала найдем все карты, которые принадлежат пользователю
        UUID id = UUID.fromString(getAuthData().getName());
        List<Card> cards = cardRepository.findByOwnerId(id);
        for (Card card : cards) {
            CardEncryptionKey cardEncryptionKey = card.getEncryptionKey();
            String encryptedKey = cardEncryptionKey.getEncryptedKey();

            try {
                // расшифровали ключ
                SecretKey decryptedKey = cardSecurityService.decryptKey(encryptedKey);
                // шифруем номер карты, который получили на входе, этим ключом
                String encryptedCardNumber = cardSecurityService.encryptNumber(cardNumber, decryptedKey);
                // проверяем, совпадают ли зашифрованное значение и зашифрованный номер карты
                if (encryptedCardNumber.equals(card.getEncryptedNumber())) {
                    return card;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        throw new NotFoundException("Card not found");
    }

    private CardFullInfoResponse mapToCardFullInfo(Card card) {
        return new CardFullInfoResponse(
            card.getId(),
            card.getEncryptedNumber(), // на самом деле здесь должен быть предварительно расшифрованный номер карты
            card.getOwner().getLogin(),
            card.getValidityPeriod(),
            card.getStatus(),
            card.getBalance()
        );
    }

    private CardInfoResponse mapToCardInfoResponse(Card card) {
        String plainNumber = getCardNumber(card);

        String maskedNumber = CardUtil.maskCardNumber(plainNumber);
        String expiryDate = CardUtil.convertValidationDate(card.getValidityPeriod());

        return new CardInfoResponse(
            card.getId(),
            maskedNumber,
            expiryDate,
            card.getStatus(),
            card.getBalance(),
            card.getOwner().getLogin()
        );
    }

    ///  Получение/генерация доп. данных

    private Authentication getAuthData() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        return auth;
    }

    /** Генерирует уникальный номер карты. Гарантирует что карты с таким номером не существует. */
    private String generateUniqueCardNumber()  {
        StringBuilder resultNumber;
        String resultHash;

        int attempt = 0;
        do {
            resultNumber = new StringBuilder();

            for (int i = 0; i < 4; i++) {
                int randomNumber = ThreadLocalRandom.current().nextInt(0, 10000);
                resultNumber.append(String.format("%04d", randomNumber));
            }

            try {
                resultHash = cardSecurityService.calculateHmac(resultNumber.toString());
            } catch (Exception e) {
                throw new RuntimeException("Internal error");
            }

            if (!cardHashRepository.existsByHmacHash(resultHash)) {
                cardHashRepository.save(new CardHash(resultHash));
                return resultNumber.toString();
            }
            ++attempt;
        } while (attempt < 100);

        throw new RuntimeException("Too much attempts to generate card number. Something went wrong.");
    }

    private Card getCardById(UUID cardId) {
        Card card = cardRepository.findCardById(cardId);

        if (card == null) {
            throw new NotFoundException("Card not found");
        }

        return card;
    }

    ///  Различные проверки

    private void validateBalanceUpdateCorrect(Card card, double sum) {
        double newBalance;
        double currentBalance = card.getBalance();
        if (currentBalance > Double.MAX_VALUE - sum) {
            throw new IllegalArgumentException("Balance is too high");
        }
        newBalance = currentBalance + sum;
        if (newBalance < 0) {
            throw new IllegalArgumentException("Not enough balance");
        }
    }

    private void checkCardAvailable(Card card) {
        if (!(card.getStatus() == Card.Status.ACTIVE
            && !card.getValidityPeriod().isBefore(LocalDate.now()))) {
            throw new AccessDeniedException("Card is not available");
        }
    }

    private boolean isCurrentUserIsAdmin() {
        Authentication auth = getAuthData();
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
