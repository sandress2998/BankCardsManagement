package com.example.bankcards.dto;

import com.example.bankcards.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос для получения роли администратора")
public record RoleRequest(
    @Schema(description = "Роль, получение которой запрашивает пользователь", example = "ADMIN")
    User.Role role,

    @Schema(description = "Секретный пароль для получения роли. В данный момент для админа он равен d337d192-4421-46fc-b81a-cb3152f3f328", example = "d337d192-4421-46fc-b81a-cb3152f3f328")
    String secret
) {}