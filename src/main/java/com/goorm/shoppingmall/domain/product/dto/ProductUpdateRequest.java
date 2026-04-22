package com.goorm.shoppingmall.domain.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequest(
        @Size(min = 1, max = 100, message = "상품명은 1자 이상 100자 이하여야 합니다.")
        String product_name,

        @Min(value = 0, message = "상품 가격은 0원 이상이어야 합니다.")
        Integer product_price,

        @Size(min = 1, max = 50, message = "상품 카테고리는 1자 이상 50자 이하여야 합니다.")
        String product_category,

        @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
        Integer stock
) {
}
