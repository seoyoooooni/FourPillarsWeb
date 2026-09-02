// 회원가입 중 발생하는 입력·중복 오류를 일관된 HTTP 응답으로 변환함.
package com.fourpillars.backend.common.error;

import com.fourpillars.backend.auth.exception.DuplicateEmailException;
import com.fourpillars.backend.auth.exception.InvalidCredentialsException;
import com.fourpillars.backend.auth.exception.InvalidPasswordException;
import com.fourpillars.backend.auth.exception.InvalidRefreshTokenException;
import com.fourpillars.backend.profile.exception.ProfileNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleProfileNotFound(ProfileNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("PROFILE_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    ResponseEntity<ApiErrorResponse> handleDuplicateEmail(DuplicateEmailException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse("DUPLICATE_EMAIL", exception.getMessage()));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidPassword(InvalidPasswordException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("INVALID_PASSWORD", exception.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse("INVALID_CREDENTIALS", exception.getMessage()));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse("INVALID_REFRESH_TOKEN", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        var fieldError = exception.getBindingResult().getFieldErrors().stream().findFirst();
        var message = fieldError.map(error -> error.getDefaultMessage()).orElse("입력값을 확인해 주세요.");
        return ResponseEntity.badRequest().body(new ApiErrorResponse("INVALID_INPUT", message));
    }
}
