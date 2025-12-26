<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>주문 상세 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/main.css">
    <style>
        .order-detail-container {
            max-width: 800px;
            margin: 0 auto;
            padding: 30px 20px;
        }
        .page-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 30px;
        }
        .page-title {
            font-size: 28px;
            font-weight: 700;
        }
        .btn-back {
            padding: 10px 20px;
            background: var(--bg-secondary);
            color: var(--text-primary);
            text-decoration: none;
            border-radius: 6px;
            font-size: 14px;
        }
        
        .detail-section {
            background: white;
            border: 1px solid var(--border-color);
            border-radius: 12px;
            padding: 25px;
            margin-bottom: 20px;
        }
        .section-title {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 20px;
            padding-bottom: 15px;
            border-bottom: 1px solid var(--border-color);
        }
        
        /* 주문 상태 */
        .order-status-bar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 12px;
            color: white;
            margin-bottom: 25px;
        }
        .status-info {
            flex: 1;
        }
        .order-number {
            font-family: monospace;
            font-size: 14px;
            opacity: 0.9;
            margin-bottom: 5px;
        }
        .status-text {
            font-size: 22px;
            font-weight: 700;
        }
        .order-date {
            text-align: right;
            font-size: 14px;
            opacity: 0.9;
        }
        
        /* 정보 그리드 */
        .info-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 15px;
        }
        .info-item {
            padding: 15px;
            background: var(--bg-secondary);
            border-radius: 8px;
        }
        .info-label {
            font-size: 13px;
            color: var(--text-secondary);
            margin-bottom: 5px;
        }
        .info-value {
            font-weight: 600;
        }
        .info-item.full {
            grid-column: span 2;
        }
        
        /* 주문 상품 */
        .order-items {
            display: flex;
            flex-direction: column;
            gap: 15px;
        }
        .order-item {
            display: flex;
            gap: 15px;
            padding: 15px;
            background: var(--bg-secondary);
            border-radius: 8px;
            cursor: pointer;
            transition: transform 0.2s;
        }
        .order-item:hover {
            transform: translateX(5px);
        }
        .item-image {
            width: 80px;
            height: 80px;
            border-radius: 8px;
            overflow: hidden;
            flex-shrink: 0;
        }
        .item-image img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
        .item-info {
            flex: 1;
        }
        .item-name {
            font-weight: 600;
            margin-bottom: 5px;
        }
        .item-price-info {
            font-size: 14px;
            color: var(--text-secondary);
        }
        .item-total {
            text-align: right;
            font-weight: 700;
            font-size: 16px;
        }
        
        /* 금액 요약 */
        .price-summary {
            border-top: 1px solid var(--border-color);
            margin-top: 20px;
            padding-top: 20px;
        }
        .price-row {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
        }
        .price-row.total {
            font-size: 20px;
            font-weight: 700;
            color: var(--btn-primary-bg);
            border-top: 2px solid var(--border-color);
            margin-top: 10px;
            padding-top: 15px;
        }
        
        /* 취소 버튼 */
        .action-buttons {
            display: flex;
            gap: 15px;
            justify-content: center;
            margin-top: 30px;
        }
        .btn-cancel {
            padding: 14px 30px;
            background: white;
            color: #e74c3c;
            border: 2px solid #e74c3c;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 600;
            cursor: pointer;
        }
        .btn-cancel:hover {
            background: #e74c3c;
            color: white;
        }
        
        @media (max-width: 600px) {
            .order-status-bar {
                flex-direction: column;
                text-align: center;
                gap: 10px;
            }
            .order-date {
                text-align: center;
            }
            .info-grid {
                grid-template-columns: 1fr;
            }
            .info-item.full {
                grid-column: span 1;
            }
            .order-item {
                flex-wrap: wrap;
            }
            .item-total {
                width: 100%;
                text-align: left;
                margin-top: 10px;
            }
        }
    </style>
</head>
<body>
    <%@ include file="common/header.jsp" %>

    <div class="order-detail-container">
        <div class="page-header">
            <h1 class="page-title">주문 상세</h1>
            <a href="${pageContext.request.contextPath}/mypage/orders" class="btn-back">← 목록으로</a>
        </div>

        <!-- 주문 상태 -->
        <div class="order-status-bar">
            <div class="status-info">
                <div class="order-number">#${order.orderNumber}</div>
                <div class="status-text">${order.orderStatusName}</div>
            </div>
            <div class="order-date">
                <fmt:parseDate value="${order.createdDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both"/>
                <fmt:formatDate value="${parsedDate}" pattern="yyyy년 MM월 dd일 HH:mm"/>
            </div>
        </div>

        <!-- 배송 정보 -->
        <div class="detail-section">
            <h2 class="section-title">배송 정보</h2>
            <div class="info-grid">
                <div class="info-item">
                    <div class="info-label">수령인</div>
                    <div class="info-value">${order.receiverName}</div>
                </div>
                <div class="info-item">
                    <div class="info-label">연락처</div>
                    <div class="info-value">${order.receiverPhone}</div>
                </div>
                <div class="info-item full">
                    <div class="info-label">배송지</div>
                    <div class="info-value">
                        [${order.postalCode}] ${order.receiverAddress}
                        <c:if test="${not empty order.receiverAddressDetail}">
                            ${order.receiverAddressDetail}
                        </c:if>
                    </div>
                </div>
                <c:if test="${not empty order.orderMemo}">
                    <div class="info-item full">
                        <div class="info-label">배송 메모</div>
                        <div class="info-value">${order.orderMemo}</div>
                    </div>
                </c:if>
            </div>
        </div>

        <!-- 결제 정보 -->
        <div class="detail-section">
            <h2 class="section-title">결제 정보</h2>
            <div class="info-grid">
                <div class="info-item">
                    <div class="info-label">결제 방법</div>
                    <div class="info-value">${order.paymentMethodName}</div>
                </div>
                <div class="info-item">
                    <div class="info-label">결제 상태</div>
                    <div class="info-value">${order.paymentStatusName}</div>
                </div>
            </div>
        </div>

        <!-- 주문 상품 -->
        <div class="detail-section">
            <h2 class="section-title">주문 상품</h2>
            <div class="order-items">
                <c:forEach var="item" items="${order.orderItems}">
                    <div class="order-item" onclick="location.href='${pageContext.request.contextPath}/product/${item.product.productId}'">
                        <div class="item-image">
                            <c:if test="${not empty item.thumbnailUrl}">
                                <img src="${pageContext.request.contextPath}${item.thumbnailUrl}" alt="${item.productName}">
                            </c:if>
                        </div>
                        <div class="item-info">
                            <div class="item-name">${item.productName}</div>
                            <div class="item-price-info">
                                <fmt:formatNumber value="${item.itemPrice}" pattern="#,###"/>원 × ${item.quantity}개
                            </div>
                        </div>
                        <div class="item-total">
                            <fmt:formatNumber value="${item.totalPrice}" pattern="#,###"/>원
                        </div>
                    </div>
                </c:forEach>
            </div>
            
            <div class="price-summary">
                <div class="price-row">
                    <span>상품 금액</span>
                    <span><fmt:formatNumber value="${order.totalPrice}" pattern="#,###"/>원</span>
                </div>
                <c:if test="${order.discountAmount > 0}">
                    <div class="price-row">
                        <span>할인 금액</span>
                        <span style="color: #e74c3c;">-<fmt:formatNumber value="${order.discountAmount}" pattern="#,###"/>원</span>
                    </div>
                </c:if>
                <div class="price-row">
                    <span>배송비</span>
                    <span>
                        <c:choose>
                            <c:when test="${order.deliveryFee == 0}">무료</c:when>
                            <c:otherwise><fmt:formatNumber value="${order.deliveryFee}" pattern="#,###"/>원</c:otherwise>
                        </c:choose>
                    </span>
                </div>
                <div class="price-row total">
                    <span>총 결제 금액</span>
                    <span><fmt:formatNumber value="${order.finalPrice}" pattern="#,###"/>원</span>
                </div>
            </div>
        </div>

        <!-- 취소된 주문인 경우 -->
        <c:if test="${order.orderStatus eq 'CANCELLED'}">
            <div class="detail-section" style="background: #fff5f5; border-color: #f5c6cb;">
                <h2 class="section-title" style="color: #721c24;">취소 정보</h2>
                <div class="info-grid">
                    <div class="info-item" style="background: white;">
                        <div class="info-label">취소 일시</div>
                        <div class="info-value">
                            <fmt:parseDate value="${order.cancelledAt}" pattern="yyyy-MM-dd'T'HH:mm" var="cancelledDate" type="both"/>
                            <fmt:formatDate value="${cancelledDate}" pattern="yyyy년 MM월 dd일 HH:mm"/>
                        </div>
                    </div>
                    <div class="info-item" style="background: white;">
                        <div class="info-label">취소 사유</div>
                        <div class="info-value">${empty order.cancelReason ? '-' : order.cancelReason}</div>
                    </div>
                </div>
            </div>
        </c:if>

        <!-- 주문 취소 버튼 -->
        <c:if test="${order.orderStatus eq 'PENDING' or order.orderStatus eq 'PAID'}">
            <div class="action-buttons">
                <button class="btn-cancel" onclick="cancelOrder()">주문 취소</button>
            </div>
        </c:if>
    </div>

    <footer class="footer">
        <p>&copy; 2024 KH SHOP. All rights reserved.</p>
    </footer>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
        const orderId = ${order.orderId};
        
        function cancelOrder() {
            const reason = prompt('취소 사유를 입력하세요:');
            if (reason === null) return;
            
            fetch(contextPath + '/mypage/order/cancel/' + orderId, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'cancelReason=' + encodeURIComponent(reason)
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert(data.message);
                    location.reload();
                } else {
                    alert(data.message);
                }
            });
        }
    </script>
</body>
</html>
