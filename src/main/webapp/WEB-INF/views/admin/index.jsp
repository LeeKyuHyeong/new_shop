<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>관리자 대시보드 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
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
                <a href="${pageContext.request.contextPath}/admin" class="menu-item active">대시보드</a>
                <a href="${pageContext.request.contextPath}/admin/category" class="menu-item">카테고리 관리</a>
                <a href="${pageContext.request.contextPath}/admin/product" class="menu-item">상품 관리</a>
                <a href="${pageContext.request.contextPath}/admin/order" class="menu-item">주문 관리</a>
                <a href="${pageContext.request.contextPath}/logout" class="menu-item logout">로그아웃</a>
            </nav>
        </aside>

        <main class="main-content">
            <header class="top-bar">
                <h1>대시보드</h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <div class="welcome-section">
                    <h2>환영합니다!</h2>
                    <p>관리자 대시보드입니다. 좌측 메뉴에서 관리할 항목을 선택하세요.</p>
                </div>
            </div>
        </main>
    </div>

    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>