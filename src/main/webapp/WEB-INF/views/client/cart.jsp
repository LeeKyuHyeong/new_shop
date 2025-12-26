<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>장바구니 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/main.css">
    <style>
        .cart-container {
            max-width: 1000px;
            margin: 0 auto;
            padding: 30px 20px;
        }
        .page-title {
            font-size: 28px;
            font-weight: 700;
            margin-bottom: 30px;
            color: var(--text-primary);
        }
        .cart-content {
            display: flex;
            gap: 30px;
        }
        .cart-items {
            flex: 1;
        }
        .cart-summary {
            width: 320px;
            flex-shrink: 0;
        }
        
        /* 장바구니 헤더 */
        .cart-header {
            display: flex;
            align-items: center;
            padding: 15px;
            background: var(--bg-secondary);
            border-radius: 8px 8px 0 0;
            border: 1px solid var(--border-color);
            border-bottom: none;
        }
        .cart-header label {
            display: flex;
            align-items: center;
            gap: 10px;
            cursor: pointer;
        }
        
        /* 장바구니 아이템 */
        .cart-item {
            display: flex;
            gap: 20px;
            padding: 20px;
            background: white;
            border: 1px solid var(--border-color);
            border-top: none;
        }
        .cart-item:last-child {
            border-radius: 0 0 8px 8px;
        }
        .item-checkbox {
            display: flex;
            align-items: center;
        }
        .item-image {
            width: 100px;
            height: 100px;
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
            font-size: 12px;
        }
        .item-info {
            flex: 1;
        }
        .item-name {
            font-weight: 600;
            margin-bottom: 8px;
            cursor: pointer;
        }
        .item-name:hover {
            color: var(--btn-primary-bg);
        }
        .item-price {
            margin-bottom: 10px;
        }
        .item-original-price {
            text-decoration: line-through;
            color: #999;
            font-size: 13px;
        }
        .item-discount {
            color: #e74c3c;
            font-size: 13px;
            margin-left: 5px;
        }
        .item-final-price {
            font-size: 18px;
            font-weight: 700;
        }
        .quantity-control {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .qty-btn {
            width: 32px;
            height: 32px;
            border: 1px solid var(--border-color);
            background: white;
            cursor: pointer;
            border-radius: 4px;
            font-size: 18px;
        }
        .qty-btn:hover {
            background: var(--bg-secondary);
        }
        .qty-input {
            width: 50px;
            height: 32px;
            text-align: center;
            border: 1px solid var(--border-color);
            border-radius: 4px;
        }
        .item-actions {
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            align-items: flex-end;
        }
        .item-total {
            font-size: 18px;
            font-weight: 700;
        }
        .btn-delete {
            padding: 6px 12px;
            border: 1px solid #e74c3c;
            background: white;
            color: #e74c3c;
            border-radius: 4px;
            cursor: pointer;
            font-size: 13px;
        }
        .btn-delete:hover {
            background: #e74c3c;
            color: white;
        }
        
        /* 요약 박스 */
        .summary-box {
            background: white;
            border: 1px solid var(--border-color);
            border-radius: 8px;
            padding: 25px;
            position: sticky;
            top: 100px;
        }
        .summary-title {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 20px;
            padding-bottom: 15px;
            border-bottom: 1px solid var(--border-color);
        }
        .summary-row {
            display: flex;
            justify-content: space-between;
            padding: 10px 0;
        }
        .summary-row.total {
            font-size: 20px;
            font-weight: 700;
            color: var(--btn-primary-bg);
            border-top: 2px solid var(--border-color);
            margin-top: 15px;
            padding-top: 20px;
        }
        .btn-order {
            width: 100%;
            padding: 16px;
            background: var(--btn-primary-bg);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            margin-top: 20px;
        }
        .btn-order:hover {
            background: var(--btn-primary-hover);
        }
        .btn-order:disabled {
            background: #bdc3c7;
            cursor: not-allowed;
        }
        
        /* 빈 장바구니 */
        .empty-cart {
            text-align: center;
            padding: 60px 20px;
            background: white;
            border-radius: 8px;
            border: 1px solid var(--border-color);
        }
        .empty-cart p {
            color: #999;
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
        
        @media (max-width: 768px) {
            .cart-content {
                flex-direction: column;
            }
            .cart-summary {
                width: 100%;
            }
            .cart-item {
                flex-wrap: wrap;
            }
            .item-actions {
                width: 100%;
                flex-direction: row;
                margin-top: 15px;
            }
        }
    </style>
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

    <script>
        const contextPath = '${pageContext.request.contextPath}';

        function toggleAll() {
            const selectAll = document.getElementById('selectAll');
            document.querySelectorAll('.cart-checkbox').forEach(cb => {
                cb.checked = selectAll.checked;
            });
            calculateTotal();
        }

        document.querySelectorAll('.cart-checkbox').forEach(cb => {
            cb.addEventListener('change', calculateTotal);
        });

        function calculateTotal() {
            let total = 0;
            document.querySelectorAll('.cart-item').forEach(item => {
                const checkbox = item.querySelector('.cart-checkbox');
                if (checkbox.checked) {
                    const price = parseInt(item.dataset.price);
                    const qty = parseInt(item.querySelector('.qty-input').value);
                    total += price * qty;
                }
            });

            const deliveryFee = total >= 50000 ? 0 : (total > 0 ? 3000 : 0);
            const finalPrice = total + deliveryFee;

            document.getElementById('totalPrice').textContent = total.toLocaleString() + '원';
            document.getElementById('deliveryFee').textContent = deliveryFee === 0 ? '무료' : deliveryFee.toLocaleString() + '원';
            document.getElementById('finalPrice').textContent = finalPrice.toLocaleString() + '원';

            // 선택된 상품이 없으면 주문 버튼 비활성화
            const checkedCount = document.querySelectorAll('.cart-checkbox:checked').length;
            document.getElementById('orderBtn').disabled = checkedCount === 0;
        }

        function updateQuantity(cartId, quantity) {
            if (quantity < 1) return;

            fetch(contextPath + '/cart/update', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'cartId=' + cartId + '&quantity=' + quantity
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    const item = document.querySelector('[data-cart-id="' + cartId + '"]');
                    item.querySelector('.qty-input').value = quantity;
                    item.querySelector('.item-total[data-cart-id="' + cartId + '"]').textContent = 
                        data.itemTotal.toLocaleString() + '원';
                    calculateTotal();
                } else {
                    alert(data.message);
                }
            });
        }

        function removeItem(cartId) {
            if (!confirm('삭제하시겠습니까?')) return;

            fetch(contextPath + '/cart/' + cartId, {
                method: 'DELETE'
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    location.reload();
                } else {
                    alert(data.message);
                }
            });
        }

        // 폼 제출 전 선택 확인
        document.getElementById('cartForm')?.addEventListener('submit', function(e) {
            const checked = document.querySelectorAll('.cart-checkbox:checked');
            if (checked.length === 0) {
                e.preventDefault();
                alert('주문할 상품을 선택해주세요.');
            }
        });
    </script>
</body>
</html>
