// 로그인 회원의 출생정보와 대한민국 고정 계산 기준을 저장함.
package com.fourpillars.backend.profile.domain;

import com.fourpillars.backend.auth.domain.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "birth_profiles")
public class BirthProfile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "birth_time", nullable = false)
    private LocalTime birthTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "calendar_type", nullable = false, length = 10)
    private CalendarType calendarType;

    @Column(name = "is_leap_month", nullable = false)
    private boolean leapMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_basis", nullable = false, length = 10)
    private GenderBasis gender;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "time_zone", nullable = false, length = 50)
    private String timeZone;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected BirthProfile() {
    }

    public BirthProfile(UserAccount user) {
        this.user = user;
        this.countryCode = "KR";
        this.timeZone = "Asia/Seoul";
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public void update(String displayName, LocalDate birthDate, LocalTime birthTime, CalendarType calendarType,
                       boolean leapMonth, GenderBasis gender) {
        this.displayName = displayName.trim();
        this.birthDate = birthDate;
        this.birthTime = birthTime;
        this.calendarType = calendarType;
        this.leapMonth = calendarType == CalendarType.LUNAR && leapMonth;
        this.gender = gender;
        this.updatedAt = Instant.now();
    }

    public String getDisplayName() { return displayName; }
    public LocalDate getBirthDate() { return birthDate; }
    public LocalTime getBirthTime() { return birthTime; }
    public CalendarType getCalendarType() { return calendarType; }
    public boolean isLeapMonth() { return leapMonth; }
    public GenderBasis getGender() { return gender; }
    public String getCountryCode() { return countryCode; }
    public String getTimeZone() { return timeZone; }
}
