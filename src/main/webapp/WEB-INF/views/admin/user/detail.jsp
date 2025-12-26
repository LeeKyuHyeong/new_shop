<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="activeMenu" value="user"/>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>사용자 상세 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/user.css">
</head>
<body>
    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/common/sidebar.jsp"/>

        <main class="main-content">
            <header class="top-bar">
                <h1>사용자 상세</h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <div class="alert-container"></div>

                <div class="detail-container">
                    <div class="detail-section">
                        <h3>기본 정보</h3>
                        <div class="info-grid">
                            <div class="info-item">
                                <span class="info-label">아이디</span>
                                <span class="info-value">${user.userId}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">이름</span>
                                <span class="info-value">${user.userName}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">이메일</span>
                                <span class="info-value">${user.email}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">성별</span>
                                <span class="info-value">
                                    <c:if test="${user.gender eq 'M'}">남성</c:if>
                                    <c:if test="${user.gender eq 'F'}">여성</c:if>
                                </span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">생년월일</span>
                                <span class="info-value">${user.birth}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">가입일</span>
                                <span class="info-value">
                                    <fmt:parseDate value="${user.createdDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedCreated" type="both"/>
                                    <fmt:formatDate value="${parsedCreated}" pattern="yyyy-MM-dd HH:mm"/>
                                </span>
                            </div>
                        </div>
                    </div>

                    <div class="detail-section">
                        <h3>권한 관리</h3>
                        <div class="action-row">
                            <span class="current-status">
                                현재 권한:
                                <c:if test="${user.userRole eq 'ADMIN'}">
                                    <span class="badge badge-admin">관리자</span>
                                </c:if>
                                <c:if test="${user.userRole eq 'USER'}">
                                    <span class="badge badge-user">일반</span>
                                </c:if>
                            </span>
                            <div class="action-buttons">
                                <c:if test="${user.userRole eq 'USER'}">
                                    <button class="btn btn-small btn-primary" onclick="changeRole('${user.userId}', 'ADMIN')">관리자로 변경</button>
                                </c:if>
                                <c:if test="${user.userRole eq 'ADMIN'}">
                                    <button class="btn btn-small btn-secondary" onclick="changeRole('${user.userId}', 'USER')">일반으로 변경</button>
                                </c:if>
                            </div>
                        </div>
                    </div>

                    <div class="detail-section">
                        <h3>계정 상태</h3>
                        <div class="action-row">
                            <span class="current-status">
                                현재 상태:
                                <c:if test="${user.useYn eq 'Y'}">
                                    <span class="badge badge-active">활성</span>
                                </c:if>
                                <c:if test="${user.useYn eq 'N'}">
                                    <span class="badge badge-inactive">비활성</span>
                                </c:if>
                            </span>
                            <div class="action-buttons">
                                <c:if test="${user.useYn eq 'Y'}">
                                    <button class="btn btn-small btn-danger" onclick="changeStatus('${user.userId}', 'N')">비활성화</button>
                                </c:if>
                                <c:if test="${user.useYn eq 'N'}">
                                    <button class="btn btn-small btn-primary" onclick="changeStatus('${user.userId}', 'Y')">활성화</button>
                                </c:if>
                            </div>
                        </div>
                    </div>

                    <div class="detail-section">
                        <h3>비밀번호 초기화</h3>
                        <div class="password-reset-form">
                            <input type="password" id="newPassword" placeholder="새 비밀번호 입력" class="password-input">
                            <button class="btn btn-small btn-warning" onclick="resetPassword('${user.userId}')">비밀번호 초기화</button>
                        </div>
                    </div>

                    <div class="form-buttons">
                        <a href="${pageContext.request.contextPath}/admin/user" class="btn btn-secondary">목록으로</a>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <script>
        const contextPath = '${pageContext.request.contextPath}';

        function changeRole(userId, role) {
            if (!confirm('권한을 변경하시겠습니까?')) return;

            fetch(contextPath + '/api/admin/user/role/' + userId, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams({ role: role })
            })
            .then(response => response.json())
            .then(data => {
                showAlert(data.message, data.success ? 'success' : 'error');
                if (data.success) {
                    setTimeout(() => location.reload(), 500);
                }
            })
            .catch(error => {
                showAlert('요청 중 오류가 발생했습니다', 'error');
            });
        }

        function changeStatus(userId, useYn) {
            const msg = useYn === 'Y' ? '계정을 활성화하시겠습니까?' : '계정을 비활성화하시겠습니까?';
            if (!confirm(msg)) return;

            fetch(contextPath + '/api/admin/user/status/' + userId, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams({ useYn: useYn })
            })
            .then(response => response.json())
            .then(data => {
                showAlert(data.message, data.success ? 'success' : 'error');
                if (data.success) {
                    setTimeout(() => location.reload(), 500);
                }
            })
            .catch(error => {
                showAlert('요청 중 오류가 발생했습니다', 'error');
            });
        }

        function resetPassword(userId) {
            const newPassword = document.getElementById('newPassword').value;
            if (!newPassword || newPassword.length < 4) {
                showAlert('비밀번호는 4자 이상 입력하세요', 'error');
                return;
            }
            if (!confirm('비밀번호를 초기화하시겠습니까?')) return;

            fetch(contextPath + '/api/admin/user/reset-password/' + userId, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams({ newPassword: newPassword })
            })
            .then(response => response.json())
            .then(data => {
                showAlert(data.message, data.success ? 'success' : 'error');
                if (data.success) {
                    document.getElementById('newPassword').value = '';
                }
            })
            .catch(error => {
                showAlert('요청 중 오류가 발생했습니다', 'error');
            });
        }

        function showAlert(message, type) {
            const alertContainer = document.querySelector('.alert-container');
            const alert = document.createElement('div');
            alert.className = `alert alert-${type}`;
            alert.textContent = message;
            alertContainer.innerHTML = '';
            alertContainer.appendChild(alert);
        }
    </script>
    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
</body>
</html>
