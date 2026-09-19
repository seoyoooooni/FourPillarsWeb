package com.fourpillars.backend.fortune.dto;

import com.fourpillars.backend.profile.domain.CalendarType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

public record MajorRecommendationRequest(
        @NotNull LocalDate birthDate,
        @NotNull LocalTime birthTime,
        @NotNull CalendarType calendarType,
        boolean leapMonth,
        Map<String, Integer> interests,
        Map<String, Integer> environments) {

    @AssertTrue(message = "양력 날짜에는 윤달을 선택할 수 없습니다.")
    public boolean isCalendarCombinationValid() {
        return calendarType == null || calendarType == CalendarType.LUNAR || !leapMonth;
    }

    public FortuneCalculationRequest fortuneRequest() {
        return new FortuneCalculationRequest(birthDate, birthTime, calendarType, leapMonth);
    }
}
