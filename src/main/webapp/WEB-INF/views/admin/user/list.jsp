<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="activeMenu" value="user"/>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>사용자 관리 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/user.css">
</head>
<body>
    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/common/sidebar.jsp"/>

        <main class="main-content">
            <header class="top-bar">
                <h1>사용자 관리</h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <div class="alert-container"></div>

                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>아이디</th>
                                <th>이름</th>
                                <th>이메일</th>
                                <th>성별</th>
                                <th>권한</th>
                                <th>상태</th>
                                <th>가입일</th>
                                <th>관리</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="user" items="${users}">
                                <tr data-user-id="${user.userId}">
                                    <td>
                                        <a href="${pageContext.request.contextPath}/admin/user/detail/${user.userId}" class="user-id-link">
                                            ${user.userId}
                                        </a>
                                    </td>
                                    <td>${user.userName}</td>
                                    <td>${user.email}</td>
                                    <td>
                                        <c:if test="${user.gender eq 'M'}">남</c:if>
                                        <c:if test="${user.gender eq 'F'}">여</c:if>
                                    </td>
                                    <td>
                                        <c:if test="${user.userRole eq 'ADMIN'}">
                                            <span class="badge badge-admin">관리자</span>
                                        </c:if>
                                        <c:if test="${user.userRole eq 'USER'}">
                                            <span class="badge badge-user">일반</span>
                                        </c:if>
                                    </td>
                                    <td>
                                        <c:if test="${user.useYn eq 'Y'}">
                                            <span class="badge badge-active">활성</span>
                                        </c:if>
                                        <c:if test="${user.useYn eq 'N'}">
                                            <span class="badge badge-inactive">비활성</span>
                                        </c:if>
                                    </td>
                                    <td>
                                        <fmt:parseDate value="${user.createdDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both"/>
                                        <fmt:formatDate value="${parsedDate}" pattern="yyyy-MM-dd"/>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/admin/user/detail/${user.userId}" class="btn btn-small btn-info">상세</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                    <c:if test="${empty users}">
                        <div class="empty-message">등록된 사용자가 없습니다.</div>
                    </c:if>
                </div>
            </div>
        </main>
    </div>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
    </script>
    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
</body>
</html>
