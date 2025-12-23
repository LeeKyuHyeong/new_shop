document.getElementById('categoryForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const categoryName = document.getElementById('categoryName').value.trim();
    const categoryDescription = document.getElementById('categoryDescription').value.trim();
    const categoryOrder = document.getElementById('categoryOrder').value || 0;
    const parentId = document.getElementById('parentId').value;

    if (!categoryName) {
        showAlert('카테고리명을 입력하세요', 'error');
        return;
    }

    const categoryId = document.getElementById('categoryId')?.value;
    const endpoint = isEdit ? `/api/admin/category/update/${categoryId}` : '/api/admin/category/create';

    const params = {
        categoryName: categoryName,
        categoryDescription: categoryDescription,
        categoryOrder: categoryOrder
    };

    // parentId가 있을 때만 추가
    if (parentId) {
        params.parentId = parentId;
    }

    fetch(contextPath + endpoint, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams(params)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert(data.message, 'success');
            setTimeout(() => {
                window.location.href = contextPath + '/admin/category';
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