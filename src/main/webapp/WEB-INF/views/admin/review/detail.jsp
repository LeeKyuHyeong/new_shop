<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="activeMenu" value="review"/>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>리뷰 상세 - KH Shop Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <style>
        .review-detail-card {
            background: white;
            border: 1px solid var(--border-color);
            border-radius: 12px;
            padding: 30px;
            margin-bottom: 20px;
        }
        .review-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 20px;
            padding-bottom: 20px;
            border-bottom: 1px solid var(--border-color);
        }
        .product-info {
            display: flex;
            gap: 15px;
            align-items: center;
        }
        .product-thumb {
            width: 80px;
            height: 80px;
            border-radius: 8px;
            overflow: hidden;
        }
        .product-thumb img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
        .product-name {
            font-size: 18px;
            font-weight: 600;
        }
        .product-name a {
            color: var(--text-primary);
            text-decoration: none;
        }
        .product-name a:hover {
            color: var(--btn-primary-bg);
        }
        .rating-stars {
            color: #f39c12;
            font-size: 24px;
        }
        .user-info {
            display: flex;
            gap: 20px;
            margin-bottom: 20px;
            color: var(--text-secondary);
            font-size: 14px;
        }
        .review-content {
            line-height: 1.8;
            margin-bottom: 20px;
            white-space: pre-wrap;
        }
        .review-images {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            margin-bottom: 20px;
        }
        .review-image {
            width: 120px;
            height: 120px;
            border-radius: 8px;
            overflow: hidden;
            cursor: pointer;
        }
        .review-image img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
        .admin-reply-section {
            background: #f0f7ff;
            border-radius: 8px;
            padding: 20px;
            margin-top: 20px;
        }
        .admin-reply-section h4 {
            margin-bottom: 15px;
            color: #333;
        }
        .existing-reply {
            background: white;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 15px;
        }
        .reply-date {
            font-size: 12px;
            color: var(--text-secondary);
            margin-bottom: 10px;
        }
        .reply-content {
            line-height: 1.6;
        }
        .reply-form textarea {
            width: 100%;
            padding: 15px;
            border: 1px solid var(--border-color);
            border-radius: 8px;
            font-size: 14px;
            resize: vertical;
            min-height: 100px;
        }
        .reply-form textarea:focus {
            outline: none;
            border-color: var(--btn-primary-bg);
        }
        .reply-form-actions {
            display: flex;
            gap: 10px;
            margin-top: 15px;
            justify-content: flex-end;
        }
        .btn {
            padding: 10px 20px;
            border-radius: 6px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
        }
        .btn-primary {
            background: var(--btn-primary-bg);
            color: white;
            border: none;
        }
        .btn-primary:hover {
            background: var(--btn-primary-hover);
        }
        .btn-secondary {
            background: white;
            color: var(--text-primary);
            border: 1px solid var(--border-color);
        }
        .btn-secondary:hover {
            background: var(--bg-secondary);
        }
        .btn-danger {
            background: #e74c3c;
            color: white;
            border: none;
        }
        .btn-danger:hover {
            background: #c0392b;
        }
        .status-badge {
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: 600;
        }
        .status-replied {
            background: #d4edda;
            color: #155724;
        }
        .status-pending {
            background: #fff3cd;
            color: #856404;
        }
        .status-deleted {
            background: #f8d7da;
            color: #721c24;
        }
        .page-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }
        .header-actions {
            display: flex;
            gap: 10px;
        }
        
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

        body.dark-mode .review-detail-card {
            background: #2c3e50;
        }
        body.dark-mode .product-name,
        body.dark-mode .product-name a {
            color: #ecf0f1;
        }
        body.dark-mode .review-content {
            color: #bdc3c7;
        }
        body.dark-mode .admin-reply-section {
            background: #34495e;
        }
        body.dark-mode .admin-reply-section h4 {
            color: #ecf0f1;
        }
        body.dark-mode .existing-reply {
            background: #2c3e50;
        }
        body.dark-mode .reply-content {
            color: #bdc3c7;
        }
        body.dark-mode .reply-form textarea {
            background: #2c3e50;
            border-color: #4a6278;
            color: #ecf0f1;
        }
        body.dark-mode .reply-form textarea::placeholder {
            color: #7f8c8d;
        }
        body.dark-mode .btn-secondary {
            background: #34495e;
            border-color: #4a6278;
            color: #ecf0f1;
        }
        body.dark-mode .profanity-error {
            background-color: #3d2020 !important;
        }
    </style>
</head>
<body>
    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/common/sidebar.jsp"/>

        <main class="main-content">
            <header class="top-bar">
                <h1>리뷰 상세</h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <div class="page-header">
                    <a href="${pageContext.request.contextPath}/admin/review" class="btn btn-secondary">← 목록으로</a>
                    <div class="header-actions">
                        <c:choose>
                            <c:when test="${review.isDeleted}">
                                <span class="status-badge status-deleted">삭제된 리뷰</span>
                            </c:when>
                            <c:when test="${not empty review.adminReply}">
                                <span class="status-badge status-replied">답변 완료</span>
                            </c:when>
                            <c:otherwise>
                                <span class="status-badge status-pending">미답변</span>
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${!review.isDeleted}">
                            <button class="btn btn-danger" onclick="deleteReview(${review.reviewId})">삭제</button>
                        </c:if>
                    </div>
                </div>

                <div class="review-detail-card">
                    <!-- 상품 정보 -->
                    <div class="review-header">
                        <div class="product-info">
                            <div class="product-thumb">
                                <c:if test="${not empty review.product.thumbnailUrl}">
                                    <img src="${pageContext.request.contextPath}${review.product.thumbnailUrl}" alt="${review.product.productName}">
                                </c:if>
                            </div>
                            <div>
                                <div class="product-name">
                                    <a href="${pageContext.request.contextPath}/admin/product/${review.product.productId}">
                                        ${review.product.productName}
                                    </a>
                                </div>
                                <div class="rating-stars">${review.ratingStars}</div>
                            </div>
                        </div>
                    </div>

                    <!-- 작성자 정보 -->
                    <div class="user-info">
                        <span>작성자: ${review.maskedUserName}</span>
                        <span>•</span>
                        <span>
                            <fmt:parseDate value="${review.createdDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both"/>
                            <fmt:formatDate value="${parsedDate}" pattern="yyyy년 MM월 dd일 HH:mm"/>
                        </span>
                        <c:if test="${review.updatedDate != review.createdDate}">
                            <span>•</span>
                            <span>수정됨</span>
                        </c:if>
                    </div>

                    <!-- 리뷰 내용 -->
                    <div class="review-content">${review.content}</div>

                    <!-- 리뷰 이미지 -->
                    <c:if test="${not empty review.images}">
                        <div class="review-images">
                            <c:forEach var="image" items="${review.images}">
                                <div class="review-image" onclick="window.open('${pageContext.request.contextPath}${image.imageUrl}', '_blank')">
                                    <img src="${pageContext.request.contextPath}${image.imageUrl}" alt="리뷰 이미지">
                                </div>
                            </c:forEach>
                        </div>
                    </c:if>

                    <!-- 관리자 답변 섹션 -->
                    <div class="admin-reply-section">
                        <h4>💬 관리자 답변 <span style="font-weight:normal;font-size:12px;color:#888;">(부적절한 표현 사용 불가)</span></h4>
                        
                        <c:if test="${not empty review.adminReply}">
                            <div class="existing-reply">
                                <div class="reply-date">
                                    <fmt:parseDate value="${review.adminReplyDate}" pattern="yyyy-MM-dd'T'HH:mm" var="replyDate" type="both"/>
                                    <fmt:formatDate value="${replyDate}" pattern="yyyy년 MM월 dd일 HH:mm"/>
                                </div>
                                <div class="reply-content">${review.adminReply}</div>
                            </div>
                        </c:if>
                        
                        <c:if test="${!review.isDeleted}">
                            <form class="reply-form" id="replyForm">
                                <textarea id="replyContent" placeholder="${empty review.adminReply ? '고객님께 답변을 작성해주세요.' : '답변을 수정하시려면 새 내용을 입력하세요.'}">${review.adminReply}</textarea>
                                <div id="replyProfanityError" class="profanity-error-msg" style="display:none;"></div>
                                <div class="reply-form-actions">
                                    <button type="button" class="btn btn-primary" id="btnSubmitReply" onclick="submitReply()">
                                        ${empty review.adminReply ? '답변 등록' : '답변 수정'}
                                    </button>
                                </div>
                            </form>
                        </c:if>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
    <script src="${pageContext.request.contextPath}/js/common/profanity.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        window.contextPath = contextPath;
        const reviewId = ${review.reviewId};
        
        // 답변 입력란 비속어 검사
        document.addEventListener('DOMContentLoaded', function() {
            const replyContent = document.getElementById('replyContent');
            const errorDiv = document.getElementById('replyProfanityError');
            
            if (replyContent) {
                ProfanityFilter.attachValidator(replyContent, {
                    errorMessage: '답변에 부적절한 표현이 포함되어 있습니다.',
                    showAlert: false,
                    debounceMs: 500
                });
            }
        });
        
        async function submitReply() {
            const content = document.getElementById('replyContent').value.trim();
            const errorDiv = document.getElementById('replyProfanityError');
            
            if (!content) {
                alert('답변 내용을 입력해주세요.');
                return;
            }
            
            // 비속어 검사
            const result = await ProfanityFilter.validate(content);
            
            if (result.hasProfanity) {
                document.getElementById('replyContent').classList.add('profanity-error');
                errorDiv.textContent = '⚠️ 답변에 부적절한 표현이 포함되어 있습니다: ' + (result.detectedWords ? result.detectedWords.join(', ') : '');
                errorDiv.style.display = 'block';
                alert('답변에 부적절한 표현이 포함되어 있습니다.\n내용을 수정해주세요.');
                return;
            }
            
            // 비속어가 없으면 서버로 전송
            fetch(contextPath + '/admin/review/reply/' + reviewId, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'reply=' + encodeURIComponent(content)
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert(data.message || '답변이 등록되었습니다.');
                    location.reload();
                } else {
                    alert(data.message || '답변 등록에 실패했습니다.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('답변 등록 중 오류가 발생했습니다.');
            });
        }
        
        function deleteReview(reviewId) {
            if (!confirm('이 리뷰를 삭제하시겠습니까?')) return;
            
            fetch(contextPath + '/admin/review/delete/' + reviewId, {
                method: 'POST'
            })
            .then(response => response.json())
            .then(data => {
                alert(data.message);
                if (data.success) {
                    location.href = contextPath + '/admin/review';
                }
            });
        }
    </script>
</body>
</html>
