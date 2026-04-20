package com.goorm.shoppingmall.domain.cart.repository;

import com.goorm.shoppingmall.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // 유저 ID로 장바구니 조회 (cartItems 함께 fetch)
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.userId = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);
}