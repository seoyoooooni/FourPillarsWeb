// 인증된 회원의 출생 프로필 조회·저장 HTTP 요청을 처리함.
package com.fourpillars.backend.profile.controller;

import com.fourpillars.backend.profile.dto.BirthProfileRequest;
import com.fourpillars.backend.profile.dto.BirthProfileResponse;
import com.fourpillars.backend.profile.service.BirthProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class BirthProfileController {
    private final BirthProfileService profileService;

    public BirthProfileController(BirthProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public BirthProfileResponse get(@AuthenticationPrincipal Jwt jwt) {
        return profileService.get(UUID.fromString(jwt.getSubject()));
    }

    @PutMapping
    public BirthProfileResponse save(@AuthenticationPrincipal Jwt jwt,
                                     @Valid @RequestBody BirthProfileRequest request) {
        return profileService.save(UUID.fromString(jwt.getSubject()), request);
    }
}
