package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.CardController;
import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Card API", description = "API для управления банковскими картами")
@RestController
@RequestMapping("/api/card")
public class CardControllerImpl implements CardController {

    private final CardService cardService;

    public CardControllerImpl(CardService cardService) {
        this.cardService = cardService;
    }

    @Operation(
        summary = "Тестовый метод: Получить подробную информацию о всех картах",
        description = "Возвращает данные всех карт включая расшифрованные номера. Использовать только для тестирования.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Список всех карт",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardFullInfoResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT отсутствует или невалидный")
        }
    )
    @GetMapping("/full-info")
    public List<CardFullInfoResponse> getAllCards() {
        return cardService.getAllCards();
    }

    @Operation(
        summary = "Создать карту для пользователя",
        description = "Создает карту, генерирует номер и шифрует его. Требуется ownerId и необязательно количество месяцев до истечения срока.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Информация о созданной карте",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardInfoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "401", description = "JWT отсутствует или невалидный")
        }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardInfoResponse create(
        @Parameter(description = "UUID владельца карты", required = true)
        @RequestParam UUID ownerId,

        @Parameter(description = "Количество месяцев до окончания срока действия карты", required = false)
        @RequestParam(required = false) Integer monthsQuantityUntilExpires
    ) {
        return cardService.create(ownerId, monthsQuantityUntilExpires);
    }

    @Operation(
        summary = "Обновить статус карты",
        description = "Изменяет статус карты (ACTIVATE или BLOCK) по ID карты и действию",
        responses = {
            @ApiResponse(responseCode = "204", description = "Статус карты обновлен"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "401", description = "JWT отсутствует или невалидный")
        }
    )
    @PatchMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCardStatus(
        @Parameter(description = "Действие смены статуса карты", required = true)
        @RequestParam Card.CardAction action,

        @Parameter(description = "UUID карты для обновления", required = true)
        @PathVariable UUID cardId
    ) {
        cardService.updateCard(action, cardId);
    }

    @Operation(
        summary = "Удалить карту",
        description = "Удаляет карту и связанные с ней данные",
        responses = {
            @ApiResponse(responseCode = "204", description = "Карта удалена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "401", description = "JWT отсутствует или невалидный")
        }
    )
    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @Parameter(description = "UUID удаляемой карты", required = true)
        @PathVariable UUID cardId
    ) {
        cardService.delete(cardId);
    }

    @Operation(
        summary = "Перевод средств между картами",
        description = "Выполняет перевод указанной суммы с одной карты на другую",
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
    public List<CardInfoResponse> getCardsInfo(
        @Parameter(description = "UUID пользователя (опционально для администратора)")
        @RequestParam(required = false) UUID id,

        @Parameter(description = "Статус карты для фильтрации")
        @RequestParam(required = false) Card.Status status,

        @Parameter(description = "Номер страницы пагинации", example = "0")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "Размер страницы пагинации", example = "3")
        @RequestParam(defaultValue = "3") int size
    ) {
        return cardService.getCardsInfo(id, status, page, size);
    }

    @Operation(
        summary = "Обновить баланс карты (пополнение/снятие)",
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
    @PatchMapping("/balance/update")
    public CardBalanceResponse updateCardBalance(
        @Parameter(description = "Действие обновления баланса", required = true)
        @RequestParam Card.BalanceAction action,

        @RequestBody(description = "Данные для обновления баланса", required = true,
            content = @Content(schema = @Schema(implementation = CardBalanceRequest.class)))
        @org.springframework.web.bind.annotation.RequestBody CardBalanceRequest body
    ) {
        return cardService.updateCardBalanceAction(action, body);
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
    @PostMapping("/balance/view")
    public CardBalanceResponse getBalance(
        @RequestBody(description = "Номер карты", required = true,
            content = @Content(schema = @Schema(implementation = CardNumberBody.class)))
        @org.springframework.web.bind.annotation.RequestBody CardNumberBody body
    ) {
        return cardService.getBalance(body);
    }

    @Operation(
        summary = "Создать заявку на блокировку карты",
        description = "Отправляет заявку на блокировку карты",
        responses = {
            @ApiResponse(responseCode = "201", description = "Заявка на блокировку создана"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Карта недоступна"),
            @ApiResponse(responseCode = "401", description = "JWT отсутствует или невалидный")
        }
    )
    @PostMapping("/blocking/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public void submitCardBlocking(
        @RequestBody(description = "Номер карты для блокировки", required = true,
            content = @Content(schema = @Schema(implementation = CardNumberBody.class)))
        @org.springframework.web.bind.annotation.RequestBody CardNumberBody body
    ) {
        cardService.submitCardBlocking(body);
    }

    @Operation(
        summary = "Обработка заявки на блокировку карты",
        description = "Меняет статус карты на BLOCKED и удаляет заявку на блокировку",
        responses = {
            @ApiResponse(responseCode = "204", description = "Заявка обработана, карта заблокирована"),
            @ApiResponse(responseCode = "400", description = "Некорректный ID карты"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "401", description = "JWT отсутствует или невалидный")
        }
    )
    @PostMapping("/blocking/process/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void processBlockRequest(
        @Parameter(description = "UUID карты для блокировки", required = true)
        @PathVariable UUID cardId
    ) {
        cardService.processBlockRequest(cardId);
    }

    @Operation(
        summary = "Получить список заявок на блокировку карт",
        description = "Возвращает список заявок на блокировку с пагинацией",
        responses = {
            @ApiResponse(responseCode = "200", description = "Список заявок на блокировку",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardBlockingResponse.class)))
        }
    )
    @GetMapping("/blocking")
    public List<CardBlockingResponse> getBlockRequests(
        @Parameter(description = "Номер страницы пагинации", example = "0")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "Размер страницы пагинации", example = "5")
        @RequestParam(defaultValue = "5") int size
    ) {
        return cardService.getBlockRequests(page, size);
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