package com.example.bankcards.repository;

import com.example.bankcards.entity.CardUpdateStatusRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CardUpdateStatusRequestRepository extends JpaRepository<CardUpdateStatusRequest, UUID> {
    @NotNull
    @Override
    Page<CardUpdateStatusRequest> findAll(@NotNull Pageable pageable);

    @Modifying
    void deleteByCardId(@NotNull UUID cardId);
}
