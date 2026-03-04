# KHSHOP - 쇼핑몰 웹 애플리케이션

> Spring Boot 기반 풀스택 이커머스 플랫폼

고객 쇼핑 경험부터 관리자 운영 대시보드까지 갖춘 종합 쇼핑몰 웹 애플리케이션입니다.

<br>

## 기술 스택

| 분류 | 기술 |
|------|------|
| **Backend** | Java 17, Spring Boot 3.4, Spring Data JPA |
| **Database** | MariaDB 10.11 |
| **Frontend** | JSP, JSTL, JavaScript (ES6+), CSS3 |
| **Infra** | Docker, Docker Compose, GitHub Actions CI/CD, Nginx |
| **외부 연동** | Portone 결제(KG이니시스), 소셜 로그인(Kakao/Naver/Google), Google Gemini API |

<br>

## 주요 기능

### 고객 영역
- **상품** — 카테고리별 탐색, 신상품·베스트·할인 상품 목록, 상품 상세 조회
- **주문/결제** — 장바구니, Portone(KG이니시스) 결제 연동, 주문 추적 및 취소
- **회원** — 이메일 회원가입, 소셜 로그인(Kakao·Naver·Google), 마이페이지
- **커뮤니티** — 구매 확정 후 리뷰 작성, 위시리스트 관리

### 관리자 영역
- **상품 관리** — CRUD, 드래그 앤 드롭 이미지 업로드, 카테고리 관리
- **주문 관리** — 주문 상태 변경, 주문 내역 조회
- **회원 관리** — 회원 검색·조회, 휴면 계정 처리
- **콘텐츠** — 홈 슬라이드 배너, 팝업 관리
- **리뷰** — 리뷰 모더레이션, 비속어 필터 관리
- **통계** — 매출·방문·상품별 통계 대시보드
- **배치 관리** — 23개 스케줄러 실시간 On/Off 제어

<br>

## 아키텍처

```
Client (Browser)
    │
    ▼
  Nginx (SSL/Reverse Proxy)
    │
    ▼
  Spring Boot App (WAR)
    ├── Controller ── Service ── Repository ── Entity
    ├── Security (CSRF, XSS, Session)
    └── Scheduler (23 Batch Jobs)
    │
    ▼
  MariaDB
```

### 패키지 구조
```
com.kh.shop
├── controller/
│   ├── admin/      # 관리자 API (16개)
│   ├── client/     # 고객 페이지 (6개)
│   └── common/     # 공통 엔드포인트
├── service/        # 비즈니스 로직 (17개)
├── repository/     # Spring Data JPA (25개)
├── entity/         # JPA 엔티티 (25개)
├── scheduler/      # 배치 스케줄러 (23개)
├── security/       # 보안 필터 및 인터셉터
├── config/         # 설정 빈
├── common/dto/     # 페이지네이션 DTO
└── util/           # 유틸리티
```

<br>

## 보안

| 보안 항목 | 구현 방식 |
|----------|----------|
| **CSRF 방어** | 커스텀 `CsrfFilter` + 토큰 기반 검증 |
| **XSS 방어** | `XssFilter` 입력 살균 + Content Security Policy 헤더 |
| **중복 로그인 방지** | `SessionRegistry`로 활성 세션 추적, 기존 세션 강제 만료 |
| **보안 헤더** | X-Frame-Options, X-Content-Type-Options 등 적용 |
| **파일 업로드 검증** | `FileUploadValidator`로 확장자·크기 검증 |

<br>

## 배치 스케줄러 (23개)

관리자 대시보드에서 개별 또는 일괄 활성화/비활성화 가능

| 카테고리 | 스케줄러 |
|---------|---------|
| **상품** | 상품 처리, AI 이미지 생성(Gemini), 베스트 상품 랭킹, 조회수 통계 |
| **주문** | 주문 상태 업데이트, 주문 생성 처리, 자동 취소 |
| **회원** | 신규 가입 처리, 휴면 계정 전환 |
| **알림** | 재입고 알림, 재고 부족 알림, 리뷰 요청, 위시리스트 가격 변동 |
| **쿠폰/포인트** | 만료 쿠폰 처리, 쿠폰 만료 알림, 포인트 만료 |
| **정리** | 장바구니·세션·임시파일·로그 정리 |
| **시스템** | DB 백업, 통계 집계, 검색 키워드 집계 |

<br>

## CI/CD & 배포

```
GitHub Push (main) → GitHub Actions → Maven Build → Docker Image → Docker Hub → SSH Deploy
```

- **빌드**: JDK 17, Maven 캐싱 적용
- **컨테이너화**: Docker 이미지 빌드 후 Docker Hub Push (`latest` + commit SHA 태그)
- **배포**: SSH를 통한 자동 배포 (docker-compose)
- **인프라**: Nginx SSL 리버스 프록시, MariaDB 컨테이너 (헬스체크 포함)

<br>

## 실행 방법

```bash
# 개발 환경 실행 (포트 8081)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Docker 배포
docker-compose up -d
```

### 환경 설정
- `application-dev.properties` — 로컬 개발 환경
- `application-prod.properties` — 프로덕션 환경
- `application-secret.properties` — API 키 (gitignored)
