package com.goorm.shoppingmall.domain.order.service;

import com.goorm.shoppingmall.domain.cart.service.CartService;
import com.goorm.shoppingmall.domain.product.entity.Product;
import com.goorm.shoppingmall.domain.product.repository.ProductRepository;
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
    private final OrderNumberGenerator orderNumberGenerator;
    private final CartService cartService;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse createOrder(String userEmail, OrderCreateRequest request) {

        if (request.getItems().isEmpty()) {
            throw new CustomException(ErrorCode.ORDER_ITEM_EMPTY);
        }

        // 1. 재고 차감
        request.getItems().forEach(item -> {

            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

            //테스트==========
            System.out.println("[DEBUG] BEFORE UPDATE stock = " + product.getStock());
            //===============

            if (product.getStock() < item.getProductCount()) {
                throw new CustomException(ErrorCode.OUT_OF_STOCK);
            }

            product.update(
                    product.getProductName(),
                    product.getProductPrice(),
                    product.getProductCategory(),
                    product.getProductDetail(),
                    product.getStock() - item.getProductCount()
            );
            //테스트========
            System.out.println("[DEBUG] AFTER UPDATE stock = " + product.getStock());
            //============
        });

        // 2. 주문 아이템 생성 (이미 조회된 값 사용)
        List<OrderItem> orderItems = request.getItems()
                .stream()
                .map(item -> {

                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

                    return OrderItem.create(
                            product.getId(),
                            product.getProductName(),
                            item.getProductCount(),
                            product.getProductPrice()
                    );
                })
                .collect(Collectors.toList());

        String orderNumber = orderNumberGenerator.generate();
        Order order = Order.create(userEmail, orderNumber, orderItems);

        orderRepository.save(order);

        log.info("[OrderService] 주문 생성 - userEmail: {}, orderNumber: {}, totalPrice: {}",
                userEmail, orderNumber, order.getTotalPrice());

        // 3. 장바구니 비우기
        if (Boolean.TRUE.equals(request.getFromCart())) {
            cartService.clearCart(userEmail);
        }

        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderListResponse> getMyOrders(String userEmail) {

        List<Order> orders = orderRepository.findAllByUserEmailWithItems(userEmail);

        return orders.stream()
                .map(OrderListResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(String userEmail, Long orderId) {

        Order order = getOrderWithValidation(userEmail, orderId);

        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancelOrder(String userEmail, Long orderId) {

        Order order = getOrderWithValidation(userEmail, orderId);

        if (!order.isCancellable()) {
            throw new CustomException(ErrorCode.ORDER_CANCEL_NOT_ALLOWED);
        }

        order.cancel();

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
}

