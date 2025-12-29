<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>주문 완료 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/order-complete.css">
</head>
<body>
    <%@ include file="common/header.jsp" %>

    <div class="complete-container">
        <div class="success-icon">✅</div>
        <h1 class="complete-title">주문이 완료되었습니다!</h1>
        <p class="complete-message">주문해 주셔서 감사합니다. 빠르게 배송해 드리겠습니다.</p>

        <div class="order-info-box">
            <div class="order-number">주문번호: ${order.orderNumber}</div>
            
            <div class="info-row">
                <span class="info-label">주문 상품</span>
                <span class="info-value">
                    <c:choose>
                        <c:when test="${order.orderItems.size() > 1}">
                            ${order.orderItems[0].productName} 외 ${order.orderItems.size() - 1}건
                        </c:when>
                        <c:otherwise>
                            ${order.orderItems[0].productName}
                        </c:otherwise>
                    </c:choose>
                </span>
            </div>
            <div class="info-row">
                <span class="info-label">수령인</span>
                <span class="info-value">${order.receiverName}</span>
            </div>
            <div class="info-row">
                <span class="info-label">배송지</span>
                <span class="info-value">${order.receiverAddress}</span>
            </div>
            <div class="info-row">
                <span class="info-label">결제 방법</span>
                <span class="info-value">${order.paymentMethodName}</span>
            </div>
            <div class="info-row total-row">
                <span>결제 금액</span>
                <span><fmt:formatNumber value="${order.finalPrice}" pattern="#,###"/>원</span>
            </div>
        </div>

        <div class="action-buttons">
            <a href="${pageContext.request.contextPath}/mypage/order/${order.orderId}" class="btn-action btn-secondary">주문 상세보기</a>
            <a href="${pageContext.request.contextPath}/" class="btn-action btn-primary">쇼핑 계속하기</a>
        </div>
    </div>

    <footer class="footer">
        <p>&copy; 2024 KH SHOP. All rights reserved.</p>
    </footer>
</body>
</html>
