package com.fourpillars.backend.fortune.dto;

import com.fourpillars.backend.profile.domain.CalendarType;
import com.fourpillars.backend.profile.domain.GenderBasis;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

public record FortuneCalculationRequest(
        @NotNull LocalDate birthDate,
        @NotNull LocalTime birthTime,
        @NotNull CalendarType calendarType,
        boolean leapMonth,
        GenderBasis gender) {

    // 성별은 선택값임: 대운(순행/역행) 계산에만 쓰이고, 없으면 대운 없이 나머지 사주 결과만 내려줌.
    public FortuneCalculationRequest(LocalDate birthDate, LocalTime birthTime, CalendarType calendarType, boolean leapMonth) {
        this(birthDate, birthTime, calendarType, leapMonth, null);
    }

    @AssertTrue(message = "양력 날짜에는 윤달을 선택할 수 없습니다.")
    public boolean isCalendarCombinationValid() {
        return calendarType == null || calendarType == CalendarType.LUNAR || !leapMonth;
    }
}
