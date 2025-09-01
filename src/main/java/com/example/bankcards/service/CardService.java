package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;

import java.util.List;
import java.util.UUID;

public interface CardService {
    CardInfoResponse create(UUID ownerId, Integer monthsQuantityUntilExpires);

    void delete(CardNumberBody request);

    void updateCard(Card.CardAction cardAction, CardNumberBody request);

    List<CardInfoResponse> getCardsInfo(UUID id, Card.Status status, int page, int size);

    void doMoneyTransfer(CardTransferMoney body);

    // ABSOLUTELY TEST METHOD (it shouldn't be in production, but I added it for knowing card number)
    List<CardFullInfoResponse> getAllCards();

    CardBalanceResponse processCardBalanceAction(Card.BalanceAction action, CardBalanceRequest body);

    void submitCardBlocking(CardNumberBody body);

    List<CardBlockingResponse> getBlockRequests(int page, int size);

    void processBlockRequest(UUID cardId);
}
