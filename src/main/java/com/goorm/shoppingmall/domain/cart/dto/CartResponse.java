package com.goorm.shoppingmall.domain.cart.dto;

import com.goorm.shoppingmall.domain.cart.entity.Cart;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CartResponse {

    private Long cartId;
    private List<CartItemResponse> items;
    private int totalPrice;   // 전체 합계
    private int totalCount;   // 전체 수량

    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems()
                .stream()
                .map(CartItemResponse::from)
                .collect(Collectors.toList());

        int totalPrice = items.stream()
                .mapToInt(CartItemResponse::getSubtotal)
                .sum();

        int totalCount = items.stream()
                .mapToInt(CartItemResponse::getProductCount)
                .sum();

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .totalPrice(totalPrice)
                .totalCount(totalCount)
                .build();
    }
}