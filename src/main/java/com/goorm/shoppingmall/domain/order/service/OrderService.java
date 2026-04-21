package com.goorm.shoppingmall.domain.order.service;

import com.goorm.shoppingmall.domain.cart.repository.CartRepository;
import com.goorm.shoppingmall.domain.order.dto.*;
import com.goorm.shoppingmall.domain.order.entity.Order;
import com.goorm.shoppingmall.domain.order.entity.OrderItem;
import com.goorm.shoppingmall.domain.order.repository.OrderRepository;
import com.goorm.shoppingmall.global.exception.CustomException;
import com.goorm.shoppingmall.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final OrderNumberGenerator orderNumberGenerator;

    @Transactional
    public OrderResponse createOrder(String userEmail, OrderCreateRequest request) {
        if (request.getItems().isEmpty()) {
            throw new CustomException(ErrorCode.ORDER_ITEM_EMPTY);
        }

        List<OrderItem> orderItems = request.getItems()
                .stream()
                .map(item -> OrderItem.create(
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductCount(),
                        item.getProductPrice()
                ))
                .collect(Collectors.toList());

        String orderNumber = orderNumberGenerator.generate();
        Order order = Order.create(userEmail, orderNumber, orderItems);
        orderRepository.save(order);

        log.info("[OrderService] 주문 생성 - userEmail: {}, orderNumber: {}, totalPrice: {}",
                userEmail, orderNumber, order.getTotalPrice());

        if (Boolean.TRUE.equals(request.getFromCart())) {
            clearCartAfterOrder(userEmail);
        }

        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderListResponse> getMyOrders(String userEmail) {
        List<Order> orders = orderRepository.findAllByUserEmailWithItems(userEmail);

        log.debug("[OrderService] 주문 목록 조회 - userEmail: {}, count: {}",
                userEmail, orders.size());

        return orders.stream()
                .map(OrderListResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(String userEmail, Long orderId) {
        Order order = getOrderWithValidation(userEmail, orderId);
        log.debug("[OrderService] 주문 상세 조회 - orderId: {}", orderId);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancelOrder(String userEmail, Long orderId) {
        Order order = getOrderWithValidation(userEmail, orderId);
        order.cancel();
        log.info("[OrderService] 주문 취소 - userEmail: {}, orderId: {}", userEmail, orderId);
        return OrderResponse.from(order);
    }

    // ─────────────────────────────────────────
    // Private 헬퍼
    // ─────────────────────────────────────────
    private Order getOrderWithValidation(String userEmail, Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserEmail().equals(userEmail)) {
            throw new CustomException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        return order;
    }

    private void clearCartAfterOrder(String userEmail) {
        cartRepository.findByUserEmailWithItems(userEmail)
                .ifPresent(cart -> {
                    cart.getCartItems().clear();
                    log.debug("[OrderService] 장바구니 비우기 완료 - userEmail: {}", userEmail);
                });
    }
}