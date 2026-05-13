<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%@ include file="/WEB-INF/views/common/security-headers.jsp" %>
    <title>팝업 관리 - KH SHOP Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/popup.css">
    <link href="https://cdn.jsdelivr.net/npm/summernote@0.8.20/dist/summernote-lite.min.css" rel="stylesheet">
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>

    <main class="main-content">
        <header class="top-bar">
            <h1><c:if test="${empty popup}">팝업 추가</c:if><c:if test="${not empty popup}">팝업 수정</c:if></h1>
            <div class="user-info">
                <%= session.getAttribute("loggedInUser") %>님
            </div>
        </header>

        <div class="content">
            <div class="alert-container"></div>

            <div class="form-box">
                <form id="popupForm" enctype="multipart/form-data">
                    <c:if test="${not empty popup}">
                        <input type="hidden" id="popupId" value="${popup.popupId}">
                    </c:if>

                    <div class="form-group">
                        <label for="popupTitle">팝업 제목 *</label>
                        <input type="text" id="popupTitle" name="popupTitle" placeholder="팝업 제목" required
                            value="<c:if test="${not empty popup}">${popup.popupTitle}</c:if>">
                    </div>

                    <div class="form-group">
                        <label for="popupContent">팝업 내용</label>
                        <div id="popupContent"><c:if test="${not empty popup}">${popup.popupContent}</c:if></div>
                        <input type="hidden" id="popupContentInput" name="popupContent">
                    </div>

                    <div class="form-group">
                        <label>팝업 이미지</label>
                        <div class="image-upload-area" id="imageArea">
                            <c:if test="${not empty popup && not empty popup.popupImageUrl}">
                                <div class="preview-image">
                                    <img src="${pageContext.request.contextPath}${popup.popupImageUrl}" alt="팝업이미지">
                                    <button type="button" class="remove-image" onclick="removeImage()">×</button>
                                </div>
                            </c:if>
                            <div class="upload-placeholder" id="imagePlaceholder" <c:if test="${not empty popup && not empty popup.popupImageUrl}">style="display:none"</c:if>>
                                <span>📷</span>
                                <p>클릭 또는 드래그하여 업로드</p>
                            </div>
                            <input type="file" id="popupImage" name="popupImage" accept="image/*" style="display:none">
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="popupLink">팝업 링크</label>
                        <input type="text" id="popupLink" name="popupLink" placeholder="클릭 시 이동할 URL"
                            value="<c:if test="${not empty popup}">${popup.popupLink}</c:if>">
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="popupWidth">가로 크기 (px)</label>
                            <input type="number" id="popupWidth" name="popupWidth" placeholder="400" min="200"
                                value="<c:if test="${not empty popup}">${popup.popupWidth}</c:if><c:if test="${empty popup}">400</c:if>">
                        </div>
                        <div class="form-group">
                            <label for="popupHeight">세로 크기 (px)</label>
                            <input type="number" id="popupHeight" name="popupHeight" placeholder="500" min="200"
                                value="<c:if test="${not empty popup}">${popup.popupHeight}</c:if><c:if test="${empty popup}">500</c:if>">
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="popupTop">Top 위치 (px)</label>
                            <input type="number" id="popupTop" name="popupTop" placeholder="100" min="0"
                                value="<c:if test="${not empty popup}">${popup.popupTop}</c:if><c:if test="${empty popup}">100</c:if>">
                        </div>
                        <div class="form-group">
                            <label for="popupLeft">Left 위치 (px)</label>
                            <input type="number" id="popupLeft" name="popupLeft" placeholder="100" min="0"
                                value="<c:if test="${not empty popup}">${popup.popupLeft}</c:if><c:if test="${empty popup}">100</c:if>">
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="popupOrder">표시 순서</label>
                        <input type="number" id="popupOrder" name="popupOrder" placeholder="0" min="0"
                            value="<c:if test="${not empty popup}">${popup.popupOrder}</c:if><c:if test="${empty popup}">0</c:if>">
                    </div>

                    <div class="form-buttons">
                        <button type="submit" class="btn btn-primary">
                            <c:if test="${empty popup}">등록</c:if>
                            <c:if test="${not empty popup}">수정</c:if>
                        </button>
                        <a href="${pageContext.request.contextPath}/admin/popup" class="btn btn-secondary">취소</a>
                    </div>
                </form>
            </div>
        </div>
    </main>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/summernote@0.8.20/dist/summernote-lite.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/summernote@0.8.20/dist/lang/summernote-ko-KR.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
    <script src="${pageContext.request.contextPath}/js/admin/popup-form.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        const isEdit = ${not empty popup};

        $(document).ready(function() {
            $('#popupContent').summernote({
                lang: 'ko-KR',
                height: 300,
                placeholder: '팝업 내용을 입력하세요.',
                toolbar: [
                    ['style', ['style']],
                    ['font', ['bold', 'italic', 'underline', 'clear']],
                    ['color', ['color']],
                    ['para', ['ul', 'ol', 'paragraph']],
                    ['insert', ['link', 'picture']],
                    ['view', ['codeview']]
                ]
            });
        });

    </script>
</body>
</html>
