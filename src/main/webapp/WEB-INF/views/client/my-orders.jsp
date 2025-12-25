<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>주문 내역 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client-main.css">
    <style>
        .orders-container {
            max-width: 900px;
            margin: 0 auto;
            padding: 30px 20px;
        }
        .page-title {
            font-size: 28px;
            font-weight: 700;
            margin-bottom: 30px;
        }
        .order-list {
            display: flex;
            flex-direction: column;
            gap: 20px;
        }
        .order-card {
            background: white;
            border: 1px solid var(--border-color);
            border-radius: 12px;
            overflow: hidden;
        }
        .order-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 20px;
            background: var(--bg-secondary);
            border-bottom: 1px solid var(--border-color);
        }
        .order-date {
            font-weight: 600;
        }
        .order-number {
            font-family: monospace;
            color: var(--text-secondary);
            font-size: 14px;
        }
        .order-status {
            padding: 6px 14px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: 600;
        }
        .status-pending { background: #fef3cd; color: #856404; }
        .status-paid { background: #cce5ff; color: #004085; }
        .status-preparing { background: #e2d5f1; color: #5e35b1; }
        .status-shipping { background: #d4edda; color: #155724; }
        .status-delivered { background: #d4edda; color: #155724; }
        .status-cancelled { background: #f8d7da; color: #721c24; }
        
        .order-body {
            padding: 20px;
        }
        .order-items {
            display: flex;
            flex-direction: column;
            gap: 15px;
        }
        .order-item {
            display: flex;
            gap: 15px;
            cursor: pointer;
        }
        .order-item:hover .item-name {
            color: var(--btn-primary-bg);
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
        .item-image.no-image {
            display: flex;
            align-items: center;
            justify-content: center;
            background: #f8f9fa;
            color: #999;
            font-size: 11px;
        }
        .item-info {
            flex: 1;
        }
        .item-name {
            font-weight: 600;
            margin-bottom: 5px;
            transition: color 0.3s;
        }
        .item-quantity {
            font-size: 14px;
            color: var(--text-secondary);
        }
        .item-price {
            font-weight: 600;
            text-align: right;
        }
        
        .order-footer {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 20px;
            border-top: 1px solid var(--border-color);
            background: var(--bg-secondary);
        }
        .order-total {
            font-size: 18px;
            font-weight: 700;
        }
        .order-actions {
            display: flex;
            gap: 10px;
        }
        .btn-detail {
            padding: 10px 20px;
            background: var(--btn-primary-bg);
            color: white;
            border: none;
            border-radius: 6px;
            text-decoration: none;
            font-size: 14px;
            font-weight: 600;
        }
        .btn-cancel {
            padding: 10px 20px;
            background: white;
            color: #e74c3c;
            border: 1px solid #e74c3c;
            border-radius: 6px;
            cursor: pointer;
            font-size: 14px;
        }
        .btn-cancel:hover {
            background: #e74c3c;
            color: white;
        }
        
        .empty-orders {
            text-align: center;
            padding: 60px 20px;
            background: white;
            border-radius: 12px;
            border: 1px solid var(--border-color);
        }
        .empty-orders p {
            color: var(--text-secondary);
            margin-bottom: 20px;
        }
        .btn-shop {
            display: inline-block;
            padding: 12px 24px;
            background: var(--btn-primary-bg);
            color: white;
            text-decoration: none;
            border-radius: 8px;
        }
        
        @media (max-width: 600px) {
            .order-header {
                flex-direction: column;
                gap: 10px;
                align-items: flex-start;
            }
            .order-footer {
                flex-direction: column;
                gap: 15px;
                align-items: stretch;
            }
            .order-actions {
                justify-content: center;
            }
        }
    </style>
</head>
<body>
    <%@ include file="common/header.jsp" %>

    <div class="orders-container">
        <h1 class="page-title">주문 내역</h1>

        <c:choose>
            <c:when test="${not empty orders}">
                <div class="order-list">
                    <c:forEach var="order" items="${orders}">
                        <div class="order-card">
                            <div class="order-header">
                                <div>
                                    <div class="order-date">
                                        <fmt:parseDate value="${order.createdDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both"/>
                                        <fmt:formatDate value="${parsedDate}" pattern="yyyy.MM.dd"/>
                                    </div>
                                    <div class="order-number">#${order.orderNumber}</div>
                                </div>
                                <span class="order-status status-${order.orderStatus.toLowerCase()}">${order.orderStatusName}</span>
                            </div>
                            
                            <div class="order-body">
                                <div class="order-items">
                                    <c:forEach var="item" items="${order.orderItems}" varStatus="status">
                                        <c:if test="${status.index < 2}">
                                            <div class="order-item" onclick="location.href='${pageContext.request.contextPath}/product/${item.product.productId}'">
                                                <div class="item-image <c:if test="${empty item.thumbnailUrl}">no-image</c:if>">
                                                    <c:choose>
                                                        <c:when test="${not empty item.thumbnailUrl}">
                                                            <img src="${pageContext.request.contextPath}${item.thumbnailUrl}" alt="${item.productName}">
                                                        </c:when>
                                                        <c:otherwise>
                                                            이미지없음
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <div class="item-info">
                                                    <div class="item-name">${item.productName}</div>
                                                    <div class="item-quantity">${item.quantity}개</div>
                                                </div>
                                                <div class="item-price">
                                                    <fmt:formatNumber value="${item.totalPrice}" pattern="#,###"/>원
                                                </div>
                                            </div>
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${order.orderItems.size() > 2}">
                                        <div style="text-align: center; color: var(--text-secondary); font-size: 14px;">
                                            외 ${order.orderItems.size() - 2}개 상품
                                        </div>
                                    </c:if>
                                </div>
                            </div>
                            
                            <div class="order-footer">
                                <div class="order-total">
                                    총 <fmt:formatNumber value="${order.finalPrice}" pattern="#,###"/>원
                                </div>
                                <div class="order-actions">
                                    <a href="${pageContext.request.contextPath}/mypage/order/${order.orderId}" class="btn-detail">상세보기</a>
                                    <c:if test="${order.orderStatus eq 'PENDING' or order.orderStatus eq 'PAID'}">
                                        <button class="btn-cancel" onclick="cancelOrder(${order.orderId})">주문취소</button>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-orders">
                    <p>주문 내역이 없습니다.</p>
                    <a href="${pageContext.request.contextPath}/" class="btn-shop">쇼핑 시작하기</a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <footer class="footer">
        <p>&copy; 2024 KH SHOP. All rights reserved.</p>
    </footer>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
        
        function cancelOrder(orderId) {
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
