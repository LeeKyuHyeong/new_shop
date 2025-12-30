<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="activeMenu" value="setting"/>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>사이트 설정 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/setting.css">
</head>
<body>
    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/common/sidebar.jsp"/>

        <main class="main-content">
            <header class="top-bar">
                <h1>사이트 설정</h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <div class="alert-container"></div>

                <div class="setting-container">
                    <form id="settingForm" class="form">
                        <div class="setting-section">
                            <h3>기본 설정</h3>
                            <div class="form-group">
                                <label for="siteName">사이트명</label>
                                <input type="text" id="siteName" name="siteName" value="${siteName}" placeholder="사이트명 입력">
                            </div>
                        </div>

                        <div class="setting-section">
                            <h3>슬라이드 설정</h3>
                            <div class="form-group">
                                <label for="slideDuration">슬라이드 지속시간 (초)</label>
                                <input type="number" id="slideDuration" name="slideDuration" value="${slideDuration}" min="1" max="30" placeholder="5">
                                <small class="form-hint">각 슬라이드가 표시되는 시간 (1~30초)</small>
                            </div>
                        </div>

                        <div class="setting-section">
                            <h3>팝업 설정</h3>
                            <div class="form-group">
                                <label for="popupDuration">팝업 지속시간 (일)</label>
                                <input type="number" id="popupDuration" name="popupDuration" value="${popupDuration}" min="1" max="365" placeholder="1">
                                <small class="form-hint">"오늘 하루 보지 않기" 체크 시 팝업이 숨겨지는 기간 (1~365일)</small>
                            </div>
                        </div>

                        <div class="form-buttons">
                            <button type="submit" class="btn btn-primary">저장</button>
                        </div>
                    </form>
                </div>
            </div>
        </main>
    </div>

    <script>
        const contextPath = '${pageContext.request.contextPath}';

        document.getElementById('settingForm').addEventListener('submit', function(e) {
            e.preventDefault();

            const siteName = document.getElementById('siteName').value.trim();
            const slideDuration = document.getElementById('slideDuration').value;
            const popupDuration = document.getElementById('popupDuration').value;

            fetch(contextPath + '/api/admin/setting/save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams({
                    siteName: siteName,
                    slideDuration: slideDuration,
                    popupDuration: popupDuration
                })
            })
            .then(response => response.json())
            .then(data => {
                showAlert(data.message, data.success ? 'success' : 'error');
            })
            .catch(error => {
                showAlert('요청 중 오류가 발생했습니다', 'error');
            });
        });

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
