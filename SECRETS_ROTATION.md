# 시크릿 회전(Rotation) 가이드

이 프로젝트는 한때 결제/OAuth 시크릿을 `application.properties` 에 평문으로 두었고, 로컬 디스크 / 백업 / 잠재적으로 git 캐시 등에 잔존했을 가능성이 있다. 따라서 **모든 외부 시크릿 1회 회전**을 권장한다.

회전 후에는 `application-secret.properties` (gitignored) 또는 환경변수만 새 값을 가진다.

---

## 회전 순서 (영향도 ↘︎)

1. **Gmail 앱 비밀번호** — 영향 최소 (메일 발송만 잠시 안 됨)
2. **Kakao client secret**
3. **Naver client secret**
4. **Google client secret**
5. **Portone API key/secret** — 결제 직결, 가장 신중하게

각 항목은 **"새 키 발급 → 로컬/운영 환경에 반영 → 동작 검증 → 옛 키 폐기"** 순서로 진행.

---

## 사전 체크리스트

- [ ] 운영 배포 사이클 확인 (재기동 가능한 시간대인지)
- [ ] 현재 `application-secret.properties` 내용 안전한 곳(패스워드 매니저 등)에 백업
- [ ] 로컬 `mvn spring-boot:run -Dspring-boot.run.profiles=dev` 가 동작하는지 확인
- [ ] 프로덕션이 환경변수 기반인지 (`application-prod.properties` 참고) 또는 secret 파일을 ConfigMap/Vault 로 주입하는지 확인

---

## 1. Gmail 앱 비밀번호

**용도**: 회원가입 이메일 인증 발송 (`EmailVerificationService`)
**난이도**: 매우 낮음
**다운타임**: 30 초 이내 (앱 재기동 시점)

### 발급
1. https://myaccount.google.com 접속 → **보안**
2. **2단계 인증**이 켜져 있는지 확인 (꺼져 있으면 켜야 앱 비밀번호 사용 가능)
3. **앱 비밀번호** → 기존 항목(예: "KH Shop") 삭제
4. 새로 추가: 앱 이름 임의 입력 → 16자리 비밀번호 발급
5. 발급된 비밀번호를 패스워드 매니저에 저장 (이후 다시 조회 불가)

### 반영
- 로컬 `src/main/resources/application-secret.properties`:
  ```properties
  spring.mail.username=rbgud2380@gmail.com
  spring.mail.password=새로_발급된_16자리
  ```
- 운영: `SPRING_MAIL_PASSWORD` 환경변수 갱신 후 재기동

### 검증
- 회원가입 페이지에서 이메일 인증 요청 → 메일 도착 확인
- 실패 시 로그에서 `MailAuthenticationException` 여부 확인

### 옛 키 폐기
- 앱 비밀번호는 삭제 시점에 즉시 무효화됨. 별도 작업 불필요.

---

## 2. Kakao OAuth

**용도**: 카카오 소셜 로그인 (`/oauth/kakao/callback`)
**난이도**: 낮음
**다운타임**: 카카오 로그인 시도 사용자에게만 30 초 이내 영향

### 발급
1. https://developers.kakao.com → **내 애플리케이션** → 해당 앱 선택
2. **앱 설정 → 보안** 메뉴
3. **Client Secret** 영역의 **코드 재발급** 클릭 (Client ID 는 변경 안 됨)
4. **활성 상태**가 "사용함"으로 설정되어 있는지 확인 (꺼져 있으면 client_secret 없이 로그인됨 — 이 프로젝트 코드는 secret 이 있을 때만 전송)
5. 발급된 새 secret 을 패스워드 매니저에 저장

### 반영
- `application-secret.properties`:
  ```properties
  social.kakao.client-secret=새로_발급된_값
  ```
- 운영: `KAKAO_CLIENT_SECRET` 환경변수 갱신 후 재기동

### 검증
- 브라우저 시크릿 모드에서 "카카오로 로그인" 버튼 클릭 → 콜백 정상 처리되는지

### 옛 키 폐기
- 카카오는 재발급 시 즉시 옛 secret 무효화 (별도 grace period 없음)

---

## 3. Naver OAuth

**용도**: 네이버 소셜 로그인 (`/oauth/naver/callback`)
**난이도**: 낮음
**다운타임**: 네이버 로그인 사용자에게만 30 초 이내 영향

### 발급
1. https://developers.naver.com → **Application → 내 애플리케이션** → 해당 앱 선택
2. **API 설정** 탭 하단 **Client Secret** 영역 → **재발급** 버튼
3. 새 secret 을 패스워드 매니저에 저장

### 반영
- `application-secret.properties`:
  ```properties
  social.naver.client-secret=새로_발급된_값
  ```
- 운영: `NAVER_CLIENT_SECRET` 환경변수 갱신 후 재기동

### 검증
- "네이버로 로그인" 버튼 → 콜백 정상 처리 확인
- `SocialLoginController.naverCallback` 에서 `state` 검증도 함께 통과하는지 확인

### 옛 키 폐기
- 네이버 재발급 시 옛 secret 즉시 무효

---

## 4. Google OAuth

**용도**: 구글 소셜 로그인 (`/oauth/google/callback`)
**난이도**: 중간 (UI 가 자주 바뀜)
**다운타임**: 구글 로그인 사용자에게만 30 초 이내 영향

### 발급
1. https://console.cloud.google.com → 프로젝트 선택
2. **API 및 서비스 → 사용자 인증 정보(Credentials)**
3. 기존 **OAuth 2.0 클라이언트 ID** 열기
4. 우측 **클라이언트 보안 비밀번호 추가** 클릭 → 새 secret 발급
5. 옛 secret 은 잠시 함께 활성 상태로 둠 (이중 secret 지원으로 무중단 회전 가능)
6. 새 secret 을 패스워드 매니저에 저장

### 반영
- `application-secret.properties`:
  ```properties
  social.google.client-secret=새로_발급된_값
  ```
- 운영: `GOOGLE_CLIENT_SECRET` 환경변수 갱신 후 재기동

### 검증
- "구글로 로그인" 버튼 → 콜백 정상 처리 확인

### 옛 키 폐기
- 새 secret 으로 1~2 일 모니터링 후 Google Console 에서 옛 secret 명시적으로 삭제

---

## 5. Portone (KG이니시스)

**용도**: 결제 처리 (`PaymentApiController.verifyPayment`, `/cancel`)
**난이도**: 높음
**다운타임**: 결제 시도 사용자에게 영향. 점검 시간 안내 권장

### 사전 작업
- 진행 중인 결제(PENDING Order)가 있는지 DB 에서 확인:
  ```sql
  SELECT COUNT(*) FROM orders WHERE order_status = 'PENDING' AND payment_status = 'PENDING';
  ```
- 0 이거나 안전하게 무시 가능한 수량인지 확인

### 발급
1. https://admin.portone.io 로그인
2. **결제 연동 → 식별코드/API Keys** 메뉴
3. **API Secret** 영역 → **재발급** 버튼 (imp_code 와 API Key 는 보통 변경 불필요)
4. 새 secret 을 패스워드 매니저에 저장
5. 포트원은 일반적으로 **이전 secret 을 일정 기간 유효 유지**하므로 무중단 회전 가능

### 반영
- `application-secret.properties`:
  ```properties
  payment.portone.imp-code=imp_그대로_유지
  payment.portone.api-key=그대로_유지_또는_갱신
  payment.portone.api-secret=새로_발급된_값
  ```
- 운영: `PORTONE_API_SECRET` 환경변수 갱신 후 재기동

### 검증
- **테스트 모드 결제 한 번 진행**:
  - 상품 1 개 주문 → 카드 결제(테스트 카드) → `/api/payment/verify` 가 200 OK 반환하는지
  - Order 의 `order_status` 가 `PAID` 로 전환되는지
- 로그에서 `[PAY] 포트원 액세스 토큰 발급 실패` 가 안 나는지 확인

### 옛 키 폐기
- 24 시간 모니터링 후 옛 secret 명시적으로 폐기 (포트원 대시보드에서)

---

## 회전 후 점검

- [ ] `application-secret.properties` 가 여전히 git 에 추적되지 않고 있는지 (`git status` 확인)
- [ ] WAR 빌드 산출물에 secret 파일이 포함되지 않는지:
  ```bash
  ./mvnw package -DskipTests
  unzip -l target/shop-*.war | grep application-secret
  # 결과가 비어있어야 함
  ```
- [ ] 운영 배포 후 첫 결제 / 첫 소셜 로그인 / 첫 메일 발송 모두 정상 동작 확인
- [ ] 패스워드 매니저에 모든 새 시크릿 저장 완료
- [ ] 옛 시크릿이 적힌 메모 / 파일 / 채팅 로그 모두 폐기

---

## 회전 실패 시 롤백

각 서비스 모두 **새 키를 옛 키로 다시 바꿀 수는 없다** (재발급은 단방향). 대신:

1. **즉시**: 영향 받는 기능을 일시적으로 비활성화 (예: 카카오 로그인 버튼 숨김)
2. **재발급 한 번 더**: 다시 새 secret 발급 → 환경변수 갱신 → 재기동
3. **포트원 결제는 별도 점검 페이지 노출** 권장 (`payment.test-mode=true` 임시 활성화는 위험)

---

## 정책 권장

- **정기 회전**: 분기 1회 모든 시크릿 회전
- **즉시 회전 트리거**:
  - git 에 잘못 push 된 경우
  - 직원 퇴사 / 협업자 권한 해제 시
  - DB 덤프 유출 의심 시
  - 시크릿 파일이 적힌 화면을 외부 공유했을 때
- 회전 시 항상 이 문서를 같이 업데이트 (절차나 UI 가 바뀐 부분이 있으면)
