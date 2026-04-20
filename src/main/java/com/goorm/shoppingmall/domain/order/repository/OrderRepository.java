package com.goorm.shoppingmall.domain.order.repository;

import com.goorm.shoppingmall.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 내 주문 목록 (orderItems 함께 fetch, 최신순)
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems " +
            "WHERE o.userId = :userId " +
            "ORDER BY o.orderDate DESC")
    List<Order> findAllByUserIdWithItems(@Param("userId") Long userId);

    // 주문 상세 (orderItems 함께 fetch)
    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems " +
            "WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    // 주문번호 중복 체크
    boolean existsByOrderNumber(String orderNumber);
}