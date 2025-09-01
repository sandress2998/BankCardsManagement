package com.example.bankcards.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity(name = "card_blocking_request")
public class CardBlockingRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "card_id", nullable = false, unique = true)
    private Card card;

    public CardBlockingRequest(Card card) {
        this.card = card;
    }

    public CardBlockingRequest() {}

    public UUID getId() {
        return id;
    }

    public Card getCard() {
        return card;
    }
}
