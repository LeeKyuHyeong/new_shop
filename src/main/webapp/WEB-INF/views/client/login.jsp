<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>로그인 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client-main.css">
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

        /* 소셜 로그인 버튼 */
        .social-login {
            display: flex;
            flex-direction: column;
            gap: 12px;
            margin-bottom: 25px;
        }

        .btn-social {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
            width: 100%;
            padding: 14px;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 600;
            text-decoration: none;
            transition: all 0.3s;
            cursor: pointer;
            border: none;
        }

        .btn-social img {
            width: 20px;
            height: 20px;
        }

        .btn-kakao {
            background: #FEE500;
            color: #000000;
        }

        .btn-kakao:hover {
            background: #F5DC00;
        }

        .btn-naver {
            background: #03C75A;
            color: white;
        }

        .btn-naver:hover {
            background: #02B150;
        }

        .btn-google {
            background: white;
            color: #333;
            border: 1px solid #ddd;
        }

        .btn-google:hover {
            background: #f8f9fa;
            border-color: #ccc;
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
                    <span>간편 로그인</span>
                </div>

                <!-- 소셜 로그인 -->
                <div class="social-login">
                    <a href="${pageContext.request.contextPath}/oauth/kakao" class="btn-social btn-kakao">
                        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                            <path fill-rule="evenodd" clip-rule="evenodd" d="M10 2C5.029 2 1 5.129 1 8.999C1 11.495 2.658 13.697 5.194 14.952L4.185 18.517C4.117 18.752 4.381 18.939 4.585 18.805L8.84 16.062C9.219 16.104 9.606 16.126 10 16.126C14.971 16.126 19 12.869 19 8.999C19 5.129 14.971 2 10 2Z" fill="#000000"/>
                        </svg>
                        카카오로 시작하기
                    </a>
                    <a href="${pageContext.request.contextPath}/oauth/naver" class="btn-social btn-naver">
                        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                            <path d="M13.5 10.5L6.5 2H2V18H6.5V9.5L13.5 18H18V2H13.5V10.5Z" fill="white"/>
                        </svg>
                        네이버로 시작하기
                    </a>
                    <a href="${pageContext.request.contextPath}/oauth/google" class="btn-social btn-google">
                        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                            <path d="M19.8 10.2c0-.7-.1-1.4-.2-2H10v3.8h5.5c-.2 1.2-1 2.3-2 3v2.5h3.2c1.9-1.7 3-4.3 3-7.3z" fill="#4285F4"/>
                            <path d="M10 20c2.7 0 5-.9 6.6-2.4l-3.2-2.5c-.9.6-2 1-3.4 1-2.6 0-4.8-1.8-5.6-4.2H1.1v2.6C2.7 17.8 6.1 20 10 20z" fill="#34A853"/>
                            <path d="M4.4 11.9c-.2-.6-.3-1.2-.3-1.9s.1-1.3.3-1.9V5.5H1.1C.4 6.9 0 8.4 0 10s.4 3.1 1.1 4.5l3.3-2.6z" fill="#FBBC05"/>
                            <path d="M10 4c1.5 0 2.8.5 3.9 1.5l2.9-2.9C15 .9 12.7 0 10 0 6.1 0 2.7 2.2 1.1 5.5l3.3 2.6C5.2 5.8 7.4 4 10 4z" fill="#EA4335"/>
                        </svg>
                        Google로 시작하기
                    </a>
                </div>

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
