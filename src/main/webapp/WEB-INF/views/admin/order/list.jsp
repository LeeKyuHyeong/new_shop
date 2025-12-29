<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>주문 관리 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/category.css">
    <style>
        .filter-bar {
            display: flex;
            gap: 10px;
            margin-bottom: 20px;
            flex-wrap: wrap;
        }
        .filter-btn {
            padding: 8px 16px;
            border: 1px solid var(--border-color);
            background: var(--bg-primary);
            color: var(--text-primary);
            border-radius: 20px;
            cursor: pointer;
            font-size: 13px;
            transition: all 0.3s;
        }
        .filter-btn:hover, .filter-btn.active {
            background: var(--btn-primary-bg);
            color: white;
            border-color: var(--btn-primary-bg);
        }
        .order-number {
            font-family: monospace;
            font-weight: 600;
        }
        .status-badge {
            padding: 4px 10px;
            border-radius: 4px;
            font-size: 12px;
            font-weight: 600;
        }
        .status-pending { background: #f39c12; color: white; }
        .status-paid { background: #3498db; color: white; }
        .status-preparing { background: #9b59b6; color: white; }
        .status-shipping { background: #1abc9c; color: white; }
        .status-delivered { background: #27ae60; color: white; }
        .status-cancelled { background: #e74c3c; color: white; }
        .status-refunded { background: #95a5a6; color: white; }
        .price-cell { text-align: right; }
    </style>
</head>
<body>
    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/common/sidebar.jsp"/>

        <main class="main-content">
            <header class="top-bar">
                <h1>주문 관리</h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <div class="alert-container"></div>

                <!-- 상태 필터 -->
                <div class="filter-bar">
                    <a href="${pageContext.request.contextPath}/admin/order" 
                       class="filter-btn <c:if test="${empty selectedStatus}">active</c:if>">전체</a>
                    <a href="${pageContext.request.contextPath}/admin/order?status=PENDING" 
                       class="filter-btn <c:if test="${selectedStatus eq 'PENDING'}">active</c:if>">주문대기</a>
                    <a href="${pageContext.request.contextPath}/admin/order?status=PAID" 
                       class="filter-btn <c:if test="${selectedStatus eq 'PAID'}">active</c:if>">결제완료</a>
                    <a href="${pageContext.request.contextPath}/admin/order?status=PREPARING" 
                       class="filter-btn <c:if test="${selectedStatus eq 'PREPARING'}">active</c:if>">상품준비중</a>
                    <a href="${pageContext.request.contextPath}/admin/order?status=SHIPPING" 
                       class="filter-btn <c:if test="${selectedStatus eq 'SHIPPING'}">active</c:if>">배송중</a>
                    <a href="${pageContext.request.contextPath}/admin/order?status=DELIVERED" 
                       class="filter-btn <c:if test="${selectedStatus eq 'DELIVERED'}">active</c:if>">배송완료</a>
                    <a href="${pageContext.request.contextPath}/admin/order?status=CANCELLED" 
                       class="filter-btn <c:if test="${selectedStatus eq 'CANCELLED'}">active</c:if>">취소</a>
                </div>

                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>주문번호</th>
                                <th>주문자</th>
                                <th>상품정보</th>
                                <th>결제금액</th>
                                <th>결제방법</th>
                                <th>주문상태</th>
                                <th>주문일시</th>
                                <th>관리</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="order" items="${orders}">
                                <tr>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/admin/order/detail/${order.orderId}" 
                                           class="order-number">${order.orderNumber}</a>
                                    </td>
                                    <td>
                                        ${order.user.userName}<br>
                                        <small style="color: var(--text-secondary);">${order.user.userId}</small>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${order.orderItems.size() > 1}">
                                                ${order.orderItems[0].productName} 외 ${order.orderItems.size() - 1}건
                                            </c:when>
                                            <c:otherwise>
                                                ${order.orderItems[0].productName}
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="price-cell">
                                        <strong><fmt:formatNumber value="${order.finalPrice}" pattern="#,###"/>원</strong>
                                    </td>
                                    <td>${order.paymentMethodName}</td>
                                    <td>
                                        <span class="status-badge status-${order.orderStatus.toLowerCase()}">${order.orderStatusName}</span>
                                    </td>
                                    <td>
                                        <fmt:parseDate value="${order.createdDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both"/>
                                        <fmt:formatDate value="${parsedDate}" pattern="yyyy-MM-dd HH:mm"/>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/admin/order/detail/${order.orderId}" 
                                           class="btn btn-small btn-info">상세</a>
                                        <c:if test="${order.orderStatus ne 'CANCELLED' and order.orderStatus ne 'DELIVERED'}">
                                            <button class="btn btn-small btn-danger" onclick="cancelOrder(${order.orderId})">취소</button>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                    <c:if test="${empty orders}">
                        <div class="empty-message">주문 내역이 없습니다.</div>
                    </c:if>
                </div>
            </div>
        </main>
    </div>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
        
        function cancelOrder(orderId) {
            const reason = prompt('취소 사유를 입력하세요:');
            if (reason === null) return;
            
            fetch(contextPath + '/admin/order/cancel/' + orderId, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'cancelReason=' + encodeURIComponent(reason)
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert(data.message);
                    location.reload();
                } else {
                    alert(data.message);
                }
            })
            .catch(error => {
                alert('오류가 발생했습니다.');
            });
        }
    </script>
    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
</body>
</html>
