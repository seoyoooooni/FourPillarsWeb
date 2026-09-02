// 토큰 재발급과 로그아웃 API가 받을 Refresh Token 형식을 정의함.
package com.fourpillars.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh Token을 입력해 주세요.")
        String refreshToken) {
}
