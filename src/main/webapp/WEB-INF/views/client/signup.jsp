<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>회원가입 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/signup.css">
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

    <script>const contextPath = '${pageContext.request.contextPath}';</script>
    <script src="${pageContext.request.contextPath}/js/client/signup.js"></script>
</body>
</html>
