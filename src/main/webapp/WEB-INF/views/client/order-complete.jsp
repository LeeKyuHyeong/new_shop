<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>주문 완료 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client-main.css">
    <style>
        .complete-container {
            max-width: 700px;
            margin: 0 auto;
            padding: 50px 20px;
            text-align: center;
        }
        .success-icon {
            font-size: 80px;
            margin-bottom: 20px;
        }
        .complete-title {
            font-size: 28px;
            font-weight: 700;
            margin-bottom: 15px;
        }
        .complete-message {
            color: var(--text-secondary);
            margin-bottom: 40px;
        }
        .order-info-box {
            background: white;
            border: 1px solid var(--border-color);
            border-radius: 12px;
            padding: 30px;
            text-align: left;
            margin-bottom: 30px;
        }
        .order-number {
            font-family: monospace;
            font-size: 20px;
            font-weight: 700;
            color: var(--btn-primary-bg);
            margin-bottom: 25px;
            text-align: center;
        }
        .info-row {
            display: flex;
            justify-content: space-between;
            padding: 12px 0;
            border-bottom: 1px solid var(--border-color);
        }
        .info-row:last-child {
            border-bottom: none;
        }
        .info-label {
            color: var(--text-secondary);
        }
        .info-value {
            font-weight: 500;
        }
        .total-row {
            font-size: 18px;
            font-weight: 700;
            color: var(--btn-primary-bg);
            margin-top: 15px;
            padding-top: 15px;
            border-top: 2px solid var(--border-color);
        }
        .action-buttons {
            display: flex;
            gap: 15px;
            justify-content: center;
        }
        .btn-action {
            padding: 14px 30px;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 600;
            text-decoration: none;
            transition: all 0.3s;
        }
        .btn-primary {
            background: var(--btn-primary-bg);
            color: white;
        }
        .btn-primary:hover {
            background: var(--btn-primary-hover);
        }
        .btn-secondary {
            background: white;
            color: var(--text-primary);
            border: 1px solid var(--border-color);
        }
        .btn-secondary:hover {
            background: var(--bg-secondary);
        }
        
        @media (max-width: 480px) {
            .action-buttons {
                flex-direction: column;
            }
            .btn-action {
                width: 100%;
                text-align: center;
            }
        }
    </style>
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
