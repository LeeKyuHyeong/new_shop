<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>회원가입 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client-main.css">
    <style>
        .signup-section {
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: calc(100vh - 300px);
            padding: 60px 20px;
        }

        .signup-card {
            background: white;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.1);
            padding: 50px 40px;
            width: 100%;
            max-width: 500px;
        }

        .signup-title {
            text-align: center;
            margin-bottom: 35px;
        }

        .signup-title h2 {
            font-size: 28px;
            color: #2c3e50;
            margin-bottom: 8px;
        }

        .signup-title p {
            color: #7f8c8d;
            font-size: 14px;
        }

        .signup-form .form-group {
            margin-bottom: 20px;
        }

        .signup-form label {
            display: block;
            margin-bottom: 8px;
            font-size: 14px;
            font-weight: 500;
            color: #2c3e50;
        }

        .signup-form label .required {
            color: #e74c3c;
            margin-left: 2px;
        }

        .signup-form input[type="text"],
        .signup-form input[type="password"],
        .signup-form input[type="email"],
        .signup-form input[type="date"],
        .signup-form select {
            width: 100%;
            padding: 14px 16px;
            border: 1px solid #ddd;
            border-radius: 8px;
            font-size: 15px;
            transition: border-color 0.3s, box-shadow 0.3s;
        }

        .signup-form input:focus,
        .signup-form select:focus {
            outline: none;
            border-color: #3498db;
            box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
        }

        .signup-form input::placeholder {
            color: #bdc3c7;
        }

        .input-with-btn {
            display: flex;
            gap: 10px;
        }

        .input-with-btn input {
            flex: 1;
        }

        .btn-check {
            padding: 14px 20px;
            background: #ecf0f1;
            color: #2c3e50;
            border: 1px solid #ddd;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.3s;
            white-space: nowrap;
        }

        .btn-check:hover {
            background: #bdc3c7;
        }

        .form-row {
            display: flex;
            gap: 20px;
        }

        .form-row .form-group {
            flex: 1;
        }

        .msg {
            display: block;
            margin-top: 6px;
            font-size: 12px;
        }

        .msg.success {
            color: #27ae60;
        }

        .msg.error {
            color: #e74c3c;
        }

        .btn-signup {
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

        .btn-signup:hover {
            background: #2980b9;
        }

        .btn-signup:disabled {
            background: #bdc3c7;
            cursor: not-allowed;
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

        .login-link {
            text-align: center;
        }

        .login-link p {
            color: #7f8c8d;
            font-size: 14px;
            margin-bottom: 15px;
        }

        .btn-login-link {
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

        .btn-login-link:hover {
            background: #2c3e50;
            color: white;
        }

        @media (max-width: 480px) {
            .signup-card {
                padding: 40px 25px;
            }

            .form-row {
                flex-direction: column;
                gap: 0;
            }

            .input-with-btn {
                flex-direction: column;
            }

            .btn-check {
                width: 100%;
            }
        }

        @media (max-width: 400px) {
            .signup-section {
                padding: 30px 12px;
            }

            .signup-card {
                padding: 30px 20px;
                border-radius: 8px;
            }

            .signup-title h2 {
                font-size: 24px;
            }

            .signup-title p {
                font-size: 13px;
            }

            .signup-form .form-group {
                margin-bottom: 16px;
            }

            .signup-form input[type="text"],
            .signup-form input[type="password"],
            .signup-form input[type="email"],
            .signup-form input[type="date"],
            .signup-form select {
                padding: 12px 14px;
                font-size: 14px;
            }

            .signup-form label {
                font-size: 13px;
                margin-bottom: 6px;
            }

            .btn-check {
                padding: 12px 16px;
                font-size: 13px;
            }

            .btn-signup {
                padding: 13px;
                font-size: 15px;
            }

            .divider {
                margin: 25px 0;
            }

            .btn-login-link {
                padding: 12px;
                font-size: 14px;
            }

            .msg {
                font-size: 11px;
            }
        }
    </style>
</head>
<body>
    <%@ include file="common/header.jsp" %>

    <main class="main-content">
        <section class="signup-section">
            <div class="signup-card">
                <div class="signup-title">
                    <h2>회원가입</h2>
                    <p>KH SHOP 회원이 되어 다양한 혜택을 누리세요</p>
                </div>

                <form action="${pageContext.request.contextPath}/signup" method="post" class="signup-form" id="signupForm">
                    <div class="form-group">
                        <label for="userId">아이디 <span class="required">*</span></label>
                        <div class="input-with-btn">
                            <input type="text" name="userId" id="userId" placeholder="6자 이상 15자 이하" required>
                            <button type="button" class="btn-check" onclick="checkUserId()">중복확인</button>
                        </div>
                        <span id="userIdMsg" class="msg"></span>
                    </div>

                    <div class="form-group">
                        <label for="userPassword">비밀번호 <span class="required">*</span></label>
                        <input type="password" name="userPassword" id="userPassword" placeholder="8자 이상의 비밀번호" required>
                    </div>

                    <div class="form-group">
                        <label for="confirmPassword">비밀번호 확인 <span class="required">*</span></label>
                        <input type="password" name="confirmPassword" id="confirmPassword" placeholder="비밀번호 재입력" required>
                        <span id="passwordMsg" class="msg"></span>
                    </div>

                    <div class="form-group">
                        <label for="userName">이름 <span class="required">*</span></label>
                        <input type="text" name="userName" id="userName" placeholder="실명 입력" required>
                    </div>

                    <div class="form-group">
                        <label for="email">이메일 <span class="required">*</span></label>
                        <div class="input-with-btn">
                            <input type="email" name="email" id="email" placeholder="example@email.com" required>
                            <button type="button" class="btn-check" onclick="checkEmail()">중복확인</button>
                        </div>
                        <span id="emailMsg" class="msg"></span>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="gender">성별</label>
                            <select name="gender" id="gender">
                                <option value="">선택</option>
                                <option value="M">남성</option>
                                <option value="F">여성</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="birth">생년월일</label>
                            <input type="date" name="birth" id="birth">
                        </div>
                    </div>

                    <button type="submit" class="btn-signup">회원가입</button>
                </form>

                <div class="divider">
                    <span>또는</span>
                </div>

                <div class="login-link">
                    <p>이미 계정이 있으신가요?</p>
                    <a href="${pageContext.request.contextPath}/login" class="btn-login-link">로그인</a>
                </div>
            </div>
        </section>
    </main>

    <footer class="footer">
        <p>&copy; 2024 KH SHOP. All rights reserved.</p>
    </footer>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
    </script>
    <script src="${pageContext.request.contextPath}/js/signup.js"></script>
</body>
</html>
