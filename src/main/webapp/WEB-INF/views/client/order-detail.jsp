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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/order-detail.css">
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
    </script>
    <script src="${pageContext.request.contextPath}/js/client/order-detail.js"></script>
</body>
</html>
