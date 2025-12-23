<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>상품 관리 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-product.css">
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
                <a href="${pageContext.request.contextPath}/admin/category" class="menu-item">카테고리 관리</a>
                <a href="${pageContext.request.contextPath}/admin/product" class="menu-item active">상품 관리</a>
                <a href="${pageContext.request.contextPath}/admin/order" class="menu-item">주문 관리</a>
                <a href="${pageContext.request.contextPath}/logout" class="menu-item logout">로그아웃</a>
            </nav>
        </aside>

        <main class="main-content">
            <header class="top-bar">
                <h1><c:if test="${empty product}">상품 추가</c:if><c:if test="${not empty product}">상품 수정</c:if></h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <div class="alert-container"></div>

                <div class="form-box product-form-box">
                    <form id="productForm" class="form" enctype="multipart/form-data">
                        <c:if test="${not empty product}">
                            <input type="hidden" id="productId" value="${product.productId}">
                        </c:if>

                        <div class="form-row">
                            <div class="form-group">
                                <label for="categoryId">카테고리</label>
                                <select id="categoryId" name="categoryId">
                                    <option value="">카테고리 선택</option>
                                    <c:forEach var="parent" items="${parentCategories}">
                                        <optgroup label="${parent.categoryName}">
                                            <c:forEach var="child" items="${parent.children}">
                                                <c:if test="${child.useYn eq 'Y'}">
                                                    <option value="${child.categoryId}"
                                                        <c:if test="${not empty product && product.categoryId eq child.categoryId}">selected</c:if>>
                                                        ${child.categoryName}
                                                    </option>
                                                </c:if>
                                            </c:forEach>
                                        </optgroup>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="productName">상품명 *</label>
                            <input type="text" id="productName" name="productName" placeholder="상품명 입력" required
                                value="<c:if test="${not empty product}">${product.productName}</c:if>">
                        </div>

                        <div class="form-row">
                            <div class="form-group">
                                <label for="productPrice">가격 *</label>
                                <input type="number" id="productPrice" name="productPrice" placeholder="가격 입력" required min="0"
                                    value="<c:if test="${not empty product}">${product.productPrice}</c:if>">
                            </div>
                            <div class="form-group">
                                <label for="productDiscount">할인율 (%)</label>
                                <input type="number" id="productDiscount" name="productDiscount" placeholder="할인율" min="0" max="100"
                                    value="<c:if test="${not empty product}">${product.productDiscount}</c:if>">
                            </div>
                            <div class="form-group">
                                <label for="productStock">재고</label>
                                <input type="number" id="productStock" name="productStock" placeholder="재고 수량" min="0"
                                    value="<c:if test="${not empty product}">${product.productStock}</c:if>">
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="productDescription">상품 설명</label>
                            <textarea id="productDescription" name="productDescription" placeholder="상품 설명 입력" rows="5"><c:if test="${not empty product}">${product.productDescription}</c:if></textarea>
                        </div>

                        <div class="form-group">
                            <label for="productOrder">순서</label>
                            <input type="number" id="productOrder" name="productOrder" placeholder="순서 입력" min="0"
                                value="<c:if test="${not empty product}">${product.productOrder}</c:if>">
                        </div>

                        <div class="form-group">
                            <label>썸네일 이미지</label>
                            <div class="image-upload-area" id="thumbnailArea">
                                <c:if test="${not empty product && not empty product.thumbnailUrl}">
                                    <div class="preview-image">
                                        <img src="${pageContext.request.contextPath}${product.thumbnailUrl}" alt="썸네일">
                                        <button type="button" class="remove-image" onclick="removeThumbnail()">×</button>
                                    </div>
                                </c:if>
                                <div class="upload-placeholder" id="thumbnailPlaceholder" <c:if test="${not empty product && not empty product.thumbnailUrl}">style="display:none"</c:if>>
                                    <span>📷</span>
                                    <p>클릭하여 이미지 업로드</p>
                                    <p class="hint">권장: 500x500px, 최대 5MB</p>
                                </div>
                                <input type="file" id="thumbnail" name="thumbnail" accept="image/*" style="display:none">
                            </div>
                        </div>

                        <div class="form-group">
                            <label>상세 이미지 (여러 장 선택 가능)</label>
                            <div class="detail-images-container" id="detailImagesContainer">
                                <c:if test="${not empty product && not empty product.images}">
                                    <c:forEach var="image" items="${product.images}">
                                        <c:if test="${image.useYn eq 'Y'}">
                                            <div class="preview-image existing-image" data-image-id="${image.imageId}">
                                                <img src="${pageContext.request.contextPath}${image.imageUrl}" alt="상세이미지">
                                                <button type="button" class="remove-image" onclick="removeDetailImage(${image.imageId}, this)">×</button>
                                            </div>
                                        </c:if>
                                    </c:forEach>
                                </c:if>
                                <div class="upload-placeholder detail-upload" id="detailPlaceholder">
                                    <span>+</span>
                                    <p>이미지 추가</p>
                                </div>
                                <input type="file" id="detailImages" name="detailImages" accept="image/*" multiple style="display:none">
                            </div>
                            <input type="hidden" id="deleteImageIds" name="deleteImageIds" value="">
                        </div>

                        <div class="form-buttons">
                            <button type="submit" class="btn btn-primary">
                                <c:if test="${empty product}">등록</c:if>
                                <c:if test="${not empty product}">수정</c:if>
                            </button>
                            <a href="${pageContext.request.contextPath}/admin/product" class="btn btn-secondary">취소</a>
                        </div>
                    </form>
                </div>
            </div>
        </main>
    </div>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
        const isEdit = ${not empty product};
    </script>
    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
    <script src="${pageContext.request.contextPath}/js/admin-product-form.js"></script>
</body>
</html>
