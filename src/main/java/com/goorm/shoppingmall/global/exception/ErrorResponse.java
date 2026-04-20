package com.gmart.global.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ErrorResponse {

    private final String code;
    private final String message;
    private final List<FieldError> errors;  // Validation 에러 상세

    // CustomException 용
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .errors(List.of())
                .build();
    }

    // @Valid 바인딩 에러 용
    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .errors(FieldError.of(bindingResult))
                .build();
    }

    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String value;
        private final String reason;

        public static List<FieldError> of(BindingResult bindingResult) {
            return bindingResult.getFieldErrors()
                    .stream()
                    .map(error -> FieldError.builder()
                            .field(error.getField())
                            .value(error.getRejectedValue() == null
                                    ? "" : error.getRejectedValue().toString())
                            .reason(error.getDefaultMessage())
                            .build())
                    .collect(Collectors.toList());
        }
    }
}




// ================================================
//FE에서 받는 에러 응답 예시:
// CustomException 케이스
//  {
//        "code": "CA001",
//        "message": "장바구니를 찾을 수 없습니다.",
//        "errors": []
//   }
// @Valid 실패 케이스
//   {
//        "code": "C001",
//        "message": "입력값이 올바르지 않습니다.",
//        "errors": [
//      {
//          "field": "productCount",
//          "value": "0",
//           "reason": "수량은 1 이상이어야 합니다."
//      }
//   ]
//}
//==================================================
