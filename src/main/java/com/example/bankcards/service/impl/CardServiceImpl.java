package com.example.bankcards.service.impl;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.*;
import com.example.bankcards.entity.CardUpdateStatusRequest;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.CardHashRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardUpdateStatusRequestRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardSecurityService;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardMapper;
import com.example.bankcards.util.CardSpecifications;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    public int monthsQuantityUntilExpiresDefault;

    private final Environment env;
    private final CardRepository cardRepository;
    private final CardHashRepository cardHashRepository;
    private final UserRepository userRepository;
    private final CardSecurityService cardSecurityService;
    private final CardUpdateStatusRequestRepository cardUpdateStatusRequestRepository;

    @PostConstruct
    void init() {
        this.monthsQuantityUntilExpiresDefault = env.getProperty("card.months-until-expires", Integer.class, 24);
    }

    // ГОТОВО
    @Transactional
    @Override
    public CardCreateResponse createCard(CardCreateRequest body) {
        User owner = userRepository.findUserById(body.getOwnerId());

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
        Card newCard = new Card(owner, encryptedCardNumber, setValidityPeriod(body.getMonthsQuantityUntilExpires()));

        newCard.setEncryptionKey(encryptedSecretKey);
        encryptedSecretKey.setCard(newCard);

        // сохраняем в базу данных
        Card receivedCard = cardRepository.save(newCard);
        cardSecurityService.saveEncryptedKey(encryptedSecretKey);

        return CardMapper.toPostResponse(receivedCard, cardNum);
    }

    @Transactional
    @Override
    public void updateCardStatus(UUID cardId, com.example.bankcards.dto.CardUpdateStatusRequest body) {
        Card card = getCardById(cardId);

        cardRepository.updateCardStatus(card.getId(), body.status());

        if (body.isRequested()) {
            cardUpdateStatusRequestRepository.deleteByCardId(cardId);
        }
    }

    // ГОТОВО
    // Должен быть метод, который создает заявку на изменение статуса карты
    @Transactional
    @Override
    public void createCardStatusUpdateRequest(UUID cardId, com.example.bankcards.dto.CardUpdateStatusRequest body) {
        Card card = getCardById(cardId);

        checkCardAvailable(card);

        cardUpdateStatusRequestRepository.save(new CardUpdateStatusRequest(card, body.status()));
    }

    @Transactional
    @Override
    public void deleteCard(UUID cardId)  {
        Card card = getCardById(cardId);
        String number = getCardNumber(card);

        // надо удалить хэш из CardHash
        String hashedKey = cardSecurityService.calculateHmac(number);
        cardHashRepository.deleteByHmacHash(hashedKey);
        // надо удалить карту из CardEncryptionKey
        cardSecurityService.deleteEncryptedKey(card.getEncryptionKey().getId());
        // если есть, надо удалить из CardBlockRequest
        cardUpdateStatusRequestRepository.deleteByCardId(card.getId());
        // надо удалить карту из Card
        cardRepository.delete(card);
    }

    @Override
    public List<CardInfoResponse> getCards(CardFilter filter) {
        Specification<Card> spec;

        if (filter instanceof AdminCardFilter) {
            spec = CardSpecifications.byFilter((AdminCardFilter) filter);
        } else {
            spec = CardSpecifications.byFilter(filter);
        }

        Sort sort = Sort.unsorted();
        if (filter.getSortBy() != null) {
            Sort.Direction dir = "DESC".equalsIgnoreCase(filter.getSortDirection()) ?
                    Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(dir, filter.getSortBy());
        }

        PageRequest page = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        return cardRepository.findAll(spec, page)
                .map(card -> CardMapper.toGetResponse(card, getCardNumber(card)))
                .getContent();
    }

    // ГОТОВО
    @Transactional
    @Override
    public CardBalanceResponse updateCardBalanceAction(UUID cardId, CardBalanceRequest body) {
        checkIfCurrentUserIsCardOwner(cardId);

        Card card = getCardById(cardId);

        switch (body.action()) {
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
    public CardBalanceResponse getBalance(UUID cardId) {
        checkIfCurrentUserIsCardOwner(cardId);

        Card card = getCardById(cardId);

        return new CardBalanceResponse(card.getBalance());
    }

    // ГОТОВА
    /** Перевод денег из одной карты на другую */
    @Transactional
    @Override
    public void doMoneyTransfer(CardTransferMoney body) {
        checkIfCurrentUserIsCardOwner(body.from());

        if (body.amount() < 0) {
            throw new BadRequestException("Amount cannot be negative");
        }

        Card cardFrom = getCardById(body.from());
        Card cardTo = getCardById(body.to());

        checkCardAvailable(cardFrom);
        checkCardAvailable(cardTo);

        validateBalanceUpdateCorrect(cardFrom, -body.amount());
        validateBalanceUpdateCorrect(cardFrom, body.amount());

        cardRepository.transferMoney(cardFrom.getId(), cardTo.getId(), body.amount());
    }

    /*
    Позволяет узнать почти все данные карты, в том числе номер карты. Тестовый метод (созданный для удобства тестирования и проверки)
    public List<CardInfoAdminResponse> getCardsToAdmin() {
        List<Card> cards = cardRepository.findAll();
        ArrayList<CardInfoAdminResponse> cardsFullInfo = new ArrayList<>();

        for (Card card : cards) {
            На самом деле здесь будет не encrypted number, а decrypted number (ради теста)
            card.setEncryptedNumber(getCardNumber(card));
            cardsFullInfo.add(mapToCardInfoAdminResponse(card));
        }
        return cardsFullInfo;
    }
    */

    /*
    public List<CardBlockingResponse> getBlockRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<CardUpdateStatusRequest> requests = cardUpdateStatusRequestRepository.findAll(pageable);
        List<CardBlockingResponse> result = new ArrayList<>();
        for (CardUpdateStatusRequest request: requests) {
            result.add(new CardBlockingResponse(request.getCard().getId()));
        }

        return result;
    }
     */

    /*
    // ГОТОВО
    @Transactional
    public void processStatusUpdateRequest(UUID cardId, Card.Status status) {
        if (!cardRepository.existsCardById(cardId)) {
            throw new NotFoundException("Card not found");
        }

        if (status == Card.Status.BLOCKED) {
            cardRepository.updateCardStatus(cardId, Card.Status.BLOCKED);
            cardUpdateStatusRequestRepository.deleteByCardId(cardId);
        } else {
            throw new BadRequestException("Invalid requested status");
        }
    }
     */

    // ГОТОВО (но не тестировано)
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

    // ГОТОВО (но не тестировано)
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

    /** Функция, расшифровывающая номер карты по переданному объекту Card
    * @throws RuntimeException если произошла ошибка про расшифровании
    */

    public String getCardNumber(Card card) {
        try {
            CardEncryptionKey encryptionKey = card.getEncryptionKey();
            SecretKey decryptedKey = cardSecurityService.decryptKey(encryptionKey.getEncryptedKey());
            return cardSecurityService.decryptNumber(card.getEncryptedNumber(), decryptedKey);
        } catch (Exception e) {
            throw new RuntimeException("Internal error");
        }
    }

    public LocalDate setValidityPeriod(Integer monthsQuantity) {
        if (monthsQuantity == null) {
            monthsQuantity = monthsQuantityUntilExpiresDefault;
        }

        LocalDate today = LocalDate.now();
        LocalDate targetMonth = today.plusMonths(monthsQuantity);

        return targetMonth.with(TemporalAdjusters.lastDayOfMonth());
    }


    ///  Получение/генерация вспомогательных данных

    /** @throws UnauthorizedException если пользователь не авторизован */
    private Authentication getAuthData() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        return auth;
    }

    /** Генерирует уникальный номер карты. Гарантирует что карты с таким номером не существует.
    * @throws RuntimeException если генерация номера прошла неудачно
    */
    public String generateUniqueCardNumber()  {
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

    /** @throws com.example.bankcards.exception.NotFoundException если карта не была найдена */
    private Card getCardById(UUID cardId) {
        Card card = cardRepository.findCardById(cardId);

        if (card == null) {
            throw new NotFoundException("Card not found");
        }

        return card;
    }

    ///  Различные проверки

    /** @throws IllegalArgumentException если операция с балансом выходит за рамку допустимого */
    public void validateBalanceUpdateCorrect(Card card, double sum) {
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

    /** @throws AccessDeniedException если карта не активна */
    public void checkCardAvailable(Card card) {
        if (!(card.getStatus() == Card.Status.ACTIVE
            && !card.getValidityPeriod().isBefore(LocalDate.now()))) {
            throw new AccessDeniedException("Card is not available");
        }
    }

    public void checkIfCurrentUserIsCardOwner(UUID cardId) {
        Card card = getCardById(cardId);

        UUID ownerId = card.getOwner().getId();
        UUID currentUserId = UUID.fromString(getAuthData().getName());

        if (!ownerId.equals(currentUserId)) {
            throw new AccessDeniedException("Current user is not owner of the card");
        }
    }
}



// НУЖНО УДАЛИТЬ (Но в тестах все еще используется, нужно переделать тесты)
/** Функция, по номеру карты определяющая объект Card (извлекается из базы данных)
 *  @throws com.example.bankcards.exception.NotFoundException если карта не была найдена
 * @throws RuntimeException если расшифрование не удалось
 */
    /*
    public Card getCardByNumber(String cardNumber) {
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
    */

// НУЖНО УДАЛИТЬ
    /*
    private CardInfoAdminResponse mapToCardInfoAdminResponse(Card card) {
        return new CardInfoAdminResponse(
            card.getId(),
            card.getEncryptedNumber(), // на самом деле здесь должен быть предварительно расшифрованный номер карты
            card.getOwner().getLogin(),
            card.getValidityPeriod(),
            card.getStatus(),
            card.getBalance()
        );
    }
    */

    /*
    private CardInfoResponse mapToCardInfoResponse(Card card) {
        String plainNumber = getCardNumber(card);

        String maskedNumber = maskCardNumber(plainNumber);
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
    */


    /*
    private boolean isCurrentUserIsAdmin() {
        Authentication auth = getAuthData();
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
     */