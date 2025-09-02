package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос, содержащий номер карты")
public record CardNumberBody(
        @Schema(description = "Номер карты", example = "1234567890123456")
        String number
) {}