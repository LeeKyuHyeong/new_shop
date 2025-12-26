<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>KH Shop - <c:choose><c:when test="${not empty selectedCategory}">${selectedCategory.categoryName}</c:when><c:otherwise>메인</c:otherwise></c:choose></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/main.css">
</head>
<body>
    <%@ include file="common/header.jsp" %>

    <!-- 메인 컨텐츠 -->
    <main class="main-content">
        <c:choose>
            <%-- 카테고리 페이지 --%>
            <c:when test="${not empty selectedCategoryId}">
                <!-- 페이지 헤더 -->
                <div class="page-header">
                    <h1 class="page-title">${selectedCategory.categoryName}</h1>
                    <div class="page-breadcrumb">
                        <a href="${pageContext.request.contextPath}/">홈</a>
                        <span>›</span>
                        <c:if test="${not empty selectedCategory.parent}">
                            <a href="${pageContext.request.contextPath}/category/${selectedCategory.parent.categoryId}">
                                ${selectedCategory.parent.categoryName}
                            </a>
                            <span>›</span>
                        </c:if>
                        <strong>${selectedCategory.categoryName}</strong>
                    </div>
                </div>

                <!-- 상품 목록 -->
                <section class="product-section">
                    <c:choose>
                        <c:when test="${not empty products}">
                            <div class="product-grid">
                                <c:forEach var="product" items="${products}">
                                    <div class="product-card" onclick="location.href='${pageContext.request.contextPath}/product/${product.productId}'">
                                        <div class="product-image <c:if test="${empty product.thumbnailUrl}">no-image</c:if>">
                                            <c:choose>
                                                <c:when test="${not empty product.thumbnailUrl}">
                                                    <img src="${pageContext.request.contextPath}${product.thumbnailUrl}" alt="${product.productName}">
                                                </c:when>
                                                <c:otherwise>
                                                    이미지 없음
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="product-info">
                                            <c:if test="${not empty product.category}">
                                                <div class="product-category">${product.category.categoryName}</div>
                                            </c:if>
                                            <div class="product-name">${product.productName}</div>
                                            <div class="product-price">
                                                <c:if test="${product.productDiscount > 0}">
                                                    <span class="original-price"><fmt:formatNumber value="${product.productPrice}" pattern="#,###"/>원</span>
                                                    <span class="discount-badge">${product.productDiscount}%</span>
                                                </c:if>
                                                <span class="final-price"><fmt:formatNumber value="${product.discountedPrice}" pattern="#,###"/>원</span>
                                            </div>
                                            <div class="stock-info <c:if test="${product.productStock == 0}">out</c:if>">
                                                <c:choose>
                                                    <c:when test="${product.productStock == 0}">품절</c:when>
                                                    <c:otherwise>재고 ${product.productStock}개</c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="no-products">
                                <p>등록된 상품이 없습니다.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:when>

            <%-- 메인 페이지 --%>
            <c:otherwise>
                <!-- 슬라이드 영역 -->
                <c:choose>
                    <c:when test="${not empty slides}">
                        <div class="slide-container" data-duration="${slideDuration}">
                            <c:forEach var="slide" items="${slides}" varStatus="status">
                                <div class="slide <c:if test="${status.first}">active</c:if>"
                                     <c:if test="${not empty slide.linkUrl}">onclick="location.href='${slide.linkUrl}'" style="cursor:pointer"</c:if>
                                     data-link="${slide.linkUrl}">
                                    <img src="${pageContext.request.contextPath}${slide.imageUrl}" alt="${slide.slideTitle}">
                                    <div class="slide-content">
                                        <div class="slide-title">${slide.slideTitle}</div>
                                        <c:if test="${not empty slide.slideDescription}">
                                            <div class="slide-description">${slide.slideDescription}</div>
                                        </c:if>
                                    </div>
                                </div>
                            </c:forEach>

                            <c:if test="${slides.size() > 1}">
                                <button class="slide-arrow prev" onclick="changeSlide(-1)">&#10094;</button>
                                <button class="slide-arrow next" onclick="changeSlide(1)">&#10095;</button>

                                <div class="slide-controls">
                                    <c:forEach var="slide" items="${slides}" varStatus="status">
                                        <span class="slide-dot <c:if test="${status.first}">active</c:if>" 
                                              onclick="goToSlide(${status.index})"></span>
                                    </c:forEach>
                                </div>
                            </c:if>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="no-slides">
                            <p>진행 중인 이벤트가 없습니다.</p>
                        </div>
                    </c:otherwise>
                </c:choose>

                <!-- 신상품 섹션 -->
                <c:if test="${not empty newProducts}">
                    <section class="product-section">
                        <div class="section-header">
                            <h2 class="section-title">
                                신상품
                                <span class="section-badge new">NEW</span>
                            </h2>
                        </div>
                        <div class="product-grid">
                            <c:forEach var="product" items="${newProducts}">
                                <div class="product-card" onclick="location.href='${pageContext.request.contextPath}/product/${product.productId}'">
                                    <div class="product-image <c:if test="${empty product.thumbnailUrl}">no-image</c:if>">
                                        <c:choose>
                                            <c:when test="${not empty product.thumbnailUrl}">
                                                <img src="${pageContext.request.contextPath}${product.thumbnailUrl}" alt="${product.productName}">
                                            </c:when>
                                            <c:otherwise>
                                                이미지 없음
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="product-info">
                                        <c:if test="${not empty product.category}">
                                            <div class="product-category">${product.category.categoryName}</div>
                                        </c:if>
                                        <div class="product-name">${product.productName}</div>
                                        <div class="product-price">
                                            <c:if test="${product.productDiscount > 0}">
                                                <span class="original-price"><fmt:formatNumber value="${product.productPrice}" pattern="#,###"/>원</span>
                                                <span class="discount-badge">${product.productDiscount}%</span>
                                            </c:if>
                                            <span class="final-price"><fmt:formatNumber value="${product.discountedPrice}" pattern="#,###"/>원</span>
                                        </div>
                                        <div class="stock-info <c:if test="${product.productStock == 0}">out</c:if>">
                                            <c:choose>
                                                <c:when test="${product.productStock == 0}">품절</c:when>
                                                <c:otherwise>재고 ${product.productStock}개</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </section>
                </c:if>

                <!-- 베스트 상품 섹션 -->
                <c:if test="${not empty bestProducts}">
                    <section class="product-section">
                        <div class="section-header">
                            <h2 class="section-title">
                                베스트 상품
                                <span class="section-badge best">BEST</span>
                            </h2>
                        </div>
                        <div class="product-grid">
                            <c:forEach var="product" items="${bestProducts}">
                                <div class="product-card" onclick="location.href='${pageContext.request.contextPath}/product/${product.productId}'">
                                    <div class="product-image <c:if test="${empty product.thumbnailUrl}">no-image</c:if>">
                                        <c:choose>
                                            <c:when test="${not empty product.thumbnailUrl}">
                                                <img src="${pageContext.request.contextPath}${product.thumbnailUrl}" alt="${product.productName}">
                                            </c:when>
                                            <c:otherwise>
                                                이미지 없음
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="product-info">
                                        <c:if test="${not empty product.category}">
                                            <div class="product-category">${product.category.categoryName}</div>
                                        </c:if>
                                        <div class="product-name">${product.productName}</div>
                                        <div class="product-price">
                                            <c:if test="${product.productDiscount > 0}">
                                                <span class="original-price"><fmt:formatNumber value="${product.productPrice}" pattern="#,###"/>원</span>
                                                <span class="discount-badge">${product.productDiscount}%</span>
                                            </c:if>
                                            <span class="final-price"><fmt:formatNumber value="${product.discountedPrice}" pattern="#,###"/>원</span>
                                        </div>
                                        <div class="stock-info <c:if test="${product.productStock == 0}">out</c:if>">
                                            <c:choose>
                                                <c:when test="${product.productStock == 0}">품절</c:when>
                                                <c:otherwise>재고 ${product.productStock}개</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </section>
                </c:if>

                <!-- 할인 상품 섹션 -->
                <c:if test="${not empty discountProducts}">
                    <section class="product-section">
                        <div class="section-header">
                            <h2 class="section-title">
                                특가 할인
                                <span class="section-badge discount">SALE</span>
                            </h2>
                        </div>
                        <div class="product-grid">
                            <c:forEach var="product" items="${discountProducts}">
                                <div class="product-card" onclick="location.href='${pageContext.request.contextPath}/product/${product.productId}'">
                                    <div class="product-image <c:if test="${empty product.thumbnailUrl}">no-image</c:if>">
                                        <c:choose>
                                            <c:when test="${not empty product.thumbnailUrl}">
                                                <img src="${pageContext.request.contextPath}${product.thumbnailUrl}" alt="${product.productName}">
                                            </c:when>
                                            <c:otherwise>
                                                이미지 없음
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="product-info">
                                        <c:if test="${not empty product.category}">
                                            <div class="product-category">${product.category.categoryName}</div>
                                        </c:if>
                                        <div class="product-name">${product.productName}</div>
                                        <div class="product-price">
                                            <span class="original-price"><fmt:formatNumber value="${product.productPrice}" pattern="#,###"/>원</span>
                                            <span class="discount-badge">${product.productDiscount}%</span>
                                            <span class="final-price"><fmt:formatNumber value="${product.discountedPrice}" pattern="#,###"/>원</span>
                                        </div>
                                        <div class="stock-info <c:if test="${product.productStock == 0}">out</c:if>">
                                            <c:choose>
                                                <c:when test="${product.productStock == 0}">품절</c:when>
                                                <c:otherwise>재고 ${product.productStock}개</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </section>
                </c:if>
            </c:otherwise>
        </c:choose>
    </main>

    <!-- 푸터 -->
    <footer class="footer">
        <p>&copy; 2024 KH SHOP. All rights reserved.</p>
    </footer>

    <script src="${pageContext.request.contextPath}/js/client/main.js"></script>
</body>
</html>
