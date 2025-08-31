package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    @Modifying
    @Query("UPDATE Card c SET c.status = :newStatus WHERE c.id = :id")
    void updateCardStatus(UUID id, Card.Status newStatus);

    List<Card> findByOwnerId(UUID ownerId);

    @Modifying
    @Query("UPDATE Card c SET c.balance = :newBalance WHERE c.id = :id")
    void updateCardBalance(UUID id, double newBalance);
}
