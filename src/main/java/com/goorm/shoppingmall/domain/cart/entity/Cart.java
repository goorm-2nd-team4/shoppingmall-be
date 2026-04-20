package com.goorm.shoppingmall.domain.cart.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;  // users.id FK (팀원 User 엔티티와 직접 연관관계 X → ID만 보관)

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 생성 팩토리 메서드
    public static Cart create(Long userId) {
        Cart cart = new Cart();
        cart.userId = userId;
        return cart;
    }

    // 비즈니스 메서드
    public void addItem(CartItem item) {
        cartItems.add(item);
    }

    public void removeItem(CartItem item) {
        cartItems.remove(item);
    }
}