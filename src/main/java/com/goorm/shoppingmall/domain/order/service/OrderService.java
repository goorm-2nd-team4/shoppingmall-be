package com.goorm.shoppingmall.domain.order.service;

import com.goorm.shoppingmall.domain.cart.entity.Cart;
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

    // ─────────────────────────────────────────
    // 주문 생성 (단건 / 장바구니 통합)
    // ─────────────────────────────────────────
    @Transactional
    public OrderResponse createOrder(Long userId, OrderCreateRequest request) {

        // 주문 상품 없으면 예외
        if (request.getItems().isEmpty()) {
            throw new CustomException(ErrorCode.ORDER_ITEM_EMPTY);
        }

        // OrderItem 엔티티 생성
        List<OrderItem> orderItems = request.getItems()
                .stream()
                .map(item -> OrderItem.create(
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductCount(),
                        item.getProductPrice()
                ))
                .collect(Collectors.toList());

        // 주문번호 생성
        String orderNumber = orderNumberGenerator.generate();

        // Order 생성 (totalPrice, totalCount 자동 계산)
        Order order = Order.create(userId, orderNumber, orderItems);
        orderRepository.save(order);

        log.info("[OrderService] 주문 생성 - userId: {}, orderNumber: {}, totalPrice: {}",
                userId, orderNumber, order.getTotalPrice());

        // 장바구니 주문이면 카트 비우기
        if (Boolean.TRUE.equals(request.getFromCart())) {
            clearCartAfterOrder(userId);
        }

        return OrderResponse.from(order);
    }

    // ─────────────────────────────────────────
    // 내 주문 목록 조회
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<OrderListResponse> getMyOrders(Long userId) {
        List<Order> orders = orderRepository.findAllByUserIdWithItems(userId);

        log.debug("[OrderService] 주문 목록 조회 - userId: {}, count: {}",
                userId, orders.size());

        return orders.stream()
                .map(OrderListResponse::from)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // 주문 상세 조회
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long userId, Long orderId) {
        Order order = getOrderWithValidation(userId, orderId);

        log.debug("[OrderService] 주문 상세 조회 - orderId: {}", orderId);

        return OrderResponse.from(order);
    }

    // ─────────────────────────────────────────
    // 주문 취소
    // ─────────────────────────────────────────
    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        Order order = getOrderWithValidation(userId, orderId);

        // 취소 가능 여부 검증은 Order 엔티티 내부에서 처리
        order.cancel();

        log.info("[OrderService] 주문 취소 - userId: {}, orderId: {}", userId, orderId);

        return OrderResponse.from(order);
    }

    // ─────────────────────────────────────────
    // Private 헬퍼 메서드
    // ─────────────────────────────────────────

    // 주문 조회 + 본인 검증 통합
    private Order getOrderWithValidation(Long userId, Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // 본인 주문인지 확인
        if (!order.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        return order;
    }

    // 주문 완료 후 장바구니 비우기
    private void clearCartAfterOrder(Long userId) {
        cartRepository.findByUserIdWithItems(userId)
                .ifPresent(cart -> {
                    cart.getCartItems().clear();
                    log.debug("[OrderService] 장바구니 비우기 완료 - userId: {}", userId);
                });
    }
}