package com.goorm.shoppingmall.domain.cart.controller;

import com.goorm.shoppingmall.domain.cart.dto.*;
import com.goorm.shoppingmall.domain.cart.service.CartService;
import com.goorm.shoppingmall.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = extractUserEmail(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCart(userEmail)));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemAddRequest request) {
        String userEmail = extractUserEmail(userDetails);
        return ResponseEntity.ok(ApiResponse.ok("장바구니에 추가되었습니다.",
                cartService.addItem(userEmail, request)));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemUpdateRequest request) {
        String userEmail = extractUserEmail(userDetails);
        return ResponseEntity.ok(ApiResponse.ok("수량이 변경되었습니다.",
                cartService.updateItemCount(userEmail, cartItemId, request)));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId) {
        String userEmail = extractUserEmail(userDetails);
        return ResponseEntity.ok(ApiResponse.ok("상품이 삭제되었습니다.",
                cartService.removeItem(userEmail, cartItemId)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = extractUserEmail(userDetails);
        cartService.clearCart(userEmail);
        return ResponseEntity.ok(ApiResponse.ok("장바구니를 비웠습니다.", null));
    }

    private String extractUserEmail(UserDetails userDetails) {
        return userDetails.getUsername();
    }
}