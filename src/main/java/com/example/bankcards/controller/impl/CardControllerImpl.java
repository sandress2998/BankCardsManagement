package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.CardController;
import com.example.bankcards.dto.CardBalanceResponse;
import com.example.bankcards.dto.CardInfoResponse;
import com.example.bankcards.dto.CardNumberBody;
import com.example.bankcards.dto.CardTransferMoney;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @PatchMapping
    public void updateCardStatus(@RequestParam Card.Action action, @RequestBody CardNumberBody body) throws Exception {
        cardService.updateCard(action, body);
    }

    @DeleteMapping
    public void deleteCard(@RequestBody CardNumberBody body) throws Exception {
        cardService.delete(body);
    }

    @PostMapping("/transfer")
    public void transfer(@RequestBody CardTransferMoney body) throws Exception {
        cardService.doMoneyTransfer(body);
    }

    @GetMapping
    public List<CardInfoResponse> getCardsInfo(@RequestParam(required = false) UUID id) throws Exception {
        return cardService.getCardsInfo(id);
    }

    @GetMapping("/balance")
    public CardBalanceResponse getCardBalance(@RequestBody CardNumberBody body) throws Exception {
        return cardService.getBalance(body);
    }
}
