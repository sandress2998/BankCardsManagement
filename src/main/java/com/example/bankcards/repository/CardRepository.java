package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID>, JpaSpecificationExecutor<Card> {

    Card findCardById(UUID id);

    boolean existsCardById(UUID id);

    List<Card> findByValidityPeriodBeforeAndStatus(LocalDate now, Card.Status status);

    @Modifying
    @Query("UPDATE Card c SET c.status = :newStatus WHERE c.id = :id")
    void updateCardStatus(UUID id, Card.Status newStatus);

    List<Card> findByOwnerId(UUID ownerId, Pageable pageable);

    List<Card> findByOwnerId(UUID ownerId);

    @Modifying
    @Query("UPDATE Card c SET c.balance = :newBalance WHERE c.id = :id")
    void updateCardBalance(UUID id, double newBalance);

    @Modifying
    @Query(value = """
    UPDATE card
    SET balance = CASE
        WHEN id = :fromId THEN balance - :amount
        WHEN id = :toId THEN balance + :amount
        ELSE balance
    END
    WHERE id IN (:fromId, :toId)
    """, nativeQuery = true)
    void transferMoney(@Param("fromId") UUID fromId, @Param("toId") UUID toId, @Param("amount") double amount);


    List<Card> findByOwnerIdAndStatus(UUID ownerId, Card.Status status, Pageable pageable);
}
