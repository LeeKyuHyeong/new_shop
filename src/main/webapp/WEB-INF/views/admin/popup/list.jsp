<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>팝업 관리 - KH SHOP Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/popup.css">
</head>
<body>
    <div class="admin-container">
        <%@ include file="../common/sidebar.jsp" %>

        <main class="main-content">
        <header class="top-bar">
            <h1>팝업 관리</h1>
            <div class="user-info">
                <%= session.getAttribute("loggedInUser") %>님
            </div>

        </header>

        <div class="content">

            <div class="page-header">
                <a href="${pageContext.request.contextPath}/admin/popup/create" class="btn btn-primary">팝업 추가</a>
            </div>
            <div class="table-container">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>제목</th>
                            <th>크기 (W x H)</th>
                            <th>위치 (Top x Left)</th>
                            <th>순서</th>
                            <th>사용여부</th>
                            <th>등록일</th>
                            <th>관리</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty popups}">
                                <tr>
                                    <td colspan="8" class="text-center">등록된 팝업이 없습니다</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="popup" items="${popups}">
                                    <tr>
                                        <td data-label="ID">${popup.popupId}</td>
                                        <td data-label="제목">${popup.popupTitle}</td>
                                        <td data-label="크기">${popup.popupWidth} x ${popup.popupHeight}</td>
                                        <td data-label="위치">${popup.popupTop} x ${popup.popupLeft}</td>
                                        <td data-label="순서">${popup.popupOrder}</td>
                                        <td data-label="사용여부">
                                            <c:choose>
                                                <c:when test="${popup.useYn eq 'Y'}">
                                                    <span class="badge badge-success">사용</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-secondary">미사용</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td data-label="등록일">${popup.createdDate}</td>
                                        <td class="action-cell">
                                            <a href="${pageContext.request.contextPath}/admin/popup/edit/${popup.popupId}" class="btn btn-small btn-secondary">수정</a>
                                            <button onclick="deletePopup(${popup.popupId})" class="btn btn-small btn-danger">삭제</button>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
    </div>

    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
    <script src="${pageContext.request.contextPath}/js/admin/popup.js"></script>
</body>
</html>
