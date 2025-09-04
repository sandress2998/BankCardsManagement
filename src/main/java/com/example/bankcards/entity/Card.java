package com.example.bankcards.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

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
    @OneToOne(mappedBy = "card", optional = false, cascade = CascadeType.ALL)
    private CardEncryptionKey encryptionKey;

    public Card() {}

    public Card(User owner, String encryptedNumber, LocalDate validityPeriod) {
        this.owner = owner;
        this.encryptedNumber = encryptedNumber;
        this.validityPeriod = validityPeriod;
        this.status = Status.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public String getEncryptedNumber() {
        return encryptedNumber;
    }

    public User getOwner() {
        return owner;
    }

    public LocalDate getValidityPeriod() {
        return validityPeriod;
    }

    public Status getStatus() {
        return status;
    }

    public double getBalance() {
        return balance;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setValidityPeriod(LocalDate validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void setEncryptionKey(CardEncryptionKey encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public void setEncryptedNumber(String encryptedNumber) {
        this.encryptedNumber = encryptedNumber;
    }

    public CardEncryptionKey getEncryptionKey() {
        return encryptionKey;
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
