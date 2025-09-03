package com.example.bankcards.service.impl;

import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardScheduler;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CardSchedulerImpl implements CardScheduler {
    @Autowired
    private CardRepository cardRepository;

    // раз в сутки
    @Scheduled(fixedRate = 60000 * 60 * 24)
    @Transactional
    @Override
    public void updateStatusToExpired() {
        List<Card> cards = cardRepository.findByValidityPeriodBeforeAndStatus(LocalDate.now(), Card.Status.ACTIVE);
        for (Card card : cards) {
            cardRepository.updateCardStatus(card.getId(), Card.Status.EXPIRED);
        }
    }
}
