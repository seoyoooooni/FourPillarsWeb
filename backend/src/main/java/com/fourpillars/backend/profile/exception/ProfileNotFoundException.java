// 로그인 회원에게 저장된 출생 프로필이 없음을 나타냄.
package com.fourpillars.backend.profile.exception;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException() {
        super("출생 프로필을 먼저 설정해 주세요.");
    }
}
