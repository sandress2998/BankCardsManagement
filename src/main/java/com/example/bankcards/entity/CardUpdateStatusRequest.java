package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity(name = "card_update_status_request")
public class CardUpdateStatusRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private Card.Status status;

    @OneToOne(optional = false)
    @JoinColumn(name = "card_id", nullable = false, unique = true)
    private Card card;

    public CardUpdateStatusRequest(Card card, Card.Status status) {
        this.card = card;
        this.status = status;
    }
}
