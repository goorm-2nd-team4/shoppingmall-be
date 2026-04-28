package com.goorm.shoppingmall.domain.cart.service;

import com.goorm.shoppingmall.domain.cart.dto.CartItemAddRequest;
import com.goorm.shoppingmall.domain.cart.dto.CartItemUpdateRequest;
import com.goorm.shoppingmall.domain.cart.dto.CartResponse;
import com.goorm.shoppingmall.domain.cart.entity.Cart;
import com.goorm.shoppingmall.domain.cart.entity.CartItem;
import com.goorm.shoppingmall.domain.cart.repository.CartItemRepository;
import com.goorm.shoppingmall.domain.cart.repository.CartRepository;
import com.goorm.shoppingmall.domain.product.entity.Product;
import com.goorm.shoppingmall.domain.product.repository.ProductRepository;
import com.goorm.shoppingmall.global.exception.CustomException;
import com.goorm.shoppingmall.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService 단위 테스트")
class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    private final String USER_EMAIL = "test@test.com";
    private final String OTHER_EMAIL = "other@test.com";
    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = Cart.create(USER_EMAIL);
    }

    // ─────────────────────────────────────────
    // 장바구니 조회
    // ─────────────────────────────────────────
    @Nested
    @DisplayName("장바구니 조회")
    class GetCart {

        @Test
        @DisplayName("성공 - 기존 장바구니 반환")
        void getCart_existingCart_success() {
            // given
            given(cartRepository.findByUserEmailWithItems(USER_EMAIL))
                    .willReturn(Optional.of(cart));
            given(productRepository.findAllById(anyIterable()))
                    .willReturn(java.util.List.of());

            // when
            CartResponse response = cartService.getCart(USER_EMAIL);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getItems()).isEmpty();
            then(cartRepository).should(times(1)).findByUserEmailWithItems(USER_EMAIL);
        }

        @Test
        @DisplayName("성공 - 장바구니 없으면 자동 생성")
        void getCart_noCart_createNew() {
            // given
            given(cartRepository.findByUserEmailWithItems(USER_EMAIL))
                    .willReturn(Optional.empty());
            given(cartRepository.save(any(Cart.class)))
                    .willReturn(cart);
            given(productRepository.findAllById(anyIterable()))
                    .willReturn(java.util.List.of());

            // when
            CartResponse response = cartService.getCart(USER_EMAIL);

            // then
            assertThat(response).isNotNull();
            then(cartRepository).should(times(1)).save(any(Cart.class));
        }
    }

    // ─────────────────────────────────────────
    // 장바구니 상품 추가
    // ─────────────────────────────────────────
    @Nested
    @DisplayName("장바구니 상품 추가")
    class AddItem {

        private CartItemAddRequest request;

        @BeforeEach
        void setUp() {
            request = createAddRequest(10L, 2);
        }

        @Test
        @DisplayName("성공 - 신규 상품 추가")
        void addItem_newProduct_success() {
            // given
            given(cartRepository.findByUserEmailWithItems(USER_EMAIL))
                    .willReturn(Optional.of(cart));
            given(cartItemRepository.existsByCartIdAndProductId(any(), any()))
                    .willReturn(false);
            given(productRepository.existsById(10L))
                    .willReturn(true);
            given(cartItemRepository.save(any(CartItem.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(productRepository.findAllById(anyIterable()))
                    .willReturn(java.util.List.of(createProduct(10L, 15000)));

            // when
            CartResponse response = cartService.addItem(USER_EMAIL, request);

            // then
            assertThat(response).isNotNull();
            then(cartItemRepository).should(times(1)).save(any(CartItem.class));
        }

        @Test
        @DisplayName("실패 - 이미 담긴 상품 추가 시 예외")
        void addItem_duplicateProduct_throwException() {
            // given
            given(cartRepository.findByUserEmailWithItems(USER_EMAIL))
                    .willReturn(Optional.of(cart));
            given(cartItemRepository.existsByCartIdAndProductId(any(), any()))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> cartService.addItem(USER_EMAIL, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException customEx = (CustomException) ex;
                        assertThat(customEx.getErrorCode())
                                .isEqualTo(ErrorCode.CART_ITEM_ALREADY_EXISTS);
                    });

            then(cartItemRepository).should(never()).save(any());
        }
    }

    // ─────────────────────────────────────────
    // 수량 변경
    // ─────────────────────────────────────────
    @Nested
    @DisplayName("장바구니 수량 변경")
    class UpdateItemCount {

        @Test
        @DisplayName("성공 - 수량 변경")
        void updateItemCount_success() throws Exception {
            // given
            setField(cart, "id", 1L);
            CartItem item = CartItem.create(cart, 10L, 2);
            setField(item, "id", 1L);

            cart.addItem(item);

            CartItemUpdateRequest request = createUpdateRequest(5);

            given(cartRepository.findByUserEmailWithItems(USER_EMAIL))
                    .willReturn(Optional.of(cart));
            given(cartItemRepository.findById(1L))
                    .willReturn(Optional.of(item));
            given(productRepository.findAllById(anyIterable()))
                    .willReturn(java.util.List.of(createProduct(10L, 15000)));

            // when
            CartResponse response = cartService.updateItemCount(USER_EMAIL, 1L, request);

            // then
            assertThat(response).isNotNull();
            assertThat(item.getProductCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 CartItem")
        void updateItemCount_notFound_throwException() {
            // given
            given(cartRepository.findByUserEmailWithItems(USER_EMAIL))
                    .willReturn(Optional.of(cart));
            given(cartItemRepository.findById(any()))
                    .willReturn(Optional.empty());

            CartItemUpdateRequest request = createUpdateRequest(3);

            // when & then
            assertThatThrownBy(() -> cartService.updateItemCount(USER_EMAIL, 999L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException customEx = (CustomException) ex;
                        assertThat(customEx.getErrorCode())
                                .isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
                    });
        }
    }

    // ─────────────────────────────────────────
    // 장바구니 비우기
    // ─────────────────────────────────────────
    @Nested
    @DisplayName("장바구니 전체 비우기")
    class ClearCart {

        @Test
        @DisplayName("성공 - 장바구니 전체 비우기")
        void clearCart_success() {
            // given
            CartItem item = CartItem.create(cart, 10L, 2);
            cart.addItem(item);

            given(cartRepository.findByUserEmailWithItems(USER_EMAIL))
                    .willReturn(Optional.of(cart));

            // when
            cartService.clearCart(USER_EMAIL);

            // then
            assertThat(cart.getCartItems()).isEmpty();
        }

        @Test
        @DisplayName("실패 - 장바구니 없으면 예외")
        void clearCart_notFound_throwException() {
            // given
            given(cartRepository.findByUserEmailWithItems(USER_EMAIL))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cartService.clearCart(USER_EMAIL))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException customEx = (CustomException) ex;
                        assertThat(customEx.getErrorCode())
                                .isEqualTo(ErrorCode.CART_NOT_FOUND);
                    });
        }
    }

    // ─────────────────────────────────────────
    // 테스트 픽스처 헬퍼
    // ─────────────────────────────────────────
    private CartItemAddRequest createAddRequest(Long productId, int count) {
        // Lombok @NoArgsConstructor + setter 없음 → 리플렉션으로 값 주입
        // 실제로는 @Builder 또는 테스트 전용 생성자 추가 권장
        CartItemAddRequest req = new CartItemAddRequest();
        try {
            setField(req, "productId", productId);
            setField(req, "productCount", count);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return req;
    }

    private Product createProduct(Long id, int price) {
        Product product = Product.create("test", price, "category", "detail", 10);
        try {
            setField(product, "id", id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return product;
    }

    private CartItemUpdateRequest createUpdateRequest(int count) {
        CartItemUpdateRequest req = new CartItemUpdateRequest();
        try {
            setField(req, "productCount", count);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return req;
    }

    private void setField(Object target, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
