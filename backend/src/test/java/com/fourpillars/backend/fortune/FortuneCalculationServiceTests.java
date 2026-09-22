package com.fourpillars.backend.fortune;

import com.fourpillars.backend.fortune.dto.FortuneCalculationRequest;
import com.fourpillars.backend.fortune.service.FortuneCalculationService;
import com.fourpillars.backend.profile.domain.CalendarType;
import com.fourpillars.backend.profile.domain.GenderBasis;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

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
        assertThat(daeun.startAge()).isEqualTo(7);
        assertThat(daeun.periods()).hasSize(8);
        assertThat(daeun.periods().get(0).order()).isEqualTo(1);
        assertThat(daeun.periods().get(0).startAge()).isEqualTo(7);
        assertThat(daeun.periods().get(0).stem()).isEqualTo("신");
        assertThat(daeun.periods().get(0).branch()).isEqualTo("묘");
        assertThat(daeun.periods().get(1).startAge()).isEqualTo(17);
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
        assertThat(yongsin.strengthScore()).isEqualTo(-14);
        assertThat(yongsin.categoryCounts()).isEqualTo(Map.of(
                "비겁", 2, "식상", 13, "재성", 5, "관성", 4, "인성", 6));
        assertThat(yongsin.yongsinReason()).contains("간이 균형 분석");
        assertThat(yongsin.weakPriority()).isEqualTo("비겁");
        assertThat(yongsin.strongPriority()).isEqualTo("관성");
        assertThat(yongsin.yongsinElement()).isEqualTo("금");
    }

    @Test
    void yongsinUsesWeightedHiddenStemsInAStrongChart() {
        var request = new FortuneCalculationRequest(LocalDate.parse("1926-01-09"), LocalTime.of(8, 0), CalendarType.SOLAR, false, null);
        var result = service.calculate(request);
        var yongsin = result.yongsinAnalysis();

        assertThat(result.analysis().dayMaster()).isEqualTo("무");
        assertThat(yongsin.strength()).isEqualTo("신강");
        assertThat(yongsin.strengthScore()).isEqualTo(6);
        assertThat(yongsin.categoryCounts()).isEqualTo(Map.of(
                "비겁", 17, "식상", 7, "재성", 5, "관성", 3, "인성", 4));
        assertThat(yongsin.yongsinReason()).contains("간이 균형 분석");
        assertThat(yongsin.weakPriority()).isEqualTo("인성");
        assertThat(yongsin.strongPriority()).isEqualTo("관성");
        assertThat(yongsin.yongsinElement()).isEqualTo("목");
    }

    @Test
    void daeunUsesTheConvertedSolarDateForLunarBirthInputs() {
        var request = new FortuneCalculationRequest(LocalDate.parse("2023-02-28"), LocalTime.NOON, CalendarType.LUNAR, true, GenderBasis.FEMALE);
        var result = service.calculate(request);
        var daeun = result.daeunAnalysis();

        assertThat(result.solarDate()).isEqualTo("2023-04-18");
        assertThat(daeun).isNotNull();
        assertThat(daeun.forward()).isTrue();
        assertThat(daeun.startAge()).isEqualTo(6);
        assertThat(daeun.periods().get(0).stem()).isEqualTo("정");
        assertThat(daeun.periods().get(0).branch()).isEqualTo("사");
        assertThat(daeun.periods().get(1).startAge()).isEqualTo(16);
        assertThat(daeun.periods().get(1).stem()).isEqualTo("무");
        assertThat(daeun.periods().get(1).branch()).isEqualTo("오");
    }

    @Test
    void yearAndMonthPillarsChangeAtLichunInsteadOfLunarNewYear() {
        var before = service.calculate(new FortuneCalculationRequest(LocalDate.parse("2023-02-04"), LocalTime.of(10, 0), CalendarType.SOLAR, false));
        var after = service.calculate(new FortuneCalculationRequest(LocalDate.parse("2023-02-04"), LocalTime.of(13, 0), CalendarType.SOLAR, false));

        assertThat(before.pillars()).containsEntry("year", "임인").containsEntry("month", "계축");
        assertThat(after.pillars()).containsEntry("year", "계묘").containsEntry("month", "갑인");
    }

    @Test
    void lateRatHourUsesTheNextDaysDayPillar() {
        var before = service.calculate(new FortuneCalculationRequest(LocalDate.parse("1926-02-13"), LocalTime.of(22, 59), CalendarType.SOLAR, false));
        var after = service.calculate(new FortuneCalculationRequest(LocalDate.parse("1926-02-13"), LocalTime.of(23, 0), CalendarType.SOLAR, false));

        assertThat(before.pillars().get("day")).isEqualTo("계유");
        assertThat(after.pillars().get("day")).isEqualTo("갑술");
        assertThat(after.pillars().get("hour")).isEqualTo("갑자");
    }
}
