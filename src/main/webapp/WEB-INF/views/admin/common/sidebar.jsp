<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<script>window.contextPath = '${pageContext.request.contextPath}';</script>

<!-- 모바일 햄버거 메뉴 버튼 -->
<button class="mobile-menu-btn" id="mobileMenuBtn" onclick="toggleSidebar()">
    <span></span>
    <span></span>
    <span></span>
</button>

<!-- 오버레이 -->
<div class="sidebar-overlay" id="sidebarOverlay" onclick="toggleSidebar()"></div>

<aside class="sidebar" id="sidebar">
    <div class="sidebar-header">
        <h2>KH SHOP Admin</h2>
    </div>

    <nav class="sidebar-menu">
        <a href="${pageContext.request.contextPath}/admin" class="menu-item <c:if test="${activeMenu eq 'dashboard'}">active</c:if>">대시보드</a>
        <a href="${pageContext.request.contextPath}/admin/category" class="menu-item <c:if test="${activeMenu eq 'category'}">active</c:if>">카테고리 관리</a>
        <a href="${pageContext.request.contextPath}/admin/product" class="menu-item <c:if test="${activeMenu eq 'product'}">active</c:if>">상품 관리</a>
        <a href="${pageContext.request.contextPath}/admin/slide" class="menu-item <c:if test="${activeMenu eq 'slide'}">active</c:if>">슬라이드 관리</a>
        <a href="${pageContext.request.contextPath}/admin/user" class="menu-item <c:if test="${activeMenu eq 'user'}">active</c:if>">사용자 관리</a>
        <a href="${pageContext.request.contextPath}/admin/popup" class="menu-item <c:if test="${activeMenu eq 'popup'}">active</c:if>">팝업 관리</a>
        <a href="${pageContext.request.contextPath}/admin/order" class="menu-item <c:if test="${activeMenu eq 'order'}">active</c:if>">주문 관리</a>
        <a href="${pageContext.request.contextPath}/admin/review" class="menu-item <c:if test="${activeMenu eq 'review'}">active</c:if>">리뷰 관리</a>
        <a href="${pageContext.request.contextPath}/admin/profanity" class="menu-item <c:if test="${activeMenu eq 'profanity'}">active</c:if>">비속어 관리</a>
        <a href="${pageContext.request.contextPath}/admin/batch" class="menu-item <c:if test="${activeMenu eq 'batch'}">active</c:if>">배치 관리</a>
        <a href="${pageContext.request.contextPath}/admin/stats" class="menu-item <c:if test="${activeMenu eq 'stats'}">active</c:if>">통계 관리</a>
        <a href="${pageContext.request.contextPath}/admin/setting" class="menu-item <c:if test="${activeMenu eq 'setting'}">active</c:if>">사이트 설정</a>
        
        <!-- 테마 토글 (user-setting 기반) -->
        <div class="menu-item theme-toggle-item" onclick="toggleTheme()" style="cursor: pointer; display: flex; justify-content: space-between; align-items: center;">
            <span id="themeText">다크</span>
            <span id="themeIcon">🌙</span>
        </div>
        
        <a href="${pageContext.request.contextPath}/logout" class="menu-item logout">로그아웃</a>
    </nav>
</aside>
