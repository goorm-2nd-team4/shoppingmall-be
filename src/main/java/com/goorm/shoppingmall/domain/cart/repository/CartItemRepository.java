package com.goorm.shoppingmall.domain.cart.repository;

import com.goorm.shoppingmall.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 같은 카트에 같은 상품이 이미 있는지 확인
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    boolean existsByCartIdAndProductId(Long cartId, Long productId);
}