package com.fourpillars.backend.fortune.repository;

import com.fourpillars.backend.fortune.domain.DailyFortuneCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.*;

public interface DailyFortuneCacheRepository extends JpaRepository<DailyFortuneCache, UUID> {
    Optional<DailyFortuneCache> findByUserIdAndFortuneDate(UUID userId, LocalDate fortuneDate);
    List<DailyFortuneCache> findByUserIdAndFortuneDateBetweenOrderByFortuneDate(UUID userId, LocalDate from, LocalDate to);
    @Modifying
    @Query("delete from DailyFortuneCache d where d.userId = :userId and d.fortuneDate between :from and :to")
    void deleteYear(UUID userId, LocalDate from, LocalDate to);
    @Query(value = "select pg_advisory_xact_lock(hashtext(:lockKey))", nativeQuery = true)
    void lockYear(String lockKey);
}
