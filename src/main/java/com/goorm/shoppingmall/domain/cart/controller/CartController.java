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

    // 장바구니 조회
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCart(userId)));
    }

    // 상품 추가
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemAddRequest request) {

        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok("장바구니에 추가되었습니다.",
                cartService.addItem(userId, request)));
    }

    // 수량 변경
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemUpdateRequest request) {

        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok("수량이 변경되었습니다.",
                cartService.updateItemCount(userId, cartItemId, request)));
    }

    // 단건 삭제
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId) {

        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.ok("상품이 삭제되었습니다.",
                cartService.removeItem(userId, cartItemId)));
    }

    // 전체 비우기
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = extractUserId(userDetails);
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.ok("장바구니를 비웠습니다.", null));
    }

    // ─────────────────────────────────────────
    // JWT에서 userId 추출 헬퍼
    // 팀원 Security 구현에 따라 수정 필요
    // ─────────────────────────────────────────
    private Long extractUserId(UserDetails userDetails) {
        // 팀원이 UserDetails 구현체에 userId를 담아주는 방식에 따라 변경
        // 현재는 username에 userId가 담긴다고 가정
        return Long.parseLong(userDetails.getUsername());
    }
}