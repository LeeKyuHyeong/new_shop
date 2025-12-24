<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<c:set var="activeMenu" value="dashboard"/>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>관리자 대시보드 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body>

    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/common/sidebar.jsp"/>

        <main class="main-content">
            <header class="top-bar">
                <h1>대시보드</h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <div class="welcome-section">
                    <h2>환영합니다!</h2>
                    <p>관리자 대시보드입니다. 좌측 메뉴에서 관리할 항목을 선택하세요.</p>
                </div>
            </div>
        </main>
    </div>

    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
</body>
</html>