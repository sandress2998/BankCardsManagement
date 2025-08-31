package com.example.bankcards.repository;

import com.example.bankcards.entity.CardHash;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CardHashRepository extends JpaRepository<CardHash, UUID> {
    boolean existsByHmacHash(String hmacHash);
}

