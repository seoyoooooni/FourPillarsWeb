// 로그인 API가 받을 아이디와 비밀번호의 입력 형식을 정의함.
package com.fourpillars.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank(message = "아이디를 입력해 주세요.")
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]{3,19}$",
                message = "아이디 형식을 확인해 주세요.")
        String loginId,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        String password) {
}
