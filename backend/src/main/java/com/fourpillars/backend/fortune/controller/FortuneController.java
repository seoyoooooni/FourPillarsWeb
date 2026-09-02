package com.fourpillars.backend.fortune.controller;

import com.fourpillars.backend.fortune.dto.FortuneCalculationRequest;
import com.fourpillars.backend.fortune.dto.FortuneCalculationResponse;
import com.fourpillars.backend.fortune.dto.TodayFortuneResponse;
import com.fourpillars.backend.fortune.service.FortuneCalculationService;
import com.fourpillars.backend.profile.service.BirthProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

@RestController
@RequestMapping("/api/fortune")
public class FortuneController {
    private final FortuneCalculationService service;
    private final BirthProfileService profileService;

    public FortuneController(FortuneCalculationService service, BirthProfileService profileService) {
        this.service = service;
        this.profileService = profileService;
    }

    @PostMapping("/calculate")
    public FortuneCalculationResponse calculate(@Valid @RequestBody FortuneCalculationRequest request) {
        return service.calculate(request);
    }

    @GetMapping("/today")
    public TodayFortuneResponse today(@AuthenticationPrincipal Jwt jwt) {
        return service.today(profileService.get(UUID.fromString(jwt.getSubject())));
    }
}
