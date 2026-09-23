// 회원가입 API가 받을 로그인 아이디와 비밀번호의 입력 형식을 정의함.
package com.fourpillars.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "아이디를 입력해 주세요.")
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]{3,19}$",
                message = "아이디는 영문자로 시작하는 4~20자의 영문, 숫자, 밑줄(_) 조합이어야 합니다.")
        String loginId,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Size(min = 8, max = 72, message = "비밀번호는 8자 이상 72자 이하여야 합니다.")
        String password) {
}
