package com.goorm.shoppingmall.domain.order.dto;

import com.goorm.shoppingmall.domain.order.entity.Order;
import com.goorm.shoppingmall.domain.order.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class OrderResponse {

    private Long orderId;
    private String orderNumber;
    private OrderStatus orderStatus;
    private int totalPrice;
    private int totalCount;
    private List<OrderItemResponse> items;
    private LocalDateTime orderDate;

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getOrderItems()
                .stream()
                .map(OrderItemResponse::from)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .totalPrice(order.getTotalPrice())
                .totalCount(order.getTotalCount())
                .items(items)
                .orderDate(order.getOrderDate())
                .build();
    }
}