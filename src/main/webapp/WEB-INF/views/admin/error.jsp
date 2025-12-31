<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>오류 - KH Shop Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <style>
        .error-container {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            min-height: calc(100vh - 200px);
            padding: 40px 20px;
            text-align: center;
        }

        .error-icon {
            font-size: 80px;
            margin-bottom: 20px;
        }

        .error-title {
            font-size: 28px;
            font-weight: 700;
            color: var(--text-primary);
            margin-bottom: 15px;
        }

        .error-message {
            font-size: 16px;
            color: var(--text-secondary);
            margin-bottom: 10px;
            max-width: 500px;
            line-height: 1.6;
        }

        .error-detail {
            font-size: 14px;
            color: var(--text-secondary);
            background: var(--bg-secondary);
            padding: 12px 20px;
            border-radius: 8px;
            margin-bottom: 30px;
            font-family: monospace;
        }

        .error-url {
            font-size: 13px;
            color: var(--text-secondary);
            margin-bottom: 30px;
        }

        .error-actions {
            display: flex;
            gap: 15px;
            flex-wrap: wrap;
            justify-content: center;
        }

        .btn {
            padding: 12px 28px;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 600;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            transition: all 0.3s;
            cursor: pointer;
            border: none;
        }

        .btn-primary {
            background: var(--btn-primary-bg);
            color: white;
        }

        .btn-primary:hover {
            background: var(--btn-primary-hover);
        }

        .btn-secondary {
            background: var(--bg-primary);
            color: var(--text-primary);
            border: 1px solid var(--border-color);
        }

        .btn-secondary:hover {
            background: var(--bg-secondary);
        }

        .error-code {
            font-size: 120px;
            font-weight: 900;
            color: var(--border-color);
            line-height: 1;
            margin-bottom: 20px;
        }

        /* 다크 모드 */
        body.dark-mode .error-detail {
            background: #34495e;
        }
    </style>
</head>
<body>
    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/common/sidebar.jsp"/>

        <main class="main-content">
            <header class="top-bar">
                <h1>오류</h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") != null ? session.getAttribute("loggedInUser") + "님" : "" %>
                </div>
            </header>

            <div class="content">
                <div class="error-container">
                    <c:choose>
                        <c:when test="${not empty errorCode}">
                            <div class="error-code">${errorCode}</div>
                        </c:when>
                        <c:otherwise>
                            <div class="error-icon">⚠️</div>
                        </c:otherwise>
                    </c:choose>

                    <h1 class="error-title">${not empty errorTitle ? errorTitle : '오류가 발생했습니다'}</h1>
                    
                    <p class="error-message">${not empty errorMessage ? errorMessage : '요청을 처리하는 중 문제가 발생했습니다.'}</p>
                    
                    <c:if test="${not empty errorDetail}">
                        <div class="error-detail">${errorDetail}</div>
                    </c:if>

                    <c:if test="${not empty requestUrl}">
                        <div class="error-url">요청 URL: ${requestUrl}</div>
                    </c:if>

                    <div class="error-actions">
                        <a href="${not empty backUrl ? backUrl : 'javascript:history.back()'}" class="btn btn-secondary">
                            ← 이전 페이지
                        </a>
                        <a href="${pageContext.request.contextPath}/admin" class="btn btn-primary">
                            🏠 관리자 홈으로
                        </a>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
</body>
</html>
