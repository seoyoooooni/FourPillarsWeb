package com.fourpillars.backend.fortune.dto;

import java.time.LocalDate;
import java.util.List;

public record TodayFortuneResponse(
        DailyFortune fortune,
        AnnualRank annualRank,
        List<TrendPoint> recentScores) {

    public record Interaction(String type, String natalPosition, String natalValue, String todayValue, int effect) {}
    public record DailyFortune(int overall, int wealth, int love, int health, int career, int relationships,
                               int study, String todayPillar, String todayTenGod, List<Interaction> interactions) {}
    public record AnnualRank(int rank, int totalDays, int topPercent) {}
    public record TrendPoint(LocalDate date, int score, boolean today) {}
}
