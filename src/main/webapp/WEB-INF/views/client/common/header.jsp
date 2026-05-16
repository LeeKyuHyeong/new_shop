<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script>window.contextPath = '${pageContext.request.contextPath}';</script>

<!-- 헤더 -->
<header class="header">
    <div class="header-top">
        <a href="${pageContext.request.contextPath}/" class="logo">KH SHOP</a>
        <!-- 데스크톱 메뉴 -->
        <div class="header-right desktop-menu">
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

        <!-- 모바일: 장바구니 + 햄버거 버튼 -->
        <div class="mobile-header-right">
            <a href="${pageContext.request.contextPath}/cart" class="mobile-cart-btn">
                🛒
                <span class="cart-count" id="mobileCartCount" style="display:none;">0</span>
            </a>
            <button type="button" class="hamburger-btn" onclick="toggleMobileMenu()" aria-label="메뉴">
                <span class="hamburger-line"></span>
                <span class="hamburger-line"></span>
                <span class="hamburger-line"></span>
            </button>
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

<!-- 평문 비밀번호 → BCrypt 자동 마이그레이션 경고 배너.
     로그인 시 LoginController 가 세션에 플래그를 set, 비밀번호 변경 시 제거.
     사용자에게 sunset 날짜 전에 비밀번호 변경을 권고. -->
<c:if test="${sessionScope.passwordMigrationWarning == true}">
    <div class="bcrypt-sunset-banner" id="bcryptSunsetBanner" role="status">
        <span class="bcrypt-sunset-icon">🔐</span>
        <span class="bcrypt-sunset-text">
            보안 강화를 위해 비밀번호 재설정을 권장드립니다.
            <c:if test="${not empty sessionScope.passwordSunsetDate}">
                <strong>${sessionScope.passwordSunsetDate}</strong> 이후로는 기존 비밀번호로 로그인할 수 없습니다.
            </c:if>
            <a href="${pageContext.request.contextPath}/mypage/setting#passwordForm" class="bcrypt-sunset-link">지금 변경하기 →</a>
        </span>
        <button type="button" class="bcrypt-sunset-close" aria-label="닫기"
                onclick="document.getElementById('bcryptSunsetBanner').remove();">×</button>
    </div>
</c:if>

<!-- 모바일 메뉴 오버레이 -->
<div class="mobile-menu-overlay" id="mobileMenuOverlay" onclick="closeMobileMenu()"></div>

<!-- 모바일 슬라이드 메뉴 -->
<nav class="mobile-menu" id="mobileMenu">
    <div class="mobile-menu-header">
        <span class="mobile-menu-title">메뉴</span>
        <button type="button" class="mobile-menu-close" onclick="closeMobileMenu()">&times;</button>
    </div>

    <!-- 사용자 정보 -->
    <div class="mobile-menu-user">
        <c:choose>
            <c:when test="${not empty sessionScope.loggedInUser}">
                <div class="mobile-user-info">
                    <span class="mobile-user-icon">👤</span>
                    <span class="mobile-user-name">${sessionScope.loggedInUser}님</span>
                </div>
            </c:when>
            <c:otherwise>
                <div class="mobile-auth-buttons">
                    <a href="${pageContext.request.contextPath}/login" class="mobile-auth-btn">로그인</a>
                    <a href="${pageContext.request.contextPath}/signup" class="mobile-auth-btn primary">회원가입</a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- 메뉴 링크 -->
    <ul class="mobile-menu-list">
        <c:if test="${not empty sessionScope.loggedInUser}">
            <li><a href="${pageContext.request.contextPath}/mypage/orders">📦 마이페이지</a></li>
            <li><a href="${pageContext.request.contextPath}/wishlist">💝 위시리스트</a></li>
        </c:if>
        <li><a href="${pageContext.request.contextPath}/cart">🛒 장바구니</a></li>
        <li class="divider"></li>
        <li class="mobile-menu-section-title">카테고리</li>
        <li><a href="${pageContext.request.contextPath}/">전체 상품</a></li>
        <c:forEach var="parent" items="${parentCategories}">
            <li class="has-submenu">
                <a href="${pageContext.request.contextPath}/category/${parent.categoryId}">${parent.categoryName}</a>
                <c:if test="${not empty parent.children}">
                    <ul class="mobile-submenu">
                        <c:forEach var="child" items="${parent.children}">
                            <li><a href="${pageContext.request.contextPath}/category/${child.categoryId}">${child.categoryName}</a></li>
                        </c:forEach>
                    </ul>
                </c:if>
            </li>
        </c:forEach>
    </ul>

    <!-- 하단 메뉴 -->
    <div class="mobile-menu-footer">
        <button type="button" class="mobile-theme-btn" onclick="toggleTheme(); updateMobileThemeBtn();">
            <span id="mobileThemeIcon">🌙</span>
            <span id="mobileThemeText">다크 모드</span>
        </button>
        <c:if test="${not empty sessionScope.loggedInUser}">
            <a href="${pageContext.request.contextPath}/logout" class="mobile-logout-btn">로그아웃</a>
            <c:if test="${sessionScope.userRole == 'ADMIN'}">
                <a href="${pageContext.request.contextPath}/admin" class="mobile-admin-btn">관리자 페이지</a>
            </c:if>
        </c:if>
    </div>
</nav>

<script src="${pageContext.request.contextPath}/js/common/theme.js"></script>

<!-- 장바구니/위시리스트 카운트 로드 + 모바일 메뉴 -->
<script>
document.addEventListener('DOMContentLoaded', function() {
    loadCartCount();
    loadWishlistHeaderCount();
    updateMobileThemeBtn();
});

function loadCartCount() {
    fetch('${pageContext.request.contextPath}/cart/count')
        .then(response => response.json())
        .then(data => {
            // 데스크톱 카운트
            const countEl = document.getElementById('cartCount');
            if (countEl && data.count > 0) {
                countEl.textContent = data.count;
                countEl.style.display = 'inline-flex';
            }
            // 모바일 카운트
            const mobileCountEl = document.getElementById('mobileCartCount');
            if (mobileCountEl && data.count > 0) {
                mobileCountEl.textContent = data.count;
                mobileCountEl.style.display = 'inline-flex';
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

// 모바일 메뉴 토글
function toggleMobileMenu() {
    const menu = document.getElementById('mobileMenu');
    const overlay = document.getElementById('mobileMenuOverlay');
    const hamburger = document.querySelector('.hamburger-btn');

    menu.classList.toggle('open');
    overlay.classList.toggle('open');
    hamburger.classList.toggle('active');
    document.body.classList.toggle('mobile-menu-open');
}

function closeMobileMenu() {
    const menu = document.getElementById('mobileMenu');
    const overlay = document.getElementById('mobileMenuOverlay');
    const hamburger = document.querySelector('.hamburger-btn');

    menu.classList.remove('open');
    overlay.classList.remove('open');
    hamburger.classList.remove('active');
    document.body.classList.remove('mobile-menu-open');
}

// 모바일 테마 버튼 상태 업데이트
function updateMobileThemeBtn() {
    const isDark = document.body.classList.contains('dark-mode');
    const mobileIcon = document.getElementById('mobileThemeIcon');
    const mobileText = document.getElementById('mobileThemeText');
    if (mobileIcon) mobileIcon.textContent = isDark ? '☀️' : '🌙';
    if (mobileText) mobileText.textContent = isDark ? '라이트 모드' : '다크 모드';
}
</script>
