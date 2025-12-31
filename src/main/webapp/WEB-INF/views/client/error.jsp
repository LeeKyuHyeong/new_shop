<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>오류 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/main.css">
    <style>
        .error-container {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            min-height: 60vh;
            padding: 60px 20px;
            text-align: center;
        }

        .error-icon {
            font-size: 80px;
            margin-bottom: 20px;
        }

        .error-title {
            font-size: 28px;
            font-weight: 700;
            color: #2c3e50;
            margin-bottom: 15px;
        }

        .error-message {
            font-size: 16px;
            color: #7f8c8d;
            margin-bottom: 10px;
            max-width: 500px;
            line-height: 1.6;
        }

        .error-detail {
            font-size: 14px;
            color: #7f8c8d;
            background: #f8f9fa;
            padding: 12px 20px;
            border-radius: 8px;
            margin-bottom: 30px;
            font-family: monospace;
        }

        .error-url {
            font-size: 13px;
            color: #95a5a6;
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
            background: #3498db;
            color: white;
        }

        .btn-primary:hover {
            background: #2980b9;
        }

        .btn-secondary {
            background: white;
            color: #2c3e50;
            border: 1px solid #ddd;
        }

        .btn-secondary:hover {
            background: #f8f9fa;
        }

        .error-code {
            font-size: 120px;
            font-weight: 900;
            color: #ecf0f1;
            line-height: 1;
            margin-bottom: 20px;
        }

        /* 다크 모드 */
        body.dark-mode .error-title {
            color: #ecf0f1;
        }

        body.dark-mode .error-message {
            color: #95a5a6;
        }

        body.dark-mode .error-detail {
            background: #34495e;
            color: #bdc3c7;
        }

        body.dark-mode .error-code {
            color: #34495e;
        }

        body.dark-mode .btn-secondary {
            background: #2c3e50;
            border-color: #34495e;
            color: #ecf0f1;
        }

        body.dark-mode .btn-secondary:hover {
            background: #34495e;
        }

        @media (max-width: 480px) {
            .error-icon {
                font-size: 60px;
            }

            .error-title {
                font-size: 22px;
            }

            .error-code {
                font-size: 80px;
            }

            .error-actions {
                flex-direction: column;
                width: 100%;
            }

            .btn {
                width: 100%;
                justify-content: center;
            }
        }
    </style>
</head>
<body>
    <%@ include file="common/header.jsp" %>

    <main class="main-content">
        <div class="error-container">
            <c:choose>
                <c:when test="${not empty errorCode}">
                    <div class="error-code">${errorCode}</div>
                </c:when>
                <c:otherwise>
                    <div class="error-icon">😵</div>
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
                <a href="${pageContext.request.contextPath}/" class="btn btn-primary">
                    🏠 홈으로
                </a>
            </div>
        </div>
    </main>

    <footer class="footer">
        <p>&copy; 2024 KH SHOP. All rights reserved.</p>
    </footer>

    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
</body>
</html>
