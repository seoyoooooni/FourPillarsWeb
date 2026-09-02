// 앱의 회원가입 요청을 받아 인증 서비스에 전달함.
package com.fourpillars.backend.auth.controller;

import com.fourpillars.backend.auth.dto.AuthTokenResponse;
import com.fourpillars.backend.auth.dto.LoginRequest;
import com.fourpillars.backend.auth.dto.RefreshTokenRequest;
import com.fourpillars.backend.auth.dto.SignUpRequest;
import com.fourpillars.backend.auth.dto.SignUpResponse;
import com.fourpillars.backend.auth.service.AuthService;
import com.fourpillars.backend.auth.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final LoginService loginService;

    public AuthController(AuthService authService, LoginService loginService) {
        this.authService = authService;
        this.loginService = loginService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(request));
    }

    @PostMapping("/login")
    public AuthTokenResponse login(@Valid @RequestBody LoginRequest request) {
        return loginService.login(request);
    }

    @PostMapping("/refresh")
    public AuthTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return loginService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        loginService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
