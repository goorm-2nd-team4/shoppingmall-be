package com.goorm.shoppingmall.domain.order.service;

import com.goorm.shoppingmall.domain.cart.entity.Cart;
import com.goorm.shoppingmall.domain.cart.repository.CartRepository;
import com.goorm.shoppingmall.domain.order.dto.*;
import com.goorm.shoppingmall.domain.order.entity.Order;
import com.goorm.shoppingmall.domain.order.entity.OrderItem;
import com.goorm.shoppingmall.domain.order.entity.OrderStatus;
import com.goorm.shoppingmall.domain.order.repository.OrderRepository;
import com.goorm.shoppingmall.global.exception.CustomException;
import com.goorm.shoppingmall.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 단위 테스트")
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderNumberGenerator orderNumberGenerator;

    // ★ B안 - 전부 String email 기준
    private final String USER_EMAIL = "test@test.com";
    private final String OTHER_EMAIL = "other@test.com";
    private final Long ORDER_ID = 100L;
    private final String ORDER_NUMBER = "ORD-20250419-000001";

    // ─────────────────────────────────────────
    // 주문 생성
    // ─────────────────────────────────────────
    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("성공 - 단건 주문 생성")
        void createOrder_singleItem_success() {
            // given
            OrderCreateRequest request = createOrderRequest(false,
                    createItemRequest(10L, "무선 마우스", 1, 30000));

            given(orderNumberGenerator.generate()).willReturn(ORDER_NUMBER);
            given(orderRepository.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            OrderResponse response = orderService.createOrder(USER_EMAIL, request);

            // then
            assertThat(response.getOrderNumber()).isEqualTo(ORDER_NUMBER);
            assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(response.getTotalPrice()).isEqualTo(30000);
            assertThat(response.getTotalCount()).isEqualTo(1);
            assertThat(response.getItems()).hasSize(1);

            // 단건 주문 → 장바구니 비우기 호출 없음
            then(cartRepository).should(never()).findByUserEmailWithItems(anyString());
        }

        @Test
        @DisplayName("성공 - 장바구니 주문 후 카트 자동 비우기")
        void createOrder_fromCart_clearCartAfterOrder() {
            // given
            Cart cart = Cart.create(USER_EMAIL);
            OrderCreateRequest request = createOrderRequest(true,
                    createItemRequest(10L, "무선 마우스", 2, 15000),
                    createItemRequest(5L, "즉석밥", 3, 5000));

            given(orderNumberGenerator.generate()).willReturn(ORDER_NUMBER);
            given(orderRepository.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(cartRepository.findByUserEmailWithItems(USER_EMAIL))
                    .willReturn(Optional.of(cart));

            // when
            OrderResponse response = orderService.createOrder(USER_EMAIL, request);

            // then
            assertThat(response.getTotalPrice()).isEqualTo(45000);
            assertThat(response.getTotalCount()).isEqualTo(5);
            assertThat(response.getItems()).hasSize(2);

            // 장바구니 주문 → 카트 비우기 호출
            then(cartRepository).should(times(1)).findByUserEmailWithItems(USER_EMAIL);
            assertThat(cart.getCartItems()).isEmpty();
        }

        @Test
        @DisplayName("실패 - 주문 상품 없으면 예외")
        void createOrder_emptyItems_throwException() {
            // given
            OrderCreateRequest request = createEmptyOrderRequest();

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(USER_EMAIL, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException customEx = (CustomException) ex;
                        assertThat(customEx.getErrorCode())
                                .isEqualTo(ErrorCode.ORDER_ITEM_EMPTY);
                    });
        }
    }

    // ─────────────────────────────────────────
    // 주문 목록 조회
    // ─────────────────────────────────────────
    @Nested
    @DisplayName("주문 목록 조회")
    class GetMyOrders {

        @Test
        @DisplayName("성공 - 주문 목록 반환")
        void getMyOrders_success() {
            // given
            Order order1 = makeOrder(USER_EMAIL, ORDER_NUMBER,
                    makeOrderItem(10L, "무선 마우스", 1, 30000));
            Order order2 = makeOrder(USER_EMAIL, "ORD-20250419-000002",
                    makeOrderItem(5L, "즉석밥", 2, 5000));

            given(orderRepository.findAllByUserEmailWithItems(USER_EMAIL))
                    .willReturn(List.of(order1, order2));

            // when
            List<OrderListResponse> responses = orderService.getMyOrders(USER_EMAIL);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getOrderNumber()).isEqualTo(ORDER_NUMBER);
        }

        @Test
        @DisplayName("성공 - 주문 없으면 빈 리스트 반환")
        void getMyOrders_empty_returnEmptyList() {
            // given
            given(orderRepository.findAllByUserEmailWithItems(USER_EMAIL))
                    .willReturn(List.of());

            // when
            List<OrderListResponse> responses = orderService.getMyOrders(USER_EMAIL);

            // then
            assertThat(responses).isEmpty();
        }
    }

    // ─────────────────────────────────────────
    // 주문 상세 조회
    // ─────────────────────────────────────────
    @Nested
    @DisplayName("주문 상세 조회")
    class GetOrderDetail {

        @Test
        @DisplayName("성공 - 본인 주문 상세 조회")
        void getOrderDetail_success() {
            // given
            Order order = makeOrder(USER_EMAIL, ORDER_NUMBER,
                    makeOrderItem(10L, "무선 마우스", 1, 30000));

            given(orderRepository.findByIdWithItems(ORDER_ID))
                    .willReturn(Optional.of(order));

            // when
            OrderResponse response = orderService.getOrderDetail(USER_EMAIL, ORDER_ID);

            // then
            assertThat(response.getOrderNumber()).isEqualTo(ORDER_NUMBER);
            assertThat(response.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 주문")
        void getOrderDetail_notFound_throwException() {
            // given
            given(orderRepository.findByIdWithItems(ORDER_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.getOrderDetail(USER_EMAIL, ORDER_ID))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException customEx = (CustomException) ex;
                        assertThat(customEx.getErrorCode())
                                .isEqualTo(ErrorCode.ORDER_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("실패 - 타인 주문 접근 시 예외")
        void getOrderDetail_otherUserOrder_throwException() {
            // given
            Order order = makeOrder(OTHER_EMAIL, ORDER_NUMBER,
                    makeOrderItem(10L, "무선 마우스", 1, 30000));

            given(orderRepository.findByIdWithItems(ORDER_ID))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.getOrderDetail(USER_EMAIL, ORDER_ID))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> {
                        CustomException customEx = (CustomException) ex;
                        assertThat(customEx.getErrorCode())
                                .isEqualTo(ErrorCode.ORDER_ACCESS_DENIED);
                    });
        }
    }

    // ─────────────────────────────────────────
    // 주문 취소
    // ─────────────────────────────────────────
    @Nested
    @DisplayName("주문 취소")
    class CancelOrder {

        @Test
        @DisplayName("성공 - PENDING 상태 주문 취소")
        void cancelOrder_pending_success() {
            // given
            Order order = makeOrder(USER_EMAIL, ORDER_NUMBER,
                    makeOrderItem(10L, "무선 마우스", 1, 30000));

            given(orderRepository.findByIdWithItems(ORDER_ID))
                    .willReturn(Optional.of(order));

            // when
            OrderResponse response = orderService.cancelOrder(USER_EMAIL, ORDER_ID);

            // then
            assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("실패 - SHIPPING 상태 주문 취소 불가")
        void cancelOrder_shipping_throwException() {
            // given
            Order order = makeOrder(USER_EMAIL, ORDER_NUMBER,
                    makeOrderItem(10L, "무선 마우스", 1, 30000));
            order.updateStatus(OrderStatus.SHIPPING);

            given(orderRepository.findByIdWithItems(ORDER_ID))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(USER_EMAIL, ORDER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("배송 중이거나 완료된 주문은 취소할 수 없습니다.");
        }

        @Test
        @DisplayName("실패 - DELIVERED 상태 주문 취소 불가")
        void cancelOrder_delivered_throwException() {
            // given
            Order order = makeOrder(USER_EMAIL, ORDER_NUMBER,
                    makeOrderItem(10L, "무선 마우스", 1, 30000));
            order.updateStatus(OrderStatus.DELIVERED);

            given(orderRepository.findByIdWithItems(ORDER_ID))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.cancelOrder(USER_EMAIL, ORDER_ID))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // ─────────────────────────────────────────
    // 테스트 픽스처 헬퍼
    // ─────────────────────────────────────────
    private Order makeOrder(String userEmail, String orderNumber,
                            OrderItem... items) {
        return Order.create(userEmail, orderNumber, List.of(items));
    }

    private OrderItem makeOrderItem(Long productId, String name,
                                    int count, int price) {
        return OrderItem.create(productId, name, count, price);
    }

    private OrderCreateRequest createOrderRequest(boolean fromCart,
                                                  OrderItemRequest... items) {
        OrderCreateRequest req = new OrderCreateRequest();
        try {
            setField(req, "items", List.of(items));
            setField(req, "fromCart", fromCart);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return req;
    }

    private OrderCreateRequest createEmptyOrderRequest() {
        OrderCreateRequest req = new OrderCreateRequest();
        try {
            setField(req, "items", List.of());
            setField(req, "fromCart", false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return req;
    }

    private OrderItemRequest createItemRequest(Long productId, String name,
                                               int count, int price) {
        OrderItemRequest req = new OrderItemRequest();
        try {
            setField(req, "productId", productId);
            setField(req, "productName", name);
            setField(req, "productCount", count);
            setField(req, "productPrice", price);
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