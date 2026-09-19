package com.fourpillars.backend.fortune.repository;

import com.fourpillars.backend.fortune.domain.FortuneViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FortuneViewHistoryRepository extends JpaRepository<FortuneViewHistory, FortuneViewHistory.Key> {
    List<FortuneViewHistory> findByUserIdAndFortuneDateBetweenOrderByFortuneDate(UUID userId, LocalDate from, LocalDate to);
}
