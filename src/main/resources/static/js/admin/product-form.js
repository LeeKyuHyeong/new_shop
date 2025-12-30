// 썸네일 업로드 영역 클릭
document.getElementById('thumbnailPlaceholder').addEventListener('click', function() {
    document.getElementById('thumbnail').click();
});

// 상세 이미지 업로드 영역 클릭
document.getElementById('detailPlaceholder').addEventListener('click', function() {
    document.getElementById('detailImages').click();
});

// 썸네일 변경 미리보기
document.getElementById('thumbnail').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const thumbnailArea = document.getElementById('thumbnailArea');
            const placeholder = document.getElementById('thumbnailPlaceholder');

            // 기존 미리보기 제거
            const existingPreview = thumbnailArea.querySelector('.preview-image');
            if (existingPreview) {
                existingPreview.remove();
            }

            // 새 미리보기 추가
            const previewDiv = document.createElement('div');
            previewDiv.className = 'preview-image';
            previewDiv.innerHTML = `
                <img src="${e.target.result}" alt="썸네일">
                <button type="button" class="remove-image" onclick="removeThumbnail()">×</button>
            `;
            thumbnailArea.insertBefore(previewDiv, placeholder);
            placeholder.style.display = 'none';
        };
        reader.readAsDataURL(file);
    }
});

// 썸네일 제거
function removeThumbnail() {
    const thumbnailArea = document.getElementById('thumbnailArea');
    const preview = thumbnailArea.querySelector('.preview-image');
    const placeholder = document.getElementById('thumbnailPlaceholder');

    if (preview) {
        preview.remove();
    }
    document.getElementById('thumbnail').value = '';
    placeholder.style.display = 'flex';
}

// 상세 이미지 변경 미리보기
document.getElementById('detailImages').addEventListener('change', function(e) {
    const files = e.target.files;
    const container = document.getElementById('detailImagesContainer');
    const placeholder = document.getElementById('detailPlaceholder');

    for (let i = 0; i < files.length; i++) {
        const file = files[i];
        const reader = new FileReader();

        reader.onload = function(e) {
            const previewDiv = document.createElement('div');
            previewDiv.className = 'preview-image new-image';
            previewDiv.innerHTML = `
                <img src="${e.target.result}" alt="상세이미지">
                <button type="button" class="remove-image" onclick="removeNewDetailImage(this)">×</button>
            `;
            container.insertBefore(previewDiv, placeholder);
        };
        reader.readAsDataURL(file);
    }
});

// 새로 추가한 상세 이미지 제거
function removeNewDetailImage(btn) {
    btn.parentElement.remove();
}

// 기존 상세 이미지 제거 (삭제 대상에 추가)
let deleteImageIds = [];
function removeDetailImage(imageId, btn) {
    deleteImageIds.push(imageId);
    document.getElementById('deleteImageIds').value = deleteImageIds.join(',');
    btn.parentElement.remove();
}

// 폼 제출
document.getElementById('productForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const productName = document.getElementById('productName').value.trim();
    const productPrice = document.getElementById('productPrice').value;

    if (!productName) {
        showAlert('상품명을 입력하세요', 'error');
        return;
    }

    if (!productPrice || productPrice < 0) {
        showAlert('올바른 가격을 입력하세요', 'error');
        return;
    }

    const formData = new FormData();
    formData.append('productName', productName);
    formData.append('productPrice', productPrice);
    formData.append('productDiscount', document.getElementById('productDiscount').value || 0);
    formData.append('productStock', document.getElementById('productStock').value || 0);
    formData.append('color', document.getElementById('color').value.trim() || '');
    formData.append('size', document.getElementById('size').value.trim() || '');

    // Summernote 에디터에서 내용 가져오기
    let description = '';
    if (typeof $ !== 'undefined' && $('#productDescription').summernote) {
        description = $('#productDescription').summernote('code');
    } else {
        const descEl = document.getElementById('productDescription');
        description = descEl ? (descEl.value || descEl.innerHTML || '') : '';
    }
    formData.append('productDescription', description);
    formData.append('productOrder', document.getElementById('productOrder').value || 0);

    const categoryId = document.getElementById('categoryId').value;
    if (categoryId) {
        formData.append('categoryId', categoryId);
    }

    // 썸네일
    const thumbnailFile = document.getElementById('thumbnail').files[0];
    if (thumbnailFile) {
        formData.append('thumbnail', thumbnailFile);
    }

    // 상세 이미지
    const detailFiles = document.getElementById('detailImages').files;
    for (let i = 0; i < detailFiles.length; i++) {
        formData.append('detailImages', detailFiles[i]);
    }

    // 삭제할 이미지 ID
    if (deleteImageIds.length > 0) {
        deleteImageIds.forEach(id => {
            formData.append('deleteImageIds', id);
        });
    }

    const productId = document.getElementById('productId')?.value;
    const endpoint = isEdit ? `/api/admin/product/update/${productId}` : '/api/admin/product/create';

    fetch(contextPath + endpoint, {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert(data.message, 'success');
            setTimeout(() => {
                window.location.href = contextPath + '/admin/product';
            }, 500);
        } else {
            showAlert(data.message, 'error');
        }
    })
    .catch(error => {
        showAlert('요청 중 오류가 발생했습니다', 'error');
    });
});

function showAlert(message, type) {
    const alertContainer = document.querySelector('.alert-container');
    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.textContent = message;
    alertContainer.innerHTML = '';
    alertContainer.appendChild(alert);
}