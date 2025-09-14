package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import org.springframework.http.converter.json.MappingJacksonValue;

import java.util.UUID;

public interface AdminCardController {
    MappingJacksonValue getCards(AdminCardFilter filter);

    CardCreateResponse createCard(CardCreateRequest body);

    void updateCardStatus(UUID cardId, CardUpdateStatusRequest body);

    void deleteCard(UUID cardId);
}
