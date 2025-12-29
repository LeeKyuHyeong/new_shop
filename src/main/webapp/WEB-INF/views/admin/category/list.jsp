<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>카테고리 관리 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/category.css">
</head>
<body>

    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/common/sidebar.jsp"/>

        <main class="main-content">
            <header class="top-bar">
                <h1>카테고리 관리</h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <div class="alert-container"></div>

                <div class="page-header">
                    <a href="${pageContext.request.contextPath}/admin/category/create" class="btn btn-primary">카테고리 추가</a>
                </div>

                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>카테고리명</th>
                                <th>설명</th>
                                <th>순서</th>
                                <th>작성일</th>
                                <th>관리</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="parent" items="${parentCategories}">
                                <%-- 상위 카테고리 행 --%>
                                <tr class="parent-row" data-category-id="${parent.categoryId}">
                                    <td>${parent.categoryId}</td>
                                    <td class="category-name">
                                        <span class="parent-icon">📁</span>
                                        <strong>${parent.categoryName}</strong>
                                        <c:if test="${not empty parent.children}">
                                            <span class="child-count">(${parent.children.size()})</span>
                                        </c:if>
                                    </td>
                                    <td>${parent.categoryDescription}</td>
                                    <td>${parent.categoryOrder}</td>
                                    <td>"${parent.createdDate}"</td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/admin/category/edit/${parent.categoryId}" class="btn btn-small btn-info">수정</a>
                                        <button class="btn btn-small btn-danger" onclick="deleteCategory(${parent.categoryId})">삭제</button>
                                    </td>
                                </tr>
                                <%-- 하위 카테고리 행 --%>
                                <c:forEach var="child" items="${parent.children}">
                                    <c:if test="${child.useYn eq 'Y'}">
                                        <tr class="child-row" data-category-id="${child.categoryId}" data-parent-id="${parent.categoryId}">
                                            <td>${child.categoryId}</td>
                                            <td class="category-name child-category">
                                                <span class="child-indent">└</span>
                                                ${child.categoryName}
                                            </td>
                                            <td>${child.categoryDescription}</td>
                                            <td>${child.categoryOrder}</td>
                                            <td>"${child.createdDate}"</td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/admin/category/edit/${child.categoryId}" class="btn btn-small btn-info">수정</a>
                                                <button class="btn btn-small btn-danger" onclick="deleteCategory(${child.categoryId})">삭제</button>
                                            </td>
                                        </tr>
                                    </c:if>
                                </c:forEach>
                            </c:forEach>
                        </tbody>
                    </table>
                    <c:if test="${empty parentCategories}">
                        <div class="empty-message">등록된 카테고리가 없습니다.</div>
                    </c:if>
                </div>
            </div>
        </main>
    </div>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
    </script>
    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
    <script src="${pageContext.request.contextPath}/js/admin/category.js"></script>
</body>
</html>
