package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос для получения роли администратора")
public record AdminRequest(
    @Schema(description = "Секретный пароль для получения роли администратора", example = "mySecret123")
    String secret
) {}