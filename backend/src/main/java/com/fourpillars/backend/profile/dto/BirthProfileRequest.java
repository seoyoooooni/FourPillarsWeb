// 앱이 저장할 출생 프로필 입력값과 검증 규칙을 정의함.
package com.fourpillars.backend.profile.dto;

import com.fourpillars.backend.profile.domain.CalendarType;
import com.fourpillars.backend.profile.domain.GenderBasis;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record BirthProfileRequest(
        @NotBlank @Size(max = 50) String displayName,
        @NotNull LocalDate birthDate,
        @NotNull LocalTime birthTime,
        @NotNull CalendarType calendarType,
        boolean leapMonth,
        @NotNull GenderBasis gender) {

    @AssertTrue(message = "양력에는 윤달을 선택할 수 없습니다.")
    public boolean isCalendarCombinationValid() {
        return calendarType == null || calendarType == CalendarType.LUNAR || !leapMonth;
    }
}
