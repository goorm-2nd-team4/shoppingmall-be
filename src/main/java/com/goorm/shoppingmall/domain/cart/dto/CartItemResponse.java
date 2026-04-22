package com.goorm.shoppingmall.domain.cart.dto;

import com.goorm.shoppingmall.domain.cart.entity.CartItem;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartItemResponse {

    private Long cartItemId;
    private Long productId;
    private int productCount;
    private int productPrice;
    private int subtotal;  // productPrice * productCount

    public static CartItemResponse from(CartItem item) {
        return CartItemResponse.builder()
                .cartItemId(item.getId())
                .productId(item.getProductId())
                .productCount(item.getProductCount())
                .productPrice(item.getProductPrice())
                .subtotal(item.calculateSubtotal())
                .build();
    }
}