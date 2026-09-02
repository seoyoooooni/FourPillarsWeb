// 로그인과 재발급 성공 시 앱에 전달할 두 토큰과 만료시간을 정의함.
package com.fourpillars.backend.auth.dto;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInSeconds) {
}
