package com.fourpillars.backend.fortune.dto;

import java.time.LocalDate;
import java.util.List;

public record FortuneCalendarResponse(int year, int month, List<Day> checkedDays) {
    public record Day(LocalDate date, int score, int annualRank, int wealth, int love, int health,
                      int career, int relationships, int study) {}
}
