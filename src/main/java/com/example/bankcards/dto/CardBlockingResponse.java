package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Ответ на запрос списка заявок на блокировку карты")
public record CardBlockingResponse(
        @Schema(description = "UUID карты", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "Номер карты", example = "**** **** **** 3456")
        String number,

        @Schema(description = "Логин владельца карты", example = "user123")
        String owner,

        @Schema(description = "Дата окончания срока действия карты", example = "2026-12-31")
        LocalDate validityPeriod,

        @Schema(description = "Статус карты", example = "ACTIVE")
        Card.Status status,

        @Schema(description = "Баланс карты", example = "2500.75")
        double balance
) {}