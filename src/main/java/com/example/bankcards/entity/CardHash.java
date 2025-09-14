package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "card_hash")
public class CardHash {
    // Геттеры и сеттеры
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hmac_hash", unique = true, nullable = false)
    private String hmacHash;

    // Конструкторы
    public CardHash() {}

    public CardHash(String hmacHash) {
        this.hmacHash = hmacHash;
    }
}

