<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- CSRF 토큰 메타 태그 --%>
<c:if test="${not empty _csrf}">
    <meta name="_csrf" content="${_csrf}">
    <meta name="_csrf_header" content="${_csrfHeader}">
    <meta name="_csrf_param" content="${_csrfParam}">
</c:if>
<%-- CSRF 토큰 자동 처리 스크립트 --%>
<script src="${pageContext.request.contextPath}/js/common/csrf.js"></script>
