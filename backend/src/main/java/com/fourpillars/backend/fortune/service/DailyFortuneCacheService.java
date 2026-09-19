package com.fourpillars.backend.fortune.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourpillars.backend.fortune.domain.DailyFortuneCache;
import com.fourpillars.backend.fortune.domain.FortuneViewHistory;
import com.fourpillars.backend.fortune.dto.TodayFortuneResponse;
import com.fourpillars.backend.fortune.dto.FortuneCalendarResponse;
import com.fourpillars.backend.fortune.repository.DailyFortuneCacheRepository;
import com.fourpillars.backend.fortune.repository.FortuneViewHistoryRepository;
import com.fourpillars.backend.profile.dto.BirthProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DailyFortuneCacheService {
    private final DailyFortuneCacheRepository repository;
    private final FortuneViewHistoryRepository viewRepository;
    private final FortuneCalculationService calculationService;
    private final AnnualFortuneGenerationService generationService;
    private final ObjectMapper objectMapper;

    public DailyFortuneCacheService(DailyFortuneCacheRepository repository, FortuneViewHistoryRepository viewRepository,
                                    FortuneCalculationService calculationService,
                                    AnnualFortuneGenerationService generationService) {
        this.repository = repository; this.viewRepository = viewRepository; this.calculationService = calculationService;
        this.generationService = generationService; this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public TodayFortuneResponse today(UUID userId, BirthProfileResponse profile) {
        var today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        viewRepository.save(new FortuneViewHistory(userId, today));
        var cached = repository.findByUserIdAndFortuneDate(userId, today);
        if (cached.isEmpty()) {
            var response = calculationService.forDate(profile, today);
            saveOne(userId, today, response);
            generationService.generateMissing(userId, profile, today.getYear());
            return response;
        }
        generationService.generateMissing(userId, profile, today.getYear());
        return responseFromCache(userId, cached.get(), today);
    }

    @Transactional(readOnly = true)
    public FortuneCalendarResponse calendar(UUID userId, int year, int month) {
        var from = LocalDate.of(year, month, 1);
        var to = from.withDayOfMonth(from.lengthOfMonth());
        var checked = viewRepository.findByUserIdAndFortuneDateBetweenOrderByFortuneDate(userId, from, to)
                .stream().map(view -> repository.findByUserIdAndFortuneDate(userId, view.getFortuneDate())
                        .map(row -> new FortuneCalendarResponse.Day(row.getFortuneDate(), row.getOverall(),
                                row.getAnnualRank(), row.getWealth(), row.getLove(), row.getHealth(),
                                row.getCareer(), row.getRelationships(), row.getStudy())))
                .flatMap(java.util.Optional::stream).toList();
        return new FortuneCalendarResponse(year, month, checked);
    }

    private void saveOne(UUID userId, LocalDate date, TodayFortuneResponse response) {
        var f = response.fortune(); var rank = response.annualRank();
        try {
            repository.save(new DailyFortuneCache(userId, date, f.overall(), f.wealth(), f.love(), f.health(),
                    f.career(), f.relationships(), f.study(), f.todayPillar(), f.todayTenGod(),
                    f.energyDescription(), objectMapper.writeValueAsString(f.interactions()), rank.rank(),
                    rank.totalDays(), rank.topPercent(), AnnualFortuneGenerationService.CALCULATION_VERSION));
        } catch (Exception ignored) {
            // 동시에 완료된 연간 생성 작업의 동일 날짜 행을 그대로 사용함.
        }
    }

    private TodayFortuneResponse responseFromCache(UUID userId, DailyFortuneCache row, LocalDate today) {
        try {
            List<TodayFortuneResponse.Interaction> interactions = objectMapper.readValue(row.getInteractionsJson(), new TypeReference<>() {});
            var fortune = new TodayFortuneResponse.DailyFortune(row.getOverall(), row.getWealth(), row.getLove(),
                    row.getHealth(), row.getCareer(), row.getRelationships(), row.getStudy(), row.getTodayPillar(),
                    row.getTodayTenGod(), row.getEnergyDescription(), interactions);
            var rank = new TodayFortuneResponse.AnnualRank(row.getAnnualRank(), row.getAnnualTotalDays(), row.getAnnualTopPercent());
            var recentRows = repository.findByUserIdAndFortuneDateBetweenOrderByFortuneDate(userId, today.minusDays(6), today);
            var trend = new ArrayList<TodayFortuneResponse.TrendPoint>();
            recentRows.forEach(item -> trend.add(new TodayFortuneResponse.TrendPoint(item.getFortuneDate(), item.getOverall(), item.getFortuneDate().equals(today))));
            return new TodayFortuneResponse(fortune, rank, trend);
        } catch (Exception e) {
            throw new IllegalStateException("저장된 운세 정보를 읽을 수 없습니다.", e);
        }
    }
}
