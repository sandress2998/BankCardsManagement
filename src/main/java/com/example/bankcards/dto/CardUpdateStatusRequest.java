package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import io.swagger.v3.oas.annotations.media.Schema;

public record CardUpdateStatusRequest(
        @Schema(description = "Новый статус карты", example = "BLOCKED")
        Card.Status status,

        @Schema(description = "Является ли смена статуса заранее запрашиваема пользователем или нет", example = "false")
        boolean isRequested
) {}
