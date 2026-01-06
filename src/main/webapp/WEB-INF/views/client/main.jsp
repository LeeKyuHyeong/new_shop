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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/pagination.css">
</head>
<body>
    <%@ include file="common/header.jsp" %>

    <!-- 메인 컨텐츠 -->
    <main class="main-content">
        <c:choose>
            <%-- 카테고리 페이지 (페이징 적용) --%>
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

                <%-- 정렬/필터 옵션 --%>
                <div class="product-toolbar">
                    <div class="toolbar-info">
                        <span>총 <strong>${result.totalCount}</strong>개의 상품</span>
                    </div>
                    <div class="toolbar-controls">
                        <select id="sortSelect" class="sort-select" onchange="changeSort(this.value)">
                            <option value="productOrder,asc" ${pageRequestDTO.sortField eq 'productOrder' && pageRequestDTO.sortDirection eq 'asc' ? 'selected' : ''}>기본순</option>
                            <option value="createdDate,desc" ${pageRequestDTO.sortField eq 'createdDate' && pageRequestDTO.sortDirection eq 'desc' ? 'selected' : ''}>최신순</option>
                            <option value="productPrice,asc" ${pageRequestDTO.sortField eq 'productPrice' && pageRequestDTO.sortDirection eq 'asc' ? 'selected' : ''}>낮은가격순</option>
                            <option value="productPrice,desc" ${pageRequestDTO.sortField eq 'productPrice' && pageRequestDTO.sortDirection eq 'desc' ? 'selected' : ''}>높은가격순</option>
                            <option value="productName,asc" ${pageRequestDTO.sortField eq 'productName' && pageRequestDTO.sortDirection eq 'asc' ? 'selected' : ''}>이름순</option>
                        </select>
                    </div>
                </div>

                <!-- 상품 목록 -->
                <section class="product-section">
                    <c:choose>
                        <c:when test="${not empty result.dtoList}">
                            <div class="product-grid">
                                <c:forEach var="product" items="${result.dtoList}">
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
                                            <c:if test="${product.productDiscount > 0}">
                                                <span class="product-badge sale">${product.productDiscount}%</span>
                                            </c:if>
                                            <c:if test="${product.productStock == 0}">
                                                <span class="product-badge soldout">품절</span>
                                            </c:if>
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
                                                    <c:when test="${product.productStock < 10}">
                                                        <span class="stock-low">재고 ${product.productStock}개</span>
                                                    </c:when>
                                                    <c:otherwise>재고 ${product.productStock}개</c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                            
                            <%-- 페이징 --%>
                            <jsp:include page="/WEB-INF/views/common/pagination.jsp">
                                <jsp:param name="theme" value="client"/>
                            </jsp:include>
                        </c:when>
                        <c:otherwise>
                            <div class="no-products">
                                <p>등록된 상품이 없습니다.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:when>

            <%-- 메인 페이지 (기존 유지) --%>
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


    <!-- 팝업 -->
    <c:if test="${not empty popups}">
        <c:forEach var="popup" items="${popups}">
            <div class="popup-container" id="popup-${popup.popupId}" 
                 style="width: ${popup.popupWidth}px; height: ${popup.popupHeight}px; 
                        top: ${popup.popupTop}px; left: ${popup.popupLeft}px;">
                <div class="popup-header">
                    <h3>${popup.popupTitle}</h3>
                    <button class="popup-close" onclick="closePopup(${popup.popupId})">×</button>
                </div>
                <div class="popup-body">
                    <c:if test="${not empty popup.popupImageUrl}">
                        <c:choose>
                            <c:when test="${not empty popup.popupLink}">
                                <a href="${popup.popupLink}" target="_blank">
                                    <img src="${pageContext.request.contextPath}${popup.popupImageUrl}" alt="${popup.popupTitle}">
                                </a>
                            </c:when>
                            <c:otherwise>
                                <img src="${pageContext.request.contextPath}${popup.popupImageUrl}" alt="${popup.popupTitle}">
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                    <c:if test="${not empty popup.popupContent}">
                        <div class="popup-content">${popup.popupContent}</div>
                    </c:if>
                </div>
                <div class="popup-footer">
                    <label>
                        <input type="checkbox" onclick="closePopupToday(${popup.popupId})"> 
                        오늘 하루 보지 않기
                    </label>
                </div>
            </div>
        </c:forEach>
    </c:if>

    <script>
        const popupDuration = ${empty popupDuration ? 0 : popupDuration};
    </script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/popup.css">
    <script src="${pageContext.request.contextPath}/js/client/popup.js"></script>
    <script src="${pageContext.request.contextPath}/js/common/pagination.js"></script>
    <script src="${pageContext.request.contextPath}/js/client/main.js"></script>
</body>
</html>
