package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import org.springframework.http.converter.json.MappingJacksonValue;

import java.util.UUID;

public interface CardController {
    // Перевод денег между картами
    void doMoneyTransfer(CardTransferMoney body);

    // Получение списка карт с фильтрацией и пагинацией
    MappingJacksonValue getCards(CardFilter filter);

    CardBalanceResponse updateCardBalance(UUID cardId, CardBalanceRequest body);

    CardBalanceResponse getBalance(UUID cardId);

    void createCardStatusUpdateRequest(UUID cardId, CardUpdateStatusRequest body);
}
