// 존재하지 않거나 만료·폐기된 Refresh Token 오류를 표현함.
package com.fourpillars.backend.auth.exception;

public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Refresh Token이 유효하지 않습니다.");
    }
}
