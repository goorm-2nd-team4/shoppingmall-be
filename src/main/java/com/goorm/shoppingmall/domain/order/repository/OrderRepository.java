package com.goorm.shoppingmall.domain.order.repository;

import com.goorm.shoppingmall.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems " +
            "WHERE o.userEmail = :userEmail " +
            "ORDER BY o.orderDate DESC")
    List<Order> findAllByUserEmailWithItems(@Param("userEmail") String userEmail);

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems " +
            "WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    boolean existsByOrderNumber(String orderNumber);
}