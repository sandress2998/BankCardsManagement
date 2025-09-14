package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Запрос на перевод денег между картами")
public record CardTransferMoney(
        @Schema(description = "Id карты отправителя", example = "c4994fd6-ed16-49f7-8d2e-1c2ae0562b8a", required = true)
        UUID from,

        @Schema(description = "Id карты получателя", example = "c4994fd6-ed16-49f7-8d2e-1c2ae0562b8a", required = true)
        UUID to,

        @Schema(description = "Сумма перевода", example = "150.00", required = true)
        double amount
) {}