package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.AdminCardController;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Admin Card API", description = "API для управления банковскими картами. Доступно только для администраторов.")
@SecurityRequirement(name = "JWT")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RestController
@RequestMapping("/admin/api/cards")
@RequiredArgsConstructor
@Slf4j
public class AdminCardControllerImpl implements AdminCardController {

    private final CardService cardService;

    @Operation(
            summary = "Получить информацию о всех картах",
            description = "Возвращает данные всех карт включая расшифрованные номера. Использовать только для тестирования. Доступ есть только у админа",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список всех карт",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardInfoResponse.class))),
                    @ApiResponse(responseCode = "401", description = "JWT отсутствует или невалидный")
            }
    )
    @GetMapping
    @JsonView(value = Views.Admin.class)
    @Override
    public MappingJacksonValue getCards(@ModelAttribute AdminCardFilter filter) {
        System.out.println("Filter status= " + filter.getStatus() + "; ownerId= " + filter.getOwnerId());
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
            summary = "Создать карту для пользователя",
            description = "Создает карту, генерирует номер и шифрует его. Требуется ownerId и необязательно количество месяцев до истечения срока. Доступ есть только у админа",
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
    @Override
    public CardCreateResponse createCard(
            @RequestBody(description = "Количество месяцев, в течение которых карта будет активна", required = true,
                    content = @Content(schema = @Schema(implementation = CardUpdateStatusRequest.class)))
            @org.springframework.web.bind.annotation.RequestBody CardCreateRequest body
    ) {
        return cardService.createCard(body);
    }

    @Operation(
            summary = "Обновить статус карты",
            description = "Изменяет статус карты (ACTIVATE или BLOCK) по ID карты и действию. Доступ есть только у админа.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Статус карты обновлен"),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "401", description = "JWT отсутствует или невалидный")
            }
    )
    @PutMapping("/{cardId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void updateCardStatus(
        @Parameter(description = "UUID карты для обновления", required = true)
        @PathVariable UUID cardId,

        @RequestBody(description = "Новый статус карты", required = true,
                content = @Content(schema = @Schema(implementation = CardUpdateStatusRequest.class)))
        @org.springframework.web.bind.annotation.RequestBody CardUpdateStatusRequest body
    ) {
        cardService.updateCardStatus(cardId, body);
    }

    @Operation(
        summary = "Удалить карту",
        description = "Удаляет карту и связанные с ней данные. Доступ есть только у админа.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Карта удалена"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "401", description = "JWT отсутствует или невалидный")
        }
    )
    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void deleteCard(
            @Parameter(description = "UUID удаляемой карты", required = true)
            @PathVariable UUID cardId
    ) {
        cardService.deleteCard(cardId);
    }
}

/*
// УБРАТЬ
    @Operation(
            summary = "Обработка заявки на блокировку карты",
            description = "Меняет статус карты на BLOCKED и удаляет заявку на блокировку. Доступ есть только у админа.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Заявка обработана, карта заблокирована"),
                    @ApiResponse(responseCode = "400", description = "Некорректный ID карты"),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "401", description = "JWT отсутствует или невалидный")
            }
    )
    @PostMapping("/{cardId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void updateCardStatusRequest(
            @Parameter(description = "UUID карты для блокировки", required = true)
            @PathVariable UUID cardId
    ) {
        cardService.processBlockRequest(cardId);
    }

    // УБРАТЬ
    @Operation(
            summary = "Получить список заявок на блокировку карт",
            description = "Возвращает список заявок на блокировку с пагинацией. Доступ есть только у админа.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список заявок на блокировку",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardBlockingResponse.class)))
            }
    )
    @GetMapping
    @Override
    public List<> getBlockRequests(
        @Parameter(description = "Статус карты", example = "BLOCKED")
        @RequestParam(required = false) Card.Status status,

        @Parameter(description = "Параметр, решающий, где нужно проводить поиск: среди заявок ", example = "true")
        @RequestParam(required = false) boolean submitted,

        @Parameter(description = "Номер страницы пагинации", example = "0")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "Размер страницы пагинации", example = "5")
        @RequestParam(defaultValue = "5") int size
    ) {
        return cardService.getBlockRequests(page, size);
    }
 */