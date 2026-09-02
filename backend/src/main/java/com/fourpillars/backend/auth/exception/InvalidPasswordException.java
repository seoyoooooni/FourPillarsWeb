// 안전하게 암호화할 수 없는 길이의 비밀번호 입력 오류를 표현함.
package com.fourpillars.backend.auth.exception;

public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException() {
        super("비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.");
    }
}
