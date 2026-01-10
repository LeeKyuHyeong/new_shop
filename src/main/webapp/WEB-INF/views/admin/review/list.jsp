<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="activeMenu" value="review"/>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>리뷰 관리 - KH Shop Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <style>
        .filter-tabs {
            display: flex;
            gap: 10px;
            margin-bottom: 20px;
        }
        .filter-tab {
            padding: 10px 20px;
            background: var(--bg-secondary);
            border: 1px solid var(--border-color);
            border-radius: 8px;
            cursor: pointer;
            text-decoration: none;
            color: var(--text-primary);
            font-weight: 500;
        }
        .filter-tab.active {
            background: var(--btn-primary-bg);
            color: white;
            border-color: var(--btn-primary-bg);
        }
        .review-table {
            width: 100%;
            border-collapse: collapse;
        }
        .review-table th,
        .review-table td {
            padding: 15px;
            text-align: left;
            border-bottom: 1px solid var(--border-color);
        }
        .review-table th {
            background: var(--bg-secondary);
            font-weight: 600;
        }
        .review-table tr:hover {
            background: var(--bg-secondary);
        }
        .rating-stars {
            color: #f39c12;
        }
        .review-content-preview {
            max-width: 300px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .badge {
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 600;
        }
        .badge-replied {
            background: #d4edda;
            color: #155724;
        }
        .badge-pending {
            background: #fff3cd;
            color: #856404;
        }
        .badge-deleted {
            background: #f8d7da;
            color: #721c24;
        }
        .action-btn {
            padding: 6px 12px;
            border: 1px solid var(--border-color);
            background: white;
            border-radius: 4px;
            cursor: pointer;
            font-size: 13px;
            margin-right: 5px;
        }
        .action-btn:hover {
            border-color: var(--btn-primary-bg);
            color: var(--btn-primary-bg);
        }
        .action-btn.delete:hover {
            border-color: #e74c3c;
            color: #e74c3c;
        }
        .pagination {
            display: flex;
            justify-content: center;
            gap: 5px;
            margin-top: 30px;
        }
        .pagination a, .pagination span {
            padding: 8px 14px;
            border: 1px solid var(--border-color);
            border-radius: 4px;
            text-decoration: none;
            color: var(--text-primary);
        }
        .pagination a:hover {
            background: var(--btn-primary-bg);
            color: white;
            border-color: var(--btn-primary-bg);
        }
        .pagination .current {
            background: var(--btn-primary-bg);
            color: white;
            border-color: var(--btn-primary-bg);
        }
        .empty-message {
            text-align: center;
            padding: 60px;
            color: var(--text-secondary);
        }

        /* 다크 모드 */
        body.dark-mode .review-card {
            background: #1a1a1a;
            border-color: #444444;
        }
        body.dark-mode .product-name {
            color: #ecf0f1;
        }
        body.dark-mode .review-text {
            color: #b0b0b0;
        }
        body.dark-mode .user-name {
            color: #ecf0f1;
        }
        body.dark-mode .review-date {
            color: #808080;
        }
    </style>
</head>
<body>
    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/common/sidebar.jsp"/>

        <main class="main-content">
            <header class="top-bar">
                <h1>리뷰 관리</h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <!-- 필터 탭 -->
                <div class="filter-tabs">
                    <a href="${pageContext.request.contextPath}/admin/review?filter=all" 
                       class="filter-tab ${filter eq 'all' ? 'active' : ''}">전체 리뷰</a>
                    <a href="${pageContext.request.contextPath}/admin/review?filter=unanswered" 
                       class="filter-tab ${filter eq 'unanswered' ? 'active' : ''}">미답변 리뷰</a>
                </div>

                <!-- 리뷰 테이블 -->
                <div class="table-container">
                    <c:choose>
                        <c:when test="${not empty reviews}">
                            <table class="data-table review-table">
                                <thead>
                                    <tr>
                                        <th>번호</th>
                                        <th>상품</th>
                                        <th>작성자</th>
                                        <th>별점</th>
                                        <th>내용</th>
                                        <th>상태</th>
                                        <th>작성일</th>
                                        <th>관리</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="review" items="${reviews}">
                                        <tr>
                                            <td data-label="번호">${review.reviewId}</td>
                                            <td data-label="상품">
                                                <a href="${pageContext.request.contextPath}/admin/product/detail/${review.product.productId}">
                                                    ${review.product.productName}
                                                </a>
                                            </td>
                                            <td data-label="작성자">${review.maskedUserName}</td>
                                            <td data-label="별점" class="rating-stars">${review.ratingStars}</td>
                                            <td data-label="내용" class="review-content-preview">${review.content}</td>
                                            <td data-label="상태">
                                                <c:choose>
                                                    <c:when test="${review.isDeleted}">
                                                        <span class="badge badge-deleted">삭제됨</span>
                                                    </c:when>
                                                    <c:when test="${not empty review.adminReply}">
                                                        <span class="badge badge-replied">답변완료</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge badge-pending">미답변</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td data-label="작성일">
                                                <fmt:parseDate value="${review.createdDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both"/>
                                                <fmt:formatDate value="${parsedDate}" pattern="yyyy-MM-dd"/>
                                            </td>
                                            <td class="action-cell">
                                                <a href="${pageContext.request.contextPath}/admin/review/${review.reviewId}" class="action-btn">상세</a>
                                                <c:if test="${!review.isDeleted}">
                                                    <button class="action-btn delete" onclick="deleteReview(${review.reviewId})">삭제</button>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-message">
                                <p>리뷰가 없습니다.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- 페이지네이션 -->
                <c:if test="${totalPages > 1}">
                    <div class="pagination">
                        <c:if test="${currentPage > 0}">
                            <a href="${pageContext.request.contextPath}/admin/review?page=${currentPage - 1}&filter=${filter}">이전</a>
                        </c:if>
                        
                        <c:forEach begin="0" end="${totalPages - 1}" var="i">
                            <c:choose>
                                <c:when test="${i == currentPage}">
                                    <span class="current">${i + 1}</span>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/admin/review?page=${i}&filter=${filter}">${i + 1}</a>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                        
                        <c:if test="${currentPage < totalPages - 1}">
                            <a href="${pageContext.request.contextPath}/admin/review?page=${currentPage + 1}&filter=${filter}">다음</a>
                        </c:if>
                    </div>
                </c:if>
            </div>
        </main>
    </div>

    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        
        function deleteReview(reviewId) {
            if (!confirm('이 리뷰를 삭제하시겠습니까?')) return;
            
            fetch(contextPath + '/admin/review/delete/' + reviewId, {
                method: 'POST'
            })
            .then(response => response.json())
            .then(data => {
                alert(data.message);
                if (data.success) {
                    location.reload();
                }
            });
        }
    </script>
</body>
</html>
