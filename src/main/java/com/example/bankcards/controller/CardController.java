package com.example.bankcards.controller;

import com.example.bankcards.dto.CardInfoResponse;

import java.util.UUID;

public interface CardController {
    public CardInfoResponse createCard(UUID owner, Integer validationPeriod) throws Exception;
}
