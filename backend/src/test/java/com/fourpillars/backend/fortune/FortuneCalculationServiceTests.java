package com.fourpillars.backend.fortune;

import com.fourpillars.backend.fortune.dto.FortuneCalculationRequest;
import com.fourpillars.backend.fortune.service.FortuneCalculationService;
import com.fourpillars.backend.profile.domain.CalendarType;
import com.fourpillars.backend.profile.domain.GenderBasis;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class FortuneCalculationServiceTests {
    private final FortuneCalculationService service;

    FortuneCalculationServiceTests() throws IOException {
        service = new FortuneCalculationService();
    }

    @Test
    void ohBranchHiddenStemsIncludeJeongAsTheMainQi() {
        var request = new FortuneCalculationRequest(LocalDate.parse("1926-01-05"), LocalTime.NOON, CalendarType.SOLAR, false, null);
        var result = service.calculate(request);

        var dayBranchHiddenStems = result.analysis().hiddenStems().get("day_ji");
        assertThat(dayBranchHiddenStems).hasSize(3);
        assertThat(dayBranchHiddenStems.get(0).stem()).isEqualTo("병");
        assertThat(dayBranchHiddenStems.get(0).position()).isEqualTo("여기");
        assertThat(dayBranchHiddenStems.get(1).stem()).isEqualTo("기");
        assertThat(dayBranchHiddenStems.get(1).position()).isEqualTo("중기");
        assertThat(dayBranchHiddenStems.get(2).stem()).isEqualTo("정");
        assertThat(dayBranchHiddenStems.get(2).position()).isEqualTo("정기");
        assertThat(dayBranchHiddenStems.get(2).tenGod()).isEqualTo("상관");
    }

    @Test
    void cheondeokIsFlaggedOnlyWhenActuallyPresentInTheChart() {
        var request = new FortuneCalculationRequest(LocalDate.parse("1926-02-13"), LocalTime.of(9, 30), CalendarType.SOLAR, false, null);
        var result = service.calculate(request);

        assertThat(result.stageThreeAnalysis().cheondeokValue()).isEqualTo("정");
        assertThat(result.pillars().get("hour")).isEqualTo("정사");
        assertThat(result.stageThreeAnalysis().gilsinByPillar().get("hour")).contains("천덕귀인");
    }

    @Test
    void daeunSequenceFollowsSixtyGanziForwardForYangYearMale() {
        var request = new FortuneCalculationRequest(LocalDate.parse("1926-02-13"), LocalTime.NOON, CalendarType.SOLAR, false, GenderBasis.MALE);
        var result = service.calculate(request);
        var daeun = result.daeunAnalysis();

        assertThat(daeun).isNotNull();
        assertThat(daeun.forward()).isTrue();
        assertThat(daeun.startAge()).isEqualTo(10);
        assertThat(daeun.periods()).hasSize(8);
        assertThat(daeun.periods().get(0).order()).isEqualTo(1);
        assertThat(daeun.periods().get(0).startAge()).isEqualTo(10);
        assertThat(daeun.periods().get(0).stem()).isEqualTo("신");
        assertThat(daeun.periods().get(0).branch()).isEqualTo("묘");
        assertThat(daeun.periods().get(1).startAge()).isEqualTo(20);
        assertThat(daeun.periods().get(1).stem()).isEqualTo("임");
        assertThat(daeun.periods().get(1).branch()).isEqualTo("진");
    }

    @Test
    void daeunIsNullWhenGenderIsNotProvided() {
        var request = new FortuneCalculationRequest(LocalDate.parse("1926-02-13"), LocalTime.NOON, CalendarType.SOLAR, false);
        var result = service.calculate(request);

        assertThat(result.daeunAnalysis()).isNull();
    }

    @Test
    void yongsinPrefersBigyeopOverInseongWhenBigyeopIsScarcerInAWeakChart() {
        var request = new FortuneCalculationRequest(LocalDate.parse("1926-01-01"), LocalTime.of(0, 0), CalendarType.SOLAR, false, null);
        var result = service.calculate(request);
        var yongsin = result.yongsinAnalysis();

        assertThat(result.analysis().dayMaster()).isEqualTo("경");
        assertThat(yongsin.strength()).isEqualTo("신약");
        assertThat(yongsin.strengthScore()).isEqualTo(-4);
        assertThat(yongsin.categoryCounts()).containsEntry("비겁", 0).containsEntry("인성", 2);
        assertThat(yongsin.weakPriority()).isEqualTo("비겁");
        assertThat(yongsin.strongPriority()).isEqualTo("관성");
        assertThat(yongsin.yongsinElement()).isEqualTo("금");
    }

    @Test
    void yongsinPrefersSiksangOverGwanseongWhenSiksangIsScarcerInAStrongChart() {
        var request = new FortuneCalculationRequest(LocalDate.parse("1926-01-09"), LocalTime.of(8, 0), CalendarType.SOLAR, false, null);
        var result = service.calculate(request);
        var yongsin = result.yongsinAnalysis();

        assertThat(result.analysis().dayMaster()).isEqualTo("무");
        assertThat(yongsin.strength()).isEqualTo("신강");
        assertThat(yongsin.strengthScore()).isEqualTo(2);
        assertThat(yongsin.categoryCounts()).containsEntry("식상", 0).containsEntry("관성", 1);
        assertThat(yongsin.weakPriority()).isEqualTo("인성");
        assertThat(yongsin.strongPriority()).isEqualTo("식상");
        assertThat(yongsin.yongsinElement()).isEqualTo("금");
    }
}
