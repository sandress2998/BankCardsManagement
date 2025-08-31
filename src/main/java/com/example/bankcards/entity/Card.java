package com.example.bankcards.entity;

import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Entity
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "encrypted_number", nullable = false, unique = true)
    private String encryptedNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User owner;

    @Column(name = "validity_period", nullable = false)
    private LocalDate validityPeriod;

    @Column(nullable = false, length = 20)
    private Status status;

    @Column(nullable = false)
    private double balance = 0;

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

    public enum Action {
        ACTIVATE, BLOCK
    }
}
