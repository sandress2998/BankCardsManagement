package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

public interface CardService {

    /**
     * Создает новую карту для пользователя с указанным ownerId.
     * Генерируется уникальный номер карты, происходит шифрование номера и ключа.
     * Сохраняет карту и ключ в базе данных.
     *
     * Нюансы:
     * - Требуется роль ROLE_ADMIN.
     * - Генерация номера карты с проверкой уникальности до 100 попыток.
     * - Использует сервис безопасности для шифрования номера и ключа.
     * - Если пользователь не найден, выбрасывается NotFoundException.
     *
     * @param body CardCreateRequest - информация, необходимая для создания карты
     * @return информация о созданной карте (CardInfoResponse)
     * @throws com.example.bankcards.exception.NotFoundException если admin ввел несуществующий userId
     */
    CardCreateResponse createCard(CardCreateRequest body);

    /**
     * Удаляет карту из системы вместе с сопутствующими данными.
     *
     * Нюансы:
     * - Требуется роль ROLE_ADMIN.
     * - Удаляются связанные записи: хэши, шифрованные ключи, заявки на блокировку.
     *
     * @param cardId UUID карты для удаления
     */
    void deleteCard(UUID cardId);

    /**
     * Обновляет статус карты (активирует или блокирует).
     *
     * Нюансы:
     * - Требуется роль ROLE_ADMIN.
     * - Поддерживаются действия: ACTIVATE, BLOCK.
     *
     * @param cardId UUID карты для обновления
     * @param body CardUpdateStatusRequest - тело, содержащее новый статус карты
     */
    void updateCardStatus(UUID cardId, CardUpdateStatusRequest body);

    /**
     * Получает список карточек пользователя с возможностью фильтрации по статусу и пагинацией.
     *
     * Нюансы:
     * - Если вызывающий администратор, можно указать id пользователя, иначе возвращаются карты текущего пользователя.
     * - При отсутствии пользователя выбрасывается NotFoundException.
     *
     * @param cardFilter CardFilter - Query-параметры для поиска фильтрации
     * @return список информации о картах (CardInfoResponse)
     * @throws com.example.bankcards.exception.NotFoundException если admin ввел несуществующий userId
     */
    List<CardInfoResponse> getCards(CardFilter cardFilter);

    /**
     * Переводит деньги с одной карты на другую.
     *
     * Нюансы:
     * - Сумма перевода не может быть отрицательной.
     * - Проверяется доступность обеих карт.
     * - Проверяется баланс карты-отправителя.
     *
     * @param body объект с параметрами перевода (содержит id карт и сумму)
     */
    void doMoneyTransfer(CardTransferMoney body);

    /**
     * Обновляет баланс карты, выполняя списание или зачисление.
     *
     * Нюансы:
     * - Проверяется валидность суммы и доступность карты.
     * - Поддерживаются действия WITHDRAW_MONEY и DEPOSIT_MONEY.
     *
     * @param body данные запроса с нужным действием и суммой для перевода/снятия
     * @return ответ с обновлённым балансом карты (CardBalanceResponse)
     * @throws com.example.bankcards.exception.BadRequestException
     */
    CardBalanceResponse updateCardBalanceAction(UUID cardId, CardBalanceRequest body);

    /**
     * Получает текущий баланс карты по её номеру.
     *
     * @param cardId UUID номер карты
     * @return ответ с балансом карты (CardBalanceResponse)
     */
    CardBalanceResponse getBalance(UUID cardId);

    /**
     * Создает заявку на смену статуса карты.
     *
     * Нюансы:
     * - Проверяется доступность карты перед созданием заявки.
     * @param cardId UUID - id карты
     * @param body объект с номером карты для блокировки
     */
    void createCardStatusUpdateRequest(UUID cardId, CardUpdateStatusRequest body);
}

/*
public interface CardService {
    CardInfoResponse create(UUID ownerId, Integer monthsQuantityUntilExpires);

    void delete(UUID cardId);

    void updateCard(Card.CardAction cardAction, UUID cardId);

    List<CardInfoResponse> getCardsInfo(UUID id, Card.Status status, int page, int size);

    void doMoneyTransfer(CardTransferMoney body);

    // ABSOLUTELY TEST METHOD (it shouldn't be in production, but I added it for knowing card number)
    List<CardFullInfoResponse> getAllCards();

    CardBalanceResponse updateCardBalanceAction(Card.BalanceAction action, CardBalanceRequest body);

    CardBalanceResponse getBalance(CardNumberBody body);

    void submitCardBlocking(CardNumberBody body);

    List<CardBlockingResponse> getBlockRequests(int page, int size);

    void processBlockRequest(UUID cardId);
}
 */