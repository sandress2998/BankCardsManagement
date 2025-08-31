package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.CardController;
import com.example.bankcards.dto.CardInfoResponse;
import com.example.bankcards.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/card")
public class CardControllerImpl implements CardController {
    @Autowired
    private CardService cardService;

    @PostMapping
    @Override
    public CardInfoResponse createCard(
        @RequestParam UUID ownerId,
        @RequestParam(required = false) Integer validationPeriod
    ) throws Exception {
        return cardService.create(ownerId, validationPeriod);
    }

}
