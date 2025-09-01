package com.example.bankcards.repository;

import com.example.bankcards.entity.CardBlockingRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CardBlockingRequestRepository extends JpaRepository<CardBlockingRequest, UUID> {
    @NotNull
    @Override
    Page<CardBlockingRequest> findAll(Pageable pageable);

    void deleteByCardId(@NotNull UUID cardId);
}
