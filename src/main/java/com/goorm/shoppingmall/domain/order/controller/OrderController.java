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

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderCreateRequest request) {
        String userEmail = extractUserEmail(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("주문이 완료되었습니다.",
                        orderService.createOrder(userEmail, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderListResponse>>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = extractUserEmail(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(orderService.getMyOrders(userEmail)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        String userEmail = extractUserEmail(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.getOrderDetail(userEmail, orderId)));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        String userEmail = extractUserEmail(userDetails);
        return ResponseEntity.ok(ApiResponse.ok("주문이 취소되었습니다.",
                orderService.cancelOrder(userEmail, orderId)));
    }

    private String extractUserEmail(UserDetails userDetails) {
        return userDetails.getUsername();
    }
}