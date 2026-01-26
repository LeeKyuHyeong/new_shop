// 썸네일 업로드 영역 클릭
document.getElementById('thumbnailPlaceholder').addEventListener('click', function() {
    document.getElementById('thumbnail').click();
});

// 상세 이미지 업로드 영역 클릭
document.getElementById('detailPlaceholder').addEventListener('click', function() {
    document.getElementById('detailImages').click();
});

// ========================================
// 드래그 앤 드롭 기능
// ========================================

// 드래그 앤 드롭 이벤트 설정
function setupDragAndDrop(dropZone, inputElement, isMultiple) {
    // 기본 동작 방지
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, preventDefaults, false);
    });

    function preventDefaults(e) {
        e.preventDefault();
        e.stopPropagation();
    }

    // 드래그 오버 스타일
    ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => {
            dropZone.classList.add('drag-over');
        }, false);
    });

    ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, () => {
            dropZone.classList.remove('drag-over');
        }, false);
    });

    // 드롭 처리
    dropZone.addEventListener('drop', (e) => {
        const dt = e.dataTransfer;
        const files = dt.files;

        if (files.length > 0) {
            // 이미지 파일만 필터링
            const imageFiles = Array.from(files).filter(file => file.type.startsWith('image/'));

            if (imageFiles.length === 0) {
                alert('이미지 파일만 업로드 가능합니다.');
                return;
            }

            if (isMultiple) {
                // 상세 이미지: 여러 파일 처리
                handleDetailImagesDrop(imageFiles);
            } else {
                // 썸네일: 단일 파일 처리
                handleThumbnailDrop(imageFiles[0]);
            }
        }
    }, false);
}

// 썸네일 드롭 처리
function handleThumbnailDrop(file) {
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

    // input에 파일 설정 (DataTransfer 사용)
    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(file);
    document.getElementById('thumbnail').files = dataTransfer.files;
}

// 상세 이미지 드롭 처리
function handleDetailImagesDrop(files) {
    const container = document.getElementById('detailImagesContainer');
    const placeholder = document.getElementById('detailPlaceholder');

    files.forEach(file => {
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
    });

    // input에 파일 추가 (기존 파일과 합침)
    const existingFiles = document.getElementById('detailImages').files;
    const dataTransfer = new DataTransfer();

    // 기존 파일 추가
    Array.from(existingFiles).forEach(file => {
        dataTransfer.items.add(file);
    });

    // 새 파일 추가
    files.forEach(file => {
        dataTransfer.items.add(file);
    });

    document.getElementById('detailImages').files = dataTransfer.files;
}

// 드래그 앤 드롭 초기화
document.addEventListener('DOMContentLoaded', function() {
    const thumbnailArea = document.getElementById('thumbnailArea');
    const detailContainer = document.getElementById('detailImagesContainer');

    if (thumbnailArea) {
        setupDragAndDrop(thumbnailArea, document.getElementById('thumbnail'), false);
    }

    if (detailContainer) {
        setupDragAndDrop(detailContainer, document.getElementById('detailImages'), true);
    }
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