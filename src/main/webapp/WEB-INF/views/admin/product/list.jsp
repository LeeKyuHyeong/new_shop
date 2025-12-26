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

                <div class="page-header">
                    <a href="${pageContext.request.contextPath}/admin/product/create" class="btn btn-primary">상품 추가</a>
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
                            <c:forEach var="product" items="${products}">
                                <tr data-product-id="${product.productId}">
                                    <td>${product.productId}</td>
                                    <td class="thumbnail-cell">
                                        <c:if test="${not empty product.thumbnailUrl}">
                                            <img src="${pageContext.request.contextPath}${product.thumbnailUrl}" alt="${product.productName}" class="thumbnail-img">
                                        </c:if>
                                        <c:if test="${empty product.thumbnailUrl}">
                                            <div class="no-image">No Image</div>
                                        </c:if>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/admin/product/detail/${product.productId}" class="product-name-link">
                                            ${product.productName}
                                        </a>
                                    </td>
                                    <td>
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
                                    <td class="price-cell">
                                        <fmt:formatNumber value="${product.productPrice}" type="number"/>원
                                    </td>
                                    <td>
                                        <c:if test="${product.productDiscount > 0}">
                                            <span class="discount-badge">${product.productDiscount}%</span>
                                        </c:if>
                                        <c:if test="${product.productDiscount == 0 || product.productDiscount == null}">
                                            -
                                        </c:if>
                                    </td>
                                    <td>
                                        <c:if test="${product.productStock > 0}">
                                            ${product.productStock}
                                        </c:if>
                                        <c:if test="${product.productStock == 0}">
                                            <span class="stock-out">품절</span>
                                        </c:if>
                                    </td>
                                    <td>${product.productOrder}</td>
                                    <td>"${product.createdDate}"</td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/admin/product/edit/${product.productId}" class="btn btn-small btn-info">수정</a>
                                        <button class="btn btn-small btn-danger" onclick="deleteProduct(${product.productId})">삭제</button>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                    <c:if test="${empty products}">
                        <div class="empty-message">등록된 상품이 없습니다.</div>
                    </c:if>
                </div>
            </div>
        </main>
    </div>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
    </script>
    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
    <script src="${pageContext.request.contextPath}/js/admin/product.js"></script>
</body>
</html>
