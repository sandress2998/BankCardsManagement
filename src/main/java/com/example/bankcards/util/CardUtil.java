package com.example.bankcards.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CardUtil {
    public static String maskCardNumber(String plainNumber) {
        return "**** **** **** " + plainNumber.substring(plainNumber.length() - 4);
    }

    private static final DateTimeFormatter CARD_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");

    public static String convertValidationDate(LocalDate validationDate) {
        // Преобразование срока действия карты в LocalDate (предполагается, что getExpiryDate() возвращает LocalDate)
        return CARD_DATE_FORMATTER.format(validationDate);
    }
}
