package com.goorm.shoppingmall.domain.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductCreateRequest(
        @NotBlank(message = "상품명은 필수입니다.")
        @Size(max = 100, message = "상품명은 100자 이하여야 합니다.")
        String product_name,

        @NotNull(message = "상품 가격은 필수입니다.")
        @Min(value = 0, message = "상품 가격은 0원 이상이어야 합니다.")
        Integer product_price,

        @NotBlank(message = "상품 카테고리는 필수입니다.")
        @Size(max = 50, message = "상품 카테고리는 50자 이하여야 합니다.")
        String product_category,

        @NotNull(message = "재고는 필수입니다.")
        @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
        Integer stock
) {
}
