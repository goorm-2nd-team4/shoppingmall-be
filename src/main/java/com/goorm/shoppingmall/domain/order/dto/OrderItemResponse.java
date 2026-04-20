package com.goorm.shoppingmall.domain.order.dto;

import com.goorm.shoppingmall.domain.order.entity.OrderItem;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemResponse {

    private Long orderItemId;
    private Long productId;
    private String productName;
    private int productCount;
    private int productPrice;
    private int totalPrice;

    public static OrderItemResponse from(OrderItem item) {
        return OrderItemResponse.builder()
                .orderItemId(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productCount(item.getProductCount())
                .productPrice(item.getProductPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}