package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;

import java.util.List;
import java.util.UUID;

public interface CardController {
    // ABSOLUTELY TEST METHOD
    List<CardFullInfoResponse> getAllCards();

    // Создание карты с опциональным периодом действия
    CardInfoResponse create(UUID ownerId, Integer monthsQuantityUntilExpires);

    // Обновление статуса карты (активация, блокировка и т.п.)
    void updateCardStatus(Card.CardAction cardAction, CardNumberBody body);

    // Удаление карты по номеру
    void delete(CardNumberBody body);

    // Перевод денег между картами
    void doMoneyTransfer(CardTransferMoney body);

    // Получение списка карт с фильтрацией и пагинацией
    List<CardInfoResponse> getCardsInfo(UUID id, Card.Status status, int page, int size);

    CardBalanceResponse updateCardBalance(Card.BalanceAction action, CardBalanceRequest body);

    // Подать заявку на блокировку карты
    void submitCardBlocking(CardNumberBody body);

    // Обработка (блокировка) карты по заявке администратором
    void processBlockRequest(UUID cardId);

    // Получение списка заявок на блокировку с пагинацией
    List<CardBlockingResponse> getBlockRequests(int page, int size);

}
