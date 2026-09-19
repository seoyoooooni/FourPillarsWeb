package com.fourpillars.backend.fortune.controller;

import com.fourpillars.backend.fortune.dto.FortuneCalculationRequest;
import com.fourpillars.backend.fortune.dto.FortuneCalculationResponse;
import com.fourpillars.backend.fortune.dto.TodayFortuneResponse;
import com.fourpillars.backend.fortune.dto.MajorRecommendationResponse;
import com.fourpillars.backend.fortune.dto.MajorRecommendationRequest;
import com.fourpillars.backend.fortune.dto.FortuneCalendarResponse;
import com.fourpillars.backend.fortune.service.FortuneCalculationService;
import com.fourpillars.backend.fortune.service.MajorRecommendationService;
import com.fourpillars.backend.fortune.service.DailyFortuneCacheService;
import com.fourpillars.backend.profile.service.BirthProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

@RestController
@RequestMapping("/api/fortune")
public class FortuneController {
    private final FortuneCalculationService service;
    private final BirthProfileService profileService;
    private final MajorRecommendationService majorRecommendationService;
    private final DailyFortuneCacheService dailyFortuneCacheService;

    public FortuneController(FortuneCalculationService service, BirthProfileService profileService,
                             MajorRecommendationService majorRecommendationService,
                             DailyFortuneCacheService dailyFortuneCacheService) {
        this.service = service;
        this.profileService = profileService;
        this.majorRecommendationService = majorRecommendationService;
        this.dailyFortuneCacheService = dailyFortuneCacheService;
    }

    @PostMapping("/calculate")
    public FortuneCalculationResponse calculate(@Valid @RequestBody FortuneCalculationRequest request) {
        return service.calculate(request);
    }

    @PostMapping("/major-recommendations")
    public MajorRecommendationResponse recommendMajors(@Valid @RequestBody MajorRecommendationRequest request) {
        return majorRecommendationService.recommend(service.calculate(request.fortuneRequest()),
                request.interests(), request.environments());
    }

    @GetMapping("/today")
    public TodayFortuneResponse today(@AuthenticationPrincipal Jwt jwt) {
        var userId = UUID.fromString(jwt.getSubject());
        return dailyFortuneCacheService.today(userId, profileService.get(userId));
    }

    @GetMapping("/calendar")
    public FortuneCalendarResponse calendar(@AuthenticationPrincipal Jwt jwt,
                                            @RequestParam int year, @RequestParam int month) {
        if (year < 1926 || year > 2027 || month < 1 || month > 12) {
            throw new IllegalArgumentException("조회할 연월을 확인해 주세요.");
        }
        return dailyFortuneCacheService.calendar(UUID.fromString(jwt.getSubject()), year, month);
    }
}
