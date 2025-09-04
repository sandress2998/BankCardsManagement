package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос для получения роли администратора")
public record AdminRequest(
    @Schema(description = "Секретный пароль для получения роли администратора. В данный момент он равен d337d192-4421-46fc-b81a-cb3152f3f328", example = "d337d192-4421-46fc-b81a-cb3152f3f328")
    String secret
) {}