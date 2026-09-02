// 유효한 Access Token으로 현재 로그인 회원 정보를 조회함.
package com.fourpillars.backend.auth.controller;

import com.fourpillars.backend.auth.dto.SessionResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class SessionController {

    @GetMapping("/session")
    public SessionResponse session(@AuthenticationPrincipal Jwt jwt) {
        return new SessionResponse(UUID.fromString(jwt.getSubject()), jwt.getClaimAsString("email"));
    }
}
