# 📮 shoppingmall Postman 테스트 가이드

> `user_email(String)`으로 유저 식별  
> 패키지: `com.goorm.shoppingmall`  
> 서버: `http://localhost:8080`

---

## ✅ 사전 준비 - Environment 설정

Postman 상단 **Environments** → **Add** → 아래 변수 등록

| Variable | Initial Value |
|----------|--------------|
| `BASE_URL` | `http://localhost:8080` |
| `TOKEN` | _(로그인 후 자동 세팅)_ |
| `CART_ITEM_ID` | _(장바구니 추가 후 자동 세팅)_ |
| `ORDER_ID` | _(주문 생성 후 자동 세팅)_ |

---

## 🔄 테스트 순서

```
① 회원가입
② 로그인 → TOKEN 자동 저장
③ 장바구니 조회 (자동 생성 확인)
④ 장바구니 상품 추가 (첫 번째)
⑤ 장바구니 상품 추가 (두 번째)
⑥ 장바구니 수량 변경
⑦ 장바구니 상품 단건 삭제
⑧ 단건 주문 생성
⑨ 장바구니 주문 생성 (카트 자동 비우기 확인)
⑩ 내 주문 목록 조회
⑪ 주문 상세 조회
⑫ 주문 취소
⑬ 예외 케이스 테스트
```

---

## 📌 공통 Headers (② 이후 모든 요청에 추가)

```
Authorization : Bearer {{TOKEN}}
Content-Type  : application/json
```

---

## ① 회원가입

```
Method : POST
URL    : {{BASE_URL}}/api/auth/register
```

**Body (raw / JSON)**
```json
{
  "userEmail": "test@test.com",
  "userPassword": "password123!",
  "userName": "홍길동"
}
```

**기대 응답 (200)**
```json
{
  "success": true,
  "message": "success",
  "data": {}
}
```

---

## ② 로그인 → TOKEN 자동 저장

```
Method : POST
URL    : {{BASE_URL}}/api/auth/login
```

**Body (raw / JSON)**
```json
{
  "userEmail": "test@test.com",
  "userPassword": "password123!"
}
```

**기대 응답 (200)**
```json
{
  "success": true,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

**Tests 탭 스크립트 (TOKEN 자동 저장)**
```javascript
const json = pm.response.json();
if (json.data && json.data.token) {
    pm.environment.set("TOKEN", json.data.token);
    console.log("TOKEN 저장 완료:", json.data.token);
}
```

---

## ③ 장바구니 조회

```
Method : GET
URL    : {{BASE_URL}}/api/cart
```

**기대 응답 (200) - 첫 조회 시 자동 생성**
```json
{
  "success": true,
  "message": "success",
  "data": {
    "cartId": 1,
    "items": [],
    "totalPrice": 0,
    "totalCount": 0
  }
}
```

---

## ④ 장바구니 상품 추가 (첫 번째)

```
Method : POST
URL    : {{BASE_URL}}/api/cart/items
```

**Body (raw / JSON)**
```json
{
  "productId": 1,
  "productCount": 2,
  "productPrice": 15000
}
```

**기대 응답 (200)**
```json
{
  "success": true,
  "message": "장바구니에 추가되었습니다.",
  "data": {
    "cartId": 1,
    "items": [
      {
        "cartItemId": 1,
        "productId": 1,
        "productCount": 2,
        "productPrice": 15000,
        "subtotal": 30000
      }
    ],
    "totalPrice": 30000,
    "totalCount": 2
  }
}
```

**Tests 탭 스크립트 (CART_ITEM_ID 자동 저장)**
```javascript
const json = pm.response.json();
if (json.data && json.data.items.length > 0) {
    pm.environment.set("CART_ITEM_ID", json.data.items[0].cartItemId);
    console.log("CART_ITEM_ID 저장:", json.data.items[0].cartItemId);
}
```

---

## ⑤ 장바구니 상품 추가 (두 번째)

```
Method : POST
URL    : {{BASE_URL}}/api/cart/items
```

**Body (raw / JSON)**
```json
{
  "productId": 2,
  "productCount": 1,
  "productPrice": 30000
}
```

**기대 응답 (200)**
```json
{
  "data": {
    "items": ["...2개..."],
    "totalPrice": 60000,
    "totalCount": 3
  }
}
```

---

## ⑥ 장바구니 수량 변경

```
Method : PUT
URL    : {{BASE_URL}}/api/cart/items/{{CART_ITEM_ID}}
```

**Body (raw / JSON)**
```json
{
  "productCount": 5
}
```

**기대 응답 (200)**
```json
{
  "success": true,
  "message": "수량이 변경되었습니다.",
  "data": {
    "items": [
      {
        "cartItemId": 1,
        "productCount": 5,
        "subtotal": 75000
      }
    ],
    "totalPrice": 105000
  }
}
```

---

## ⑦ 장바구니 상품 단건 삭제

```
Method : DELETE
URL    : {{BASE_URL}}/api/cart/items/{{CART_ITEM_ID}}
```

**기대 응답 (200)**
```json
{
  "success": true,
  "message": "상품이 삭제되었습니다.",
  "data": {
    "items": ["...1개만 남음..."]
  }
}
```

---

## ⑧ 단건 주문 생성

```
Method : POST
URL    : {{BASE_URL}}/api/orders
```

**Body (raw / JSON)**
```json
{
  "items": [
    {
      "productId": 3,
      "productName": "무선 마우스",
      "productCount": 1,
      "productPrice": 30000
    }
  ],
  "fromCart": false
}
```

**기대 응답 (201)**
```json
{
  "success": true,
  "message": "주문이 완료되었습니다.",
  "data": {
    "orderId": 1,
    "orderNumber": "ORD-20250419-000001",
    "orderStatus": "PENDING",
    "totalPrice": 30000,
    "totalCount": 1,
    "items": [
      {
        "orderItemId": 1,
        "productId": 3,
        "productName": "무선 마우스",
        "productCount": 1,
        "productPrice": 30000,
        "totalPrice": 30000
      }
    ],
    "orderDate": "2025-04-19T14:30:00"
  }
}
```

**Tests 탭 스크립트 (ORDER_ID 자동 저장)**
```javascript
const json = pm.response.json();
if (json.data && json.data.orderId) {
    pm.environment.set("ORDER_ID", json.data.orderId);
    console.log("ORDER_ID 저장:", json.data.orderId);
}
```

---

## ⑨ 장바구니 주문 생성 (카트 자동 비우기)

```
Method : POST
URL    : {{BASE_URL}}/api/orders
```

**Body (raw / JSON)**
```json
{
  "items": [
    {
      "productId": 2,
      "productName": "즉석밥 3개입",
      "productCount": 3,
      "productPrice": 5000
    }
  ],
  "fromCart": true
}
```

> ⚠️ 주문 후 `GET /api/cart` 재조회해서 `items: []` 인지 반드시 확인

---

## ⑩ 내 주문 목록 조회

```
Method : GET
URL    : {{BASE_URL}}/api/orders
```

**기대 응답 (200) - 최신순 정렬**
```json
{
  "success": true,
  "message": "success",
  "data": [
    {
      "orderId": 2,
      "orderNumber": "ORD-20250419-000002",
      "orderStatus": "PENDING",
      "totalPrice": 15000,
      "totalCount": 3,
      "orderDate": "2025-04-19T14:35:00"
    },
    {
      "orderId": 1,
      "orderNumber": "ORD-20250419-000001",
      "orderStatus": "PENDING",
      "totalPrice": 30000,
      "totalCount": 1,
      "orderDate": "2025-04-19T14:30:00"
    }
  ]
}
```

---

## ⑪ 주문 상세 조회

```
Method : GET
URL    : {{BASE_URL}}/api/orders/{{ORDER_ID}}
```

**기대 응답 (200)**
```json
{
  "success": true,
  "message": "success",
  "data": {
    "orderId": 1,
    "orderNumber": "ORD-20250419-000001",
    "orderStatus": "PENDING",
    "totalPrice": 30000,
    "totalCount": 1,
    "items": [
      {
        "productId": 3,
        "productName": "무선 마우스",
        "productCount": 1,
        "productPrice": 30000,
        "totalPrice": 30000
      }
    ],
    "orderDate": "2025-04-19T14:30:00"
  }
}
```

---

## ⑫ 주문 취소

```
Method : PATCH
URL    : {{BASE_URL}}/api/orders/{{ORDER_ID}}/cancel
```

**기대 응답 (200)**
```json
{
  "success": true,
  "message": "주문이 취소되었습니다.",
  "data": {
    "orderStatus": "CANCELLED"
  }
}
```

---

## ⑬ 예외 케이스 테스트

### 토큰 없이 요청 → 401
```
Method : GET
URL    : {{BASE_URL}}/api/cart
Headers: (Authorization 없음)
```
```json
{
  "code": "C004",
  "message": "인증이 필요합니다.",
  "errors": []
}
```

### 중복 상품 추가 → 409
```
Method : POST
URL    : {{BASE_URL}}/api/cart/items
Body   : { "productId": 1, "productCount": 1, "productPrice": 15000 }
```
```json
{
  "code": "CA004",
  "message": "이미 장바구니에 담긴 상품입니다.",
  "errors": []
}
```

### 수량 0으로 변경 → 400
```
Method : PUT
URL    : {{BASE_URL}}/api/cart/items/{{CART_ITEM_ID}}
Body   : { "productCount": 0 }
```
```json
{
  "code": "C001",
  "message": "입력값이 올바르지 않습니다.",
  "errors": [
    {
      "field": "productCount",
      "value": "0",
      "reason": "수량은 1개 이상이어야 합니다."
    }
  ]
}
```

### 없는 주문 조회 → 404
```
Method : GET
URL    : {{BASE_URL}}/api/orders/999
```
```json
{
  "code": "O001",
  "message": "주문을 찾을 수 없습니다.",
  "errors": []
}
```

### 배송중 주문 취소 시도 → 400
```
DB에서 직접 order_status = 'SHIPPING' 변경 후
Method : PATCH
URL    : {{BASE_URL}}/api/orders/{{ORDER_ID}}/cancel
```
```json
{
  "code": "O002",
  "message": "배송 중이거나 완료된 주문은 취소할 수 없습니다.",
  "errors": []
}
```

---

## 📁 Postman Collection 구조

```
📁 shoppingmall API
  📁 Auth
    POST 회원가입
    POST 로그인 ← Tests 탭에 TOKEN 저장 스크립트 추가
  📁 Cart
    GET    장바구니 조회
    POST   상품 추가 ← Tests 탭에 CART_ITEM_ID 저장 스크립트 추가
    POST   상품 추가 (두 번째)
    PUT    수량 변경
    DELETE 단건 삭제
    DELETE 전체 비우기
  📁 Order
    POST  단건 주문 생성 ← Tests 탭에 ORDER_ID 저장 스크립트 추가
    POST  장바구니 주문 생성
    GET   주문 목록
    GET   주문 상세
    PATCH 주문 취소
  📁 예외 케이스
    401 인증 없음
    409 중복 상품
    400 잘못된 입력
    404 없는 주문
    400 배송중 취소 불가
```

---

## 핵심 특징

```
- JWT subject = email (String)
- UserDetails.getUsername() = email
- carts.user_email = varchar(100) NOT NULL UNIQUE
- orders.user_email = varchar(100) NOT NULL
- extractUserEmail(userDetails) 로 email 추출
```
