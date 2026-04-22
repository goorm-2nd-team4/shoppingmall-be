# shoppingmall Postman 테스트 가이드

> 서버: `http://localhost:8080`
> 인증 방식: `Authorization: Bearer {{TOKEN}}`

---

## Environment 설정

Postman 상단 `Environments`에서 아래 변수를 등록합니다.

| Variable | Initial Value |
|----------|--------------|
| `BASE_URL` | `http://localhost:8080` |
| `TOKEN` | _(로그인 후 자동 저장)_ |
| `PRODUCT_ID` | _(상품 등록 후 자동 저장)_ |
| `CART_ITEM_ID` | _(장바구니 추가 후 자동 저장)_ |
| `ORDER_ID` | _(주문 생성 후 자동 저장)_ |

---

## 공통 Headers

인증이 필요한 요청에는 아래 헤더를 추가합니다.

```http
Authorization: Bearer {{TOKEN}}
Content-Type: application/json
```

---

## 1. 회원가입

```http
POST {{BASE_URL}}/api/auth/register
Content-Type: application/json
```

```json
{
  "user_email": "test@test.com",
  "user_password": "password123!",
  "user_password_confirm": "password123!",
  "user_name": "홍길동"
}
```

예상 응답

```json
{
  "success": true,
  "message": "회원가입 성공",
  "data": {
    "id": 1,
    "user_email": "test@test.com",
    "user_name": "홍길동",
    "user_role": "USER"
  }
}
```

---

## 2. 로그인

```http
POST {{BASE_URL}}/api/auth/login
Content-Type: application/json
```

```json
{
  "user_email": "test@test.com",
  "user_password": "password123!"
}
```

예상 응답

```json
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "id": 1,
    "user_email": "test@test.com",
    "user_name": "홍길동",
    "user_role": "USER",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

Tests 탭 스크립트

```javascript
const json = pm.response.json();
if (json.data && json.data.token) {
  pm.environment.set("TOKEN", json.data.token);
}
```

---

## 3. 상품 목록 조회

```http
GET {{BASE_URL}}/api/products
```

예상 응답

```json
{
  "success": true,
  "message": "success",
  "data": [
    {
      "id": 1,
      "product_name": "사과",
      "product_price": 1000,
      "product_category": "food",
      "stock": 50
    },
    {
      "id": 2,
      "product_name": "바나나",
      "product_price": 500,
      "product_category": "food",
      "stock": 30
    }
  ]
}
```

---

## 4. 상품 상세 조회

```http
GET {{BASE_URL}}/api/products/1
```

예상 응답

```json
{
  "success": true,
  "message": "success",
  "data": {
    "id": 1,
    "product_name": "사과",
    "product_price": 1000,
    "product_category": "food",
    "stock": 50
  }
}
```

---

## 5. 상품 등록

`ADMIN` 권한 토큰이 필요합니다.

```http
POST {{BASE_URL}}/api/products
Authorization: Bearer {{TOKEN}}
Content-Type: application/json
```

```json
{
  "product_name": "상품명",
  "product_price": 10000,
  "product_category": "food",
  "stock": 100
}
```

예상 응답

```json
{
  "success": true,
  "message": "상품 등록 성공",
  "data": {
    "id": 3,
    "product_name": "상품명",
    "product_price": 10000,
    "product_category": "food",
    "stock": 100
  }
}
```

Tests 탭 스크립트

```javascript
const json = pm.response.json();
if (json.data && json.data.id) {
  pm.environment.set("PRODUCT_ID", json.data.id);
}
```

---

## 6. 상품 수정

`ADMIN` 권한 토큰이 필요합니다.

```http
PATCH {{BASE_URL}}/api/products/{{PRODUCT_ID}}
Authorization: Bearer {{TOKEN}}
Content-Type: application/json
```

```json
{
  "product_price": 12000,
  "stock": 80
}
```

예상 응답

```json
{
  "success": true,
  "message": "상품 수정 성공",
  "data": {
    "id": 3,
    "product_name": "상품명",
    "product_price": 12000,
    "product_category": "food",
    "stock": 80
  }
}
```

`product_name`, `product_category`, `product_price`, `stock` 중 필요한 필드만 보내도 됩니다.

---

## 7. 상품 삭제

`ADMIN` 권한 토큰이 필요합니다.

```http
DELETE {{BASE_URL}}/api/products/{{PRODUCT_ID}}
Authorization: Bearer {{TOKEN}}
```

예상 응답

```json
{
  "success": true,
  "message": "상품 삭제 성공",
  "data": null
}
```

---

## 8. 인증/권한 예외 확인

비로그인 상태로 상품 등록 요청

```http
POST {{BASE_URL}}/api/products
Content-Type: application/json
```

예상 응답

```json
{
  "message": "인증이 필요합니다."
}
```

일반 사용자 토큰으로 상품 등록 요청

```json
{
  "message": "접근 권한이 없습니다."
}
```

---

## 9. 이후 cart/order 테스트

장바구니와 주문 API는 로그인 후 발급받은 `{{TOKEN}}`을 그대로 사용하면 됩니다.
상품을 먼저 등록해 두면 `productId`를 이용해 cart/order 테스트를 이어서 진행할 수 있습니다.
