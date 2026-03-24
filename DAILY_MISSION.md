# Daily Dev Mission — new_shop

> 생성일: 2026-03-24 | 프로젝트: new_shop

---

## 미션: Scheduler 배치 작업의 N+1 업데이트 패턴 제거 및 벌크 연산 적용

- **영역**: 배치 스케줄러 성능 최적화
- **난이도**: 중급

### 문제점

현재 `BestProductBatchScheduler`, `DormantUserBatchScheduler`, `OrderCancelBatchScheduler` 등 핵심 스케줄러들이 **루프 안에서 개별 `save()` 호출**을 하고 있습니다.

- `BestProductBatchScheduler.java` (Line ~69): 베스트 상품 10개를 한 건씩 `productRepository.save(product)` — 총 11쿼리 (1 조회 + 10 저장)
- `DormantUserBatchScheduler.java` (Line ~49): 휴면 대상 유저를 한 명씩 `userRepository.save(user)` — 1000명이면 1000번 DB 쓰기
- `OrderCancelBatchScheduler.java` (Line ~52): 미결제 주문을 건건이 `orderRepository.save(order)` — 30분마다 반복 실행
- `CartCleanupBatchScheduler.java` (Line ~48): 전체 대상을 메모리에 로드 후 `deleteAll()` — JPA가 내부적으로 개별 DELETE 생성

이 패턴은 데이터가 적을 때는 문제없지만, 운영 환경에서 데이터가 늘어나면 배치 실행 시간이 선형 증가하고 DB 커넥션 풀을 오래 점유합니다.

### 왜 면접 강점이 되는가

JPA의 `save()` vs `saveAll()` vs `@Modifying` 벌크 쿼리의 차이를 실무 배치에서 직접 적용해본 경험은, 대용량 처리와 DB 성능 최적화에 대한 깊은 이해를 보여줍니다. 특히 "왜 JPA 기본 메서드 대신 벌크 연산을 선택했는가"를 설명할 수 있으면 강력한 차별점이 됩니다.

### 구현 가이드

1. **`BestProductBatchScheduler` / `DormantUserBatchScheduler` — `saveAll()` 적용**
   - 루프 안의 `repository.save(entity)`를 제거하고, 루프에서는 엔티티 필드 변경만 수행
   - 루프 종료 후 `repository.saveAll(list)`로 일괄 저장
   - `application.properties`에 `spring.jpa.properties.hibernate.jdbc.batch_size=50` 설정 추가하여 실제 JDBC 배치 활성화

2. **`OrderCancelBatchScheduler` — `@Modifying` 벌크 UPDATE 쿼리 적용**
   - `OrderRepository`에 벌크 업데이트 메서드 추가:
     ```java
     @Modifying(clearAutomatically = true)
     @Query("UPDATE Order o SET o.orderStatus = 'CANCELLED', o.paymentStatus = 'CANCELLED', o.cancelledAt = :now, o.cancelReason = :reason WHERE o.orderId IN :orderIds")
     int bulkCancelOrders(@Param("orderIds") List<Long> orderIds, @Param("now") LocalDateTime now, @Param("reason") String reason);
     ```
   - 스케줄러에서 대상 주문 ID 리스트만 추출 후 벌크 쿼리 1회 호출

3. **`CartCleanupBatchScheduler` — 벌크 DELETE 쿼리 적용**
   - `CartRepository`에 추가:
     ```java
     @Modifying(clearAutomatically = true)
     @Query("DELETE FROM Cart c WHERE c.updatedDate < :cutoffDate")
     int bulkDeleteAbandonedCarts(@Param("cutoffDate") LocalDateTime cutoffDate);
     ```
   - 엔티티를 메모리에 로드하지 않고 직접 삭제하여 메모리 사용량 대폭 감소

4. **배치 로그에 처리 건수와 소요 시간 기록**
   - 각 스케줄러의 `BatchLog`에 처리 건수(`processedCount`)와 실행 시간을 기록하여 개선 전/후 비교 가능하도록 구성

### 면접 질문 3선

**Q1.** JPA의 `save()`를 루프에서 호출하는 것과 `saveAll()`의 차이는 무엇이고, `saveAll()`만으로 충분하지 않은 경우는 언제인가요?
> 핵심 키워드: hibernate.jdbc.batch_size, flush 모드, 1차 캐시 메모리

**Q2.** `@Modifying` 벌크 연산 시 `clearAutomatically = true`를 설정하는 이유와, 이를 생략하면 어떤 문제가 발생하나요?
> 핵심 키워드: 영속성 컨텍스트 불일치, 1차 캐시 stale data, EntityManager.clear()

**Q3.** 대량 데이터 삭제 시 JPA `deleteAll(entities)` vs JPQL 벌크 DELETE vs 네이티브 쿼리 각각의 트레이드오프는 무엇인가요?
> 핵심 키워드: 영속성 컨텍스트 동기화, cascade 처리, DB 락 범위
