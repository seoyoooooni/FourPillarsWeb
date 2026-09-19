package com.fourpillars.backend.fortune.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fortune_view_history")
@IdClass(FortuneViewHistory.Key.class)
public class FortuneViewHistory {
    @Id @Column(name = "user_id") private UUID userId;
    @Id @Column(name = "fortune_date") private LocalDate fortuneDate;
    @Column(name = "viewed_at", nullable = false) private Instant viewedAt;

    protected FortuneViewHistory() {}
    public FortuneViewHistory(UUID userId, LocalDate fortuneDate) {
        this.userId = userId; this.fortuneDate = fortuneDate; this.viewedAt = Instant.now();
    }
    public UUID getUserId() { return userId; }
    public LocalDate getFortuneDate() { return fortuneDate; }
    public Instant getViewedAt() { return viewedAt; }
    public static class Key implements java.io.Serializable {
        public UUID userId; public LocalDate fortuneDate;
        public Key() {}
        public boolean equals(Object o) { return o instanceof Key k && java.util.Objects.equals(userId, k.userId) && java.util.Objects.equals(fortuneDate, k.fortuneDate); }
        public int hashCode() { return java.util.Objects.hash(userId, fortuneDate); }
    }
}
