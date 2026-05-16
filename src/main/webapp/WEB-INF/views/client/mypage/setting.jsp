<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%@ include file="/WEB-INF/views/common/security-headers.jsp" %>
    <title>개인설정 - KH SHOP</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/mypage.css">
</head>
<body>
    <%@ include file="../common/header.jsp" %>

    <div class="mypage-container">
        <div class="mypage-sidebar">
            <h3>마이페이지</h3>
            <nav class="mypage-nav">
                <a href="${pageContext.request.contextPath}/mypage/orders">주문내역</a>
                <a href="${pageContext.request.contextPath}/mypage/setting" class="active">개인설정</a>
            </nav>
        </div>

        <div class="mypage-content">
            <h2 class="mypage-title">개인설정</h2>

            <!-- 프로필 정보 -->
            <section class="mypage-section">
                <h3 class="section-title">프로필 정보</h3>
                    <div class="profile-info">
                        <div class="info-row">
                            <span class="info-label">아이디</span>
                            <span class="info-value">${user.userId}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">이름</span>
                            <span class="info-value">${user.userName}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">이메일</span>
                            <span class="info-value">${user.email}</span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">성별</span>
                            <span class="info-value">
                                <c:choose>
                                    <c:when test="${user.gender == 'M'}">남성</c:when>
                                    <c:when test="${user.gender == 'F'}">여성</c:when>
                                    <c:otherwise>미설정</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">생년월일</span>
                            <span class="info-value">
                                <c:choose>
                                    <c:when test="${not empty user.birth}">
                                        <fmt:parseDate value="${user.birth}" pattern="yyyy-MM-dd" var="parsedDate" type="date"/>
                                        <fmt:formatDate value="${parsedDate}" pattern="yyyy년 MM월 dd일"/>
                                    </c:when>
                                    <c:otherwise>미설정</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">가입일</span>
                            <span class="info-value">
                                <fmt:parseDate value="${user.createdDate}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="createdDate" type="both"/>
                                <fmt:formatDate value="${createdDate}" pattern="yyyy년 MM월 dd일"/>
                            </span>
                        </div>
                    </div>
                </section>

                <!-- 화면 설정 -->
                <section class="mypage-section">
                    <h3 class="section-title">화면 설정</h3>
                    <div class="setting-item">
                        <div class="setting-info">
                            <span class="setting-name">테마</span>
                            <span class="setting-desc">화면 테마를 변경합니다</span>
                        </div>
                        <div class="setting-control">
                            <div class="theme-toggle-wrapper">
                                <button type="button" class="theme-btn ${setting.theme == 'LIGHT' ? 'active' : ''}" 
                                        data-theme="LIGHT" onclick="setTheme('LIGHT')">
                                    ☀️ 라이트
                                </button>
                                <button type="button" class="theme-btn ${setting.theme == 'DARK' ? 'active' : ''}" 
                                        data-theme="DARK" onclick="setTheme('DARK')">
                                    🌙 다크
                                </button>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- 알림 설정 -->
                <section class="mypage-section">
                    <h3 class="section-title">알림 설정</h3>
                    <div class="setting-item">
                        <div class="setting-info">
                            <span class="setting-name">알림 수신</span>
                            <span class="setting-desc">주문 상태 변경 등의 알림을 받습니다</span>
                        </div>
                        <div class="setting-control">
                            <label class="toggle-switch">
                                <input type="checkbox" id="notificationYn" 
                                       ${setting.notificationYn == 'Y' ? 'checked' : ''} 
                                       onchange="saveSetting('notificationYn', this.checked ? 'Y' : 'N')">
                                <span class="toggle-slider"></span>
                            </label>
                        </div>
                    </div>
                    <div class="setting-item">
                        <div class="setting-info">
                            <span class="setting-name">이메일 수신</span>
                            <span class="setting-desc">프로모션 및 이벤트 이메일을 받습니다</span>
                        </div>
                        <div class="setting-control">
                            <label class="toggle-switch">
                                <input type="checkbox" id="emailReceiveYn" 
                                       ${setting.emailReceiveYn == 'Y' ? 'checked' : ''} 
                                       onchange="saveSetting('emailReceiveYn', this.checked ? 'Y' : 'N')">
                                <span class="toggle-slider"></span>
                            </label>
                        </div>
                    </div>
                </section>

                <!-- 소셜 계정 연동 -->
                <section class="mypage-section">
                    <h3 class="section-title">소셜 계정 연동</h3>
                    <p class="section-desc">연동된 소셜 계정으로 빠르게 로그인할 수 있습니다. 해제 시 본인 비밀번호 확인이 필요합니다.</p>

                    <c:if test="${not empty settingMessage}">
                        <div class="alert alert-success" role="status">${settingMessage}</div>
                    </c:if>
                    <c:if test="${not empty settingError}">
                        <div class="alert alert-error" role="alert">${settingError}</div>
                    </c:if>

                    <div class="social-link-list">
                        <c:forEach var="provider" items="${supportedProviders}">
                            <c:set var="account" value="${linkedSocials[provider]}" />
                            <c:set var="label" value="${provider == 'KAKAO' ? '카카오' : (provider == 'NAVER' ? '네이버' : '구글')}" />
                            <div class="social-link-item social-${provider}">
                                <div class="social-link-info">
                                    <span class="social-link-name">${label}</span>
                                    <c:choose>
                                        <c:when test="${not empty account}">
                                            <span class="social-link-status linked">연동됨</span>
                                            <c:if test="${not empty account.socialEmail}">
                                                <span class="social-link-meta">${account.socialEmail}</span>
                                            </c:if>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="social-link-status unlinked">미연동</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="social-link-action">
                                    <c:choose>
                                        <c:when test="${not empty account}">
                                            <button type="button" class="btn btn-secondary btn-sm"
                                                    onclick="openUnlinkDialog('${provider}', '${label}')">
                                                해제
                                            </button>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="${pageContext.request.contextPath}/oauth/link/${provider}"
                                               class="btn btn-primary btn-sm">연동</a>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </section>

                <!-- 연동 해제 모달 -->
                <div id="unlinkDialog" class="modal-backdrop" hidden>
                    <div class="modal-panel" role="dialog" aria-modal="true" aria-labelledby="unlinkDialogTitle">
                        <h4 id="unlinkDialogTitle" class="modal-title">
                            <span id="unlinkProviderLabel"></span> 연동 해제
                        </h4>
                        <p class="modal-desc">계정 보안을 위해 현재 비밀번호를 입력해주세요.</p>
                        <form id="unlinkForm" class="modal-form">
                            <input type="hidden" id="unlinkProvider" name="provider" value="">
                            <div class="form-group">
                                <label for="unlinkPassword">비밀번호</label>
                                <input type="password" id="unlinkPassword" name="password" required autocomplete="current-password">
                            </div>
                            <div class="modal-actions">
                                <button type="button" class="btn btn-secondary" onclick="closeUnlinkDialog()">취소</button>
                                <button type="submit" class="btn btn-primary">해제</button>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- 비밀번호 변경 -->
                <section class="mypage-section">
                    <h3 class="section-title">비밀번호 변경</h3>
                    <form id="passwordForm" class="password-form">
                        <div class="form-group">
                            <label for="currentPassword">현재 비밀번호</label>
                            <input type="password" id="currentPassword" name="currentPassword" required>
                        </div>
                        <div class="form-group">
                            <label for="newPassword">새 비밀번호</label>
                            <input type="password" id="newPassword" name="newPassword" required>
                        </div>
                        <div class="form-group">
                            <label for="confirmPassword">새 비밀번호 확인</label>
                            <input type="password" id="confirmPassword" name="confirmPassword" required>
                        </div>
                        <button type="submit" class="btn btn-primary">비밀번호 변경</button>
                    </form>
                </section>
            </div>
        </div>
    </div>

    <!-- 푸터 -->
    <footer class="footer">
        <p>&copy; 2024 KH SHOP. All rights reserved.</p>
    </footer>

    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';

        // 테마 설정
        function setTheme(theme) {
            // 버튼 상태 업데이트
            document.querySelectorAll('.theme-btn').forEach(btn => {
                btn.classList.remove('active');
                if (btn.dataset.theme === theme) {
                    btn.classList.add('active');
                }
            });

            // 바디 클래스 적용
            if (theme === 'DARK') {
                document.body.classList.add('dark-mode');
            } else {
                document.body.classList.remove('dark-mode');
            }

            // localStorage 저장
            localStorage.setItem('theme', theme.toLowerCase());

            // 헤더의 테마 버튼 텍스트 업데이트
            updateThemeButton();

            // 서버에 저장
            fetch(contextPath + '/api/setting/theme', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'theme=' + theme
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showToast('테마가 변경되었습니다');
                }
            })
            .catch(err => console.error('Theme save error:', err));
        }

        // 개별 설정 저장
        function saveSetting(settingName, value) {
            const formData = new URLSearchParams();
            formData.append(settingName, value);

            fetch(contextPath + '/api/setting/save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: formData.toString()
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showToast('설정이 저장되었습니다');
                } else {
                    showToast(data.message, 'error');
                }
            })
            .catch(err => {
                console.error('Setting save error:', err);
                showToast('설정 저장 중 오류가 발생했습니다', 'error');
            });
        }

        // 비밀번호 변경
        document.getElementById('passwordForm').addEventListener('submit', function(e) {
            e.preventDefault();

            const currentPassword = document.getElementById('currentPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (newPassword !== confirmPassword) {
                showToast('새 비밀번호가 일치하지 않습니다', 'error');
                return;
            }

            if (newPassword.length < 4) {
                showToast('비밀번호는 4자 이상이어야 합니다', 'error');
                return;
            }

            const formData = new URLSearchParams();
            formData.append('currentPassword', currentPassword);
            formData.append('newPassword', newPassword);
            formData.append('confirmPassword', confirmPassword);

            fetch(contextPath + '/api/setting/password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: formData.toString()
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showToast('비밀번호가 변경되었습니다');
                    document.getElementById('passwordForm').reset();
                } else {
                    showToast(data.message, 'error');
                }
            })
            .catch(err => {
                console.error('Password change error:', err);
                showToast('비밀번호 변경 중 오류가 발생했습니다', 'error');
            });
        });

        // ===== 소셜 계정 연동 해제 =====
        function openUnlinkDialog(provider, label) {
            document.getElementById('unlinkProvider').value = provider;
            document.getElementById('unlinkProviderLabel').textContent = label;
            document.getElementById('unlinkPassword').value = '';
            const dialog = document.getElementById('unlinkDialog');
            dialog.hidden = false;
            // 비밀번호 입력에 포커스
            setTimeout(() => document.getElementById('unlinkPassword').focus(), 50);
        }

        function closeUnlinkDialog() {
            document.getElementById('unlinkDialog').hidden = true;
        }

        // 배경 클릭 시 닫기 (패널 영역 클릭은 닫지 않음)
        document.getElementById('unlinkDialog').addEventListener('click', function(e) {
            if (e.target === this) closeUnlinkDialog();
        });

        document.getElementById('unlinkForm').addEventListener('submit', function(e) {
            e.preventDefault();

            const provider = document.getElementById('unlinkProvider').value;
            const password = document.getElementById('unlinkPassword').value;

            if (!password) {
                showToast('비밀번호를 입력해주세요', 'error');
                return;
            }

            const body = new URLSearchParams();
            body.append('provider', provider);
            body.append('password', password);

            fetch(contextPath + '/api/setting/social/unlink', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: body.toString()
            })
            .then(r => r.json())
            .then(data => {
                if (data.success) {
                    showToast(data.message || '연동이 해제되었습니다');
                    closeUnlinkDialog();
                    // 상태 반영을 위해 새로고침
                    setTimeout(() => window.location.reload(), 600);
                } else {
                    showToast(data.message || '연동 해제 실패', 'error');
                }
            })
            .catch(err => {
                console.error('Unlink error:', err);
                showToast('연동 해제 중 오류가 발생했습니다', 'error');
            });
        });

        // 토스트 메시지
        function showToast(message, type = 'success') {
            // 기존 토스트 제거
            const existingToast = document.querySelector('.toast');
            if (existingToast) {
                existingToast.remove();
            }

            const toast = document.createElement('div');
            toast.className = 'toast ' + type;
            toast.textContent = message;
            document.body.appendChild(toast);

            setTimeout(() => {
                toast.classList.add('show');
            }, 10);

            setTimeout(() => {
                toast.classList.remove('show');
                setTimeout(() => toast.remove(), 300);
            }, 3000);
        }

        // 장바구니 카운트
        function loadCartCount() {
            fetch(contextPath + '/cart/count')
                .then(response => response.json())
                .then(data => {
                    const countEl = document.getElementById('cartCount');
                    if (countEl && data.count > 0) {
                        countEl.textContent = data.count;
                        countEl.style.display = 'inline-flex';
                    }
                })
                .catch(err => console.log('Cart count error:', err));
        }

        document.addEventListener('DOMContentLoaded', function() {
            loadCartCount();
        });
    </script>
</body>
</html>
