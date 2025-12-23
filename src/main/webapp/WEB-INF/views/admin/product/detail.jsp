<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>상품 상세 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-product.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-product-detail.css">
</head>
<body>
    <div class="theme-toggle">
        <button id="themeBtn" onclick="toggleTheme()">🌙</button>
    </div>

    <div class="admin-container">
        <aside class="sidebar">
            <div class="sidebar-header">
                <h2>KH SHOP Admin</h2>
            </div>

            <nav class="sidebar-menu">
                <a href="${pageContext.request.contextPath}/admin" class="menu-item">대시보드</a>
                <a href="${pageContext.request.contextPath}/admin/category" class="menu-item">카테고리 관리</a>
                <a href="${pageContext.request.contextPath}/admin/product" class="menu-item active">상품 관리</a>
                <a href="${pageContext.request.contextPath}/admin/order" class="menu-item">주문 관리</a>
                <a href="${pageContext.request.contextPath}/logout" class="menu-item logout">로그아웃</a>
            </nav>
        </aside>

        <main class="main-content">
            <header class="top-bar">
                <h1>상품 상세</h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <div class="alert-container"></div>

                <div class="detail-container">
                    <!-- 상단 영역: 이미지 + 기본 정보 -->
                    <div class="detail-top">
                        <div class="detail-images">
                            <div class="main-image">
                                <c:if test="${not empty product.thumbnailUrl}">
                                    <img id="mainImage" src="${pageContext.request.contextPath}${product.thumbnailUrl}" alt="${product.productName}">
                                </c:if>
                                <c:if test="${empty product.thumbnailUrl}">
                                    <div class="no-image-large">No Image</div>
                                </c:if>
                            </div>
                            <div class="image-thumbnails">
                                <c:if test="${not empty product.thumbnailUrl}">
                                    <div class="thumb-item active" onclick="changeMainImage('${pageContext.request.contextPath}${product.thumbnailUrl}', this)">
                                        <img src="${pageContext.request.contextPath}${product.thumbnailUrl}" alt="썸네일">
                                    </div>
                                </c:if>
                                <c:forEach var="image" items="${product.images}">
                                    <c:if test="${image.useYn eq 'Y'}">
                                        <div class="thumb-item" onclick="changeMainImage('${pageContext.request.contextPath}${image.imageUrl}', this)">
                                            <img src="${pageContext.request.contextPath}${image.imageUrl}" alt="상세이미지">
                                        </div>
                                    </c:if>
                                </c:forEach>
                            </div>
                        </div>

                        <div class="detail-info">
                            <div class="info-header">
                                <span class="product-id">#${product.productId}</span>
                                <c:if test="${product.productStock == 0}">
                                    <span class="badge badge-danger">품절</span>
                                </c:if>
                                <c:if test="${product.productStock > 0 && product.productStock <= 10}">
                                    <span class="badge badge-warning">재고부족</span>
                                </c:if>
                            </div>

                            <h2 class="product-name">${product.productName}</h2>

                            <div class="category-info">
                                <c:if test="${not empty product.category}">
                                    <c:if test="${not empty product.category.parent}">
                                        <span class="category-tag">${product.category.parent.categoryName}</span>
                                        <span class="category-arrow">›</span>
                                    </c:if>
                                    <span class="category-tag">${product.category.categoryName}</span>
                                </c:if>
                                <c:if test="${empty product.category}">
                                    <span class="category-tag none">카테고리 없음</span>
                                </c:if>
                            </div>

                            <div class="price-section">
                                <c:if test="${product.productDiscount > 0}">
                                    <span class="original-price"><fmt:formatNumber value="${product.productPrice}" type="number"/>원</span>
                                    <span class="discount-rate">${product.productDiscount}%</span>
                                </c:if>
                                <span class="final-price"><fmt:formatNumber value="${product.discountedPrice}" type="number"/>원</span>
                            </div>

                            <div class="info-table">
                                <div class="info-row">
                                    <span class="info-label">재고</span>
                                    <span class="info-value">${product.productStock}개</span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">정렬 순서</span>
                                    <span class="info-value">${product.productOrder}</span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">등록일</span>
                                    <span class="info-value">
                                        <fmt:parseDate value="${product.createdDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedCreatedDate" type="both"/>
                                        "${parsedCreatedDate}"
                                    </span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">수정일</span>
                                    <span class="info-value">
                                        <fmt:parseDate value="${product.updatedDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedUpdatedDate" type="both"/>
                                        "${parsedUpdatedDate}"
                                    </span>
                                </div>
                            </div>

                            <div class="action-buttons">
                                <a href="${pageContext.request.contextPath}/admin/product/edit/${product.productId}" class="btn btn-primary">수정</a>
                                <button class="btn btn-danger" onclick="deleteProduct(${product.productId})">삭제</button>
                                <a href="${pageContext.request.contextPath}/admin/product" class="btn btn-secondary">목록</a>
                            </div>
                        </div>
                    </div>

                    <!-- 하단 영역: 상세 설명 -->
                    <div class="detail-bottom">
                        <div class="section-title">
                            <h3>상품 설명</h3>
                        </div>
                        <div class="description-content">
                            <c:if test="${not empty product.productDescription}">
                                <pre>${product.productDescription}</pre>
                            </c:if>
                            <c:if test="${empty product.productDescription}">
                                <p class="no-description">등록된 상품 설명이 없습니다.</p>
                            </c:if>
                        </div>
                    </div>

                    <!-- 상세 이미지 영역 -->
                    <c:if test="${not empty product.images}">
                        <div class="detail-bottom">
                            <div class="section-title">
                                <h3>상세 이미지</h3>
                                <span class="image-count">${product.images.size()}장</span>
                            </div>
                            <div class="detail-images-list">
                                <c:forEach var="image" items="${product.images}">
                                    <c:if test="${image.useYn eq 'Y'}">
                                        <div class="detail-image-item">
                                            <img src="${pageContext.request.contextPath}${image.imageUrl}" alt="상세이미지">
                                        </div>
                                    </c:if>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
        </main>
    </div>

    <script>
        const contextPath = '${pageContext.request.contextPath}';

        function changeMainImage(src, element) {
            document.getElementById('mainImage').src = src;
            document.querySelectorAll('.thumb-item').forEach(item => item.classList.remove('active'));
            element.classList.add('active');
        }

        function deleteProduct(productId) {
            if (!confirm('정말 삭제하시겠습니까?')) {
                return;
            }

            fetch(contextPath + '/api/admin/product/delete/' + productId, {
                method: 'POST'
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert(data.message);
                    window.location.href = contextPath + '/admin/product';
                } else {
                    alert(data.message);
                }
            })
            .catch(error => {
                alert('요청 중 오류가 발생했습니다');
            });
        }
    </script>
    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>
