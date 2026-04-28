package com.goorm.shoppingmall.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ──────────────────────────────
    // Common
    // ──────────────────────────────
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C003", "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C004", "인증이 필요합니다."),

    // ──────────────────────────────
    // Auth
    // ──────────────────────────────
    PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "A001", "비밀번호 확인이 일치하지 않습니다."),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A002", "이메일 또는 비밀번호가 올바르지 않습니다."),

    // ──────────────────────────────
    // Admin
    // ──────────────────────────────
    SEED_ADMIN_ROLE_CHANGE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "AD001", "기본 관리자 계정의 ADMIN 권한은 제거할 수 없습니다."),
    SEED_ADMIN_DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "AD002", "기본 관리자 계정은 삭제할 수 없습니다."),

    // ──────────────────────────────
    // User
    // ──────────────────────────────
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "U003", "비밀번호가 올바르지 않습니다."),

    // ──────────────────────────────
    // Product
    // ──────────────────────────────
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "상품을 찾을 수 없습니다."),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "P002", "상품 재고가 부족합니다."),

    // ──────────────────────────────
    // Cart
    // ──────────────────────────────
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CA001", "장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CA002", "장바구니 상품을 찾을 수 없습니다."),
    CART_ITEM_COUNT_INVALID(HttpStatus.BAD_REQUEST, "CA003", "수량은 1개 이상이어야 합니다."),
    CART_ITEM_ALREADY_EXISTS(HttpStatus.CONFLICT, "CA004", "이미 장바구니에 담긴 상품입니다."),

    // ──────────────────────────────
    // Order
    // ──────────────────────────────
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문을 찾을 수 없습니다."),
    ORDER_CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "O002", "배송 중이거나 완료된 주문은 취소할 수 없습니다."),
    ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "O003", "본인의 주문만 조회할 수 있습니다."),
    ORDER_ITEM_EMPTY(HttpStatus.BAD_REQUEST, "O004", "주문 상품이 비어있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}