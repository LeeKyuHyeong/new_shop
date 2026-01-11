<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%@ include file="/WEB-INF/views/common/security-headers.jsp" %>
    <title>장바구니 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/cart.css">
</head>
<body>
    <%@ include file="common/header.jsp" %>

    <div class="cart-container">
        <h1 class="page-title">장바구니</h1>

        <c:choose>
            <c:when test="${not empty cartItems}">
                <form id="cartForm" action="${pageContext.request.contextPath}/order/checkout" method="post">
                    <div class="cart-content">
                        <div class="cart-items">
                            <div class="cart-header">
                                <label>
                                    <input type="checkbox" id="selectAll" checked onchange="toggleAll()">
                                    <span>전체 선택</span>
                                </label>
                            </div>
                            
                            <c:forEach var="cart" items="${cartItems}">
                                <div class="cart-item" data-cart-id="${cart.cartId}" data-price="${cart.product.discountedPrice}">
                                    <div class="item-checkbox">
                                        <input type="checkbox" name="cartIds" value="${cart.cartId}" class="cart-checkbox" checked>
                                    </div>
                                    <div class="item-image <c:if test="${empty cart.product.thumbnailUrl}">no-image</c:if>">
                                        <c:choose>
                                            <c:when test="${not empty cart.product.thumbnailUrl}">
                                                <img src="${pageContext.request.contextPath}${cart.product.thumbnailUrl}" alt="${cart.product.productName}">
                                            </c:when>
                                            <c:otherwise>
                                                이미지없음
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="item-info">
                                        <div class="item-name" onclick="location.href='${pageContext.request.contextPath}/product/${cart.product.productId}'">
                                            ${cart.product.productName}
                                        </div>
                                        <!-- 옵션 표시 -->
                                        <c:if test="${not empty cart.optionText}">
                                            <div class="item-option">
                                                옵션: ${cart.optionText}
                                            </div>
                                        </c:if>
                                        <div class="item-price">
                                            <c:if test="${cart.product.productDiscount > 0}">
                                                <span class="item-original-price"><fmt:formatNumber value="${cart.product.productPrice}" pattern="#,###"/>원</span>
                                                <span class="item-discount">${cart.product.productDiscount}%</span>
                                            </c:if>
                                            <div class="item-final-price"><fmt:formatNumber value="${cart.product.discountedPrice}" pattern="#,###"/>원</div>
                                        </div>
                                        <div class="quantity-control">
                                            <button type="button" class="qty-btn" onclick="updateQuantity(${cart.cartId}, ${cart.quantity - 1})">−</button>
                                            <input type="number" class="qty-input" value="${cart.quantity}" min="1" max="${cart.product.productStock}" 
                                                   onchange="updateQuantity(${cart.cartId}, this.value)" data-cart-id="${cart.cartId}">
                                            <button type="button" class="qty-btn" onclick="updateQuantity(${cart.cartId}, ${cart.quantity + 1})">+</button>
                                        </div>
                                    </div>
                                    <div class="item-actions">
                                        <div class="item-total" data-cart-id="${cart.cartId}">
                                            <fmt:formatNumber value="${cart.totalPrice}" pattern="#,###"/>원
                                        </div>
                                        <button type="button" class="btn-delete" onclick="removeItem(${cart.cartId})">삭제</button>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>

                        <div class="cart-summary">
                            <div class="summary-box">
                                <h3 class="summary-title">결제 예정 금액</h3>
                                <div class="summary-row">
                                    <span>상품 금액</span>
                                    <span id="totalPrice"><fmt:formatNumber value="${totalPrice}" pattern="#,###"/>원</span>
                                </div>
                                <div class="summary-row">
                                    <span>배송비</span>
                                    <span id="deliveryFee">
                                        <c:choose>
                                            <c:when test="${deliveryFee == 0}">무료</c:when>
                                            <c:otherwise><fmt:formatNumber value="${deliveryFee}" pattern="#,###"/>원</c:otherwise>
                                        </c:choose>
                                    </span>
                                </div>
                                <div class="summary-row total">
                                    <span>합계</span>
                                    <span id="finalPrice"><fmt:formatNumber value="${finalPrice}" pattern="#,###"/>원</span>
                                </div>
                                <button type="submit" class="btn-order" id="orderBtn">주문하기</button>
                            </div>
                        </div>
                    </div>
                </form>
            </c:when>
            <c:otherwise>
                <div class="empty-cart">
                    <p>장바구니가 비어있습니다.</p>
                    <a href="${pageContext.request.contextPath}/" class="btn-shop">쇼핑 계속하기</a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <footer class="footer">
        <p>&copy; 2024 KH SHOP. All rights reserved.</p>
    </footer>

    <script>const contextPath = '${pageContext.request.contextPath}';</script>
    <script src="${pageContext.request.contextPath}/js/client/cart.js"></script>
</body>
</html>
