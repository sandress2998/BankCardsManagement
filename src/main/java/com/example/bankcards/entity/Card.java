package com.example.bankcards.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
@Entity
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "encrypted_number", nullable = false, unique = true)
    private String encryptedNumber;

    @JsonManagedReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User owner;

    @Column(name = "validity_period", nullable = false)
    private LocalDate validityPeriod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(nullable = false)
    private double balance = 0;

    @JsonManagedReference("encryption")
    @OneToOne(mappedBy = "card", optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
    private CardEncryptionKey encryptionKey;

    public Card() {}

    public Card(User owner, String encryptedNumber, LocalDate validityPeriod) {
        this.owner = owner;
        this.encryptedNumber = encryptedNumber;
        this.validityPeriod = validityPeriod;
        this.status = Status.ACTIVE;
    }

    public enum Status {
        ACTIVE, BLOCKED, EXPIRED
    }

    public enum CardAction {
        ACTIVATE, BLOCK
    }

    public enum BalanceAction {
        DEPOSIT_MONEY, WITHDRAW_MONEY
    }
}
