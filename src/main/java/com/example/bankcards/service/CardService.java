package com.example.bankcards.service;

import com.example.bankcards.dto.CardBalanceResponse;
import com.example.bankcards.dto.CardInfoResponse;
import com.example.bankcards.dto.CardNumberBody;
import com.example.bankcards.dto.CardTransferMoney;
import com.example.bankcards.entity.Card;

import java.util.List;
import java.util.UUID;

public interface CardService {
    CardInfoResponse create(UUID ownerId, Integer monthsQuantityUntilExpires) throws Exception;

    void delete(CardNumberBody request) throws Exception;

    void updateCard(Card.Action action, CardNumberBody request) throws Exception;

    CardBalanceResponse getBalance(CardNumberBody request);

    List<CardInfoResponse> getCardsInfo(UUID id) throws Exception;

    void doMoneyTransfer(CardTransferMoney body) throws Exception;
}
