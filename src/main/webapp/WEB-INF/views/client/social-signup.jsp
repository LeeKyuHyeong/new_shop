<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>소셜 회원가입 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/signup.css">
</head>
<body>
    <%@ include file="common/header.jsp" %>

    <main class="main-content">
        <section class="signup-section">
            <div class="signup-card">
                <div class="signup-header">
                    <div class="provider-badge ${provider}">
                        <c:choose>
                            <c:when test="${provider == 'KAKAO'}">
                                <svg width="18" height="18" viewBox="0 0 20 20" fill="none">
                                    <path fill-rule="evenodd" clip-rule="evenodd" d="M10 2C5.029 2 1 5.129 1 8.999C1 11.495 2.658 13.697 5.194 14.952L4.185 18.517C4.117 18.752 4.381 18.939 4.585 18.805L8.84 16.062C9.219 16.104 9.606 16.126 10 16.126C14.971 16.126 19 12.869 19 8.999C19 5.129 14.971 2 10 2Z" fill="#000000"/>
                                </svg>
                                카카오 계정
                            </c:when>
                            <c:when test="${provider == 'NAVER'}">
                                <svg width="18" height="18" viewBox="0 0 20 20" fill="none">
                                    <path d="M13.5 10.5L6.5 2H2V18H6.5V9.5L13.5 18H18V2H13.5V10.5Z" fill="white"/>
                                </svg>
                                네이버 계정
                            </c:when>
                            <c:when test="${provider == 'GOOGLE'}">
                                <svg width="18" height="18" viewBox="0 0 20 20" fill="none">
                                    <path d="M19.8 10.2c0-.7-.1-1.4-.2-2H10v3.8h5.5c-.2 1.2-1 2.3-2 3v2.5h3.2c1.9-1.7 3-4.3 3-7.3z" fill="#4285F4"/>
                                    <path d="M10 20c2.7 0 5-.9 6.6-2.4l-3.2-2.5c-.9.6-2 1-3.4 1-2.6 0-4.8-1.8-5.6-4.2H1.1v2.6C2.7 17.8 6.1 20 10 20z" fill="#34A853"/>
                                    <path d="M4.4 11.9c-.2-.6-.3-1.2-.3-1.9s.1-1.3.3-1.9V5.5H1.1C.4 6.9 0 8.4 0 10s.4 3.1 1.1 4.5l3.3-2.6z" fill="#FBBC05"/>
                                    <path d="M10 4c1.5 0 2.8.5 3.9 1.5l2.9-2.9C15 .9 12.7 0 10 0 6.1 0 2.7 2.2 1.1 5.5l3.3 2.6C5.2 5.8 7.4 4 10 4z" fill="#EA4335"/>
                                </svg>
                                구글 계정
                            </c:when>
                        </c:choose>
                    </div>
                    <h2>회원가입</h2>
                    <p>추가 정보를 입력하여 회원가입을 완료해주세요</p>
                </div>

                <!-- 소셜 프로필 미리보기 -->
                <div class="profile-preview">
                    <c:choose>
                        <c:when test="${not empty profileImage}">
                            <img src="${profileImage}" alt="프로필" class="profile-image">
                        </c:when>
                        <c:otherwise>
                            <div class="profile-image" style="display: flex; align-items: center; justify-content: center; font-size: 24px;">👤</div>
                        </c:otherwise>
                    </c:choose>
                    <div class="profile-info">
                        <div class="profile-name">${not empty nickname ? nickname : '소셜 회원'}</div>
                        <div class="profile-email">${not empty email ? email : '이메일 정보 없음'}</div>
                    </div>
                </div>

                <c:if test="${not empty error}">
                    <div class="error-message">${error}</div>
                </c:if>

                <form action="${pageContext.request.contextPath}/oauth/signup" method="post" id="signupForm">
                    <!-- Hidden fields -->
                    <input type="hidden" name="provider" value="${provider}">
                    <input type="hidden" name="providerId" value="${providerId}">
                    <input type="hidden" name="accessToken" value="${accessToken}">
                    <input type="hidden" name="profileImage" value="${profileImage}">

                    <div class="form-group">
                        <label>아이디 <span class="required">*</span></label>
                        <div class="input-with-btn">
                            <input type="text" name="userId" id="userId" required 
                                   placeholder="영문, 숫자 조합 4~20자" 
                                   pattern="^[a-zA-Z0-9_]{4,20}$"
                                   maxlength="20">
                            <button type="button" class="btn-check" onclick="checkUserId()">중복확인</button>
                        </div>
                        <div class="validation-msg" id="userIdMsg"></div>
                    </div>

                    <div class="form-group">
                        <label>이름 <span class="required">*</span></label>
                        <input type="text" name="userName" id="userName" required 
                               placeholder="실명을 입력하세요" 
                               value="${nickname}"
                               maxlength="50">
                    </div>

                    <div class="form-group">
                        <label>이메일 <span class="required">*</span></label>
                        <div class="input-with-btn">
                            <input type="email" name="email" id="email" required 
                                   placeholder="example@email.com" 
                                   value="${email}">
                            <button type="button" class="btn-check" onclick="checkEmail()">중복확인</button>
                        </div>
                        <div class="validation-msg" id="emailMsg"></div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label>성별</label>
                            <select name="gender" id="gender">
                                <option value="">선택안함</option>
                                <option value="M">남성</option>
                                <option value="F">여성</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>생년월일</label>
                            <input type="date" name="birth" id="birth">
                        </div>
                    </div>

                    <div class="terms-section">
                        <h4>약관 동의</h4>
                        <div class="checkbox-group">
                            <label class="checkbox-label">
                                <input type="checkbox" id="agreeAll" onchange="toggleAllAgree()">
                                <span><strong>전체 동의</strong></span>
                            </label>
                            <label class="checkbox-label">
                                <input type="checkbox" name="agreeTerms" id="agreeTerms" required onchange="checkAgree()">
                                <span>[필수] 이용약관 동의</span>
                            </label>
                            <label class="checkbox-label">
                                <input type="checkbox" name="agreePrivacy" id="agreePrivacy" required onchange="checkAgree()">
                                <span>[필수] 개인정보 처리방침 동의</span>
                            </label>
                            <label class="checkbox-label">
                                <input type="checkbox" name="agreeMarketing" id="agreeMarketing" onchange="checkAgree()">
                                <span>[선택] 마케팅 정보 수신 동의</span>
                            </label>
                        </div>
                    </div>

                    <button type="submit" class="btn-submit" id="submitBtn" disabled>가입하기</button>
                </form>

                <a href="${pageContext.request.contextPath}/login" class="cancel-link">로그인 화면으로 돌아가기</a>
            </div>
        </section>
    </main>

    <footer class="footer">
        <p>&copy; 2024 KH SHOP. All rights reserved.</p>
    </footer>

    <script>const contextPath = '${pageContext.request.contextPath}';</script>
    <script src="${pageContext.request.contextPath}/js/client/social-signup.js"></script>
</body>
</html>
