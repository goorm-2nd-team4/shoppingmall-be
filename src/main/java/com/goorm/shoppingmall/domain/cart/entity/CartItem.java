package com.goorm.shoppingmall.domain.cart.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private Long productId;  // products.id FK (동일 이유로 ID만 보관)

    @Column(name = "product_count", nullable = false)
    private int productCount;

    // 생성 팩토리 메서드
    public static CartItem create(Cart cart, Long productId, int productCount) {
        CartItem item = new CartItem();
        item.cart = cart;
        item.productId = productId;
        item.productCount = productCount;
        return item;
    }

    // 비즈니스 메서드 - 수량 변경
    public void updateCount(int productCount) {
        if (productCount < 1) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        this.productCount = productCount;
    }

    // 소계 계산
    public int calculateSubtotal(int currentProductPrice) {
        return currentProductPrice * this.productCount;
    }
}
