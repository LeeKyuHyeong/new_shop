<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="activeMenu" value="product"/>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%@ include file="/WEB-INF/views/common/security-headers.jsp" %>
    <title>상품 관리 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/product.css">
    <!-- Summernote CSS -->
    <link href="https://cdn.jsdelivr.net/npm/summernote@0.8.20/dist/summernote-lite.min.css" rel="stylesheet">
    <style>
        .note-editor {
            border: 1px solid #ddd !important;
            border-radius: 8px !important;
            overflow: hidden;
        }
        .note-editor .note-toolbar {
            background: #f8f9fa;
            border-bottom: 1px solid #ddd;
        }
        .note-editor .note-editing-area {
            background: white;
        }
        .note-editor .note-editable {
            min-height: 300px;
            padding: 20px;
        }
        body.dark-mode .note-editor {
            border-color: #34495e !important;
        }
        body.dark-mode .note-editor .note-toolbar {
            background: #34495e;
            border-bottom-color: #2c3e50;
        }
        body.dark-mode .note-editor .note-editing-area,
        body.dark-mode .note-editor .note-editable {
            background: #2c3e50;
            color: #ecf0f1;
        }
        body.dark-mode .note-btn {
            background: #34495e;
            border-color: #34495e;
            color: #ecf0f1;
        }
        body.dark-mode .note-dropdown-menu {
            background: #34495e;
        }
        body.dark-mode .note-dropdown-item:hover {
            background: #2c3e50;
        }
        
        /* 옵션 입력 스타일 */
        .option-section {
            background: #f8f9fa;
            border-radius: 8px;
            padding: 20px;
            margin-bottom: 20px;
        }
        .option-section h3 {
            margin: 0 0 15px 0;
            font-size: 16px;
            color: #333;
        }
        .option-hint {
            font-size: 12px;
            color: #888;
            margin-top: 5px;
        }
        .option-preview {
            margin-top: 10px;
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
        }
        .option-tag {
            display: inline-flex;
            align-items: center;
            background: #e9ecef;
            padding: 4px 10px;
            border-radius: 20px;
            font-size: 13px;
        }
        .option-tag.color-tag {
            background: #3498db;
            color: white;
        }
        .option-tag.size-tag {
            background: #9b59b6;
            color: white;
        }
        body.dark-mode .option-section {
            background: #34495e;
        }
        body.dark-mode .option-section h3 {
            color: #ecf0f1;
        }
        body.dark-mode .option-tag {
            background: #2c3e50;
            color: #ecf0f1;
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
        .input-warning {
            color: #856404;
            background-color: #fff3cd;
            border: 1px solid #ffc107;
            padding: 8px 12px;
            border-radius: 4px;
            margin-top: 8px;
            font-size: 0.9rem;
        }
        body.dark-mode .profanity-error {
            background-color: #3d2020 !important;
        }
        body.dark-mode .input-warning {
            background-color: #3d3820;
            color: #f5dba5;
            border-color: #856404;
        }
    </style>
</head>
<body>

    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/common/sidebar.jsp"/>

        <main class="main-content">
            <header class="top-bar">
                <h1><c:if test="${empty product}">상품 추가</c:if><c:if test="${not empty product}">상품 수정</c:if></h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <div class="alert-container"></div>

                <div class="form-box product-form-box">
                    <form id="productForm" class="form" enctype="multipart/form-data">
                        <c:if test="${not empty product}">
                            <input type="hidden" id="productId" value="${product.productId}">
                        </c:if>

                        <div class="form-row">
                            <div class="form-group">
                                <label for="categoryId">카테고리</label>
                                <select id="categoryId" name="categoryId">
                                    <option value="">카테고리 선택</option>
                                    <c:forEach var="parent" items="${parentCategories}">
                                        <optgroup label="${parent.categoryName}">
                                            <c:forEach var="child" items="${parent.children}">
                                                <c:if test="${child.useYn eq 'Y'}">
                                                    <option value="${child.categoryId}"
                                                        <c:if test="${not empty product && product.categoryId eq child.categoryId}">selected</c:if>>
                                                        ${child.categoryName}
                                                    </option>
                                                </c:if>
                                            </c:forEach>
                                        </optgroup>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="productName">상품명 * <span style="font-weight:normal;font-size:12px;color:#888;">(부적절한 표현 사용 불가)</span></label>
                            <input type="text" id="productName" name="productName" placeholder="상품명 입력" required
                                value="<c:if test="${not empty product}">${product.productName}</c:if>">
                            <div id="productNameError" class="profanity-error-msg" style="display:none;"></div>
                        </div>

                        <div class="form-row">
                            <div class="form-group">
                                <label for="productPrice">가격 *</label>
                                <input type="number" id="productPrice" name="productPrice" placeholder="가격 입력" required min="0"
                                    value="<c:if test="${not empty product}">${product.productPrice}</c:if>">
                            </div>
                            <div class="form-group">
                                <label for="productDiscount">할인율 (%)</label>
                                <input type="number" id="productDiscount" name="productDiscount" placeholder="할인율" min="0" max="100"
                                    value="<c:if test="${not empty product}">${product.productDiscount}</c:if>">
                            </div>
                            <div class="form-group">
                                <label for="productStock">재고</label>
                                <input type="number" id="productStock" name="productStock" placeholder="재고 수량" min="0"
                                    value="<c:if test="${not empty product}">${product.productStock}</c:if>">
                            </div>
                        </div>

                        <div class="option-section">
                            <h3>상품 옵션</h3>
                            <div class="form-row">
                                <div class="form-group">
                                    <label for="color">색상</label>
                                    <input type="text" id="color" name="color" placeholder="예: 블랙, 화이트, 네이비"
                                        value="<c:if test="${not empty product}">${product.color}</c:if>">
                                    <p class="option-hint">콤마(,)로 구분하여 여러 색상 입력</p>
                                    <div class="option-preview" id="colorPreview"></div>
                                </div>
                                <div class="form-group">
                                    <label for="size">사이즈</label>
                                    <input type="text" id="size" name="size" placeholder="예: S, M, L, XL"
                                        value="<c:if test="${not empty product}">${product.size}</c:if>">
                                    <p class="option-hint">콤마(,)로 구분하여 여러 사이즈 입력</p>
                                    <div class="option-preview" id="sizePreview"></div>
                                </div>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="productDescription">상품 설명 <span style="font-weight:normal;font-size:12px;color:#888;">(부적절한 표현 사용 불가)</span></label>
                            <div id="productDescription"><c:if test="${not empty product}">${product.productDescription}</c:if></div>
                            <input type="hidden" id="productDescriptionInput" name="productDescription">
                            <div id="productDescError" class="profanity-error-msg" style="display:none;"></div>
                        </div>

                        <div class="form-group">
                            <label for="productOrder">순서</label>
                            <input type="number" id="productOrder" name="productOrder" placeholder="순서 입력" min="0"
                                value="<c:if test="${not empty product}">${product.productOrder}</c:if>">
                        </div>

                        <div class="form-group">
                            <label>썸네일 이미지</label>
                            <div class="image-upload-area" id="thumbnailArea">
                                <c:if test="${not empty product && not empty product.thumbnailUrl}">
                                    <div class="preview-image">
                                        <img src="${pageContext.request.contextPath}${product.thumbnailUrl}" alt="썸네일">
                                        <button type="button" class="remove-image" onclick="removeThumbnail()">×</button>
                                    </div>
                                </c:if>
                                <div class="upload-placeholder" id="thumbnailPlaceholder" <c:if test="${not empty product && not empty product.thumbnailUrl}">style="display:none"</c:if>>
                                    <span>📷</span>
                                    <p>클릭 또는 드래그하여 업로드</p>
                                    <p class="hint">권장: 500x500px, 최대 5MB</p>
                                </div>
                                <input type="file" id="thumbnail" name="thumbnail" accept="image/*" style="display:none">
                            </div>
                        </div>

                        <div class="form-group">
                            <label>상세 이미지 (여러 장 선택 가능)</label>
                            <div class="detail-images-container" id="detailImagesContainer">
                                <c:if test="${not empty product && not empty product.images}">
                                    <c:forEach var="image" items="${product.images}">
                                        <c:if test="${image.useYn eq 'Y'}">
                                            <div class="preview-image existing-image" data-image-id="${image.imageId}">
                                                <img src="${pageContext.request.contextPath}${image.imageUrl}" alt="상세이미지">
                                                <button type="button" class="remove-image" onclick="removeDetailImage(${image.imageId}, this)">×</button>
                                            </div>
                                        </c:if>
                                    </c:forEach>
                                </c:if>
                                <div class="upload-placeholder detail-upload" id="detailPlaceholder">
                                    <span>+</span>
                                    <p>클릭 또는 드래그</p>
                                </div>
                                <input type="file" id="detailImages" name="detailImages" accept="image/*" multiple style="display:none">
                            </div>
                            <input type="hidden" id="deleteImageIds" name="deleteImageIds" value="">
                        </div>

                        <div class="form-buttons">
                            <button type="submit" class="btn btn-primary" id="btnSubmit">
                                <c:if test="${empty product}">등록</c:if>
                                <c:if test="${not empty product}">수정</c:if>
                            </button>
                            <a href="${pageContext.request.contextPath}/admin/product" class="btn btn-secondary">취소</a>
                        </div>
                    </form>
                </div>
            </div>
        </main>
    </div>

    <!-- jQuery (Summernote 의존성) -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <!-- Summernote JS -->
    <script src="https://cdn.jsdelivr.net/npm/summernote@0.8.20/dist/summernote-lite.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/summernote@0.8.20/dist/lang/summernote-ko-KR.min.js"></script>
    
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        const isEdit = ${not empty product};
        
        // Summernote 초기화
        $(document).ready(function() {
            $('#productDescription').summernote({
                lang: 'ko-KR',
                height: 400,
                placeholder: '상품 설명을 입력하세요. 이미지도 추가할 수 있습니다.',
                toolbar: [
                    ['style', ['style']],
                    ['font', ['bold', 'italic', 'underline', 'strikethrough', 'clear']],
                    ['fontname', ['fontname']],
                    ['fontsize', ['fontsize']],
                    ['color', ['color']],
                    ['para', ['ul', 'ol', 'paragraph']],
                    ['table', ['table']],
                    ['insert', ['link', 'picture', 'video']],
                    ['view', ['fullscreen', 'codeview', 'help']]
                ],
                fontNames: ['맑은 고딕', '굴림', '돋움', 'Noto Sans KR', 'Arial', 'Times New Roman'],
                fontNamesIgnoreCheck: ['맑은 고딕', '굴림', '돋움', 'Noto Sans KR'],
                callbacks: {
                    onImageUpload: function(files) {
                        for (let i = 0; i < files.length; i++) {
                            uploadImage(files[i]);
                        }
                    }
                }
            });
            
            // 옵션 미리보기 초기화
            updateOptionPreview('color');
            updateOptionPreview('size');
            
            // 상품명 비속어 검사 이벤트
            const productNameInput = document.getElementById('productName');
            const productNameError = document.getElementById('productNameError');
            let nameDebounceTimer;
            
            productNameInput.addEventListener('input', function() {
                clearTimeout(nameDebounceTimer);
                nameDebounceTimer = setTimeout(async () => {
                    const text = this.value;
                    if (text.length < 2) {
                        productNameError.style.display = 'none';
                        productNameInput.classList.remove('profanity-error');
                        return;
                    }
                    
                    const result = await validateProfanity(text);
                    
                    if (result.hasProfanity) {
                        productNameInput.classList.add('profanity-error');
                        productNameError.textContent = '⚠️ 상품명에 부적절한 표현이 포함되어 있습니다.';
                        productNameError.style.display = 'block';
                    } else {
                        productNameInput.classList.remove('profanity-error');
                        productNameError.style.display = 'none';
                    }
                }, 500);
            });
        });
        
        // 비속어 검증 API 호출
        async function validateProfanity(text) {
            try {
                const response = await fetch(contextPath + '/api/profanity/validate', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ text: text })
                });
                return await response.json();
            } catch (error) {
                console.error('Profanity validation error:', error);
                return { isValid: true, hasProfanity: false };
            }
        }
        
        // 이미지 업로드
        function uploadImage(file) {
            const formData = new FormData();
            formData.append('file', file);
            
            $.ajax({
                url: contextPath + '/api/upload/editor-image',
                method: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function(response) {
                    if (response.success) {
                        $('#productDescription').summernote('insertImage', contextPath + response.url);
                    } else {
                        alert(response.message);
                    }
                },
                error: function() {
                    alert('이미지 업로드에 실패했습니다.');
                }
            });
        }
        
        // 옵션 미리보기 업데이트
        function updateOptionPreview(type) {
            const input = document.getElementById(type);
            const preview = document.getElementById(type + 'Preview');
            const value = input.value.trim();
            
            if (!value) {
                preview.innerHTML = '';
                return;
            }
            
            const options = value.split(',').map(s => s.trim()).filter(s => s);
            const tagClass = type === 'color' ? 'color-tag' : 'size-tag';
            
            preview.innerHTML = options.map(opt => 
                `<span class="option-tag ${tagClass}">${opt}</span>`
            ).join('');
        }
        
        // 옵션 입력 이벤트
        document.getElementById('color').addEventListener('input', () => updateOptionPreview('color'));
        document.getElementById('size').addEventListener('input', () => updateOptionPreview('size'));
        
        // 폼 제출 전 비속어 검증
        document.getElementById('productForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const productName = document.getElementById('productName').value;
            const productDescription = $('#productDescription').summernote('code');
            // HTML 태그 제거하여 텍스트만 추출
            const descText = productDescription.replace(/<[^>]*>/g, ' ').replace(/&nbsp;/g, ' ').trim();
            
            // 상품명 비속어 검사
            const nameResult = await validateProfanity(productName);
            if (nameResult.hasProfanity) {
                document.getElementById('productName').classList.add('profanity-error');
                document.getElementById('productNameError').textContent = '⚠️ 상품명에 부적절한 표현이 포함되어 있습니다: ' + (nameResult.detectedWords ? nameResult.detectedWords.join(', ') : '');
                document.getElementById('productNameError').style.display = 'block';
                alert('상품명에 부적절한 표현이 포함되어 있습니다.\n내용을 수정해주세요.');
                document.getElementById('productName').focus();
                return;
            }
            
            // 상품 설명 비속어 검사
            if (descText.length > 0) {
                const descResult = await validateProfanity(descText);
                if (descResult.hasProfanity) {
                    document.getElementById('productDescError').textContent = '⚠️ 상품 설명에 부적절한 표현이 포함되어 있습니다: ' + (descResult.detectedWords ? descResult.detectedWords.join(', ') : '');
                    document.getElementById('productDescError').style.display = 'block';
                    alert('상품 설명에 부적절한 표현이 포함되어 있습니다.\n내용을 수정해주세요.');
                    return;
                }
            }
            
            // 비속어가 없으면 원래 제출 로직 실행
            submitProductForm();
        });
        
        // 상품 폼 제출 (원래 로직)
        function submitProductForm() {
            const formData = new FormData(document.getElementById('productForm'));
            formData.set('productDescription', $('#productDescription').summernote('code'));

            const productId = document.getElementById('productId') ? document.getElementById('productId').value : null;
            const url = productId
                ? contextPath + '/api/admin/product/update/' + productId
                : contextPath + '/api/admin/product/create';
            const method = 'POST';
            
            fetch(url, {
                method: method,
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert(productId ? '상품이 수정되었습니다.' : '상품이 등록되었습니다.');
                    location.href = contextPath + '/admin/product';
                } else {
                    alert(data.message || '처리 중 오류가 발생했습니다.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('처리 중 오류가 발생했습니다.');
            });
        }
    </script>
    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
    <script src="${pageContext.request.contextPath}/js/admin/product-form.js"></script>
</body>
</html>
