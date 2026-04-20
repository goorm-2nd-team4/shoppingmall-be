package com.goorm.shoppingmall.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderCreateRequest {

    // 단건 주문 or 장바구니 주문 모두 items 리스트로 통일
    // 단건: items에 1개
    // 장바구니: items에 N개
    @Valid
    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다.")
    private List<OrderItemRequest> items;

    // 장바구니 주문인 경우 주문 후 카트 자동 비우기 여부
    @NotNull(message = "장바구니 주문 여부는 필수입니다.")
    private Boolean fromCart;
}