package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на изменение баланса карты")
public record CardBalanceRequest(
        @Schema(description = "Номер карты", example = "1234-5678-9012-3456", required = true)
        String cardNumber,

        @Schema(description = "Сумма для изменения баланса. Положительное число для пополнения, отрицательное для снятия", example = "100.50", required = true)
        double amount
) {}