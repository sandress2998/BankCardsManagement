package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Информация о созданной карте")
public record CardCreateResponse (
    @Schema(description = "UUID карты", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "Маскированный номер карты (для безопасности)", example = "**** **** **** 1234")
    String number,

    @Schema(description = "Дата окончания срока действия карты в формате ММ/ГГ", example = "12/26")
    String date,

    @Schema(description = "Статус карты", example = "ACTIVE")
    Card.Status status,

    @Schema(description = "Баланс карты", example = "2500.75")
    double balance,

    @Schema(description = "Логин владельца карты", example = "user123")
    String owner
) {}
