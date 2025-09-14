package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.CardController;
import com.example.bankcards.dto.*;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Card API", description = "API для управления банковскими картами")
@SecurityRequirement(name = "JWT")
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardControllerImpl implements CardController {

    private final CardService cardService;

    @Operation(
        summary = "Перевод средств между картами",
        description = "Выполняет перевод указанной суммы с одной карты на другую.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Перевод выполнен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Одна из карт не найдена"),
            @ApiResponse(responseCode = "403", description = "Карта недоступна для операции"),
            @ApiResponse(responseCode = "401", description = "JWT отсутствует или невалидный")
        }
    )
    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void doMoneyTransfer(
        @RequestBody(description = "Данные перевода", required = true,
            content = @Content(schema = @Schema(implementation = CardTransferMoney.class)))
        @org.springframework.web.bind.annotation.RequestBody CardTransferMoney body
    ) {
        cardService.doMoneyTransfer(body);
    }

    @Operation(
        summary = "Получить список карт пользователя",
        description = "Возвращает информацию о картах с возможностью фильтрации по статусу и пагинации",
        responses = {
            @ApiResponse(responseCode = "200", description = "Список карт пользователя",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardInfoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "401", description = "JWT отсутствует или невалидный")
        }
    )
    @GetMapping
    @JsonView(Views.User.class)
    @Override
    public MappingJacksonValue getCards(@ModelAttribute CardFilter filter) {
        List<CardInfoResponse> cardsInfo = cardService.getCards(filter);

        SimpleFilterProvider filters;
        String[] fields = filter.getFields();
        if (fields == null || fields.length == 0) {
            // разрешаем сериализовать все поля
            filters = new SimpleFilterProvider().setFailOnUnknownId(false);
        } else {
            filters = new SimpleFilterProvider()
                    .addFilter("dynamicFilter", SimpleBeanPropertyFilter.filterOutAllExcept(fields));
        }

        MappingJacksonValue wrapper = new MappingJacksonValue(cardsInfo);
        wrapper.setFilters(filters);
        return wrapper;

    }

    @Operation(
        summary = "Изменить баланс карты (пополнение/снятие)",
        description = "Изменяет баланс карты в зависимости от действия WITHDRAW_MONEY или DEPOSIT_MONEY",
        responses = {
            @ApiResponse(responseCode = "200", description = "Баланс карты обновлен",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardBalanceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные или операция невозможна"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Карта недоступна"),
            @ApiResponse(responseCode = "401", description = "JWT отсутствует или невалидный")
        }
    )
    @PutMapping("/{cardId}/balance")
    @Override
    public CardBalanceResponse updateCardBalance(
        @Parameter(description = "Уникальный идентификатор карты")
        @PathVariable UUID cardId,

        @RequestBody(description = "Данные для обновления баланса", required = true,
            content = @Content(schema = @Schema(implementation = CardBalanceRequest.class)))
        @org.springframework.web.bind.annotation.RequestBody CardBalanceRequest body
    ) {
        return cardService.updateCardBalanceAction(cardId, body);
    }

    @Operation(
        summary = "Получить баланс карты",
        description = "Возвращает текущий баланс карты по номеру",
        responses = {
            @ApiResponse(responseCode = "200", description = "Баланс карты",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardBalanceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
                @ApiResponse(responseCode = "401", description = "JWT отсутствует или невалидный")
        }
    )
    @GetMapping("/{cardId}/balance")
    @Override
    public CardBalanceResponse getBalance(
        @Parameter(description = "Уникальный идентификатор карты")
        @PathVariable UUID cardId
    ) {
        return cardService.getBalance(cardId);
    }

    @Operation(
        summary = "Создать заявку на изменение статуса карты",
        description = "Отправляет заявку на изменение статуса карты",
        responses = {
            @ApiResponse(responseCode = "201", description = "Заявка на изменение статуса карты создана"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Карта недоступна"),
            @ApiResponse(responseCode = "401", description = "JWT отсутствует или невалидный")
        }
    )
    @PostMapping("/{cardId}/status")
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public void createCardStatusUpdateRequest(
        @Parameter(description = "Уникальный идентификатор карты")
        @PathVariable UUID cardId,

        @RequestBody(description = "Статус, который клиент хочет присвоить карте", required = true,
                content = @Content(schema = @Schema(implementation = CardUpdateStatusRequest.class)))
        @org.springframework.web.bind.annotation.RequestBody CardUpdateStatusRequest body
    ) {
        cardService.createCardStatusUpdateRequest(cardId, body);
    }
}


/*
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

    @PatchMapping("/{cardId}")
    @Override
    public void updateCardStatus(@RequestParam Card.CardAction action, @PathVariable UUID cardId) {
        cardService.updateCard(action, cardId);
    }

    @DeleteMapping("/{cardId}")
    @Override
    public void delete(@PathVariable UUID cardId) {
        cardService.delete(cardId);
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

    @PostMapping("/balance/update")
    @Override
    public CardBalanceResponse updateCardBalance(@RequestParam Card.BalanceAction action, @RequestBody CardBalanceRequest body) {
        return cardService.updateCardBalanceAction(action, body);
    }

    @PostMapping("/balance/view")
    @Override
    public CardBalanceResponse updateCardBalance( @RequestBody CardNumberBody body) {
        return cardService.getBalance(body);
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
*/