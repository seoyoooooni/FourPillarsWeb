// 회원가입 성공 시 앱에 돌려줄 회원 식별자와 이메일을 정의함.
package com.fourpillars.backend.auth.dto;

import java.util.UUID;

public record SignUpResponse(UUID userId, String email) {
}
