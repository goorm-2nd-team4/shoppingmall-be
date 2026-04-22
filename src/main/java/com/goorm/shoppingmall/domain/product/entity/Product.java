package com.goorm.shoppingmall.domain.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "product_price", nullable = false)
    private int productPrice;

    @Column(name = "product_category", nullable = false, length = 50)
    private String productCategory;

    @Column(nullable = false)
    private int stock;

    @Builder
    private Product(String productName, int productPrice, String productCategory, int stock) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.productCategory = productCategory;
        this.stock = stock;
    }

    public static Product create(String productName, int productPrice, String productCategory, int stock) {
        return Product.builder()
                .productName(productName)
                .productPrice(productPrice)
                .productCategory(productCategory)
                .stock(stock)
                .build();
    }

    public void update(String productName, Integer productPrice, String productCategory, Integer stock) {
        if (productName != null) {
            this.productName = productName;
        }
        if (productPrice != null) {
            this.productPrice = productPrice;
        }
        if (productCategory != null) {
            this.productCategory = productCategory;
        }
        if (stock != null) {
            this.stock = stock;
        }
    }
}
