package com.example.bankcards.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity(name = "card_encryption_key")
public class CardEncryptionKey {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "card_id", nullable = false, unique = true)
    private Card card;

    @Column
    private String encryptedKey; // зашифрованный ключ

    public CardEncryptionKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public CardEncryptionKey() {}

    public UUID getId() {
        return id;
    }

    public Card getCard() {
        return card;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }
}
