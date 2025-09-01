package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;

import java.time.LocalDate;
import java.util.UUID;

public record CardFullInfoResponse (
    UUID id,
    String number,
    String owner,
    LocalDate validityPeriod,
    Card.Status status,
    double balance
) {}
