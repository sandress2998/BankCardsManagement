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
     * @param ownerId UUID пользователя-владельца карты
     * @param monthsQuantityUntilExpires количество месяцев до окончания срока действия карты (может быть null)
     * @return информация о созданной карте (CardInfoResponse)
     * @throws com.example.bankcards.exception.NotFoundException если admin ввел несуществующий userId
     */
    CardInfoResponse create(UUID ownerId, Integer monthsQuantityUntilExpires);

    /**
     * Удаляет карту из системы вместе с сопутствующими данными.
     *
     * Нюансы:
     * - Требуется роль ROLE_ADMIN.
     * - Удаляются связанные записи: хэши, шифрованные ключи, заявки на блокировку.
     *
     * @param cardId UUID карты для удаления
     */
    void delete(UUID cardId);

    /**
     * Обновляет статус карты (активирует или блокирует).
     *
     * Нюансы:
     * - Требуется роль ROLE_ADMIN.
     * - Поддерживаются действия: ACTIVATE, BLOCK.
     *
     * @param cardAction действие для изменения статуса карты
     * @param cardId UUID карты для обновления
     */
    void updateCard(Card.CardAction cardAction, UUID cardId);

    /**
     * Получает список карточек пользователя с возможностью фильтрации по статусу и пагинацией.
     *
     * Нюансы:
     * - Если вызывающий администратор, можно указать id пользователя, иначе возвращаются карты текущего пользователя.
     * - При отсутствии пользователя выбрасывается NotFoundException.
     *
     * @param id UUID пользователя, карты которого запрашиваются (может быть null)
     * @param status статус карты для фильтрации (может быть null)
     * @param page номер страницы для пагинации (0-основанный)
     * @param size размер страницы
     * @return список информации о картах (CardInfoResponse)
     * @throws com.example.bankcards.exception.NotFoundException если admin ввел несуществующий userId
     */
    List<CardInfoResponse> getCardsInfo(UUID id, Card.Status status, int page, int size);

    /**
     * Переводит деньги с одной карты на другую.
     *
     * Нюансы:
     * - Сумма перевода не может быть отрицательной.
     * - Проверяется доступность обеих карт.
     * - Проверяется баланс карты-отправителя.
     *
     * @param body объект с параметрами перевода (содержит номера карт и сумму)
     */
    void doMoneyTransfer(CardTransferMoney body);

    /**
     * Тестовый метод для получения полной информации обо всех картах, включая расшифрованные номера.
     *
     * Такого не должно быть в production, он существует для быстрой проверки корректности работы сервера
     *
     * Нюансы:
     * - Требуется роль ROLE_ADMIN.
     * - Использовать только для тестирования, не рекомендуется в продакшене из-за безопасности.
     *
     * @return список полной информации о картах (CardFullInfoResponse)
     */
    List<CardFullInfoResponse> getAllCards();

    /**
     * Обновляет баланс карты, выполняя списание или зачисление.
     *
     * Нюансы:
     * - Проверяется валидность суммы и доступность карты.
     * - Поддерживаются действия WITHDRAW_MONEY и DEPOSIT_MONEY.
     *
     * @param action действие обновления баланса
     * @param body данные запроса с номером карты и суммой
     * @return ответ с обновлённым балансом карты (CardBalanceResponse)
     * @throws com.example.bankcards.exception.BadRequestException
     */
    CardBalanceResponse updateCardBalanceAction(Card.BalanceAction action, CardBalanceRequest body);

    /**
     * Получает текущий баланс карты по её номеру.
     *
     * @param body объект с номером карты
     * @return ответ с балансом карты (CardBalanceResponse)
     */
    CardBalanceResponse getBalance(CardNumberBody body);

    /**
     * Создает заявку на блокировку карты.
     *
     * Нюансы:
     * - Проверяется доступность карты перед созданием заявки.
     *
     * @param body объект с номером карты для блокировки
     */
    void submitCardBlocking(CardNumberBody body);

    /**
     * Возвращает список заявок на блокировку карт с пагинацией.
     *
     * Нюансы:
     * - Требуется роль ROLE_ADMIN.
     *
     * @param page номер страницы для пагинации (0-основанный)
     * @param size размер страницы
     * @return список заявок на блокировку (CardBlockingResponse)
     */
    List<CardBlockingResponse> getBlockRequests(int page, int size);

    /**
     * Обрабатывает заявку на блокировку карты — меняет статус на BLOCKED и удаляет заявку.
     *
     * Нюансы:
     * - Требуется роль ROLE_ADMIN.
     * - Проверяется существование карты.
     * - cardId не может быть null.
     *
     * @param cardId UUID карты, для которой обрабатывается блокировка
     * @throws NotFoundException если карта не найдена
     */
    void processBlockRequest(UUID cardId);
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