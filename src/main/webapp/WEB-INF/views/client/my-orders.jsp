<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>주문 내역 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/mypage.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/my-orders.css">
</head>
<body>
    <%@ include file="common/header.jsp" %>

    <div class="mypage-container">
        <div class="mypage-sidebar">
            <h3>마이페이지</h3>
            <nav class="mypage-nav">
                <a href="${pageContext.request.contextPath}/mypage/orders" class="active">주문내역</a>
                <a href="${pageContext.request.contextPath}/mypage/setting">개인설정</a>
            </nav>
        </div>

        <div class="mypage-content">
            <h2 class="mypage-title">주문 내역</h2>

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
    </div>

    <footer class="footer">
        <p>&copy; 2024 KH SHOP. All rights reserved.</p>
    </footer>

    <script>const contextPath = '${pageContext.request.contextPath}';</script>
    <script src="${pageContext.request.contextPath}/js/client/my-orders.js"></script>
</body>
</html>
