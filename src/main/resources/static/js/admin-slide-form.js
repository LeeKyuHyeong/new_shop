// 이미지 업로드 영역 클릭
document.getElementById('imagePlaceholder').addEventListener('click', function() {
    document.getElementById('image').click();
});

// 이미지 변경 미리보기
document.getElementById('image').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const imageArea = document.getElementById('imageArea');
            const placeholder = document.getElementById('imagePlaceholder');

            // 기존 미리보기 제거
            const existingPreview = imageArea.querySelector('.preview-image');
            if (existingPreview) {
                existingPreview.remove();
            }

            // 새 미리보기 추가
            const previewDiv = document.createElement('div');
            previewDiv.className = 'preview-image slide-preview';
            previewDiv.innerHTML = `
                <img src="${e.target.result}" alt="슬라이드 이미지">
                <button type="button" class="remove-image" onclick="removeImage()">×</button>
            `;
            imageArea.insertBefore(previewDiv, placeholder);
            placeholder.style.display = 'none';
        };
        reader.readAsDataURL(file);
    }
});

// 이미지 제거
function removeImage() {
    const imageArea = document.getElementById('imageArea');
    const preview = imageArea.querySelector('.preview-image');
    const placeholder = document.getElementById('imagePlaceholder');

    if (preview) {
        preview.remove();
    }
    document.getElementById('image').value = '';
    placeholder.style.display = 'flex';
}

// 폼 제출
document.getElementById('slideForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const slideTitle = document.getElementById('slideTitle').value.trim();
    const imageFile = document.getElementById('image').files[0];
    const hasExistingImage = document.querySelector('.preview-image img') !== null;

    if (!slideTitle) {
        showAlert('슬라이드 제목을 입력하세요', 'error');
        return;
    }

    // 신규 등록 시 이미지 필수
    if (!isEdit && !imageFile) {
        showAlert('이미지를 선택하세요', 'error');
        return;
    }

    const formData = new FormData();
    formData.append('slideTitle', slideTitle);
    formData.append('slideDescription', document.getElementById('slideDescription').value || '');
    formData.append('linkUrl', document.getElementById('linkUrl').value || '');
    formData.append('slideOrder', document.getElementById('slideOrder').value || 0);

    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    if (startDate) formData.append('startDate', startDate);
    if (endDate) formData.append('endDate', endDate);

    // 이미지
    if (imageFile) {
        formData.append('image', imageFile);
    }

    const slideId = document.getElementById('slideId')?.value;
    const endpoint = isEdit ? `/api/admin/slide/update/${slideId}` : '/api/admin/slide/create';

    fetch(contextPath + endpoint, {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert(data.message, 'success');
            setTimeout(() => {
                window.location.href = contextPath + '/admin/slide';
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