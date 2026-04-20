package com.goorm.shoppingmall.domain.order.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    // 주문 시점 스냅샷 - 상품이 수정/삭제돼도 주문내역 보존
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "product_count", nullable = false)
    private int productCount;

    @Column(name = "product_price", nullable = false)
    private int productPrice;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;  // productPrice * productCount

    // 생성 팩토리 메서드
    public static OrderItem create(Long productId, String productName,
                                   int productCount, int productPrice) {
        OrderItem item = new OrderItem();
        item.productId = productId;
        item.productName = productName;
        item.productCount = productCount;
        item.productPrice = productPrice;
        item.totalPrice = productPrice * productCount;
        return item;
    }

    // Order.create() 내부에서 호출
    void assignOrder(Order order) {
        this.order = order;
    }
}