// 저장된 출생 프로필과 고정된 지역 기준을 앱에 전달함.
package com.fourpillars.backend.profile.dto;

import com.fourpillars.backend.profile.domain.BirthProfile;
import com.fourpillars.backend.profile.domain.CalendarType;
import com.fourpillars.backend.profile.domain.GenderBasis;

import java.time.LocalDate;
import java.time.LocalTime;

public record BirthProfileResponse(
        String displayName,
        LocalDate birthDate,
        LocalTime birthTime,
        CalendarType calendarType,
        boolean leapMonth,
        GenderBasis gender,
        String countryCode,
        String timeZone) {

    public static BirthProfileResponse from(BirthProfile profile) {
        return new BirthProfileResponse(profile.getDisplayName(), profile.getBirthDate(), profile.getBirthTime(), profile.getCalendarType(),
                profile.isLeapMonth(), profile.getGender(), profile.getCountryCode(), profile.getTimeZone());
    }
}
