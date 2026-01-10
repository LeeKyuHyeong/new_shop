<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="activeMenu" value="product"/>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>상품 관리 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/product.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/pagination.css">
</head>
<body>

    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/common/sidebar.jsp"/>

        <main class="main-content">
            <header class="top-bar">
                <h1>상품 관리</h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <div class="alert-container"></div>

                <%-- 검색/필터 영역 --%>
                <div class="search-filter-box">
                    <form id="searchForm" method="get" action="${pageContext.request.contextPath}/admin/product">
                        <input type="hidden" name="size" value="${pageRequestDTO.size}">
                        
                        <div class="filter-row">
                            <div class="filter-group">
                                <label for="categoryId">카테고리</label>
                                <select id="categoryId" name="categoryId" class="form-select">
                                    <option value="">전체</option>
                                    <c:forEach var="parent" items="${parentCategories}">
                                        <optgroup label="${parent.categoryName}">
                                            <c:forEach var="child" items="${parent.children}">
                                                <option value="${child.categoryId}" 
                                                        ${pageRequestDTO.categoryId eq child.categoryId ? 'selected' : ''}>
                                                    ${child.categoryName}
                                                </option>
                                            </c:forEach>
                                        </optgroup>
                                    </c:forEach>
                                </select>
                            </div>
                            
                            <div class="filter-group">
                                <label for="searchKeyword">상품명 검색</label>
                                <input type="text" id="searchKeyword" name="searchKeyword" 
                                       class="form-input" placeholder="상품명 입력"
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
                        <select id="sortOption" class="form-select-sm" onchange="changeSort(this.value)">
                            <option value="productOrder,asc" ${pageRequestDTO.sortField eq 'productOrder' && pageRequestDTO.sortDirection eq 'asc' ? 'selected' : ''}>순서순</option>
                            <option value="productId,desc" ${pageRequestDTO.sortField eq 'productId' && pageRequestDTO.sortDirection eq 'desc' ? 'selected' : ''}>최신순</option>
                            <option value="productName,asc" ${pageRequestDTO.sortField eq 'productName' && pageRequestDTO.sortDirection eq 'asc' ? 'selected' : ''}>이름순</option>
                            <option value="productPrice,desc" ${pageRequestDTO.sortField eq 'productPrice' && pageRequestDTO.sortDirection eq 'desc' ? 'selected' : ''}>높은가격순</option>
                            <option value="productPrice,asc" ${pageRequestDTO.sortField eq 'productPrice' && pageRequestDTO.sortDirection eq 'asc' ? 'selected' : ''}>낮은가격순</option>
                            <option value="productStock,asc" ${pageRequestDTO.sortField eq 'productStock' && pageRequestDTO.sortDirection eq 'asc' ? 'selected' : ''}>재고적은순</option>
                        </select>
                        <a href="${pageContext.request.contextPath}/admin/product/create" class="btn btn-primary">상품 추가</a>
                    </div>
                </div>

                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>썸네일</th>
                                <th>상품명</th>
                                <th>카테고리</th>
                                <th>가격</th>
                                <th>할인율</th>
                                <th>재고</th>
                                <th>순서</th>
                                <th>등록일</th>
                                <th>관리</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:if test="${empty result.dtoList}">
                                <tr>
                                    <td colspan="10" class="empty-message">등록된 상품이 없습니다.</td>
                                </tr>
                            </c:if>
                            <c:forEach var="product" items="${result.dtoList}">
                                <tr data-product-id="${product.productId}">
                                    <td data-label="ID">${product.productId}</td>
                                    <td class="thumbnail-cell" data-label="썸네일">
                                        <c:if test="${not empty product.thumbnailUrl}">
                                            <img src="${pageContext.request.contextPath}${product.thumbnailUrl}" alt="${product.productName}" class="thumbnail-img">
                                        </c:if>
                                        <c:if test="${empty product.thumbnailUrl}">
                                            <div class="no-image">No Image</div>
                                        </c:if>
                                        <span class="mobile-product-name">
                                            <a href="${pageContext.request.contextPath}/admin/product/detail/${product.productId}" class="product-name-link">
                                                ${product.productName}
                                            </a>
                                        </span>
                                    </td>
                                    <td data-label="상품명" class="desktop-only">
                                        <a href="${pageContext.request.contextPath}/admin/product/detail/${product.productId}" class="product-name-link">
                                            ${product.productName}
                                        </a>
                                    </td>
                                    <td data-label="카테고리">
                                        <c:if test="${not empty product.category}">
                                            <c:if test="${not empty product.category.parent}">
                                                ${product.category.parent.categoryName} &gt;
                                            </c:if>
                                            ${product.category.categoryName}
                                        </c:if>
                                        <c:if test="${empty product.category}">
                                            -
                                        </c:if>
                                    </td>
                                    <td class="price-cell" data-label="가격">
                                        <fmt:formatNumber value="${product.productPrice}" type="number"/>원
                                    </td>
                                    <td data-label="할인율">
                                        <c:if test="${product.productDiscount > 0}">
                                            <span class="discount-badge">${product.productDiscount}%</span>
                                        </c:if>
                                        <c:if test="${product.productDiscount == 0 || product.productDiscount == null}">
                                            -
                                        </c:if>
                                    </td>
                                    <td data-label="재고">
                                        <c:if test="${product.productStock > 0}">
                                            <span class="${product.productStock < 10 ? 'stock-low' : ''}">${product.productStock}</span>
                                        </c:if>
                                        <c:if test="${product.productStock == 0}">
                                            <span class="stock-out">품절</span>
                                        </c:if>
                                    </td>
                                    <td data-label="순서">${product.productOrder}</td>
                                    <td data-label="등록일">${product.createdDate}</td>
                                    <td class="action-cell">
                                        <a href="${pageContext.request.contextPath}/admin/product/edit/${product.productId}" class="btn btn-small btn-info">수정</a>
                                        <button class="btn btn-small btn-danger" onclick="deleteProduct(${product.productId})">삭제</button>
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
    <script src="${pageContext.request.contextPath}/js/admin/product.js"></script>
</body>
</html>
