package com.goorm.shoppingmall.domain.cart.service;

import com.goorm.shoppingmall.domain.cart.dto.*;
import com.goorm.shoppingmall.domain.cart.entity.Cart;
import com.goorm.shoppingmall.domain.cart.entity.CartItem;
import com.goorm.shoppingmall.domain.cart.repository.CartItemRepository;
import com.goorm.shoppingmall.domain.cart.repository.CartRepository;
import com.goorm.shoppingmall.global.exception.CustomException;
import com.goorm.shoppingmall.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    // ─────────────────────────────────────────
    // 장바구니 조회
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        log.debug("[CartService] 장바구니 조회 - userId: {}, itemCount: {}",
                userId, cart.getCartItems().size());
        return CartResponse.from(cart);
    }

    // ─────────────────────────────────────────
    // 장바구니 상품 추가
    // ─────────────────────────────────────────
    @Transactional
    public CartResponse addItem(Long userId, CartItemAddRequest request) {
        Cart cart = getOrCreateCart(userId);

        // 이미 담긴 상품이면 예외 (중복 방지 - 수량 변경 API 사용 유도)
        boolean alreadyExists = cartItemRepository
                .existsByCartIdAndProductId(cart.getId(), request.getProductId());

        if (alreadyExists) {
            throw new CustomException(ErrorCode.CART_ITEM_ALREADY_EXISTS);
        }

        CartItem item = CartItem.create(
                cart,
                request.getProductId(),
                request.getProductCount(),
                request.getProductPrice()
        );

        cartItemRepository.save(item);
        cart.addItem(item);

        log.debug("[CartService] 장바구니 추가 - userId: {}, productId: {}",
                userId, request.getProductId());

        return CartResponse.from(cart);
    }

    // ─────────────────────────────────────────
    // 장바구니 수량 변경
    // ─────────────────────────────────────────
    @Transactional
    public CartResponse updateItemCount(Long userId, Long cartItemId,
                                        CartItemUpdateRequest request) {
        Cart cart = getCartByUserId(userId);
        CartItem item = getCartItem(cartItemId);

        // 본인 카트의 아이템인지 검증
        validateCartOwnership(cart, item);

        item.updateCount(request.getProductCount());

        log.debug("[CartService] 수량 변경 - cartItemId: {}, count: {}",
                cartItemId, request.getProductCount());

        return CartResponse.from(cart);
    }

    // ─────────────────────────────────────────
    // 장바구니 상품 단건 삭제
    // ─────────────────────────────────────────
    @Transactional
    public CartResponse removeItem(Long userId, Long cartItemId) {
        Cart cart = getCartByUserId(userId);
        CartItem item = getCartItem(cartItemId);

        validateCartOwnership(cart, item);

        cart.removeItem(item);
        cartItemRepository.delete(item);

        log.debug("[CartService] 상품 삭제 - cartItemId: {}", cartItemId);

        return CartResponse.from(cart);
    }

    // ─────────────────────────────────────────
    // 장바구니 전체 비우기
    // ─────────────────────────────────────────
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getCartByUserId(userId);
        cart.getCartItems().clear();  // orphanRemoval = true → 자동 DELETE

        log.debug("[CartService] 장바구니 전체 비우기 - userId: {}", userId);
    }

    // ─────────────────────────────────────────
    // Private 헬퍼 메서드
    // ─────────────────────────────────────────

    // 카트 조회 (없으면 자동 생성 - 1인 1카트)
    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> cartRepository.save(Cart.create(userId)));
    }

    // 카트 조회 (없으면 예외)
    private Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));
    }

    // CartItem 조회 (없으면 예외)
    private CartItem getCartItem(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_ITEM_NOT_FOUND));
    }

    // 본인 카트의 아이템인지 검증
    private void validateCartOwnership(Cart cart, CartItem item) {
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
}