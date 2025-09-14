package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
@Schema(description = "Запрос на создание карты")
public class CardCreateRequest {
    public CardCreateRequest(@NotNull UUID ownerId, Integer monthsQuantityUntilExpires) {
        this.ownerId = ownerId;
        this.monthsQuantityUntilExpires = monthsQuantityUntilExpires == null ? 24 : monthsQuantityUntilExpires;
    }

    @Schema(description = "UUID владельца карты")
    private final UUID ownerId;

    @Schema(description = "Количество месяцев до окончания срока действия карты. Доступ есть только у админа.", required = false)
    private final Integer monthsQuantityUntilExpires;

}
