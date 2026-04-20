package com.goorm.shoppingmall.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CartItemAddRequest {

    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private Integer productCount;

    @NotNull(message = "상품 가격은 필수입니다.")
    @Min(value = 0, message = "상품 가격은 0원 이상이어야 합니다.")
    private Integer productPrice;
}