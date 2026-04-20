package com.goorm.shoppingmall.domain.order.dto;

import com.goorm.shoppingmall.domain.order.entity.Order;
import com.goorm.shoppingmall.domain.order.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderListResponse {

    private Long orderId;
    private String orderNumber;
    private OrderStatus orderStatus;
    private int totalPrice;
    private int totalCount;
    private LocalDateTime orderDate;

    // 목록에서는 items 제외 (상세에서 확인)
    public static OrderListResponse from(Order order) {
        return OrderListResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .totalPrice(order.getTotalPrice())
                .totalCount(order.getTotalCount())
                .orderDate(order.getOrderDate())
                .build();
    }
}