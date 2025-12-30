// 이미지 업로드 영역 클릭
document.getElementById('imagePlaceholder').addEventListener('click', function() {
    document.getElementById('popupImage').click();
});

// 이미지 변경 미리보기
document.getElementById('popupImage').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const imageArea = document.getElementById('imageArea');
            const placeholder = document.getElementById('imagePlaceholder');

            const existingPreview = imageArea.querySelector('.preview-image');
            if (existingPreview) {
                existingPreview.remove();
            }

            const previewDiv = document.createElement('div');
            previewDiv.className = 'preview-image';
            previewDiv.innerHTML = `
                <img src="${e.target.result}" alt="팝업이미지">
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
    document.getElementById('popupImage').value = '';
    placeholder.style.display = 'flex';
}

// 폼 제출
document.getElementById('popupForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const popupTitle = document.getElementById('popupTitle').value.trim();

    if (!popupTitle) {
        showAlert('팝업 제목을 입력하세요', 'error');
        return;
    }

    const formData = new FormData();
    formData.append('popupTitle', popupTitle);

    // Summernote 에디터에서 내용 가져오기
    let content = '';
    if (typeof $ !== 'undefined' && $('#popupContent').summernote) {
        content = $('#popupContent').summernote('code');
    }
    formData.append('popupContent', content);

    formData.append('popupLink', document.getElementById('popupLink').value.trim() || '');
    formData.append('popupWidth', document.getElementById('popupWidth').value || 400);
    formData.append('popupHeight', document.getElementById('popupHeight').value || 500);
    formData.append('popupTop', document.getElementById('popupTop').value || 100);
    formData.append('popupLeft', document.getElementById('popupLeft').value || 100);
    formData.append('popupOrder', document.getElementById('popupOrder').value || 0);

    const imageFile = document.getElementById('popupImage').files[0];
    if (imageFile) {
        formData.append('popupImage', imageFile);
    }

    const popupId = document.getElementById('popupId')?.value;
    const endpoint = isEdit ? `/api/admin/popup/update/${popupId}` : '/api/admin/popup/create';

    fetch(contextPath + endpoint, {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert(data.message, 'success');
            setTimeout(() => {
                window.location.href = contextPath + '/admin/popup';
            }, 500);
        } else {
            showAlert(data.message, 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
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