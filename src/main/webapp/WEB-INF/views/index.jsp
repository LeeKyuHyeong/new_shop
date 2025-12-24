<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>메인 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
</head>
<body>

    <nav class="navbar">
        <div class="nav-container">
            <div class="logo">KH SHOP</div>
            <div class="nav-menu">
                <a href="${pageContext.request.contextPath}/index" class="nav-link">홈</a>
                <span class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </span>
                <a href="${pageContext.request.contextPath}/logout" class="nav-link">로그아웃</a>
            </div>
        </div>
    </nav>

    <main class="container">
        <div class="welcome-section">
            <h1>환영합니다!</h1>
            <p>로그인되었습니다.</p>
        </div>
    </main>

    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>