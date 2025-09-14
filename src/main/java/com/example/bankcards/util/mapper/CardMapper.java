package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.CardCreateResponse;
import com.example.bankcards.dto.CardInfoResponse;
import com.example.bankcards.entity.Card;

public class CardMapper {
    public static CardInfoResponse toGetResponse(Card card, String cardNumber) {
        if (card == null) return null;

        // Маскирование номера: **** **** **** 1234
        String masked = maskNumber(cardNumber);

        // Формат даты: MM/yy
        String formattedDate = card.getValidityPeriod() != null
                ? card.getValidityPeriod().format(java.time.format.DateTimeFormatter.ofPattern("MM/yy"))
                : null;

        // Логин владельца (предположим, что в User есть getUsername())
        String ownerLogin = card.getOwner() != null ? card.getOwner().getLogin() : null;

        return new CardInfoResponse(
                card.getId(),
                masked,
                formattedDate,
                card.getStatus(),
                card.getBalance(),
                ownerLogin
        );
    }

    public static CardCreateResponse toPostResponse(Card card, String cardNumber) {
        // Маскирование номера: **** **** **** 1234
        String masked = maskNumber(cardNumber);

        // Формат даты: MM/yy
        String formattedDate = card.getValidityPeriod() != null
                ? card.getValidityPeriod().format(java.time.format.DateTimeFormatter.ofPattern("MM/yy"))
                : null;

        // Логин владельца (предположим, что в User есть getUsername())
        String ownerLogin = card.getOwner() != null ? card.getOwner().getLogin() : null;

        return new CardCreateResponse(
                card.getId(),
                masked,
                formattedDate,
                card.getStatus(),
                card.getBalance(),
                ownerLogin
        );
    }

    private static String maskNumber(String encryptedNumber) {
        if (encryptedNumber == null || encryptedNumber.length() <= 4) {
            return encryptedNumber;
        }
        String last4 = encryptedNumber.substring(encryptedNumber.length() - 4);
        return "**** **** **** " + last4;
    }
}
