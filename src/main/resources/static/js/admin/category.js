function deleteCategory(categoryId) {
    if (confirm('정말 삭제하시겠습니까?')) {
        fetch(contextPath + '/api/admin/category/delete/' + categoryId, {
            method: 'POST'
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAlert(data.message, 'success');
                setTimeout(() => {
                    location.reload();
                }, 500);
            } else {
                showAlert(data.message, 'error');
            }
        })
        .catch(error => {
            showAlert('삭제 중 오류가 발생했습니다', 'error');
        });
    }
}

function showAlert(message, type) {
    const alertContainer = document.querySelector('.alert-container');
    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.textContent = message;
    alertContainer.innerHTML = '';
    alertContainer.appendChild(alert);

    if (type === 'success') {
        setTimeout(() => {
            alert.remove();
        }, 500);
    }
}