// API 오류의 식별 코드와 사용자 표시 메시지 형식을 정의함.
package com.fourpillars.backend.common.error;

public record ApiErrorResponse(String code, String message) {
}
