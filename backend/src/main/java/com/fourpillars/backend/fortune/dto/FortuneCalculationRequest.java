package com.fourpillars.backend.fortune.dto;

import com.fourpillars.backend.profile.domain.CalendarType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

public record FortuneCalculationRequest(
        @NotNull LocalDate birthDate,
        @NotNull LocalTime birthTime,
        @NotNull CalendarType calendarType,
        boolean leapMonth) {

    @AssertTrue(message = "양력 날짜에는 윤달을 선택할 수 없습니다.")
    public boolean isCalendarCombinationValid() {
        return calendarType == null || calendarType == CalendarType.LUNAR || !leapMonth;
    }
}
