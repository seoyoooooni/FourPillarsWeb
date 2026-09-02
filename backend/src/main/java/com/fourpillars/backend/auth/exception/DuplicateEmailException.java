// 이미 가입된 이메일로 회원가입할 때 발생할 오류를 표현함.
package com.fourpillars.backend.auth.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("이미 가입된 이메일입니다.");
    }
}
