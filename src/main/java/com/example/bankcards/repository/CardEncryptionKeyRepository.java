package com.example.bankcards.repository;

import com.example.bankcards.entity.CardEncryptionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CardEncryptionKeyRepository extends JpaRepository<CardEncryptionKey, UUID> {}
