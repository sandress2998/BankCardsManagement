package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Ответ на запрос списка заявок на блокировку карты")
public record CardBlockingResponse(
        @Schema(description = "UUID карты, по которой отправлена заявка на блокировку", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID cardId
) {}