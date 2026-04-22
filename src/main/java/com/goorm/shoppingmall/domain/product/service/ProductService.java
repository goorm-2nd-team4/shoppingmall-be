package com.goorm.shoppingmall.domain.product.service;

import com.goorm.shoppingmall.domain.product.dto.ProductCreateRequest;
import com.goorm.shoppingmall.domain.product.dto.ProductResponse;
import com.goorm.shoppingmall.domain.product.dto.ProductUpdateRequest;
import com.goorm.shoppingmall.domain.product.entity.Product;
import com.goorm.shoppingmall.domain.product.repository.ProductRepository;
import com.goorm.shoppingmall.global.error.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts() {
        return productRepository.findAllByOrderByIdAsc().stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        return ProductResponse.from(findProduct(productId));
    }

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        Product product = Product.create(
                request.product_name(),
                request.product_price(),
                request.product_category(),
                request.product_detail(),
                request.stock()
        );

        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = findProduct(productId);
        product.update(
                request.product_name(),
                request.product_price(),
                request.product_category(),
                request.product_detail(),
                request.stock()
        );

        return ProductResponse.from(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        productRepository.delete(findProduct(productId));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다."));
    }
}
