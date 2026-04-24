# 🛒 Shoppingmall Backend

> **패키지**: `com.goorm.shoppingmall`  
> **DB**: PostgreSQL (`shoppingmall`)  
> **유저 식별 방식**: `user_email (String)

---

## 👥 팀 구성

| 역할 | 담당 도메인 |
|------|------------|
| Backend - 전성우 | Cart, Order, Global, JWT/Security |
| Backend - 박규나 | Auth, User, Product, Admin |
| Frontend - 이홍섭 | 로그인, 회원가입, 메인 페이지 |
| Frontend - 김준영 | 장바구니, 결제, 관리자 페이지 |

---

## 🛠 기술 스택

| 항목 | 내용 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Build | Gradle (Groovy) |
| DB | PostgreSQL |
| ORM | Spring Data JPA (Hibernate) |
| 인증 | Spring Security + JWT (jjwt 0.11.5) |
| 문서화 | SpringDoc OpenAPI (Swagger) |
| 기타 | Lombok, Validation, Slf4j |

---

## 📦 의존성

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
    runtimeOnly 'org.postgresql:postgresql'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testCompileOnly 'org.projectlombok:lombok'
    testAnnotationProcessor 'org.projectlombok:lombok'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

---

## 🗂 프로젝트 구조

```
src/
├── main/
│   ├── java/com/goorm/shoppingmall/
│   │   ├── ShoppingmallApplication.java
│   │   ├── domain/
│   │   │   ├── auth/                      
│   │   │   │   ├── controller/            AuthController.java
│   │   │   │   ├── dto/                   LoginRequest / LoginResult / RegisterRequest / RegisterResult
│   │   │   │   └── service/               AuthService.java
│   │   │   ├── user/                     
│   │   │   │   ├── controller/            AdminMemberController / UserController
│   │   │   │   ├── domain/                User.java / UserRole.java
│   │   │   │   ├── dto/                   MemberListResponse / MemberResponse / UserResponse
│   │   │   │   ├── repository/            UserRepository.java
│   │   │   │   └── service/               AdminMemberService / UserService
│   │   │   ├── product/                   
│   │   │   │   ├── controller/            ProductController.java
│   │   │   │   ├── dto/                   ProductCreateRequest / ProductUpdateRequest / ProductResponse
│   │   │   │   ├── entity/                Product.java
│   │   │   │   ├── repository/            ProductRepository.java
│   │   │   │   └── service/               ProductService.java
│   │   │   ├── cart/                      
│   │   │   │   ├── controller/            CartController.java
│   │   │   │   ├── dto/                   CartItemAddRequest / UpdateRequest / Response / CartResponse
│   │   │   │   ├── entity/                Cart.java / CartItem.java
│   │   │   │   ├── repository/            CartRepository / CartItemRepository
│   │   │   │   └── service/               CartService.java
│   │   │   └── order/                     
│   │   │       ├── controller/            OrderController.java
│   │   │       ├── dto/                   OrderCreateRequest / ItemRequest / Response / ListResponse
│   │   │       ├── entity/                Order / OrderItem / OrderStatus
│   │   │       ├── repository/            OrderRepository / OrderItemRepository
│   │   │       └── service/               OrderService / OrderNumberGenerator
│   │   └── global/
│   │       ├── config/                    SecurityConfig / AdminAccountInitializer / AdminAccountProperties / OpenApiConfig
│   │       ├── exception/                 CustomException / ErrorCode / ErrorResponse / GlobalExceptionHandler
│   │       ├── jwt/                       JwtProvider / JwtAuthenticationFilter
│   │       └── response/                  ApiResponse.java
│   └── resources/
│       ├── application.yml
│       ├── application-local.yml          ← .gitignore 처리
│       └── application.properties
└── test/
    └── java/com/goorm/shoppingmall/
        ├── auth/service/                  AuthServiceTest.java
        ├── domain/
        │   ├── cart/service/              CartServiceTest.java
        │   ├── order/service/             OrderServiceTest.java
        │   └── product/controller/        ProductControllerTest.java
        └── user/controller/               AdminMemberControllerTest.java
```

---

## ⚙️ 로컬 환경 설정

### 1. PostgreSQL DB 생성

```sql
CREATE DATABASE shoppingmall;
```

### 2. `application-local.yml` 생성

> ⚠️ `.gitignore`에 포함된 파일입니다. 직접 생성해야 합니다.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/shoppingmall
    username: postgres
    password: abcd1234
    driver-class-name: org.postgresql.Driver

jwt:
  secret: test-secret-key-test-secret-key-test-secret-key
  expiration: 86400000
```

### 3. 빌드 및 실행

```bash
# 빌드 (테스트 포함)
./gradlew build

# 빌드 (테스트 제외)
./gradlew build -x test

# 서버 실행
./gradlew bootRun
```

### 4. Swagger UI

```
http://localhost:8080/swagger-ui.html
```

---

## 🗄 DB 테이블 구조

### users
| 컬럼 | 타입 | 제약 |
|------|------|------|
| id | integer | PK |
| user_email | varchar(100) | NOT NULL UNIQUE |
| user_password | varchar(255) | NOT NULL |
| user_name | varchar(50) | NOT NULL |
| user_role | varchar(20) | NOT NULL |

### carts
| 컬럼 | 타입 | 제약 |
|------|------|------|
| id | integer | PK |
| user_email | varchar(100) | NOT NULL UNIQUE |
| created_at | timestamp | |
| updated_at | timestamp | |

### cart_items
| 컬럼 | 타입 | 제약 |
|------|------|------|
| id | integer | PK |
| cart_id | integer | FK → carts |
| product_id | integer | FK → products |
| product_count | integer | NOT NULL |
| product_price | integer | NOT NULL |

### products
| 컬럼 | 타입 | 제약 |
|------|------|------|
| id | integer | PK |
| product_name | varchar(100) | NOT NULL |
| product_price | integer | NOT NULL |
| product_category | varchar(50) | NOT NULL |
| product_detail | text | |
| stock | integer | NOT NULL |

### orders
| 컬럼 | 타입 | 제약 |
|------|------|------|
| id | integer | PK |
| order_number | varchar(50) | NOT NULL UNIQUE |
| user_email | varchar(100) | NOT NULL |
| total_price | integer | NOT NULL |
| total_count | integer | NOT NULL |
| order_status | varchar(20) | NOT NULL |
| order_date | timestamp | |
| updated_at | timestamp | |

### order_items
| 컬럼 | 타입 | 제약 |
|------|------|------|
| id | integer | PK |
| order_id | integer | FK → orders |
| product_id | integer | |
| product_name | varchar(100) | NOT NULL (스냅샷) |
| product_count | integer | NOT NULL |
| product_price | integer | NOT NULL (스냅샷) |
| total_price | integer | NOT NULL |

---

## 🔐 인증 방식

```
JWT subject = email (String)
generateToken(email, role)
UserDetails.getUsername() = email
user_role 값 = "USER" / "ADMIN"
```

### 인증 흐름
```
로그인 → JWT 발급 (subject = email)
        ↓
요청 헤더: Authorization: Bearer {token}
        ↓
JwtAuthenticationFilter → email 추출
        ↓
SecurityContextHolder 저장
        ↓
@AuthenticationPrincipal UserDetails
→ getUsername() = email
```

---

## 🌐 API 명세

### 인증
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/auth/register | 회원가입 |
| POST | /api/auth/login | 로그인 |
| POST | /api/auth/logout | 로그아웃 |

### 유저
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| GET | /api/users/me | 내 정보 조회 |

### 상품
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| GET | /api/products | 상품 목록 |
| POST | /api/products | 상품 등록 | ✅ ADMIN |
| PATCH | /api/products/{id} | 상품 수정 | ✅ ADMIN |
| DELETE | /api/products/{id} | 상품 삭제 | ✅ ADMIN |

### 장바구니
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| GET | /api/cart | 장바구니 조회 |
| POST | /api/cart/items | 상품 추가 |
| PUT | /api/cart/items/{id} | 수량 변경 |
| DELETE | /api/cart/items/{id} | 단건 삭제 |
| DELETE | /api/cart | 전체 비우기 |

### 주문
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/orders | 주문 생성 |
| GET | /api/orders | 내 주문 목록 |
| GET | /api/orders/{id} | 주문 상세 |
| PATCH | /api/orders/{id}/cancel | 주문 취소 |

### 관리자
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| GET | /api/admin/members | 회원 목록 | ✅ ADMIN |
| GET | /api/admin/members/{id} | 회원 상세 | ✅ ADMIN |
| PATCH | /api/admin/members/{id}/role | 권한 변경 | ✅ ADMIN |
| DELETE | /api/admin/members/{id} | 회원 삭제 | ✅ ADMIN |

---

## 📋 공통 응답 포맷

### 성공
```json
{
  "success": true,
  "message": "success",
  "data": {}
}
```

### 실패
```json
{
  "code": "CA001",
  "message": "장바구니를 찾을 수 없습니다.",
  "errors": []
}
```

### 에러 코드표
| 코드 | 설명 | HTTP |
|------|------|------|
| C001 | 입력값 오류 | 400 |
| C002 | 서버 내부 오류 | 500 |
| C003 | 접근 권한 없음 | 403 |
| C004 | 인증 필요 | 401 |
| A001 | 비밀번호 확인 불일치 | 400 |
| A002 | 이메일 또는 비밀번호 오류 | 401 |
| AD001 | 시드 관리자 권한 변경 불가 | 400 |
| AD002 | 시드 관리자 삭제 불가 | 400 |
| U001 | 유저 없음 | 404 |
| U002 | 이메일 중복 | 409 |
| U003 | 비밀번호 불일치 | 401 |
| P001 | 상품 없음 | 404 |
| P002 | 재고 부족 | 400 |
| CA001 | 장바구니 없음 | 404 |
| CA002 | 장바구니 상품 없음 | 404 |
| CA003 | 수량 오류 | 400 |
| CA004 | 중복 상품 | 409 |
| O001 | 주문 없음 | 404 |
| O002 | 주문 취소 불가 | 400 |
| O003 | 주문 접근 권한 없음 | 403 |
| O004 | 주문 상품 없음 | 400 |

---

## 🌿 브랜치 전략

```
main      → 최종 배포, 직접 push 금지
develop   → 통합 브랜치, PR로만 머지
  ├── feature/global-common
  ├── feature/cart-domain
  ├── feature/order-domain
  ├── feature/security
  ├── feature/user-domain
  ├── feature/product-domain
  └── fix/pre-deploy
```

### 커밋 메시지 컨벤션
```
feat     새 기능 추가
fix      버그 수정
refactor 리팩토링
test     테스트 코드
docs     문서 수정
chore    설정 변경
```

---

## ✅ User Flow

```
상품 목록
  └─ 장바구니 추가
       └─ 구매하기
            ├─ 미로그인 → 로그인 → 회원가입 → 직전 화면 복귀
            └─ 로그인됨 → 주문 생성 → 구매 완료 → 내 주문 목록
```

---

## 🧪 테스트

```
단위 테스트 (Mockito / AssertJ)
  CartServiceTest    8개
  OrderServiceTest   9개

통합 테스트 (@SpringBootTest)
  AuthServiceTest          6개
  ProductControllerTest    8개
  AdminMemberControllerTest 7개

총 38개 테스트 케이스
```

### 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 특정 클래스만
./gradlew test --tests "com.goorm.shoppingmall.domain.cart.service.CartServiceTest"
```

---

## 🔧 트러블슈팅

```
1. 패키지명 오류
   com.shoppingmall → com.goorm.shoppingmall
   → IntelliJ Replace in Files (Ctrl+Shift+H) 로 전체 교체

2. JWT 라이브러리 버전 충돌
   .subject() / .verifyWith() 메서드 없음
   → jjwt 0.12.x → 0.11.x 다운그레이드
   → setSubject() / setSigningKey() 로 변경

3. 읽기 전용 트랜잭션 INSERT 오류
   @Transactional(readOnly = true) 에서
   카트 자동 생성 시 INSERT 발생
   → @Transactional 로 수정

4. DB 설정 오류
   ddl-auto: create → 재시작마다 데이터 초기화
   → ddl-auto: update 로 수정

5. 예외 처리 패키지 중복
   global/error (팀원) + global/exception (본인) 충돌
   → global/exception 으로 통일
   → global/error 패키지 전체 삭제
```

---

## ⚠️ 핵심 설정 요약

```
✅ JWT subject = email
✅ carts.user_email = varchar NOT NULL UNIQUE
✅ orders.user_email = varchar NOT NULL
✅ user_role = "USER" / "ADMIN"
✅ ddl-auto = update (로컬) / create-drop (테스트)
✅ application-local.yml → .gitignore 처리 필수
✅ 시드 관리자 계정 자동 생성 (AdminAccountInitializer)
```