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

// showAlert 함수 (다른 곳에서 사용할 수 있도록 유지)
function showAlert(message, type) {
    const alertContainer = document.querySelector('.alert-container');
    if (alertContainer) {
        const alert = document.createElement('div');
        alert.className = `alert alert-${type}`;
        alert.textContent = message;
        alertContainer.innerHTML = '';
        alertContainer.appendChild(alert);
    }
}