package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос аутентификации или регистрации")
public record AuthRequest(
    @Schema(description = "Логин пользователя", example = "user123")
    String login,

    @Schema(description = "Пароль пользователя", example = "Password123")
    String password
) {}