package com.goorm.shoppingmall.domain.product.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.goorm.shoppingmall.domain.product.entity.Product;
import com.goorm.shoppingmall.domain.product.repository.ProductRepository;
import com.goorm.shoppingmall.domain.user.domain.UserRole;
import com.goorm.shoppingmall.global.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private Long productId;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        productId = productRepository.save(
                Product.create("사과", 1000, "food", 50)
        ).getId();
    }

    @Test
    void allowAnyoneToGetProducts() throws Exception {
        productRepository.save(Product.create("바나나", 500, "food", 30));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("success")))
                .andExpect(jsonPath("$.data[0].product_name", is("사과")))
                .andExpect(jsonPath("$.data[1].product_name", is("바나나")));
    }

    @Test
    void allowAnyoneToGetProduct() throws Exception {
        mockMvc.perform(get("/api/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(productId.intValue())))
                .andExpect(jsonPath("$.data.product_name", is("사과")))
                .andExpect(jsonPath("$.data.product_price", is(1000)))
                .andExpect(jsonPath("$.data.product_category", is("food")))
                .andExpect(jsonPath("$.data.stock", is(50)));
    }

    @Test
    void rejectUnauthenticatedProductCreate() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "product_name": "상품명",
                                  "product_price": 10000,
                                  "product_category": "food",
                                  "stock": 100
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("인증이 필요합니다.")));
    }

    @Test
    void rejectNonAdminProductCreate() throws Exception {
        mockMvc.perform(post("/api/products")
                        .header("Authorization", bearerToken(UserRole.USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "product_name": "상품명",
                                  "product_price": 10000,
                                  "product_category": "food",
                                  "stock": 100
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", is("접근 권한이 없습니다.")));
    }

    @Test
    void allowAdminToCreateProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .header("Authorization", bearerToken(UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "product_name": "상품명",
                                  "product_price": 10000,
                                  "product_category": "food",
                                  "stock": 100
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("상품 등록 성공")))
                .andExpect(jsonPath("$.data.product_name", is("상품명")))
                .andExpect(jsonPath("$.data.product_price", is(10000)))
                .andExpect(jsonPath("$.data.stock", is(100)));
    }

    @Test
    void allowAdminToUpdateProduct() throws Exception {
        mockMvc.perform(patch("/api/products/{productId}", productId)
                        .header("Authorization", bearerToken(UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "product_price": 12000,
                                  "stock": 80
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("상품 수정 성공")))
                .andExpect(jsonPath("$.data.id", is(productId.intValue())))
                .andExpect(jsonPath("$.data.product_name", is("사과")))
                .andExpect(jsonPath("$.data.product_price", is(12000)))
                .andExpect(jsonPath("$.data.stock", is(80)));
    }

    @Test
    void allowAdminToDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/{productId}", productId)
                        .header("Authorization", bearerToken(UserRole.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("상품 삭제 성공")));
    }

    @Test
    void returnNotFoundWhenProductDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/products/{productId}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("상품을 찾을 수 없습니다.")));
    }

    private String bearerToken(UserRole role) {
        return "Bearer " + jwtProvider.generateToken("admin@test.com", role.name());
    }
}
