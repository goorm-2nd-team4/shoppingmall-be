package com.goorm.shoppingmall.domain.cart.repository;

import com.goorm.shoppingmall.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.userEmail = :userEmail")
    Optional<Cart> findByUserEmailWithItems(@Param("userEmail") String userEmail);

    boolean existsByUserEmail(String userEmail);
}