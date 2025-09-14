package com.example.bankcards.util;

public enum SearchOperation {
    EQUAL,
    NOT_EQUAL,
    LIKE,
    LIKE_START,
    LIKE_END,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    IN,
    NOT_IN,
    IS_NULL,
    IS_NOT_NULL,
    BETWEEN // можно добавить для диапазонов
}

