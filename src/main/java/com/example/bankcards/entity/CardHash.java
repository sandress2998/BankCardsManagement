package com.example.bankcards.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "card_hash")
public class CardHash {
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

    // Геттеры и сеттеры
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getHmacHash() {
        return hmacHash;
    }

    public void setHmacHash(String hmacHash) {
        this.hmacHash = hmacHash;
    }
}

