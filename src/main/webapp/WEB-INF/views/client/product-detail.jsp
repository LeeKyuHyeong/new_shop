<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${product.productName} - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client-main.css">
    <style>
        .product-detail {
            display: flex;
            gap: 50px;
            background: white;
            border-radius: 12px;
            padding: 40px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            margin-bottom: 40px;
        }

        .product-gallery {
            flex: 0 0 500px;
        }

        .main-image {
            width: 100%;
            height: 500px;
            border-radius: 8px;
            overflow: hidden;
            border: 1px solid #eee;
            margin-bottom: 15px;
        }

        .main-image img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        .main-image.no-image {
            display: flex;
            align-items: center;
            justify-content: center;
            background: #f8f9fa;
            color: #999;
            font-size: 18px;
        }

        .thumbnail-list {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }

        .thumb-item {
            width: 80px;
            height: 80px;
            border-radius: 6px;
            overflow: hidden;
            border: 2px solid #eee;
            cursor: pointer;
            transition: border-color 0.3s;
        }

        .thumb-item:hover,
        .thumb-item.active {
            border-color: #3498db;
        }

        .thumb-item img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        .product-info-detail {
            flex: 1;
            display: flex;
            flex-direction: column;
        }

        .product-breadcrumb {
            font-size: 14px;
            color: #7f8c8d;
            margin-bottom: 15px;
        }

        .product-breadcrumb a {
            color: #7f8c8d;
            text-decoration: none;
        }

        .product-breadcrumb a:hover {
            color: #3498db;
        }

        .product-title {
            font-size: 28px;
            font-weight: 700;
            color: #2c3e50;
            margin-bottom: 20px;
        }

        .price-box {
            padding: 25px 0;
            border-top: 1px solid #eee;
            border-bottom: 1px solid #eee;
            margin-bottom: 25px;
        }

        .price-row {
            display: flex;
            align-items: center;
            gap: 15px;
            margin-bottom: 10px;
        }

        .price-label {
            width: 80px;
            color: #7f8c8d;
            font-size: 14px;
        }

        .original-price-large {
            font-size: 18px;
            color: #95a5a6;
            text-decoration: line-through;
        }

        .discount-badge-large {
            background: #e74c3c;
            color: white;
            padding: 4px 10px;
            border-radius: 4px;
            font-size: 14px;
            font-weight: 600;
        }

        .final-price-large {
            font-size: 32px;
            font-weight: 700;
            color: #e74c3c;
        }

        .info-table {
            margin-bottom: 25px;
        }

        .info-row {
            display: flex;
            padding: 12px 0;
            border-bottom: 1px solid #f0f0f0;
        }

        .info-label {
            width: 100px;
            color: #7f8c8d;
            font-size: 14px;
        }

        .info-value {
            flex: 1;
            color: #2c3e50;
            font-size: 14px;
        }

        .stock-status {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 4px;
            font-size: 13px;
            font-weight: 500;
        }

        .stock-status.in-stock {
            background: #d4edda;
            color: #155724;
        }

        .stock-status.out-of-stock {
            background: #f8d7da;
            color: #721c24;
        }

        .action-buttons {
            display: flex;
            gap: 15px;
            margin-top: auto;
        }

        .btn-cart {
            flex: 1;
            padding: 16px;
            background: white;
            color: #2c3e50;
            border: 2px solid #2c3e50;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
        }

        .btn-cart:hover {
            background: #2c3e50;
            color: white;
        }

        .btn-buy {
            flex: 1;
            padding: 16px;
            background: #3498db;
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.3s;
        }

        .btn-buy:hover {
            background: #2980b9;
        }

        .btn-buy:disabled,
        .btn-cart:disabled {
            background: #bdc3c7;
            border-color: #bdc3c7;
            color: white;
            cursor: not-allowed;
        }

        /* 상품 설명 */
        .product-description-section {
            background: white;
            border-radius: 12px;
            padding: 40px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            margin-bottom: 40px;
        }

        .section-title {
            font-size: 20px;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 25px;
            padding-bottom: 15px;
            border-bottom: 2px solid #2c3e50;
        }

        .description-content {
            line-height: 1.8;
            color: #333;
            white-space: pre-wrap;
        }

        .detail-images {
            display: flex;
            flex-direction: column;
            gap: 20px;
        }

        .detail-images img {
            width: 100%;
            max-width: 800px;
            border-radius: 8px;
        }

        /* 관련 상품 */
        .related-section {
            margin-bottom: 40px;
        }

        .related-title {
            font-size: 20px;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 20px;
        }

        /* 반응형 */
        @media (max-width: 900px) {
            .product-detail {
                flex-direction: column;
                padding: 25px;
            }

            .product-gallery {
                flex: none;
                width: 100%;
            }

            .main-image {
                height: 400px;
            }
        }

        @media (max-width: 480px) {
            .product-detail {
                padding: 20px;
            }

            .main-image {
                height: 300px;
            }

            .thumb-item {
                width: 60px;
                height: 60px;
            }

            .product-title {
                font-size: 22px;
            }

            .final-price-large {
                font-size: 26px;
            }

            .action-buttons {
                flex-direction: column;
            }

            .product-description-section {
                padding: 20px;
            }
        }

        /* 수량 선택 */
        .quantity-section {
            display: flex;
            align-items: center;
            gap: 20px;
            padding: 20px 0;
            border-top: 1px solid #eee;
            margin-bottom: 20px;
            flex-wrap: wrap;
        }
        .qty-label {
            font-weight: 600;
            color: #7f8c8d;
        }
        .quantity-control {
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .qty-btn {
            width: 36px;
            height: 36px;
            border: 1px solid #ddd;
            background: white;
            cursor: pointer;
            border-radius: 4px;
            font-size: 18px;
            transition: all 0.2s;
        }
        .qty-btn:hover {
            background: #f8f9fa;
            border-color: #3498db;
        }
        .qty-input {
            width: 60px;
            height: 36px;
            text-align: center;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 16px;
        }
        .qty-total {
            margin-left: auto;
            font-size: 18px;
            color: #2c3e50;
        }
        .qty-total strong {
            font-size: 24px;
            color: #e74c3c;
        }

        @media (max-width: 480px) {
            .quantity-section {
                flex-direction: column;
                align-items: flex-start;
                gap: 15px;
            }
            .qty-total {
                margin-left: 0;
            }
        }
    </style>
</head>
<body>
    <%@ include file="common/header.jsp" %>

    <main class="main-content">
        <!-- 상품 상세 -->
        <div class="product-detail">
            <!-- 이미지 갤러리 -->
            <div class="product-gallery">
                <div class="main-image <c:if test="${empty product.thumbnailUrl}">no-image</c:if>" id="mainImage">
                    <c:choose>
                        <c:when test="${not empty product.thumbnailUrl}">
                            <img src="${pageContext.request.contextPath}${product.thumbnailUrl}" alt="${product.productName}" id="mainImg">
                        </c:when>
                        <c:otherwise>
                            이미지 없음
                        </c:otherwise>
                    </c:choose>
                </div>
                
                <c:if test="${not empty product.images || not empty product.thumbnailUrl}">
                    <div class="thumbnail-list">
                        <c:if test="${not empty product.thumbnailUrl}">
                            <div class="thumb-item active" onclick="changeImage('${pageContext.request.contextPath}${product.thumbnailUrl}', this)">
                                <img src="${pageContext.request.contextPath}${product.thumbnailUrl}" alt="썸네일">
                            </div>
                        </c:if>
                        <c:forEach var="image" items="${product.images}">
                            <div class="thumb-item" onclick="changeImage('${pageContext.request.contextPath}${image.imageUrl}', this)">
                                <img src="${pageContext.request.contextPath}${image.imageUrl}" alt="상세이미지">
                            </div>
                        </c:forEach>
                    </div>
                </c:if>
            </div>

            <!-- 상품 정보 -->
            <div class="product-info-detail">
                <div class="product-breadcrumb">
                    <a href="${pageContext.request.contextPath}/">홈</a>
                    <span> › </span>
                    <c:if test="${not empty product.category}">
                        <c:if test="${not empty product.category.parent}">
                            <a href="${pageContext.request.contextPath}/category/${product.category.parent.categoryId}">
                                ${product.category.parent.categoryName}
                            </a>
                            <span> › </span>
                        </c:if>
                        <a href="${pageContext.request.contextPath}/category/${product.category.categoryId}">
                            ${product.category.categoryName}
                        </a>
                    </c:if>
                </div>

                <h1 class="product-title">${product.productName}</h1>

                <div class="price-box">
                    <c:if test="${product.productDiscount > 0}">
                        <div class="price-row">
                            <span class="price-label">정가</span>
                            <span class="original-price-large"><fmt:formatNumber value="${product.productPrice}" pattern="#,###"/>원</span>
                            <span class="discount-badge-large">${product.productDiscount}% OFF</span>
                        </div>
                    </c:if>
                    <div class="price-row">
                        <span class="price-label">판매가</span>
                        <span class="final-price-large"><fmt:formatNumber value="${product.discountedPrice}" pattern="#,###"/>원</span>
                    </div>
                </div>

                <div class="info-table">
                    <div class="info-row">
                        <span class="info-label">재고</span>
                        <span class="info-value">
                            <c:choose>
                                <c:when test="${product.productStock > 0}">
                                    <span class="stock-status in-stock">재고 ${product.productStock}개</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="stock-status out-of-stock">품절</span>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">배송</span>
                        <span class="info-value">무료배송</span>
                    </div>
                </div>

                <!-- 수량 선택 -->
                <c:if test="${product.productStock > 0}">
                    <div class="quantity-section">
                        <span class="qty-label">수량</span>
                        <div class="quantity-control">
                            <button type="button" class="qty-btn" onclick="changeQty(-1)">−</button>
                            <input type="number" id="quantity" class="qty-input" value="1" min="1" max="${product.productStock}">
                            <button type="button" class="qty-btn" onclick="changeQty(1)">+</button>
                        </div>
                        <div class="qty-total">
                            총 상품금액: <strong id="totalPriceDisplay"><fmt:formatNumber value="${product.discountedPrice}" pattern="#,###"/></strong>원
                        </div>
                    </div>
                </c:if>

                <div class="action-buttons">
                    <button class="btn-cart" onclick="addToCart()" <c:if test="${product.productStock == 0}">disabled</c:if>>
                        🛒 장바구니
                    </button>
                    <button class="btn-buy" onclick="buyNow()" <c:if test="${product.productStock == 0}">disabled</c:if>>
                        바로구매
                    </button>
                </div>
            </div>
        </div>

        <!-- 상품 설명 -->
        <div class="product-description-section">
            <h2 class="section-title">상품 설명</h2>
            <c:choose>
                <c:when test="${not empty product.productDescription}">
                    <div class="description-content">${product.productDescription}</div>
                </c:when>
                <c:otherwise>
                    <p style="color: #999;">상품 설명이 없습니다.</p>
                </c:otherwise>
            </c:choose>

            <c:if test="${not empty product.images}">
                <div class="detail-images" style="margin-top: 30px;">
                    <c:forEach var="image" items="${product.images}">
                        <img src="${pageContext.request.contextPath}${image.imageUrl}" alt="상세이미지">
                    </c:forEach>
                </div>
            </c:if>
        </div>

        <!-- 관련 상품 -->
        <c:if test="${not empty relatedProducts}">
            <section class="related-section">
                <h2 class="related-title">관련 상품</h2>
                <div class="product-grid">
                    <c:forEach var="product" items="${relatedProducts}">
                        <div class="product-card" onclick="location.href='${pageContext.request.contextPath}/product/${product.productId}'">
                            <div class="product-image <c:if test="${empty product.thumbnailUrl}">no-image</c:if>">
                                <c:choose>
                                    <c:when test="${not empty product.thumbnailUrl}">
                                        <img src="${pageContext.request.contextPath}${product.thumbnailUrl}" alt="${product.productName}">
                                    </c:when>
                                    <c:otherwise>
                                        이미지 없음
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="product-info">
                                <div class="product-name">${product.productName}</div>
                                <div class="product-price">
                                    <c:if test="${product.productDiscount > 0}">
                                        <span class="original-price"><fmt:formatNumber value="${product.productPrice}" pattern="#,###"/>원</span>
                                        <span class="discount-badge">${product.productDiscount}%</span>
                                    </c:if>
                                    <span class="final-price"><fmt:formatNumber value="${product.discountedPrice}" pattern="#,###"/>원</span>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </section>
        </c:if>
    </main>

    <footer class="footer">
        <p>&copy; 2024 KH SHOP. All rights reserved.</p>
    </footer>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
        const productId = ${product.productId};
        const productPrice = ${product.discountedPrice};
        const maxStock = ${product.productStock};

        function changeImage(src, element) {
            document.getElementById('mainImg').src = src;
            document.querySelectorAll('.thumb-item').forEach(item => item.classList.remove('active'));
            element.classList.add('active');
        }

        // 수량 변경
        function changeQty(delta) {
            const qtyInput = document.getElementById('quantity');
            let qty = parseInt(qtyInput.value) + delta;
            if (qty < 1) qty = 1;
            if (qty > maxStock) qty = maxStock;
            qtyInput.value = qty;
            updateTotalPrice();
        }

        // 총 금액 업데이트
        function updateTotalPrice() {
            const qty = parseInt(document.getElementById('quantity').value);
            const total = productPrice * qty;
            document.getElementById('totalPriceDisplay').textContent = total.toLocaleString();
        }

        // 수량 입력 이벤트
        document.getElementById('quantity')?.addEventListener('change', function() {
            let qty = parseInt(this.value);
            if (isNaN(qty) || qty < 1) qty = 1;
            if (qty > maxStock) qty = maxStock;
            this.value = qty;
            updateTotalPrice();
        });

        // 장바구니 추가
        function addToCart() {
            const qty = parseInt(document.getElementById('quantity')?.value || 1);
            
            fetch(contextPath + '/cart/add', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'productId=' + productId + '&quantity=' + qty
            })
            .then(response => response.json())
            .then(data => {
                if (data.needLogin) {
                    if (confirm('로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?')) {
                        location.href = contextPath + '/login?redirect=/product/' + productId;
                    }
                } else if (data.success) {
                    // 장바구니 카운트 업데이트
                    const countEl = document.getElementById('cartCount');
                    if (countEl) {
                        countEl.textContent = data.cartCount;
                        countEl.style.display = 'inline-flex';
                    }
                    
                    if (confirm('장바구니에 추가되었습니다.\n장바구니로 이동하시겠습니까?')) {
                        location.href = contextPath + '/cart';
                    }
                } else {
                    alert(data.message);
                }
            })
            .catch(error => {
                alert('오류가 발생했습니다.');
            });
        }

        // 바로구매
        function buyNow() {
            const qty = parseInt(document.getElementById('quantity')?.value || 1);
            location.href = contextPath + '/order/direct?productId=' + productId + '&quantity=' + qty;
        }
    </script>
</body>
</html>
