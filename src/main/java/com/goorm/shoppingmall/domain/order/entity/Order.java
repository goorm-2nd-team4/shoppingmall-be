package com.goorm.shoppingmall.domain.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(name = "user_email", nullable = false, length = 100)
    private String userEmail;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "order_date", updatable = false)
    private LocalDateTime orderDate;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static Order create(String userEmail, String orderNumber,
                               List<OrderItem> items) {
        Order order = new Order();
        order.userEmail = userEmail;
        order.orderNumber = orderNumber;
        order.orderStatus = OrderStatus.PENDING;

        for (OrderItem item : items) {
            item.assignOrder(order);
            order.orderItems.add(item);
        }

        order.totalCount = items.stream()
                .mapToInt(OrderItem::getProductCount)
                .sum();

        order.totalPrice = items.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();

        return order;
    }

    public void cancel() {
        if (this.orderStatus == OrderStatus.SHIPPING ||
                this.orderStatus == OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송 중이거나 완료된 주문은 취소할 수 없습니다.");
        }
        this.orderStatus = OrderStatus.CANCELLED;
    }

    public void updateStatus(OrderStatus status) {
        this.orderStatus = status;
    }
}