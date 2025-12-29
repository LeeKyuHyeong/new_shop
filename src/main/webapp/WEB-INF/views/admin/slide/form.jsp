<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="activeMenu" value="slide"/>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>슬라이드 관리 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/slide.css">
</head>
<body>

    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/common/sidebar.jsp"/>

        <main class="main-content">
            <header class="top-bar">
                <h1><c:if test="${empty slide}">슬라이드 추가</c:if><c:if test="${not empty slide}">슬라이드 수정</c:if></h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <div class="alert-container"></div>

                <div class="form-box slide-form-box">
                    <form id="slideForm" class="form" enctype="multipart/form-data">
                        <c:if test="${not empty slide}">
                            <input type="hidden" id="slideId" value="${slide.slideId}">
                        </c:if>

                        <div class="form-group">
                            <label for="slideTitle">슬라이드 제목 *</label>
                            <input type="text" id="slideTitle" name="slideTitle" placeholder="슬라이드 제목 입력" required
                                value="<c:if test="${not empty slide}">${slide.slideTitle}</c:if>">
                        </div>

                        <div class="form-group">
                            <label for="slideDescription">설명</label>
                            <textarea id="slideDescription" name="slideDescription" placeholder="슬라이드 설명 입력" rows="3"><c:if test="${not empty slide}">${slide.slideDescription}</c:if></textarea>
                        </div>

                        <div class="form-group">
                            <label for="linkUrl">링크 URL</label>
                            <input type="text" id="linkUrl" name="linkUrl" placeholder="클릭 시 이동할 URL (예: /product/1)"
                                value="<c:if test="${not empty slide}">${slide.linkUrl}</c:if>">
                        </div>

                        <div class="form-row">
                            <div class="form-group">
                                <label for="duration">지속 시간 (초)</label>
                                <input type="number" id="duration" name="duration" placeholder="5" min="1" max="30"
                                    value="<c:if test="${not empty slide}">${slide.duration}</c:if><c:if test="${empty slide}">5</c:if>">
                            </div>
                            <div class="form-group">
                                <label for="slideOrder">순서</label>
                                <input type="number" id="slideOrder" name="slideOrder" placeholder="0" min="0"
                                    value="<c:if test="${not empty slide}">${slide.slideOrder}</c:if>">
                            </div>
                        </div>

                        <div class="form-row">
                            <div class="form-group">
                                <label for="startDate">시작일시</label>
                                <input type="datetime-local" id="startDate" name="startDate"
                                    <c:if test="${not empty slide && not empty slide.startDate}">
                                        value="<fmt:parseDate value="${slide.startDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedStart" type="both"/><fmt:formatDate value="${parsedStart}" pattern="yyyy-MM-dd'T'HH:mm"/>"
                                    </c:if>>
                                <small class="form-hint">비워두면 즉시 시작</small>
                            </div>
                            <div class="form-group">
                                <label for="endDate">종료일시</label>
                                <input type="datetime-local" id="endDate" name="endDate"
                                    <c:if test="${not empty slide && not empty slide.endDate}">
                                        value="<fmt:parseDate value="${slide.endDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedEnd" type="both"/><fmt:formatDate value="${parsedEnd}" pattern="yyyy-MM-dd'T'HH:mm"/>"
                                    </c:if>>
                                <small class="form-hint">비워두면 종료 없음</small>
                            </div>
                        </div>

                        <div class="form-group">
                            <label>슬라이드 이미지 <c:if test="${empty slide}">*</c:if></label>
                            <div class="image-upload-area" id="imageArea">
                                <c:if test="${not empty slide && not empty slide.imageUrl}">
                                    <div class="preview-image slide-preview">
                                        <img src="${pageContext.request.contextPath}${slide.imageUrl}" alt="슬라이드 이미지">
                                        <button type="button" class="remove-image" onclick="removeImage()">×</button>
                                    </div>
                                </c:if>
                                <div class="upload-placeholder slide-upload" id="imagePlaceholder" <c:if test="${not empty slide && not empty slide.imageUrl}">style="display:none"</c:if>>
                                    <span>🖼️</span>
                                    <p>클릭하여 이미지 업로드</p>
                                    <p class="hint">권장: 1920x600px, 최대 10MB</p>
                                </div>
                                <input type="file" id="image" name="image" accept="image/*" style="display:none">
                            </div>
                        </div>

                        <div class="form-buttons">
                            <button type="submit" class="btn btn-primary">
                                <c:if test="${empty slide}">등록</c:if>
                                <c:if test="${not empty slide}">수정</c:if>
                            </button>
                            <a href="${pageContext.request.contextPath}/admin/slide" class="btn btn-secondary">취소</a>
                        </div>
                    </form>
                </div>
            </div>
        </main>
    </div>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
        const isEdit = ${not empty slide};
    </script>
    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
    <script src="${pageContext.request.contextPath}/js/admin/slide-form.js"></script>
</body>
</html>
