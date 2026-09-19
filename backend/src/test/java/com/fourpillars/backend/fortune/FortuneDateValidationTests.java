package com.fourpillars.backend.fortune;

import com.fourpillars.backend.fortune.dto.FortuneCalculationRequest;
import com.fourpillars.backend.fortune.service.FortuneCalculationService;
import com.fourpillars.backend.profile.domain.CalendarType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FortuneDateValidationTests {
    private final FortuneCalculationService service;

    FortuneDateValidationTests() throws IOException {
        service = new FortuneCalculationService();
    }

    @Test
    void rejectsLeapMonthWhenThatLunarMonthHasNoLeapMonth() {
        assertThatThrownBy(() -> service.calculate(request("2023-03-01", true)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("2023년 음력 3월에는 윤달이 없습니다. 평달을 선택해 주세요.");
    }

    @Test
    void rejectsDayThatDoesNotExistInSelectedLunarMonth() {
        assertThatThrownBy(() -> service.calculate(request("2023-03-31", false)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("선택한 음력 날짜는 존재하지 않습니다. 날짜를 다시 확인해 주세요.");
    }

    @Test
    void acceptsExistingLeapMonthDate() {
        service.calculate(request("2023-02-28", true));
    }

    private FortuneCalculationRequest request(String date, boolean leapMonth) {
        return new FortuneCalculationRequest(LocalDate.parse(date), LocalTime.NOON, CalendarType.LUNAR, leapMonth);
    }
}
