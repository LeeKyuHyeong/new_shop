<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>로그인 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
</head>
<body>
    <div class="login-container">

        <div class="login-box">
            <div class="login-header">
                <h1 class="logo">KH SHOP</h1>
                <p>로그인</p>
            </div>

            <form action="${pageContext.request.contextPath}/login" method="post" class="login-form">
                <div class="form-group">
                    <input type="text" name="userId" placeholder="아이디" required>
                </div>

                <div class="form-group">
                    <input type="password" name="userPassword" placeholder="비밀번호" required>
                </div>

                <button type="submit" class="btn-login">로그인</button>
            </form>

            <div class="button-group">
                <a href="${pageContext.request.contextPath}/signup" class="btn-secondary">회원가입</a>
                <a href="${pageContext.request.contextPath}/password-reset" class="btn-secondary">비밀번호 초기화</a>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>