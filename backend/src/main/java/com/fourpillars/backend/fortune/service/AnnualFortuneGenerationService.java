package com.fourpillars.backend.fortune.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourpillars.backend.fortune.domain.DailyFortuneCache;
import com.fourpillars.backend.fortune.repository.DailyFortuneCacheRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.util.ArrayList;

@Service
public class AnnualFortuneGenerationService {
    public static final String CALCULATION_VERSION = "daily-v3-solar-terms";
    private final FortuneCalculationService calculationService;
    private final DailyFortuneCacheRepository repository;
    private final ObjectMapper objectMapper;

    public AnnualFortuneGenerationService(FortuneCalculationService calculationService,
                                          DailyFortuneCacheRepository repository) {
        this.calculationService = calculationService; this.repository = repository; this.objectMapper = new ObjectMapper();
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refresh(ProfileFortuneRefreshRequested event) {
        int year = LocalDate.now(java.time.ZoneId.of("Asia/Seoul")).getYear();
        repository.lockYear(event.userId() + ":" + year);
        generate(event.userId(), event.profile(), year);
    }

    @Async
    @Transactional
    public void generateMissing(java.util.UUID userId, com.fourpillars.backend.profile.dto.BirthProfileResponse profile, int year) {
        var from = LocalDate.of(year, 1, 1); var to = LocalDate.of(year, 12, 31);
        repository.lockYear(userId + ":" + year);
        var existing = repository.findByUserIdAndFortuneDateBetweenOrderByFortuneDate(userId, from, to);
        boolean outdated = existing.stream().anyMatch(row -> !CALCULATION_VERSION.equals(row.getCalculationVersion()));
        if (existing.size() < to.getDayOfYear() || outdated) {
            generate(userId, profile, year);
        }
    }

    private void generate(java.util.UUID userId, com.fourpillars.backend.profile.dto.BirthProfileResponse profile, int year) {
        var calculated = calculationService.calculateYear(profile, year);
        int total = calculated.size();
        var rows = new ArrayList<DailyFortuneCache>(total);
        calculated.forEach((date, fortune) -> {
            int rank = (int) calculated.values().stream().filter(other -> other.overall() > fortune.overall()).count() + 1;
            int topPercent = Math.max(1, (int) Math.ceil(rank * 100d / total));
            try {
                rows.add(new DailyFortuneCache(userId, date, fortune.overall(), fortune.wealth(), fortune.love(),
                        fortune.health(), fortune.career(), fortune.relationships(), fortune.study(),
                        fortune.todayPillar(), fortune.todayTenGod(), fortune.energyDescription(),
                        objectMapper.writeValueAsString(fortune.interactions()), rank, total, topPercent, CALCULATION_VERSION));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("운세 상호작용 정보를 저장할 수 없습니다.", e);
            }
        });
        var from = LocalDate.of(year, 1, 1); var to = LocalDate.of(year, 12, 31);
        repository.deleteYear(userId, from, to);
        repository.flush();
        repository.saveAll(rows);
    }
}
