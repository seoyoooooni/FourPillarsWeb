// 이메일 또는 비밀번호가 일치하지 않는 로그인 오류를 표현함.
package com.fourpillars.backend.auth.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}
