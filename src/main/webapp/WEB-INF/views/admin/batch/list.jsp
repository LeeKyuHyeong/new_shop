<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>배치 관리 - KH Shop Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/batch.css">
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>

    <main class="main-content">
        <div class="page-header">
            <h1>배치 관리</h1>
            <p class="page-description">자동 실행되는 배치 작업을 관리하고 수동으로 실행할 수 있습니다.</p>
        </div>

        <div class="batch-section">
            <h2 class="section-title">🟢 활성 배치 (구현됨)</h2>
            <div class="batch-grid">
                <c:forEach var="batch" items="${batches}">
                    <c:if test="${batch.batchId eq 'PRODUCT_CREATE' or batch.batchId eq 'ORDER_STATUS_UPDATE' or batch.batchId eq 'ORDER_CREATE' or batch.batchId eq 'USER_SIGNUP'}">
                        <div class="batch-card" data-batch-id="${batch.batchId}">
                            <div class="batch-header">
                                <h3 class="batch-name">${batch.batchName}</h3>
                                <span class="batch-status status-${batch.lastStatus != null ? batch.lastStatus.toLowerCase() : 'none'}">
                                    ${batch.lastStatusName}
                                </span>
                            </div>

                            <div class="batch-info">
                                <div class="info-row">
                                    <span class="info-label">실행 주기</span>
                                    <span class="info-value">${batch.schedule}</span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">설명</span>
                                    <span class="info-value">${batch.description}</span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">마지막 실행</span>
                                    <span class="info-value last-executed">
                                        <c:choose>
                                            <c:when test="${batch.lastExecutedAt != null}">
                                                <fmt:parseDate value="${batch.lastExecutedAt}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="parsedDate" type="both"/>
                                                <fmt:formatDate value="${parsedDate}" pattern="yyyy-MM-dd HH:mm:ss"/>
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">실행 시간</span>
                                    <span class="info-value">${batch.lastExecutionTime}</span>
                                </div>
                            </div>

                            <div class="batch-log">
                                <span class="log-label">마지막 로그</span>
                                <p class="log-message">${batch.lastMessage}</p>
                            </div>

                            <div class="batch-actions">
                                <button class="btn btn-primary btn-execute"
                                        onclick="executeBatch('${batch.batchId}')"
                                        ${batch.isRunning ? 'disabled' : ''}>
                                    <c:choose>
                                        <c:when test="${batch.isRunning}">
                                            <span class="spinner"></span> 실행중...
                                        </c:when>
                                        <c:otherwise>
                                            ▶ 수동 실행
                                        </c:otherwise>
                                    </c:choose>
                                </button>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>
            </div>
        </div>

        <div class="batch-section">
            <h2 class="section-title">⚪ 추천 배치 (미구현)</h2>
            <div class="batch-grid">
                <c:forEach var="batch" items="${batches}">
                    <c:if test="${batch.batchId eq 'CART_CLEANUP' or batch.batchId eq 'ORDER_CANCEL' or batch.batchId eq 'BEST_PRODUCT_UPDATE' or batch.batchId eq 'DORMANT_USER'}">
                        <div class="batch-card disabled" data-batch-id="${batch.batchId}">
                            <div class="batch-header">
                                <h3 class="batch-name">${batch.batchName}</h3>
                                <span class="batch-status status-disabled">미구현</span>
                            </div>

                            <div class="batch-info">
                                <div class="info-row">
                                    <span class="info-label">실행 주기</span>
                                    <span class="info-value">${batch.schedule}</span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">설명</span>
                                    <span class="info-value">${batch.description}</span>
                                </div>
                            </div>

                            <div class="batch-actions">
                                <button class="btn btn-secondary" disabled>
                                    구현 예정
                                </button>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>
            </div>
        </div>

        <div class="batch-section">
            <h2 class="section-title">📋 추천 배치 목록</h2>
            <div class="recommend-table-wrapper">
                <table class="recommend-table">
                    <thead>
                        <tr>
                            <th>배치명</th>
                            <th>추천 주기</th>
                            <th>설명</th>
                            <th>우선순위</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>장바구니 정리</td>
                            <td>매일 03:00</td>
                            <td>7일 이상 방치된 장바구니 항목 삭제</td>
                            <td><span class="priority high">높음</span></td>
                        </tr>
                        <tr>
                            <td>미결제 주문 취소</td>
                            <td>매시 30분</td>
                            <td>24시간 이상 미결제 상태인 주문 자동 취소</td>
                            <td><span class="priority high">높음</span></td>
                        </tr>
                        <tr>
                            <td>베스트 상품 갱신</td>
                            <td>매일 00:00</td>
                            <td>최근 30일 주문량 기반 베스트 상품 순위 갱신</td>
                            <td><span class="priority medium">중간</span></td>
                        </tr>
                        <tr>
                            <td>휴면 계정 처리</td>
                            <td>매일 02:00</td>
                            <td>1년 이상 미접속 계정 휴면 전환</td>
                            <td><span class="priority medium">중간</span></td>
                        </tr>
                        <tr>
                            <td>재고 부족 알림</td>
                            <td>매일 09:00</td>
                            <td>재고 10개 이하 상품 관리자 알림</td>
                            <td><span class="priority medium">중간</span></td>
                        </tr>
                        <tr>
                            <td>리뷰 작성 요청</td>
                            <td>매일 10:00</td>
                            <td>배송완료 7일 후 리뷰 작성 요청 메일 발송</td>
                            <td><span class="priority low">낮음</span></td>
                        </tr>
                        <tr>
                            <td>쿠폰 만료 알림</td>
                            <td>매일 09:00</td>
                            <td>3일 내 만료 예정 쿠폰 보유자에게 알림</td>
                            <td><span class="priority low">낮음</span></td>
                        </tr>
                        <tr>
                            <td>통계 데이터 집계</td>
                            <td>매일 01:00</td>
                            <td>일별/월별 매출, 방문자 통계 집계</td>
                            <td><span class="priority low">낮음</span></td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </main>

    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
    <script src="${pageContext.request.contextPath}/js/admin/batch.js"></script>
</body>
</html>