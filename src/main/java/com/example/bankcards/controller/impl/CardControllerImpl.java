package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.CardController;
import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/card")
public class CardControllerImpl implements CardController {
    private final CardService cardService;

    public CardControllerImpl(CardService cardService) {
        this.cardService = cardService;
    }

    // ABSOLUTELY TEST METHOD
    @GetMapping("/full-info")
    public List<CardFullInfoResponse> getAllCards() {
        return cardService.getAllCards();
    }

    @PostMapping
    @Override
    public CardInfoResponse create(
        @RequestParam UUID ownerId,
        @RequestParam(required = false) Integer monthsQuantityUntilExpires
    ) {
        return cardService.create(ownerId, monthsQuantityUntilExpires);
    }

    @PatchMapping
    @Override
    public void updateCardStatus(@RequestParam Card.CardAction action, @RequestBody CardNumberBody body) {
        cardService.updateCard(action, body);
    }

    @DeleteMapping
    @Override
    public void delete(@RequestBody CardNumberBody body) {
        cardService.delete(body);
    }

    @PostMapping("/transfer")
    @Override
    public void doMoneyTransfer(@RequestBody CardTransferMoney body) {
        cardService.doMoneyTransfer(body);
    }

    @GetMapping
    @Override
    public List<CardInfoResponse> getCardsInfo(
        @RequestParam(required = false) UUID id,
        @RequestParam(required = false) Card.Status status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "3") int size
    ) {
        return cardService.getCardsInfo(id, status, page, size);
    }

    @PostMapping("/balance")
    @Override
    public CardBalanceResponse updateCardBalance(@RequestParam Card.BalanceAction action, @RequestBody CardBalanceRequest body) {
        return cardService.processCardBalanceAction(action, body);
    }

    @PostMapping("/blocking/submit")
    @Override
    public void submitCardBlocking(@RequestBody CardNumberBody body) {
        cardService.submitCardBlocking(body);
    }

    @PostMapping("/blocking/process/{cardId}")
    @Override
    public void processBlockRequest(@PathVariable UUID cardId) {
        cardService.processBlockRequest(cardId);
    }

    @GetMapping("/blocking")
    @Override
    public List<CardBlockingResponse> getBlockRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return cardService.getBlockRequests(page, size);
    }
}
