<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<aside class="sidebar">
    <div class="sidebar-header">
        <h2>KH SHOP Admin</h2>
        <button id="themeBtn" class="theme-btn" onclick="toggleTheme()">🌙</button>
    </div>

    <nav class="sidebar-menu">
        <a href="${pageContext.request.contextPath}/admin" class="menu-item <c:if test="${activeMenu eq 'dashboard'}">active</c:if>">대시보드</a>
        <a href="${pageContext.request.contextPath}/admin/category" class="menu-item <c:if test="${activeMenu eq 'category'}">active</c:if>">카테고리 관리</a>
        <a href="${pageContext.request.contextPath}/admin/product" class="menu-item <c:if test="${activeMenu eq 'product'}">active</c:if>">상품 관리</a>
        <a href="${pageContext.request.contextPath}/admin/slide" class="menu-item <c:if test="${activeMenu eq 'slide'}">active</c:if>">슬라이드 관리</a>
        <a href="${pageContext.request.contextPath}/admin/user" class="menu-item <c:if test="${activeMenu eq 'user'}">active</c:if>">사용자 관리</a>
        <a href="${pageContext.request.contextPath}/admin/order" class="menu-item <c:if test="${activeMenu eq 'order'}">active</c:if>">주문 관리</a>
        <a href="${pageContext.request.contextPath}/admin/setting" class="menu-item <c:if test="${activeMenu eq 'setting'}">active</c:if>">사이트 설정</a>
        <a href="${pageContext.request.contextPath}/logout" class="menu-item logout">로그아웃</a>
    </nav>
</aside>
