package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с текущим балансом карты")
public record CardBalanceResponse(
        @Schema(description = "Текущий баланс карты", example = "2500.75")
        double balance
) {}