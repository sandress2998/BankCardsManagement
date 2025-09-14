package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class AdminCardFilter extends CardFilter {
    @Parameter(
            description = "Нужно ли показывать только карты, на которые были поданы заявки об изменении статуса",
            example = "BLOCKED",
            in = ParameterIn.QUERY
    )
    Card.Status statusUpdateRequest;

    @Parameter(
            description = "Id пользователя, информацию о картах которого нужно получить",
            example = "bbd82638-05a6-4d98-b2da-4a3eaa7e8757",
            in = ParameterIn.QUERY
    )
    UUID ownerId;
}
