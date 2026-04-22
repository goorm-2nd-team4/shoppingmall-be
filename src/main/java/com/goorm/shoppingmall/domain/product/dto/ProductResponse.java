package com.goorm.shoppingmall.domain.product.dto;

import com.goorm.shoppingmall.domain.product.entity.Product;

public record ProductResponse(
        Long id,
        String product_name,
        int product_price,
        String product_category,
        String product_detail,
        int stock
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getProductPrice(),
                product.getProductCategory(),
                product.getProductDetail(),
                product.getStock()
        );
    }
}
