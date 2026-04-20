package com.goorm.shoppingmall.global.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

// 사용예시
// 이렇게 던지면 ExceptionHandler가 잡아서 FE에 일관된 형식으로 응답
//throw new CustomException(ErrorCode.CART_NOT_FOUND);
//throw new CustomException(ErrorCode.OUT_OF_STOCK);