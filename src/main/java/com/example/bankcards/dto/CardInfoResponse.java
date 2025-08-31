package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;

public record CardInfoResponse(String maskedNumber, String date, Card.Status status, Long balance, String owner) {};