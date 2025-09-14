package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class CardFilter {
    @Parameter(description = "Номер страницы пагинации", example = "BLOCKED", in = ParameterIn.QUERY)
    Card.Status status;

    @Parameter(description = "Параметр, по которому следует провести сортировку", in = ParameterIn.QUERY)
    @Pattern(regexp = "^(id|balance|status|validityPeriod)$", message = "Недопустимое поле для сортировки")
    String sortBy;

    @Parameter(description = "Порядок сортировки", example = "ASC", in = ParameterIn.QUERY)
    @Pattern(regexp = "^(ASC|DESC)$", message = "Направление может быть только ASC или DESC")
    String sortDirection;

    @Parameter(description = "Номер страницы пагинации", example = "0", in = ParameterIn.QUERY)
    @Min(value = 0, message = "Номер страницы не может быть отрицательным")
    int page = 0;

    @Parameter(description = "Размер страницы пагинации", example = "5", in = ParameterIn.QUERY)
    @Min(value = 1, message = "Размер страницы должен быть больше 0")
    @Max(value = 100, message = "Размер страницы не может превышать 100")
    int size = 5;

    @Parameter(description = "Поля, которые должны быть в ответе", example = "id,balance,status", in = ParameterIn.QUERY)
    String[] fields;
}
