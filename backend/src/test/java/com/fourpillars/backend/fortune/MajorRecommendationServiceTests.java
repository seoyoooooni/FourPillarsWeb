package com.fourpillars.backend.fortune;

import com.fourpillars.backend.fortune.dto.FortuneCalculationResponse;
import com.fourpillars.backend.fortune.service.MajorRecommendationService;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MajorRecommendationServiceTests {
    private final MajorRecommendationService service = new MajorRecommendationService();

    @Test
    void returnsThreeFacultiesAndNeverReturnsAdvancedMajorCourse() {
        var result = service.recommend(fortune(
                Map.of("목", 1, "화", 1, "토", 1, "금", 3, "수", 2),
                Map.of("비겁", 1, "식상", 1, "재성", 1, "관성", 3, "인성", 4)));

        assertThat(result.recommendations()).hasSize(3);
        assertThat(result.recommendations())
                .extracting(recommendation -> recommendation.faculty())
                .doesNotContain("전공심화과정");
        assertThat(result.traits()).containsOnlyKeys("분석", "창의", "소통", "실행", "탐구");
        assertThat(result.recommendations()).allSatisfy(recommendation -> {
            assertThat(recommendation.score()).isBetween(0, 100);
            assertThat(recommendation.reasons()).hasSize(2);
        });
    }

    @Test
    void producesStableRankingForSameFortune() {
        var fortune = fortune(
                Map.of("목", 2, "화", 2, "토", 2, "금", 1, "수", 1),
                Map.of("비겁", 2, "식상", 3, "재성", 1, "관성", 1, "인성", 1));

        assertThat(service.recommend(fortune)).isEqualTo(service.recommend(fortune));
    }

    private static FortuneCalculationResponse fortune(Map<String, Integer> elements, Map<String, Integer> categories) {
        var orderedElements = new LinkedHashMap<String, Integer>();
        List.of("목", "화", "토", "금", "수").forEach(key -> orderedElements.put(key, elements.getOrDefault(key, 0)));
        var orderedCategories = new LinkedHashMap<String, Integer>();
        List.of("비겁", "식상", "재성", "관성", "인성").forEach(key -> orderedCategories.put(key, categories.getOrDefault(key, 0)));
        return new FortuneCalculationResponse(
                "2000-01-01",
                new FortuneCalculationResponse.LunarDate(1999, 11, 25, false),
                Map.of("year", "기묘", "month", "병자", "day", "무오", "hour", "무오"),
                new FortuneCalculationResponse.BasicAnalysis("무", Map.of(), Map.of(), orderedElements),
                new FortuneCalculationResponse.StageTwoAnalysis(Map.of(), List.of(), Map.of(), Map.of()),
                new FortuneCalculationResponse.StageThreeAnalysis("", Map.of(), Map.of(), List.of()),
                new FortuneCalculationResponse.YongsinAnalysis(0, "신약", orderedCategories, "화", "", "인성", "관성"));
    }
}
