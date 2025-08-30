package com.example.bankcards.entity;

import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Entity
public class Card {
    @Value("${card.months-until-expires}")
    private static int monthsQuantityUntilExpires;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String number;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User owner;

    @Column(name = "validity_period", nullable = false)
    private LocalDate validityPeriod;

    @Column(nullable = false, length = 20)
    private Status status;

    @Column(nullable = false)
    private long balance = 0L;

    public Card() {}

    public Card(
        User owner
    ) {
        this.owner = owner;
        this.number = getRandomNumber() + getRandomNumber() + getRandomNumber() + getRandomNumber();
        this.validityPeriod = setValidityPeriod(monthsQuantityUntilExpires);
        this.status = Status.ACTIVE;
    }

    enum Status {
        ACTIVE, BLOCKED, EXPIRED
    }

    // ПЕРЕНЕСТИ В SERVICE
    public static LocalDate setValidityPeriod(int monthsQuantity) {
        LocalDate today = LocalDate.now();
        LocalDate targetMonth = today.plusMonths(monthsQuantity);

        return targetMonth.with(TemporalAdjusters.lastDayOfMonth());
    }

    // ПЕРЕНЕСТИ В SERVICE
    public boolean isBalanceUpdateCorrect(long sum) {
        long newBalance;
        if (balance > Long.MAX_VALUE - sum) {
            throw new IllegalArgumentException("Balance is too high");
        }
        newBalance = balance + sum;
        if (newBalance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        return true;
    }

    /** Возвращает случайное число от 0 до 9999 */
    private String getRandomNumber() {
        int randomNumber = ThreadLocalRandom.current().nextInt(0, 10000);
        return String.format("%04d", randomNumber);
    }
}
