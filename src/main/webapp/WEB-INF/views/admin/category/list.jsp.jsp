<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="activeMenu" value="category"/>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>카테고리 관리 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/category.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/pagination.css">
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

                <%-- 검색/필터 영역 --%>
                <div class="search-filter-box">
                    <form id="searchForm" method="get" action="${pageContext.request.contextPath}/admin/category">
                        <input type="hidden" name="size" value="${pageRequestDTO.size}">

                        <div class="filter-row">
                            <div class="filter-group">
                                <label for="categoryId">카테고리 필터</label>
                                <select id="categoryId" name="categoryId" class="form-select">
                                    <option value="">전체</option>
                                    <c:forEach var="parent" items="${parentCategories}">
                                        <option value="${parent.categoryId}"
                                                ${pageRequestDTO.categoryId eq parent.categoryId ? 'selected' : ''}>
                                            📁 ${parent.categoryName}
                                        </option>
                                        <c:forEach var="child" items="${parent.children}">
                                            <option value="${child.categoryId}"
                                                    ${pageRequestDTO.categoryId eq child.categoryId ? 'selected' : ''}>
                                                &nbsp;&nbsp;&nbsp;└ ${child.categoryName}
                                            </option>
                                        </c:forEach>
                                    </c:forEach>
                                </select>
                            </div>
                            
                            <div class="filter-group">
                                <label for="searchKeyword">카테고리명 검색</label>
                                <input type="text" id="searchKeyword" name="searchKeyword" 
                                       class="form-input" placeholder="카테고리명 입력"
                                       value="${pageRequestDTO.searchKeyword}">
                            </div>

                            <div class="filter-actions">
                                <button type="submit" class="btn btn-primary">검색</button>
                                <button type="button" class="btn btn-secondary" onclick="resetSearch()">초기화</button>
                            </div>
                        </div>
                    </form>
                </div>

                <%-- 목록 옵션 --%>
                <div class="list-options">
                    <div class="list-info">
                        <span>총 <strong>${result.totalCount}</strong>개</span>
                    </div>
                    <div class="list-controls">
                        <select id="pageSize" class="form-select-sm" onchange="changePageSize(this.value)">
                            <option value="10" ${pageRequestDTO.size eq 10 ? 'selected' : ''}>10개씩</option>
                            <option value="20" ${pageRequestDTO.size eq 20 ? 'selected' : ''}>20개씩</option>
                            <option value="50" ${pageRequestDTO.size eq 50 ? 'selected' : ''}>50개씩</option>
                        </select>
                        <a href="${pageContext.request.contextPath}/admin/category/create" class="btn btn-primary">카테고리 추가</a>
                    </div>
                </div>

                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>카테고리명</th>
                                <th>상위 카테고리</th>
                                <th>설명</th>
                                <th>순서</th>
                                <th>작성일</th>
                                <th>관리</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:if test="${empty result.dtoList}">
                                <tr>
                                    <td colspan="7" class="empty-message">등록된 카테고리가 없습니다.</td>
                                </tr>
                            </c:if>
                            <c:forEach var="category" items="${result.dtoList}">
                                <tr class="${empty category.parent ? 'parent-row' : 'child-row'}" data-category-id="${category.categoryId}">
                                    <td>${category.categoryId}</td>
                                    <td class="category-name">
                                        <c:choose>
                                            <c:when test="${empty category.parent}">
                                                <span class="parent-icon">📁</span>
                                                <strong>${category.categoryName}</strong>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="child-indent">└</span>
                                                ${category.categoryName}
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty category.parent}">
                                                <span class="parent-badge">${category.parent.categoryName}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="root-badge">최상위</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${category.categoryDescription}</td>
                                    <td>${category.categoryOrder}</td>
                                    <td>
                                        <fmt:parseDate value="${category.createdDate}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="parsedDate" type="both"/>
                                        <fmt:formatDate value="${parsedDate}" pattern="yyyy-MM-dd HH:mm"/>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/admin/category/edit/${category.categoryId}" class="btn btn-small btn-info">수정</a>
                                        <button class="btn btn-small btn-danger" onclick="deleteCategory(${category.categoryId})">삭제</button>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
                
                <%-- 페이징 --%>
                <jsp:include page="/WEB-INF/views/common/pagination.jsp">
                    <jsp:param name="theme" value="admin"/>
                </jsp:include>
            </div>
        </main>
    </div>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
    </script>
    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
    <script src="${pageContext.request.contextPath}/js/common/pagination.js"></script>
    <script src="${pageContext.request.contextPath}/js/admin/category.js"></script>
</body>
</html>
