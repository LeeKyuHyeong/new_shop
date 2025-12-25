<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>주문 상세 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <style>
        .order-detail-container {
            display: flex;
            flex-direction: column;
            gap: 25px;
            max-width: 1000px;
        }
        .detail-card {
            background: var(--bg-primary);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            padding: 25px;
        }
        .card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            padding-bottom: 15px;
            border-bottom: 1px solid var(--border-color);
        }
        .card-title {
            font-size: 18px;
            font-weight: 600;
            color: var(--text-primary);
        }
        .order-number {
            font-family: monospace;
            font-size: 16px;
            color: var(--text-secondary);
        }
        .info-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 20px;
        }
        .info-item {
            display: flex;
            flex-direction: column;
            gap: 5px;
        }
        .info-label {
            font-size: 13px;
            color: var(--text-secondary);
        }
        .info-value {
            font-size: 15px;
            color: var(--text-primary);
            font-weight: 500;
        }
        .status-badge {
            display: inline-block;
            padding: 6px 14px;
            border-radius: 4px;
            font-size: 13px;
            font-weight: 600;
        }
        .status-pending { background: #f39c12; color: white; }
        .status-paid { background: #3498db; color: white; }
        .status-preparing { background: #9b59b6; color: white; }
        .status-shipping { background: #1abc9c; color: white; }
        .status-delivered { background: #27ae60; color: white; }
        .status-cancelled { background: #e74c3c; color: white; }
        .status-refunded { background: #95a5a6; color: white; }
        
        /* 상품 목록 */
        .item-list {
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
        }
        .item-image {
            width: 80px;
            height: 80px;
            border-radius: 6px;
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
            background: #eee;
            color: #999;
            font-size: 11px;
        }
        .item-info {
            flex: 1;
        }
        .item-name {
            font-weight: 600;
            margin-bottom: 5px;
        }
        .item-price-info {
            font-size: 13px;
            color: var(--text-secondary);
        }
        .item-total {
            text-align: right;
            font-weight: 600;
            font-size: 15px;
        }
        
        /* 금액 요약 */
        .price-summary {
            margin-top: 20px;
            padding-top: 20px;
            border-top: 1px solid var(--border-color);
        }
        .price-row {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
        }
        .price-row.total {
            font-size: 18px;
            font-weight: 700;
            color: var(--btn-primary-bg);
            border-top: 2px solid var(--border-color);
            margin-top: 10px;
            padding-top: 15px;
        }
        
        /* 상태 변경 */
        .status-change {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }
        .status-btn {
            padding: 8px 16px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 13px;
            font-weight: 500;
            transition: opacity 0.3s;
        }
        .status-btn:hover {
            opacity: 0.8;
        }
        .status-btn.paid { background: #3498db; color: white; }
        .status-btn.preparing { background: #9b59b6; color: white; }
        .status-btn.shipping { background: #1abc9c; color: white; }
        .status-btn.delivered { background: #27ae60; color: white; }
        .status-btn.cancelled { background: #e74c3c; color: white; }
        
        .action-buttons {
            display: flex;
            gap: 10px;
            margin-top: 20px;
        }
        
        @media (max-width: 768px) {
            .info-grid {
                grid-template-columns: 1fr;
            }
            .order-item {
                flex-direction: column;
            }
            .item-total {
                text-align: left;
                margin-top: 10px;
            }
        }
    </style>
</head>
<body>
    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/common/sidebar.jsp"/>

        <main class="main-content">
            <header class="top-bar">
                <h1>주문 상세</h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <div class="alert-container"></div>

                <div class="order-detail-container">
                    <!-- 주문 정보 -->
                    <div class="detail-card">
                        <div class="card-header">
                            <h2 class="card-title">주문 정보</h2>
                            <span class="order-number">#${order.orderNumber}</span>
                        </div>
                        <div class="info-grid">
                            <div class="info-item">
                                <span class="info-label">주문 상태</span>
                                <span class="info-value">
                                    <span class="status-badge status-${order.orderStatus.toLowerCase()}">${order.orderStatusName}</span>
                                </span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">결제 상태</span>
                                <span class="info-value">${order.paymentStatusName}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">주문 일시</span>
                                <span class="info-value">
                                    <fmt:parseDate value="${order.createdDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both"/>
                                    <fmt:formatDate value="${parsedDate}" pattern="yyyy년 MM월 dd일 HH:mm"/>
                                </span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">결제 방법</span>
                                <span class="info-value">${order.paymentMethodName}</span>
                            </div>
                            <c:if test="${order.orderStatus eq 'CANCELLED'}">
                                <div class="info-item">
                                    <span class="info-label">취소 일시</span>
                                    <span class="info-value">
                                        <fmt:parseDate value="${order.cancelledAt}" pattern="yyyy-MM-dd'T'HH:mm" var="cancelledDate" type="both"/>
                                        <fmt:formatDate value="${cancelledDate}" pattern="yyyy년 MM월 dd일 HH:mm"/>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">취소 사유</span>
                                    <span class="info-value">${empty order.cancelReason ? '-' : order.cancelReason}</span>
                                </div>
                            </c:if>
                        </div>
                        
                        <!-- 상태 변경 버튼 -->
                        <c:if test="${order.orderStatus ne 'CANCELLED' and order.orderStatus ne 'DELIVERED'}">
                            <div style="margin-top: 20px; padding-top: 20px; border-top: 1px solid var(--border-color);">
                                <span class="info-label" style="margin-bottom: 10px; display: block;">상태 변경</span>
                                <div class="status-change">
                                    <c:if test="${order.orderStatus eq 'PENDING'}">
                                        <button class="status-btn paid" onclick="changeStatus('PAID')">결제완료</button>
                                    </c:if>
                                    <c:if test="${order.orderStatus eq 'PAID'}">
                                        <button class="status-btn preparing" onclick="changeStatus('PREPARING')">상품준비중</button>
                                    </c:if>
                                    <c:if test="${order.orderStatus eq 'PREPARING'}">
                                        <button class="status-btn shipping" onclick="changeStatus('SHIPPING')">배송시작</button>
                                    </c:if>
                                    <c:if test="${order.orderStatus eq 'SHIPPING'}">
                                        <button class="status-btn delivered" onclick="changeStatus('DELIVERED')">배송완료</button>
                                    </c:if>
                                    <button class="status-btn cancelled" onclick="cancelOrder()">주문취소</button>
                                </div>
                            </div>
                        </c:if>
                    </div>

                    <!-- 주문자 정보 -->
                    <div class="detail-card">
                        <div class="card-header">
                            <h2 class="card-title">주문자 정보</h2>
                        </div>
                        <div class="info-grid">
                            <div class="info-item">
                                <span class="info-label">주문자</span>
                                <span class="info-value">${order.user.userName} (${order.user.userId})</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">이메일</span>
                                <span class="info-value">${order.user.email}</span>
                            </div>
                        </div>
                    </div>

                    <!-- 배송 정보 -->
                    <div class="detail-card">
                        <div class="card-header">
                            <h2 class="card-title">배송 정보</h2>
                        </div>
                        <div class="info-grid">
                            <div class="info-item">
                                <span class="info-label">수령인</span>
                                <span class="info-value">${order.receiverName}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">연락처</span>
                                <span class="info-value">${order.receiverPhone}</span>
                            </div>
                            <div class="info-item" style="grid-column: span 2;">
                                <span class="info-label">배송지</span>
                                <span class="info-value">
                                    [${order.postalCode}] ${order.receiverAddress}
                                    <c:if test="${not empty order.receiverAddressDetail}">
                                        ${order.receiverAddressDetail}
                                    </c:if>
                                </span>
                            </div>
                            <c:if test="${not empty order.orderMemo}">
                                <div class="info-item" style="grid-column: span 2;">
                                    <span class="info-label">배송 메모</span>
                                    <span class="info-value">${order.orderMemo}</span>
                                </div>
                            </c:if>
                        </div>
                    </div>

                    <!-- 주문 상품 -->
                    <div class="detail-card">
                        <div class="card-header">
                            <h2 class="card-title">주문 상품</h2>
                        </div>
                        <div class="item-list">
                            <c:forEach var="item" items="${order.orderItems}">
                                <div class="order-item">
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
                                        <div class="item-price-info">
                                            <c:if test="${item.productDiscount > 0}">
                                                <span style="text-decoration: line-through;"><fmt:formatNumber value="${item.productPrice}" pattern="#,###"/>원</span>
                                                <span style="color: #e74c3c;">${item.productDiscount}%</span>
                                                →
                                            </c:if>
                                            <fmt:formatNumber value="${item.itemPrice}" pattern="#,###"/>원 × ${item.quantity}개
                                        </div>
                                    </div>
                                    <div class="item-total">
                                        <fmt:formatNumber value="${item.totalPrice}" pattern="#,###"/>원
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                        
                        <!-- 금액 요약 -->
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

                    <div class="action-buttons">
                        <a href="${pageContext.request.contextPath}/admin/order" class="btn btn-secondary">목록으로</a>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
        const orderId = ${order.orderId};
        
        function changeStatus(status) {
            if (!confirm('주문 상태를 변경하시겠습니까?')) return;
            
            fetch(contextPath + '/admin/order/status/' + orderId, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'status=' + status
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert(data.message);
                    location.reload();
                } else {
                    alert(data.message);
                }
            })
            .catch(error => {
                alert('오류가 발생했습니다.');
            });
        }
        
        function cancelOrder() {
            const reason = prompt('취소 사유를 입력하세요:');
            if (reason === null) return;
            
            fetch(contextPath + '/admin/order/cancel/' + orderId, {
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
            })
            .catch(error => {
                alert('오류가 발생했습니다.');
            });
        }
    </script>
    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>
