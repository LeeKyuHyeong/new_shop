<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>카테고리 관리 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-category.css">
</head>
<body>
    <div class="theme-toggle">
        <button id="themeBtn" onclick="toggleTheme()">🌙</button>
    </div>

    <div class="admin-container">
        <aside class="sidebar">
            <div class="sidebar-header">
                <h2>KH SHOP Admin</h2>
            </div>

            <nav class="sidebar-menu">
                <a href="${pageContext.request.contextPath}/admin" class="menu-item">대시보드</a>
                <a href="${pageContext.request.contextPath}/admin/category" class="menu-item active">카테고리 관리</a>
                <a href="${pageContext.request.contextPath}/admin/product" class="menu-item">상품 관리</a>
                <a href="${pageContext.request.contextPath}/admin/order" class="menu-item">주문 관리</a>
                <a href="${pageContext.request.contextPath}/logout" class="menu-item logout">로그아웃</a>
            </nav>
        </aside>

        <main class="main-content">
            <header class="top-bar">
                <h1><c:if test="${empty category}">카테고리 추가</c:if><c:if test="${not empty category}">카테고리 수정</c:if></h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <div class="alert-container"></div>

                <div class="form-box">
                    <form id="categoryForm" class="form">
                        <c:if test="${not empty category}">
                            <input type="hidden" id="categoryId" value="${category.categoryId}">
                        </c:if>

                        <div class="form-group">
                            <label for="parentId">상위 카테고리</label>
                            <select id="parentId" name="parentId">
                                <option value="">상위 카테고리 (대분류)</option>
                                <c:forEach var="parent" items="${parentCategories}">
                                    <%-- 수정 시 자기 자신은 상위 카테고리로 선택 불가 --%>
                                    <c:if test="${empty category || category.categoryId != parent.categoryId}">
                                        <option value="${parent.categoryId}"
                                            <c:if test="${not empty category && category.parentId eq parent.categoryId}">selected</c:if>>
                                            ${parent.categoryName}
                                        </option>
                                    </c:if>
                                </c:forEach>
                            </select>
                            <small class="form-hint">비워두면 상위 카테고리(대분류)로 등록됩니다.</small>
                        </div>

                        <div class="form-group">
                            <label for="categoryName">카테고리명 *</label>
                            <input type="text" id="categoryName" name="categoryName" placeholder="카테고리명 입력" required
                                value="<c:if test="${not empty category}">${category.categoryName}</c:if>">
                        </div>

                        <div class="form-group">
                            <label for="categoryDescription">설명</label>
                            <textarea id="categoryDescription" name="categoryDescription" placeholder="카테고리 설명 입력" rows="5"><c:if test="${not empty category}">${category.categoryDescription}</c:if></textarea>
                        </div>

                        <div class="form-group">
                            <label for="categoryOrder">순서</label>
                            <input type="number" id="categoryOrder" name="categoryOrder" placeholder="순서 입력" value="<c:if test="${not empty category}">${category.categoryOrder}</c:if>" min="0">
                        </div>

                        <div class="form-buttons">
                            <button type="submit" class="btn btn-primary">
                                <c:if test="${empty category}">등록</c:if>
                                <c:if test="${not empty category}">수정</c:if>
                            </button>
                            <a href="${pageContext.request.contextPath}/admin/category" class="btn btn-secondary">취소</a>
                        </div>
                    </form>
                </div>
            </div>
        </main>
    </div>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
        const isEdit = ${not empty category};
    </script>
    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
    <script src="${pageContext.request.contextPath}/js/admin-category-form.js"></script>
</body>
</html>
