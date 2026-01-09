<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script>window.contextPath = '${pageContext.request.contextPath}';</script>

<!-- 헤더 -->
<header class="header">
    <div class="header-top">
        <a href="${pageContext.request.contextPath}/" class="logo">KH SHOP</a>
        <div class="header-right">
            <!-- 테마 토글 버튼 (항상 표시 - 비로그인도 사용 가능) -->
            <button type="button" class="header-btn theme-toggle-btn" onclick="toggleTheme()">
                <span id="themeIcon">🌙</span>
                <span id="themeText" class="theme-text">다크</span>
            </button>
            
            <c:choose>
                <c:when test="${not empty sessionScope.loggedInUser}">
                    <span class="header-btn user-name">${sessionScope.loggedInUser}님</span>
                    <a href="${pageContext.request.contextPath}/mypage/orders" class="header-btn">마이페이지</a>
                    <a href="${pageContext.request.contextPath}/wishlist" class="header-btn wishlist-btn">
                        💝 위시리스트
                        <span class="wishlist-count" id="wishlistCount">0</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/cart" class="header-btn cart-btn">
                        🛒 장바구니
                        <span class="cart-count" id="cartCount">0</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/logout" class="header-btn">로그아웃</a>
                    <c:if test="${sessionScope.userRole == 'ADMIN'}">
                        <a href="${pageContext.request.contextPath}/admin" class="header-btn primary">관리자</a>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/cart" class="header-btn cart-btn">
                        🛒 장바구니
                        <span class="cart-count" id="cartCount" style="display:none;">0</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/login" class="header-btn">로그인</a>
                    <a href="${pageContext.request.contextPath}/signup" class="header-btn primary">회원가입</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- 카테고리 메뉴 -->
    <nav class="category-nav">
        <ul class="category-menu">
            <li <c:if test="${empty selectedCategoryId}">class="active"</c:if>>
                <a href="${pageContext.request.contextPath}/">전체</a>
            </li>
            <c:forEach var="parent" items="${parentCategories}">
                <li <c:if test="${selectedCategoryId == parent.categoryId || selectedParentId == parent.categoryId}">class="active"</c:if>>
                    <a href="${pageContext.request.contextPath}/category/${parent.categoryId}">${parent.categoryName}</a>
                    <c:if test="${not empty parent.children}">
                        <ul class="sub-menu">
                            <c:forEach var="child" items="${parent.children}">
                                <li <c:if test="${selectedCategoryId == child.categoryId}">class="active"</c:if>>
                                    <a href="${pageContext.request.contextPath}/category/${child.categoryId}">
                                        ${child.categoryName}
                                    </a>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:if>
                </li>
            </c:forEach>
        </ul>
    </nav>
</header>

<script src="${pageContext.request.contextPath}/js/common/theme.js"></script>

<!-- 장바구니/위시리스트 카운트 로드 -->
<script>
document.addEventListener('DOMContentLoaded', function() {
    loadCartCount();
    loadWishlistHeaderCount();
});

function loadCartCount() {
    fetch('${pageContext.request.contextPath}/cart/count')
        .then(response => response.json())
        .then(data => {
            const countEl = document.getElementById('cartCount');
            if (countEl && data.count > 0) {
                countEl.textContent = data.count;
                countEl.style.display = 'inline-flex';
            }
        })
        .catch(err => console.log('Cart count error:', err));
}

function loadWishlistHeaderCount() {
    fetch('${pageContext.request.contextPath}/api/wishlist/count')
        .then(response => response.json())
        .then(data => {
            const countEl = document.getElementById('wishlistCount');
            if (countEl) {
                if (data.count > 0) {
                    countEl.textContent = data.count;
                    countEl.style.display = 'inline-flex';
                } else {
                    countEl.style.display = 'none';
                }
            }
        })
        .catch(err => console.log('Wishlist count error:', err));
}
</script>
