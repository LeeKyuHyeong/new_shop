<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>회원가입 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/signup.css">
</head>
<body>
    <div class="signup-container">

        <div class="signup-box">
            <div class="signup-header">
                <h1>KH SHOP</h1>
                <p>회원가입</p>
            </div>

            <c:if test="${signupError != null}">
                <div class="alert-container"></div>
            </c:if>

            <form action="${pageContext.request.contextPath}/signup" method="post" class="signup-form" id="signupForm">
                <div class="form-group">
                    <label for="userId">아이디 *</label>
                    <div class="input-with-btn">
                        <input type="text" name="userId" id="userId" placeholder="6자 이상 15자 이하" required>
                        <button type="button" class="btn-check" onclick="checkUserId()">중복확인</button>
                    </div>
                    <span id="userIdMsg"></span>
                </div>

                <div class="form-group">
                    <label for="userPassword">비밀번호 *</label>
                    <input type="password" name="userPassword" id="userPassword" placeholder="8자 이상의 비밀번호" required>
                </div>

                <div class="form-group">
                    <label for="confirmPassword">비밀번호 확인 *</label>
                    <input type="password" name="confirmPassword" id="confirmPassword" placeholder="비밀번호 재입력" required>
                    <span id="passwordMsg"></span>
                </div>

                <div class="form-group">
                    <label for="userName">이름 *</label>
                    <input type="text" name="userName" id="userName" placeholder="실명 입력" required>
                </div>

                <div class="form-group">
                    <label for="email">이메일 *</label>
                    <div class="input-with-btn">
                        <input type="email" name="email" id="email" placeholder="example@email.com" required>
                        <button type="button" class="btn-check" onclick="checkEmail()">중복확인</button>
                    </div>
                    <span id="emailMsg"></span>
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

            <div class="login-link">
                이미 계정이 있으신가요? <a href="${pageContext.request.contextPath}/login">로그인</a>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';
    </script>
    <script src="${pageContext.request.contextPath}/js/signup.js?ver=12"></script>
</body>
</html>