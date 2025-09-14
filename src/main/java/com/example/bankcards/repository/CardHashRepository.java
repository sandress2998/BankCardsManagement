package com.example.bankcards.repository;

import com.example.bankcards.entity.CardHash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.UUID;

public interface CardHashRepository extends JpaRepository<CardHash, UUID> {
    boolean existsByHmacHash(String hmacHash);

    @Modifying
    void deleteByHmacHash(String hmacHash);
}

