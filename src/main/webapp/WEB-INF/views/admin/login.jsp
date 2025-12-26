<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>관리자 로그인 - KH Shop Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
    <style>
        .admin-badge {
            display: inline-block;
            background: #e74c3c;
            color: white;
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: bold;
            margin-left: 8px;
        }
        .login-header h1 {
            display: flex;
            align-items: center;
            justify-content: center;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="login-box">
            <div class="login-header">
                <h1>
                    KH SHOP
                    <span class="admin-badge">ADMIN</span>
                </h1>
                <p>관리자 로그인</p>
            </div>

            <% if (request.getAttribute("loginError") != null) { %>
                <div class="alert alert-danger">
                    <%= request.getAttribute("loginError") %>
                </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/admin/login" method="post" class="login-form">
                <div class="form-group">
                    <input type="text" name="userId" placeholder="관리자 아이디" required>
                </div>

                <div class="form-group">
                    <input type="password" name="userPassword" placeholder="비밀번호" required>
                </div>

                <button type="submit" class="btn-login">관리자 로그인</button>
            </form>

            <div class="button-group">
                <a href="${pageContext.request.contextPath}/" class="btn-secondary">사용자 페이지로 이동</a>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
</body>
</html>
