<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- Favicon (?v=N: 아이콘 변경 시 버전을 올려 브라우저 파비콘 캐시 무력화) --%>
<link rel="icon" href="${pageContext.request.contextPath}/images/favicon.ico?v=3" type="image/x-icon">
<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico?v=3" type="image/x-icon">
<%-- CSRF 토큰 메타 태그 --%>
<c:if test="${not empty _csrf}">
    <meta name="_csrf" content="${_csrf}">
    <meta name="_csrf_header" content="${_csrfHeader}">
    <meta name="_csrf_param" content="${_csrfParam}">
</c:if>
<%-- 웹접근성 CSS --%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/accessibility.css">
<%-- CSRF 토큰 자동 처리 스크립트 --%>
<script src="${pageContext.request.contextPath}/js/common/csrf.js"></script>
<%-- 웹접근성 개선 스크립트 --%>
<script src="${pageContext.request.contextPath}/js/common/accessibility.js"></script>
