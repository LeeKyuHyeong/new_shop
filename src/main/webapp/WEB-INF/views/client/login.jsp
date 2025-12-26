<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>로그인 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/main.css">
    <style>
        .login-section {
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: calc(100vh - 300px);
            padding: 60px 20px;
        }

        .login-card {
            background: white;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.1);
            padding: 50px 40px;
            width: 100%;
            max-width: 420px;
        }

        .login-title {
            text-align: center;
            margin-bottom: 35px;
        }

        .login-title h2 {
            font-size: 28px;
            color: #2c3e50;
            margin-bottom: 8px;
        }

        .login-title p {
            color: #7f8c8d;
            font-size: 14px;
        }

        .login-form .form-group {
            margin-bottom: 20px;
        }

        .login-form label {
            display: block;
            margin-bottom: 8px;
            font-size: 14px;
            font-weight: 500;
            color: #2c3e50;
        }

        .login-form input[type="text"],
        .login-form input[type="password"] {
            width: 100%;
            padding: 14px 16px;
            border: 1px solid #ddd;
            border-radius: 8px;
            font-size: 15px;
            transition: border-color 0.3s, box-shadow 0.3s;
        }

        .login-form input:focus {
            outline: none;
            border-color: #3498db;
            box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
        }

        .login-form input::placeholder {
            color: #bdc3c7;
        }

        .btn-login {
            width: 100%;
            padding: 15px;
            background: #3498db;
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.3s;
            margin-top: 10px;
        }

        .btn-login:hover {
            background: #2980b9;
        }

        .login-options {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin: 20px 0;
            font-size: 14px;
        }

        .remember-me {
            display: flex;
            align-items: center;
            gap: 8px;
            color: #7f8c8d;
            cursor: pointer;
        }

        .remember-me input[type="checkbox"] {
            width: 16px;
            height: 16px;
            cursor: pointer;
        }

        .forgot-password {
            color: #3498db;
            text-decoration: none;
        }

        .forgot-password:hover {
            text-decoration: underline;
        }

        .divider {
            display: flex;
            align-items: center;
            margin: 30px 0;
        }

        .divider::before,
        .divider::after {
            content: '';
            flex: 1;
            height: 1px;
            background: #e0e0e0;
        }

        .divider span {
            padding: 0 15px;
            color: #95a5a6;
            font-size: 13px;
        }

        .signup-link {
            text-align: center;
        }

        .signup-link p {
            color: #7f8c8d;
            font-size: 14px;
            margin-bottom: 15px;
        }

        .btn-signup {
            display: inline-block;
            width: 100%;
            padding: 14px;
            background: white;
            color: #2c3e50;
            border: 2px solid #2c3e50;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 600;
            text-decoration: none;
            text-align: center;
            transition: all 0.3s;
        }

        .btn-signup:hover {
            background: #2c3e50;
            color: white;
        }

        .error-message {
            background: #fee;
            border: 1px solid #fcc;
            color: #c0392b;
            padding: 12px 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
            text-align: center;
        }

        @media (max-width: 480px) {
            .login-card {
                padding: 40px 25px;
            }

            .login-options {
                flex-direction: column;
                gap: 15px;
            }
        }

        @media (max-width: 400px) {
            .login-section {
                padding: 30px 12px;
            }

            .login-card {
                padding: 30px 20px;
                border-radius: 8px;
            }

            .login-title h2 {
                font-size: 24px;
            }

            .login-title p {
                font-size: 13px;
            }

            .login-form input[type="text"],
            .login-form input[type="password"] {
                padding: 12px 14px;
                font-size: 14px;
            }

            .login-form label {
                font-size: 13px;
            }

            .btn-login {
                padding: 13px;
                font-size: 15px;
            }

            .login-options {
                font-size: 13px;
            }

            .divider {
                margin: 25px 0;
            }

            .btn-signup {
                padding: 12px;
                font-size: 14px;
            }
        }
    </style>
</head>
<body>
    <%@ include file="common/header.jsp" %>

    <main class="main-content">
        <section class="login-section">
            <div class="login-card">
                <div class="login-title">
                    <h2>로그인</h2>
                    <p>KH SHOP에 오신 것을 환영합니다</p>
                </div>

                <c:if test="${not empty loginError}">
                    <div class="error-message">${loginError}</div>
                </c:if>

                <form action="${pageContext.request.contextPath}/login" method="post" class="login-form">
                    <div class="form-group">
                        <label for="userId">아이디</label>
                        <input type="text" id="userId" name="userId" placeholder="아이디를 입력하세요" required>
                    </div>

                    <div class="form-group">
                        <label for="userPassword">비밀번호</label>
                        <input type="password" id="userPassword" name="userPassword" placeholder="비밀번호를 입력하세요" required>
                    </div>

                    <div class="login-options">
                        <label class="remember-me">
                            <input type="checkbox" name="remember">
                            <span>로그인 상태 유지</span>
                        </label>
                        <a href="${pageContext.request.contextPath}/password-reset" class="forgot-password">비밀번호 찾기</a>
                    </div>

                    <button type="submit" class="btn-login">로그인</button>
                </form>

                <div class="divider">
                    <span>또는</span>
                </div>

                <div class="signup-link">
                    <p>아직 회원이 아니신가요?</p>
                    <a href="${pageContext.request.contextPath}/signup" class="btn-signup">회원가입</a>
                </div>
            </div>
        </section>
    </main>

    <footer class="footer">
        <p>&copy; 2024 KH SHOP. All rights reserved.</p>
    </footer>
</body>
</html>
