<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${product.productName} - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/product-detail.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/review.css">
    <style>
        /* 비속어 에러 스타일 */
        .profanity-error {
            border-color: #dc3545 !important;
            background-color: #fff5f5 !important;
        }
        .profanity-error-msg {
            color: #dc3545;
            font-size: 0.85rem;
            margin-top: 5px;
            display: block;
        }
        .input-warning {
            color: #856404;
            background-color: #fff3cd;
            border: 1px solid #ffc107;
            padding: 8px 12px;
            border-radius: 4px;
            margin-top: 8px;
            font-size: 0.9rem;
        }
    </style>
</head>
<body>
    <%@ include file="common/header.jsp" %>

    <main class="main-content">
        <div class="product-detail">
            <!-- 상품 갤러리 -->
            <div class="product-gallery">
                <div class="main-image <c:if test="${empty product.thumbnailUrl}">no-image</c:if>">
                    <c:choose>
                        <c:when test="${not empty product.thumbnailUrl}">
                            <img id="mainImg" src="${pageContext.request.contextPath}${product.thumbnailUrl}" alt="${product.productName}">
                        </c:when>
                        <c:otherwise>
                            이미지 없음
                        </c:otherwise>
                    </c:choose>
                </div>
                <c:if test="${not empty product.images}">
                    <div class="thumbnail-list">
                        <div class="thumb-item active" onclick="changeImage('${pageContext.request.contextPath}${product.thumbnailUrl}', this)">
                            <img src="${pageContext.request.contextPath}${product.thumbnailUrl}" alt="썸네일">
                        </div>
                        <c:forEach var="image" items="${product.images}">
                            <div class="thumb-item" onclick="changeImage('${pageContext.request.contextPath}${image.imageUrl}', this)">
                                <img src="${pageContext.request.contextPath}${image.imageUrl}" alt="상세이미지">
                            </div>
                        </c:forEach>
                    </div>
                </c:if>
            </div>

            <!-- 상품 정보 -->
            <div class="product-info-detail">
                <div class="product-breadcrumb">
                    <a href="${pageContext.request.contextPath}/">홈</a>
                    <span> › </span>
                    <c:if test="${not empty product.category}">
                        <c:if test="${not empty product.category.parent}">
                            <a href="${pageContext.request.contextPath}/category/${product.category.parent.categoryId}">
                                ${product.category.parent.categoryName}
                            </a>
                            <span> › </span>
                        </c:if>
                        <a href="${pageContext.request.contextPath}/category/${product.category.categoryId}">
                            ${product.category.categoryName}
                        </a>
                    </c:if>
                </div>

                <h1 class="product-title">${product.productName}</h1>

                <div class="price-box">
                    <c:if test="${product.productDiscount > 0}">
                        <div class="price-row">
                            <span class="price-label">정가</span>
                            <span class="original-price-large"><fmt:formatNumber value="${product.productPrice}" pattern="#,###"/>원</span>
                            <span class="discount-badge-large">${product.productDiscount}% OFF</span>
                        </div>
                    </c:if>
                    <div class="price-row">
                        <span class="price-label">판매가</span>
                        <span class="final-price-large"><fmt:formatNumber value="${product.discountedPrice}" pattern="#,###"/>원</span>
                    </div>
                </div>

                <div class="info-table">
                    <div class="info-row">
                        <span class="info-label">재고</span>
                        <span class="info-value">
                            <c:choose>
                                <c:when test="${product.productStock > 0}">
                                    <span class="stock-status in-stock">재고 ${product.productStock}개</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="stock-status out-of-stock">품절</span>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">배송</span>
                        <span class="info-value">무료배송</span>
                    </div>
                </div>

                <!-- 옵션 선택 -->
                <c:if test="${product.productStock > 0}">
                    <!-- 색상 옵션 -->
                    <c:if test="${not empty product.color}">
                        <div class="option-section">
                            <span class="option-label">색상 선택</span>
                            <div class="option-list color-options" id="colorOptions">
                                <c:forEach var="color" items="${product.color.split(',')}">
                                    <c:set var="trimmedColor" value="${color.trim()}"/>
                                    <c:if test="${not empty trimmedColor}">
                                        <button type="button" class="option-btn" data-value="${trimmedColor}" onclick="selectOption('color', '${trimmedColor}', this)">
                                            ${trimmedColor}
                                        </button>
                                    </c:if>
                                </c:forEach>
                            </div>
                            <input type="hidden" id="selectedColor" value="">
                        </div>
                    </c:if>

                    <!-- 사이즈 옵션 -->
                    <c:if test="${not empty product.size}">
                        <div class="option-section">
                            <span class="option-label">사이즈 선택</span>
                            <div class="option-list size-options" id="sizeOptions">
                                <c:forEach var="size" items="${product.size.split(',')}">
                                    <c:set var="trimmedSize" value="${size.trim()}"/>
                                    <c:if test="${not empty trimmedSize}">
                                        <button type="button" class="option-btn" data-value="${trimmedSize}" onclick="selectOption('size', '${trimmedSize}', this)">
                                            ${trimmedSize}
                                        </button>
                                    </c:if>
                                </c:forEach>
                            </div>
                            <input type="hidden" id="selectedSize" value="">
                        </div>
                    </c:if>

                    <!-- 수량 선택 -->
                    <div class="quantity-section">
                        <span class="qty-label">수량</span>
                        <div class="quantity-control">
                            <button type="button" class="qty-btn" onclick="changeQty(-1)">−</button>
                            <input type="number" id="quantity" class="qty-input" value="1" min="1" max="${product.productStock}">
                            <button type="button" class="qty-btn" onclick="changeQty(1)">+</button>
                        </div>
                        <div class="qty-total">
                            총 상품금액: <strong id="totalPriceDisplay"><fmt:formatNumber value="${product.discountedPrice}" pattern="#,###"/></strong>원
                        </div>
                    </div>
                </c:if>

                <div class="action-buttons">
                    <button class="btn-wishlist" id="btnWishlist" onclick="toggleWishlist()">
                        <span class="wishlist-icon" id="wishlistIcon">🤍</span>
                        <span id="wishlistText">찜하기</span>
                    </button>
                    <button class="btn-cart" onclick="addToCart()" <c:if test="${product.productStock == 0}">disabled</c:if>>
                        🛒 장바구니
                    </button>
                    <button class="btn-buy" onclick="buyNow()" <c:if test="${product.productStock == 0}">disabled</c:if>>
                        바로구매
                    </button>
                </div>
            </div>
        </div>

        <!-- 상품 설명 -->
        <div class="product-description-section">
            <h2 class="section-title">상품 설명</h2>
            <c:choose>
                <c:when test="${not empty product.productDescription}">
                    <div class="description-content">${product.productDescription}</div>
                </c:when>
                <c:otherwise>
                    <p style="color: #999;">상품 설명이 없습니다.</p>
                </c:otherwise>
            </c:choose>

            <c:if test="${not empty product.images}">
                <div class="detail-images" style="margin-top: 30px;">
                    <c:forEach var="image" items="${product.images}">
                        <img src="${pageContext.request.contextPath}${image.imageUrl}" alt="상세이미지">
                    </c:forEach>
                </div>
            </c:if>
        </div>

        <!-- 리뷰 섹션 -->
        <section class="review-section">
            <h2 class="section-title">
                상품 리뷰 <span class="review-count">(<span id="reviewCount">0</span>)</span>
            </h2>

            <!-- 리뷰 통계 -->
            <div class="review-stats" id="reviewStats">
                <!-- JS로 렌더링 -->
            </div>

            <!-- 리뷰 작성 버튼 -->
            <div class="review-write-area">
                <button class="btn-write-review" id="btnWriteReview" onclick="toggleReviewForm()">
                    ✏️ 리뷰 작성하기
                </button>
            </div>

            <!-- 리뷰 작성 폼 -->
            <div class="review-form" id="reviewForm">
                <h3 class="review-form-title" id="reviewFormTitle">리뷰 작성</h3>
                
                <div class="rating-input">
                    <label>별점</label>
                    <div class="star-rating">
                        <input type="radio" id="star5" name="rating" value="5">
                        <label for="star5">★</label>
                        <input type="radio" id="star4" name="rating" value="4">
                        <label for="star4">★</label>
                        <input type="radio" id="star3" name="rating" value="3">
                        <label for="star3">★</label>
                        <input type="radio" id="star2" name="rating" value="2">
                        <label for="star2">★</label>
                        <input type="radio" id="star1" name="rating" value="1">
                        <label for="star1">★</label>
                    </div>
                    <input type="hidden" id="reviewRating" name="rating">
                </div>

                <div class="review-content-input">
                    <label for="reviewContent">리뷰 내용</label>
                    <textarea id="reviewContent" placeholder="상품에 대한 솔직한 리뷰를 작성해주세요. (부적절한 표현은 사용할 수 없습니다.)"></textarea>
                    <div id="contentProfanityError" class="profanity-error-msg" style="display:none;"></div>
                </div>

                <div class="image-upload-area">
                    <label>이미지 첨부 (최대 5장)</label>
                    <div class="image-preview" id="imagePreview"></div>
                    <label class="image-input-label">
                        <input type="file" id="reviewImages" accept="image/*" multiple 
                               style="display:none;" onchange="handleImageSelect(this)">
                        +
                    </label>
                </div>

                <div class="review-form-actions">
                    <button type="button" class="btn-cancel" onclick="toggleReviewForm()">취소</button>
                    <button type="button" class="btn-submit-review" id="btnSubmitReview" onclick="submitReview()">등록</button>
                </div>
            </div>

            <!-- 리뷰 목록 -->
            <div class="review-list" id="reviewList">
                <!-- JS로 렌더링 -->
            </div>

            <!-- 더보기 버튼 -->
            <div class="load-more">
                <button class="btn-load-more" id="loadMoreBtn" onclick="loadMoreReviews()" style="display:none;">
                    더보기
                </button>
            </div>
        </section>

        <!-- 관련 상품 -->
        <c:if test="${not empty relatedProducts}">
            <section class="related-section">
                <h2 class="related-title">관련 상품</h2>
                <div class="product-grid">
                    <c:forEach var="relProduct" items="${relatedProducts}">
                        <div class="product-card" onclick="location.href='${pageContext.request.contextPath}/product/${relProduct.productId}'">
                            <div class="product-image <c:if test="${empty relProduct.thumbnailUrl}">no-image</c:if>">
                                <c:choose>
                                    <c:when test="${not empty relProduct.thumbnailUrl}">
                                        <img src="${pageContext.request.contextPath}${relProduct.thumbnailUrl}" alt="${relProduct.productName}">
                                    </c:when>
                                    <c:otherwise>
                                        이미지 없음
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="product-info">
                                <div class="product-name">${relProduct.productName}</div>
                                <div class="product-price">
                                    <c:if test="${relProduct.productDiscount > 0}">
                                        <span class="original-price"><fmt:formatNumber value="${relProduct.productPrice}" pattern="#,###"/>원</span>
                                        <span class="discount-badge">${relProduct.productDiscount}%</span>
                                    </c:if>
                                    <span class="final-price"><fmt:formatNumber value="${relProduct.discountedPrice}" pattern="#,###"/>원</span>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </section>
        </c:if>
    </main>

    <!-- 이미지 모달 -->
    <div class="image-modal" id="imageModal" onclick="closeImageModal()">
        <span class="image-modal-close">&times;</span>
        <img src="" alt="리뷰 이미지">
    </div>

    <footer class="footer">
        <p>&copy; 2024 KH SHOP. All rights reserved.</p>
    </footer>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
        const productId = ${product.productId};
        const productPrice = ${product.discountedPrice};
        const maxStock = ${product.productStock};
        
        // 옵션 필수 여부
        const hasColorOption = ${not empty product.color};
        const hasSizeOption = ${not empty product.size};
    </script>
    <script src="${pageContext.request.contextPath}/js/common/profanity.js"></script>
    <script src="${pageContext.request.contextPath}/js/client/product-detail.js"></script>
    <script src="${pageContext.request.contextPath}/js/client/review.js"></script>
    
    <script>
        // 리뷰 내용 실시간 비속어 검사
        document.addEventListener('DOMContentLoaded', function() {
            const reviewContent = document.getElementById('reviewContent');
            const errorDiv = document.getElementById('contentProfanityError');
            let debounceTimer;
            
            if (reviewContent) {
                // 입력 시 실시간 검사 (debounce 적용)
                reviewContent.addEventListener('input', function() {
                    clearTimeout(debounceTimer);
                    debounceTimer = setTimeout(async () => {
                        const text = this.value;
                        if (text.length < 2) {
                            errorDiv.style.display = 'none';
                            reviewContent.classList.remove('profanity-error');
                            return;
                        }
                        
                        const result = await ProfanityFilter.validate(text);
                        
                        if (result.hasProfanity) {
                            reviewContent.classList.add('profanity-error');
                            errorDiv.textContent = '⚠️ 부적절한 표현이 포함되어 있습니다. 수정 후 등록해주세요.';
                            errorDiv.style.display = 'block';
                        } else {
                            reviewContent.classList.remove('profanity-error');
                            errorDiv.style.display = 'none';
                        }
                    }, 500);
                });
                
                // 포커스 아웃 시 검사
                reviewContent.addEventListener('blur', async function() {
                    const text = this.value;
                    if (!text) return;
                    
                    const result = await ProfanityFilter.validate(text);
                    
                    if (result.hasProfanity) {
                        reviewContent.classList.add('profanity-error');
                        errorDiv.textContent = '⚠️ 부적절한 표현이 포함되어 있습니다. 수정 후 등록해주세요.';
                        errorDiv.style.display = 'block';
                    }
                });
            }
        });
        
        // 기존 submitReview 함수 오버라이드
        const originalSubmitReview = window.submitReview;
        window.submitReview = async function() {
            const content = document.getElementById('reviewContent').value;
            const errorDiv = document.getElementById('contentProfanityError');
            
            // 비속어 검사
            const result = await ProfanityFilter.validate(content);
            
            if (result.hasProfanity) {
                document.getElementById('reviewContent').classList.add('profanity-error');
                errorDiv.textContent = '⚠️ 부적절한 표현이 포함되어 있습니다: ' + (result.detectedWords ? result.detectedWords.join(', ') : '');
                errorDiv.style.display = 'block';
                alert('리뷰에 부적절한 표현이 포함되어 있습니다.\n내용을 수정해주세요.');
                return;
            }
            
            // 비속어가 없으면 원래 함수 실행
            if (typeof originalSubmitReview === 'function') {
                originalSubmitReview();
            } else {
                // 원래 함수가 없으면 직접 처리
                submitReviewToServer();
            }
        };
        
        // 서버로 리뷰 전송 (기존 로직)
        async function submitReviewToServer() {
            const rating = document.querySelector('input[name="rating"]:checked');
            const content = document.getElementById('reviewContent').value;
            const imageInput = document.getElementById('reviewImages');
            
            if (!rating) {
                alert('별점을 선택해주세요.');
                return;
            }
            
            if (!content.trim()) {
                alert('리뷰 내용을 입력해주세요.');
                return;
            }
            
            const formData = new FormData();
            formData.append('productId', productId);
            formData.append('rating', rating.value);
            formData.append('content', content);
            
            if (imageInput.files.length > 0) {
                for (let i = 0; i < imageInput.files.length; i++) {
                    formData.append('images', imageInput.files[i]);
                }
            }
            
            try {
                const response = await fetch(contextPath + '/api/review', {
                    method: 'POST',
                    body: formData
                });
                
                const result = await response.json();
                
                if (result.success) {
                    alert('리뷰가 등록되었습니다.');
                    location.reload();
                } else {
                    alert(result.message || '리뷰 등록에 실패했습니다.');
                }
            } catch (error) {
                console.error('Error:', error);
                alert('리뷰 등록 중 오류가 발생했습니다.');
            }
        }
    </script>
</body>
</html>
