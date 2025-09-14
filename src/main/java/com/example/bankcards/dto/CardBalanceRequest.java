package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на изменение баланса карты")
public record CardBalanceRequest(
        @Schema(description = "Действие, которое нужно выполнить с балансом", example = "WITHDRAW_MONEY", required = true)
        Card.BalanceAction action,

        @Schema(description = "Сумма для изменения баланса. Положительное число для пополнения, отрицательное для снятия", example = "100.50", required = true)
        double amount
) {}