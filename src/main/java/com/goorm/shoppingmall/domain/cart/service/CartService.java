package com.goorm.shoppingmall.domain.cart.service;

import com.goorm.shoppingmall.domain.cart.dto.*;
import com.goorm.shoppingmall.domain.cart.entity.Cart;
import com.goorm.shoppingmall.domain.cart.entity.CartItem;
import com.goorm.shoppingmall.domain.cart.repository.CartItemRepository;
import com.goorm.shoppingmall.domain.cart.repository.CartRepository;
import com.goorm.shoppingmall.domain.product.entity.Product;
import com.goorm.shoppingmall.domain.product.repository.ProductRepository;
import com.goorm.shoppingmall.global.exception.CustomException;
import com.goorm.shoppingmall.global.exception.ErrorCode;
import java.util.Map;
import java.util.function.Function;
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
    private final ProductRepository productRepository;

    @Transactional
    public CartResponse getCart(String userEmail) {
        Cart cart = getOrCreateCart(userEmail);
        log.debug("[CartService] 장바구니 조회 - userEmail: {}, itemCount: {}",
                userEmail, cart.getCartItems().size());
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse addItem(String userEmail, CartItemAddRequest request) {
        Cart cart = getOrCreateCart(userEmail);

        boolean alreadyExists = cartItemRepository
                .existsByCartIdAndProductId(cart.getId(), request.getProductId());

        if (alreadyExists) {
            throw new CustomException(ErrorCode.CART_ITEM_ALREADY_EXISTS);
        }

        CartItem item = CartItem.create(
                cart,
                request.getProductId(),
                request.getProductCount()
        );

        ensureProductExists(request.getProductId());

        cartItemRepository.save(item);
        cart.addItem(item);

        log.debug("[CartService] 장바구니 추가 - userEmail: {}, productId: {}",
                userEmail, request.getProductId());

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse updateItemCount(String userEmail, Long cartItemId,
                                        CartItemUpdateRequest request) {
        Cart cart = getCartByUserEmail(userEmail);
        CartItem item = getCartItem(cartItemId);

        validateCartOwnership(cart, item);
        item.updateCount(request.getProductCount());

        log.debug("[CartService] 수량 변경 - cartItemId: {}, count: {}",
                cartItemId, request.getProductCount());

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(String userEmail, Long cartItemId) {
        Cart cart = getCartByUserEmail(userEmail);
        CartItem item = getCartItem(cartItemId);

        validateCartOwnership(cart, item);
        cart.removeItem(item);
        cartItemRepository.delete(item);

        log.debug("[CartService] 상품 삭제 - cartItemId: {}", cartItemId);

        return buildCartResponse(cart);
    }

    @Transactional
    public void clearCart(String userEmail) {
        Cart cart = getCartByUserEmail(userEmail);
        cart.getCartItems().clear();
        log.debug("[CartService] 장바구니 전체 비우기 - userEmail: {}", userEmail);
    }

    // ─────────────────────────────────────────
    // Private 헬퍼
    // ─────────────────────────────────────────
    private Cart getOrCreateCart(String userEmail) {
        return cartRepository.findByUserEmailWithItems(userEmail)
                .orElseGet(() -> cartRepository.save(Cart.create(userEmail)));
    }

    private Cart getCartByUserEmail(String userEmail) {
        return cartRepository.findByUserEmailWithItems(userEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));
    }

    private CartItem getCartItem(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_ITEM_NOT_FOUND));
    }

    private void validateCartOwnership(Cart cart, CartItem item) {
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private CartResponse buildCartResponse(Cart cart) {
        Map<Long, Product> productsById = productRepository.findAllById(
                        cart.getCartItems().stream()
                                .map(CartItem::getProductId)
                                .distinct()
                                .toList()
                ).stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, Function.identity()));

        boolean hasMissingProduct = cart.getCartItems().stream()
                .anyMatch(item -> !productsById.containsKey(item.getProductId()));

        if (hasMissingProduct) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        return CartResponse.from(cart, productsById);
    }
}
