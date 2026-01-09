<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>위시리스트 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/wishlist.css">
</head>
<body>
    <%@ include file="common/header.jsp" %>

    <main class="main-content">
        <div class="wishlist-container">
            <div class="wishlist-header">
                <h1>💝 위시리스트</h1>
                <p class="wishlist-description">찜한 상품 목록입니다.</p>
            </div>

            <!-- 로딩 -->
            <div class="loading" id="loading">
                <div class="spinner"></div>
                <p>로딩 중...</p>
            </div>

            <!-- 빈 위시리스트 -->
            <div class="empty-wishlist" id="emptyWishlist" style="display: none;">
                <div class="empty-icon">💔</div>
                <h3>위시리스트가 비어있습니다</h3>
                <p>마음에 드는 상품을 찜해보세요!</p>
                <a href="${pageContext.request.contextPath}/" class="btn-shop">쇼핑하러 가기</a>
            </div>

            <!-- 위시리스트 목록 -->
            <div class="wishlist-content" id="wishlistContent" style="display: none;">
                <div class="wishlist-count">
                    총 <strong id="totalCount">0</strong>개의 상품
                </div>

                <div class="wishlist-grid" id="wishlistGrid">
                    <!-- JS로 렌더링 -->
                </div>
            </div>
        </div>
    </main>

    <footer class="footer">
        <p>&copy; 2024 KH SHOP. All rights reserved.</p>
    </footer>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
    </script>
    <script src="${pageContext.request.contextPath}/js/client/wishlist.js"></script>
</body>
</html>
