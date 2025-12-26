<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="activeMenu" value="slide"/>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>슬라이드 관리 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/slide.css">
</head>
<body>

    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/common/sidebar.jsp"/>

        <main class="main-content">
            <header class="top-bar">
                <h1>슬라이드 관리</h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <div class="alert-container"></div>

                <div class="page-header">
                    <a href="${pageContext.request.contextPath}/admin/slide/create" class="btn btn-primary">슬라이드 추가</a>
                </div>

                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>이미지</th>
                                <th>제목</th>
                                <th>순서</th>
                                <th>기간</th>
                                <th>상태</th>
                                <th>등록일</th>
                                <th>관리</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="slide" items="${slides}">
                                <tr data-slide-id="${slide.slideId}">
                                    <td>${slide.slideId}</td>
                                    <td class="thumbnail-cell">
                                        <img src="${pageContext.request.contextPath}${slide.imageUrl}" alt="${slide.slideTitle}" class="slide-thumb">
                                    </td>
                                    <td>
                                        <div class="slide-title">${slide.slideTitle}</div>
                                        <c:if test="${not empty slide.linkUrl}">
                                            <div class="slide-link">${slide.linkUrl}</div>
                                        </c:if>
                                    </td>
                                    <td>${slide.slideOrder}</td>
                                    <td class="period-cell">
                                        <c:if test="${not empty slide.startDate || not empty slide.endDate}">
                                            <c:if test="${not empty slide.startDate}">
                                                <fmt:parseDate value="${slide.startDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedStart" type="both"/>
                                                <fmt:formatDate value="${parsedStart}" pattern="MM/dd HH:mm"/>
                                            </c:if>
                                            ~
                                            <c:if test="${not empty slide.endDate}">
                                                <fmt:parseDate value="${slide.endDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedEnd" type="both"/>
                                                <fmt:formatDate value="${parsedEnd}" pattern="MM/dd HH:mm"/>
                                            </c:if>
                                        </c:if>
                                        <c:if test="${empty slide.startDate && empty slide.endDate}">
                                            <span class="text-muted">상시</span>
                                        </c:if>
                                    </td>
                                    <td>
                                        <c:if test="${slide.active}">
                                            <span class="badge badge-success">활성</span>
                                        </c:if>
                                        <c:if test="${!slide.active}">
                                            <span class="badge badge-secondary">비활성</span>
                                        </c:if>
                                    </td>
                                    <td>
                                        <fmt:parseDate value="${slide.createdDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedCreated" type="both"/>
                                        <fmt:formatDate value="${parsedCreated}" pattern="yyyy-MM-dd"/>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/admin/slide/edit/${slide.slideId}" class="btn btn-small btn-info">수정</a>
                                        <button class="btn btn-small btn-danger" onclick="deleteSlide(${slide.slideId})">삭제</button>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                    <c:if test="${empty slides}">
                        <div class="empty-message">등록된 슬라이드가 없습니다.</div>
                    </c:if>
                </div>
            </div>
        </main>
    </div>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
    </script>
    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
    <script src="${pageContext.request.contextPath}/js/admin/slide.js"></script>
</body>
</html>
