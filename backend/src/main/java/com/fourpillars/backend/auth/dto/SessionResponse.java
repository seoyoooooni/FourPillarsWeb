// Access Token으로 확인한 로그인 회원 정보를 앱에 전달함.
package com.fourpillars.backend.auth.dto;

import java.util.UUID;

public record SessionResponse(UUID userId, String email) {
}
