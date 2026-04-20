package com.goorm.shoppingmall.domain.order.controller;

import com.goorm.shoppingmall.domain.order.dto.*;
import com.goorm.shoppingmall.domain.order.service.OrderService;
import com.goorm.shoppingmall.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 주문 생성
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderCreateRequest request) {

        Long userId = extractUserId(userDetails);
        OrderResponse response = orderService.createOrder(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("주문이 완료되었습니다.", response));
    }

    // 내 주문 목록
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderListResponse>>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(orderService.getMyOrders(userId)));
    }

    // 주문 상세
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {

        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.getOrderDetail(userId, orderId)));
    }

    // 주문 취소
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {

        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok("주문이 취소되었습니다.",
                orderService.cancelOrder(userId, orderId)));
    }

    // Cart Controller와 동일 - 팀원 Security 구현에 맞춰 수정
    private Long extractUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}