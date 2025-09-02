package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на перевод денег между картами")
public record CardTransferMoney(
        @Schema(description = "Номер карты отправителя", example = "1234-5678-9012-3456", required = true)
        String from,

        @Schema(description = "Номер карты получателя", example = "6543-2109-8765-4321", required = true)
        String to,

        @Schema(description = "Сумма перевода", example = "150.00", required = true)
        double amount
) {}