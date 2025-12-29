<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>소셜 회원가입 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/main.css">
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
            border-radius: 16px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.1);
            padding: 50px 40px;
            width: 100%;
            max-width: 500px;
        }

        .signup-header {
            text-align: center;
            margin-bottom: 35px;
        }

        .provider-badge {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 14px;
            font-weight: 600;
            margin-bottom: 15px;
        }

        .provider-badge.KAKAO {
            background: #FEE500;
            color: #000;
        }

        .provider-badge.NAVER {
            background: #03C75A;
            color: white;
        }

        .provider-badge.GOOGLE {
            background: #f1f3f4;
            color: #333;
            border: 1px solid #ddd;
        }

        .signup-header h2 {
            font-size: 28px;
            color: #2c3e50;
            margin-bottom: 8px;
        }

        .signup-header p {
            color: #7f8c8d;
            font-size: 14px;
        }

        .profile-preview {
            display: flex;
            align-items: center;
            gap: 15px;
            padding: 20px;
            background: #f8f9fa;
            border-radius: 12px;
            margin-bottom: 25px;
        }

        .profile-image {
            width: 60px;
            height: 60px;
            border-radius: 50%;
            object-fit: cover;
            background: #ddd;
        }

        .profile-info {
            flex: 1;
        }

        .profile-name {
            font-weight: 600;
            font-size: 16px;
            margin-bottom: 4px;
        }

        .profile-email {
            color: #7f8c8d;
            font-size: 14px;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-size: 14px;
            font-weight: 600;
            color: #2c3e50;
        }

        .form-group label .required {
            color: #e74c3c;
        }

        .form-group input,
        .form-group select {
            width: 100%;
            padding: 14px 16px;
            border: 1px solid #ddd;
            border-radius: 8px;
            font-size: 15px;
            transition: border-color 0.3s, box-shadow 0.3s;
        }

        .form-group input:focus,
        .form-group select:focus {
            outline: none;
            border-color: #3498db;
            box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
        }

        .form-group input::placeholder {
            color: #bdc3c7;
        }

        .form-group input.error {
            border-color: #e74c3c;
        }

        .form-group input.success {
            border-color: #27ae60;
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
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            white-space: nowrap;
            transition: background 0.3s;
        }

        .btn-check:hover {
            background: #bdc3c7;
        }

        .validation-msg {
            font-size: 13px;
            margin-top: 6px;
        }

        .validation-msg.error {
            color: #e74c3c;
        }

        .validation-msg.success {
            color: #27ae60;
        }

        .form-row {
            display: flex;
            gap: 15px;
        }

        .form-row .form-group {
            flex: 1;
        }

        .btn-submit {
            width: 100%;
            padding: 16px;
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

        .btn-submit:hover {
            background: #2980b9;
        }

        .btn-submit:disabled {
            background: #bdc3c7;
            cursor: not-allowed;
        }

        .terms-section {
            margin-top: 20px;
            padding: 20px;
            background: #f8f9fa;
            border-radius: 8px;
        }

        .terms-section h4 {
            font-size: 14px;
            margin-bottom: 15px;
            color: #2c3e50;
        }

        .checkbox-group {
            display: flex;
            flex-direction: column;
            gap: 12px;
        }

        .checkbox-label {
            display: flex;
            align-items: center;
            gap: 10px;
            cursor: pointer;
            font-size: 14px;
            color: #555;
        }

        .checkbox-label input[type="checkbox"] {
            width: 18px;
            height: 18px;
            cursor: pointer;
        }

        .checkbox-label a {
            color: #3498db;
            text-decoration: none;
        }

        .checkbox-label a:hover {
            text-decoration: underline;
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

        .cancel-link {
            display: block;
            text-align: center;
            margin-top: 20px;
            color: #7f8c8d;
            font-size: 14px;
            text-decoration: none;
        }

        .cancel-link:hover {
            color: #2c3e50;
        }

        @media (max-width: 480px) {
            .signup-card {
                padding: 30px 20px;
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
    </style>
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

    <script>
        const contextPath = '${pageContext.request.contextPath}';
        let userIdChecked = false;
        let emailChecked = ${not empty email ? 'true' : 'false'};
        const originalEmail = '${email}';

        // 아이디 중복 체크
        function checkUserId() {
            const userId = document.getElementById('userId').value.trim();
            const msgEl = document.getElementById('userIdMsg');
            const inputEl = document.getElementById('userId');

            if (!userId) {
                msgEl.textContent = '아이디를 입력해주세요.';
                msgEl.className = 'validation-msg error';
                return;
            }

            if (!/^[a-zA-Z0-9_]{4,20}$/.test(userId)) {
                msgEl.textContent = '영문, 숫자, 언더스코어(_) 조합 4~20자로 입력해주세요.';
                msgEl.className = 'validation-msg error';
                inputEl.className = 'error';
                return;
            }

            fetch(contextPath + '/oauth/check-userid?userId=' + encodeURIComponent(userId))
                .then(response => response.json())
                .then(data => {
                    if (data.available) {
                        msgEl.textContent = '사용 가능한 아이디입니다.';
                        msgEl.className = 'validation-msg success';
                        inputEl.className = 'success';
                        userIdChecked = true;
                    } else {
                        msgEl.textContent = '이미 사용 중인 아이디입니다.';
                        msgEl.className = 'validation-msg error';
                        inputEl.className = 'error';
                        userIdChecked = false;
                    }
                    updateSubmitButton();
                });
        }

        // 이메일 중복 체크
        function checkEmail() {
            const email = document.getElementById('email').value.trim();
            const msgEl = document.getElementById('emailMsg');
            const inputEl = document.getElementById('email');

            if (!email) {
                msgEl.textContent = '이메일을 입력해주세요.';
                msgEl.className = 'validation-msg error';
                return;
            }

            if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
                msgEl.textContent = '올바른 이메일 형식이 아닙니다.';
                msgEl.className = 'validation-msg error';
                inputEl.className = 'error';
                return;
            }

            fetch(contextPath + '/oauth/check-email?email=' + encodeURIComponent(email))
                .then(response => response.json())
                .then(data => {
                    if (data.available) {
                        msgEl.textContent = '사용 가능한 이메일입니다.';
                        msgEl.className = 'validation-msg success';
                        inputEl.className = 'success';
                        emailChecked = true;
                    } else {
                        msgEl.textContent = '이미 등록된 이메일입니다.';
                        msgEl.className = 'validation-msg error';
                        inputEl.className = 'error';
                        emailChecked = false;
                    }
                    updateSubmitButton();
                });
        }

        // 아이디 입력 시 체크 상태 초기화
        document.getElementById('userId').addEventListener('input', function() {
            userIdChecked = false;
            this.className = '';
            document.getElementById('userIdMsg').textContent = '';
            updateSubmitButton();
        });

        // 이메일 입력 시 체크 상태 초기화
        document.getElementById('email').addEventListener('input', function() {
            // 원래 소셜에서 가져온 이메일과 같으면 체크 유지
            if (this.value === originalEmail && originalEmail) {
                emailChecked = true;
            } else {
                emailChecked = false;
                this.className = '';
                document.getElementById('emailMsg').textContent = '';
            }
            updateSubmitButton();
        });

        // 전체 동의
        function toggleAllAgree() {
            const agreeAll = document.getElementById('agreeAll').checked;
            document.getElementById('agreeTerms').checked = agreeAll;
            document.getElementById('agreePrivacy').checked = agreeAll;
            document.getElementById('agreeMarketing').checked = agreeAll;
            updateSubmitButton();
        }

        // 개별 동의 체크
        function checkAgree() {
            const terms = document.getElementById('agreeTerms').checked;
            const privacy = document.getElementById('agreePrivacy').checked;
            const marketing = document.getElementById('agreeMarketing').checked;
            
            document.getElementById('agreeAll').checked = terms && privacy && marketing;
            updateSubmitButton();
        }

        // 제출 버튼 상태 업데이트
        function updateSubmitButton() {
            const userId = document.getElementById('userId').value.trim();
            const userName = document.getElementById('userName').value.trim();
            const email = document.getElementById('email').value.trim();
            const terms = document.getElementById('agreeTerms').checked;
            const privacy = document.getElementById('agreePrivacy').checked;

            const isValid = userId && userName && email && 
                           userIdChecked && emailChecked && 
                           terms && privacy;

            document.getElementById('submitBtn').disabled = !isValid;
        }

        // 폼 제출 전 최종 검증
        document.getElementById('signupForm').addEventListener('submit', function(e) {
            if (!userIdChecked) {
                e.preventDefault();
                alert('아이디 중복확인을 해주세요.');
                return;
            }

            if (!emailChecked) {
                e.preventDefault();
                alert('이메일 중복확인을 해주세요.');
                return;
            }
        });

        // 초기화
        document.addEventListener('DOMContentLoaded', function() {
            // 소셜에서 가져온 이메일이 있으면 자동 체크
            if (originalEmail) {
                checkEmail();
            }
        });
    </script>
</body>
</html>
