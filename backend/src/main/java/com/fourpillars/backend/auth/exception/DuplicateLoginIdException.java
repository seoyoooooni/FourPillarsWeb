// 이미 가입된 아이디로 회원가입할 때 발생할 오류를 표현함.
package com.fourpillars.backend.auth.exception;

public class DuplicateLoginIdException extends RuntimeException {

    public DuplicateLoginIdException() {
        super("이미 사용 중인 아이디입니다.");
    }
}
