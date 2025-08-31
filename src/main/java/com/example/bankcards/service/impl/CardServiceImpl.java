package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardBalanceResponse;
import com.example.bankcards.dto.CardInfoResponse;
import com.example.bankcards.dto.CardNumberBody;
import com.example.bankcards.dto.CardTransferMoney;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardEncryptionKey;
import com.example.bankcards.entity.CardHash;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.CardHashRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardSecurityService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardUtil;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${card.months-until-expires}")
    private int monthsQuantityUntilExpiresDefault;

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
        String cardNum = generateUniqueCardNumber();

        // если все норм, то генерируем ключ AES(CardSecurityService) и шифруем номер карты
        SecretKey cardSecretKey = cardSecurityService.generateKey();
        String encryptedCardNumber = cardSecurityService.encryptNumber(cardNum, cardSecretKey);

        // шифруем ключ
        CardEncryptionKey encryptedSecretKey = new CardEncryptionKey(cardSecurityService.encryptKey(cardSecretKey));
        Card newCard = new Card(owner, encryptedCardNumber, setValidityPeriod(monthsQuantityUntilExpires));

        newCard.setEncryptionKey(encryptedSecretKey);
        encryptedSecretKey.setCard(newCard);

        Card receivedCard = cardRepository.save(newCard);
        cardSecurityService.saveEncryptedKey(encryptedSecretKey);

        return mapToCardInfoResponse(receivedCard);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public void updateCard(Card.Action action, CardNumberBody request) {
        Card card = getCardByNumber(request.number());
        switch (action) {
            case ACTIVATE -> {
                cardRepository.updateCardStatus(card.getId(), Card.Status.ACTIVE);
            }
            case BLOCK -> {
                cardRepository.updateCardStatus(card.getId(), Card.Status.BLOCKED);
            }
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public void delete(CardNumberBody request) throws Exception {
        Card card = getCardByNumber(request.number());

        // надо удалить хэш из CardHash
        String hashedKey = cardSecurityService.calculateHmac(request.number());
        cardHashRepository.deleteByHmacHash(hashedKey);
        // надо удалить карту из CardEncryptionKey
        cardSecurityService.deleteEncryptedKey(card.getEncryptionKey().getId());
        // надо удалить карту из Card
        cardRepository.delete(card);


        // если есть, надо удалить из CardBlockRequest
    }

    @Override
    public CardBalanceResponse getBalance(CardNumberBody request) {
        Card card = getCardByNumber(request.number());
        return new CardBalanceResponse(card.getBalance());
    }

    @Override
    public List<CardInfoResponse> getCardsInfo(UUID id) throws Exception {
        Authentication auth = getAuthData();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        UUID currentUserId = UUID.fromString(auth.getName());

        if (!isAdmin && !currentUserId.equals(id)) {
            throw new AccessDeniedException("Access denied: not admin and not owner");
        }

        if (isAdmin && id == null) {
            throw new BadRequestException("Id is missing");
        }

        UUID ownerId = isAdmin ? id : currentUserId;

        List<Card> cards = cardRepository.findByOwnerId(ownerId);
        List<CardInfoResponse> cardInfoResponses = new ArrayList<>();
        for (Card card : cards) {
            cardInfoResponses.add(mapToCardInfoResponse(card));
        }
        return cardInfoResponses;
    }


    private String getCardNumber(Card card) throws Exception {
        CardEncryptionKey encryptionKey = card.getEncryptionKey();
        SecretKey decryptedKey = cardSecurityService.decryptKey(encryptionKey.getEncryptedKey());

        return cardSecurityService.decryptNumber(card.getEncryptedNumber(), decryptedKey);
    }

    @Transactional
    @Override
    public void doMoneyTransfer(CardTransferMoney body) throws BadRequestException {
        if (body.amount() < 0) {
            throw new BadRequestException("Amount cannot be negative");
        }

        Card cardFrom = getCardByNumber(body.from());
        Card cardTo = getCardByNumber(body.to());

        if (!cardFrom.getOwner().getId().equals(UUID.fromString(getAuthData().getName())))
        {
            throw new AccessDeniedException("You don't have permission to transfer money");
        }

        if (isBalanceUpdateCorrect(cardFrom, -body.amount()) &&  isBalanceUpdateCorrect(cardFrom, +body.amount())) {
            cardRepository.updateCardBalance(cardFrom.getId(), cardFrom.getBalance() - body.amount());
            cardRepository.updateCardBalance(cardTo.getId(), cardTo.getBalance() + body.amount());
        }
    }

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

    private CardInfoResponse mapToCardInfoResponse(Card card) throws Exception {
        String plainNumber = getCardNumber(card);

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
            resultHash = cardSecurityService.calculateHmac(resultNumber.toString());

            if (!cardHashRepository.existsByHmacHash(resultHash)) {
                cardHashRepository.save(new CardHash(resultHash));
                return resultNumber.toString();
            }
            ++attempt;
        } while (attempt < 100);

        throw new RuntimeException("Too much attempts to generate card number. Something went wrong.");
    }

    private boolean isBalanceUpdateCorrect(Card card, double sum) {
        double newBalance;
        double currentBalance = card.getBalance();
        if (currentBalance > Double.MAX_VALUE - sum) {
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
}
