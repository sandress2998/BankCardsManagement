package com.example.bankcards.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Entity(name = "card_encryption_key")
public class CardEncryptionKey {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    @JsonBackReference("encryption")
    @OneToOne(optional = false)
    @JoinColumn(name = "card_id", nullable = false, unique = true)
    private Card card;

    @Column
    private String encryptedKey; // зашифрованный ключ

    public CardEncryptionKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public CardEncryptionKey() {}
}
