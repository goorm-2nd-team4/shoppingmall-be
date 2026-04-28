package com.goorm.shoppingmall.domain.cart.dto;

import com.goorm.shoppingmall.domain.cart.entity.CartItem;
import com.goorm.shoppingmall.domain.product.entity.Product;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartItemResponse {

    private Long cartItemId;
    private Long productId;
    private String productName; // 추가
    private int productCount;
    private int productPrice;
    private int subtotal;  // productPrice * productCount

    public static CartItemResponse from(CartItem item, Product product) {
        int currentPrice = product.getProductPrice();
        return CartItemResponse.builder()
                .cartItemId(item.getId())
                .productId(item.getProductId())
                .productName(product.getProductName()) // 추가
                .productCount(item.getProductCount())
                .productPrice(currentPrice)
                .subtotal(item.calculateSubtotal(currentPrice))
                .build();
    }
}
