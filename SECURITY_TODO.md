# Security / Quality TODO

소스 audit 결과 (2026-05-14 기준). 시급도 순서. 완료 시 `[x]` 체크.

---

## 🔥 즉시 처리해야 할 운영 작업 (코드 변경 외)

- [ ] **시크릿 회전(rotation)** — 시크릿 값들이 로컬 디스크에 평문 존재했고, 운영자가 아닌 사람이 접근 가능했을 수 있음. 발급처에서 재발급 권장. **자세한 절차는 [SECRETS_ROTATION.md](SECRETS_ROTATION.md) 참고**:
  - [ ] Gmail 앱 비밀번호 (영향 최소, 가장 먼저)
  - [ ] Kakao client secret
  - [ ] Naver client secret
  - [ ] Google client secret
  - [ ] Portone API secret (결제 직결, 가장 신중하게)
- [ ] `application-dev.properties` 의 DB 비밀번호 `1234` 가 git 에 노출됨 — 로컬 dev 한정이지만 env 변수화 검토

---

## 🔴 CRITICAL — 보안 침해 직결

- [x] **#2 권한 상승 취약점** — 6개 admin 컨트롤러 `isAdmin()` 이 `userRole` 미확인 → 일반 USER 가 자기 자신을 ADMIN 으로 승격 가능. 완료 (2026-05-14)
- [x] **#1 시크릿 평문 노출** — `application.properties` 에 결제/OAuth 시크릿 하드코딩 → `application-secret.properties` 분리, WAR 패키징 제외, `.example` 템플릿 추가. 완료 (2026-05-14)
- [x] **#4 결제 검증 누락** — `PaymentApiController.verifyPayment` 전면 재작성. Order 를 PENDING 으로 미리 생성(`/api/order/prepare`) → server-generated `orderNumber` 를 Portone `merchant_uid` 로 사용 → 검증 시 DB Order + 포트원 응답 양쪽 검증 (소유권/상태/금액/merchant_uid 일치) → PAID 전환. 검증 실패 시 포트원 자동 환불 + Order 취소(재고 복구). 완료 (2026-05-14)
- [x] **#5 CSRF 우회** — `CsrfFilter` 가 `/api/*` 호출에서 `X-Requested-With` 헤더가 없으면 CSRF 검증을 skip 하던 버그 수정. 이제 모든 state-changing `/api/*` 호출에 토큰 필수 (webhook/OAuth callback 만 EXCLUDED_PATHS 로 면제). 또한 admin JSP 20개 + client JSP 3개에 `security-headers.jsp` include 추가하여 CSRF meta tag + `csrf.js` 가 모든 페이지에 로드되도록 함 → `window.fetch` wrapping 으로 자동 토큰 전송. 완료 (2026-05-14)
- [x] **#6 파일 업로드 검증 부실** — `FileUploadValidator` (이미 있던 컴포넌트, 안 쓰이고 있었음) 를 모든 업로드 경로에 통합. `FileUploadController` (editor-image 는 ADMIN, review-image 는 로그인 필수 + 인증 체크), `PopupService.saveFile`, `SlideService.saveFile`, `ReviewService.saveReviewImages` 모두 validator 통과 후 저장. validator 자체도 `readNBytes(12)` 로 short-read 가능성 차단, 확장자별 헤더 길이 가드 추가. 완료 (2026-05-14)
- [x] **#7 OAuth 토큰 평문 저장 + 자동 계정 연동** — `SocialAccount.accessToken` 이 어디서도 안 읽히는 dead data 였음을 확인 후 저장 자체를 중단 (DB 평문 위험 제거). 향후 사용 시 AES 암호화 권장 — 엔티티 필드는 backwards compat 위해 보존. 이메일 기반 자동 연동도 제거 → 기존 회원이 있으면 명시적 로그인 후 직접 연동 안내. 또한 `socialSignupProcess` 에 서버 세션 검증 추가 (임의 provider/providerId 가짜 가입 차단), `accessToken` form hidden field 제거 (XSS exfiltration 위험 제거). 완료 (2026-05-14)

### 후속 작업 (지금 안 한 것)
- [x] **마이페이지 소셜 계정 연동 UI** — `/mypage/setting` 에 "소셜 계정 연동" 섹션 추가. KAKAO/NAVER/GOOGLE 연동 상태 표시 + 연동/해제 버튼. 연동은 `GET /oauth/link/{provider}` 진입 → 세션 `socialLinkMode` 마킹 → 기존 OAuth 콜백이 link 모드 분기 → `SocialLoginService.linkOAuthByCode` 호출 (providerId 기준, 이메일 충돌 무시). 해제는 `POST /api/setting/social/unlink` 에 비밀번호 확인 필수 — 비밀번호로 로그인 가능한 계정만 해제 허용하여 lock-out 방지. 완료 (2026-05-16)
- [ ] **기존 DB 의 잔존 accessToken 정리 마이그레이션** — `UPDATE social_account SET access_token = NULL, refresh_token = NULL;` 한 번 실행 권장.
- [x] **#11 결제 API 인증 누락** — `PaymentApiController.verifyPayment` 와 `/cancel` 에 세션 인증 추가 (`/cancel` 은 ADMIN 권한 요구). CSRF 는 csrf.js 가 fetch 래핑하여 `X-CSRF-TOKEN` + `X-Requested-With` 자동 전송 → CsrfFilter 통과. 완료 (2026-05-14)

- [x] **추가 발견: `/order/submit` 미인증 주문 생성** — `ClientOrderController.submitOrder` 핸들러 삭제. `checkout.jsp` form 의 `action` 속성 제거 + `onsubmit="return false;"` + `<noscript>` JS 활성화 안내 추가. 이제 결제는 `/api/order/prepare` → 포트원 → `/api/payment/verify` 흐름만 가능. 완료 (2026-05-15)

---

## 🟠 HIGH — 데이터 무결성 / 런타임 오류

- [x] **#8 재고 overselling race condition** — `ProductRepository.findByIdForUpdate` (PESSIMISTIC_WRITE) 추가. `OrderService.createOrderFromCart` / `createDirectOrder` / `restoreStock` 모두 락 사용. 다중 상품 주문 시 productId 오름차순 정렬로 데드락 회피. 추가로 `OrderRepository.findByIdForUpdate` 로 `markOrderPaid` / `cancelPendingOrder` 의 중복 처리도 차단. 완료 (2026-05-14)
- [x] **#9 장바구니 race condition** — `CartService` 의 stock 체크는 UI courtesy 일 뿐 실제 차감은 주문 시점에 발생. 주문 시점 락(#8)으로 overselling 차단됨. 추가 락 불필요. 완료 (2026-05-14)
- [~] **#10 NPE 가능 — FALSE POSITIVE** — `OrderService.getOrdersByUser` 는 다음 줄에서 `if (user == null) return List.of();` 로 이미 처리됨. 수정 불필요. (2026-05-14)
- [x] **#11 NPE 가능** — `ClientOrderController` 3 곳(`/order/complete`, `/mypage/order/{id}`, `/mypage/order/cancel/{id}`)에 `order.getUser() == null` 체크 추가. 완료 (2026-05-14)
- [~] **#12 IDOR — FALSE POSITIVE** — `CartRepository.findByUserAndCartIdIn(user, cartIds)` 가 user 로 필터링하므로 타인의 cart ID 를 넘겨도 매치되지 않음. 수정 불필요. (2026-05-14)
- [~] **#13 트랜잭션 누락 — FALSE POSITIVE** — `ReviewService` 가 class-level `@Transactional` 을 가지므로 모든 메서드가 자동 transactional. (단, 파일시스템 쓰기는 트랜잭션 밖이므로 부분 저장 가능성은 남아있음 - 별개 이슈) (2026-05-14)
- [x] **#14 유저 enumeration / timing attack** — `UserService.loginUser` 에 더미 BCrypt 해시 추가. 존재하지 않는 userId 로그인 시도에도 `passwordEncoder.matches()` 한 번 수행하여 응답 시간 차이로 enumeration 되지 않게 함. 완료 (2026-05-14)
- [x] **#15 N+1 쿼리** — `OrderRepository.findByUserAndUseYnWithItemsOrderByCreatedDateDesc` (`LEFT JOIN FETCH orderItems + product`) 추가. `OrderService.getOrdersByUser` 가 이걸 사용하도록 변경. 완료 (2026-05-14)
- [x] **#16 페이징 누락 / OOM** — `AdminOrderController.orderList` 에 page/size 파라미터 + 상한(100) 추가, `OrderRepository`/`OrderService` 에 paginated 메서드 추가, `admin/order/list.jsp` 에 페이지네이션 UI 추가. 완료 (2026-05-14)

---

## 🟡 MEDIUM — 점진적 개선

- [x] **#17 비효율 정렬/제한** — `ProductRepository` 의 `findNewProducts`/`findBestProducts`/`findDiscountProducts` 에 `Pageable` 인자 받는 변형 추가. `ProductService` 가 `PageRequest.of(0, limit)` 로 호출하여 DB 가 LIMIT 으로 잘라 가져옴. `getRelatedProducts` 도 전용 쿼리 `findRelatedProducts` 신규 추가 (`productId <> :excludeId` 포함). `Collectors` import 제거. 완료 (2026-05-14)
- [x] **#18 LIKE 와일드카드 미이스케이프** — `LikeQueryUtil.escape()` 신규. `%`, `_`, `\` 를 백슬래시로 이스케이프. `ProductService`, `CategoryService`, `UserService.searchUsers` 의 검색어 입력에 모두 적용 → 의도치 않은 광범위 매치 / DoS 패턴 차단. 완료 (2026-05-14)
- [x] **#19 BCrypt 마이그레이션 마무리** — `UserRepository.countPlainPasswordUsers()` 진단 쿼리 추가. `loginUser` 에서 평문 마이그레이션 발생 시 WARN 로그 (`[BCRYPT-MIGRATION] ...userId=X`). `UserService.countPlainPasswordUsers()` + admin API `GET /api/admin/user/migration/plain-password-count` 노출. 코드에 sunset 가이드 주석 추가 (count 가 0 이 되면 평문 분기 + DUMMY_BCRYPT_HASH 제거). 완료 (2026-05-14)

### 후속 작업 (자동화는 보류)
- [x] **남은 평문 비밀번호 사용자 강제 마이그레이션 (1차)** — 평문 비밀번호 sunset 메커니즘 구현. `application.properties` 의 `bcrypt.sunset.date` 로 차단 시점 설정 (기본 2026-08-15). `UserService.attemptLogin` 가 `LoginResult` 반환하여 SUCCESS_BCRYPT / SUCCESS_PLAIN_MIGRATED / BLOCKED_PLAIN_SUNSET / INVALID 4가지 상태 노출. sunset 이전: 평문 로그인 시 BCrypt 자동 마이그레이션 + 세션 `passwordMigrationWarning` 플래그 set → header.jsp 가 노란 배너로 변경 권고 노출. sunset 이후: 평문 로그인 자체를 거부하여 사용자가 비밀번호 재설정 요청 필요. 운영자 수동 트리거 API `POST /api/admin/user/migration/send-warning-emails` 신설 — 평문 비밀번호 사용자 전원에게 sunset 날짜 + 변경 안내 메일 발송. 완료 (2026-05-16)

- [ ] **평문 분기 코드 제거 (sunset 통과 후)** — `bcrypt.sunset.date` 도래 + `countPlainPasswordUsers()` = 0 확인되면 `UserService.attemptLogin` 의 평문 비교 분기, `DUMMY_BCRYPT_HASH`(BCrypt 만 남으면 timing parity 가 동일 코드 경로로 보장됨), 그리고 sunset 관련 코드 자체를 정리. 헤더의 배너 / admin 발송 API 도 함께 제거.

---

## 진행 정책

- 새 항목 발견 시 심각도 분류 후 추가
- 완료 시 `[x]` 체크 + 완료 일자 / 커밋 해시 표기
- CRITICAL 은 가급적 같은 주 안에 처리
- 이 파일은 git 에 추적됨. 외부 노출 우려되는 운영 비밀(키 값 등)은 절대 적지 말 것
