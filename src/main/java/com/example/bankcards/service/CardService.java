package com.example.bankcards.service;

import com.example.bankcards.dto.CardActionRequest;
import com.example.bankcards.dto.CardInfoResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.dto.CardBalanceResponse;

import java.util.List;
import java.util.UUID;

public interface CardService {
    public CardInfoResponse create(UUID ownerId, Integer monthsQuantityUntilExpires) throws Exception;

    public void activate(CardActionRequest request);

    public void block(CardActionRequest request);

    public void delete(CardActionRequest request);

    public CardBalanceResponse getBalance(CardActionRequest request);

    public List<Card> getAllCards(UUID ownerId);

    public void doMoneyTransfer(String cardNumberFrom, String cardNumberTo);
}
