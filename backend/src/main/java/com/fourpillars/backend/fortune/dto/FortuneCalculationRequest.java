package com.fourpillars.backend.fortune.dto;

import com.fourpillars.backend.profile.domain.CalendarType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record FortuneCalculationRequest(
        @NotNull LocalDate birthDate,
        @NotNull LocalTime birthTime,
        @NotNull CalendarType calendarType,
        boolean leapMonth) {
}
