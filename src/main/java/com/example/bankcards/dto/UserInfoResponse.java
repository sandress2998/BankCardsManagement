package com.example.bankcards.dto;

import com.example.bankcards.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Информация о пользователе")
public record UserInfoResponse(
    @Schema(description = "Уникальный идентификатор пользователя", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID id,

    @Schema(description = "Логин пользователя", example = "user123")
    String login,

    @Schema(description = "Роль пользователя", example = "USER")
    User.Role role
) {}