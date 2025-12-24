function deleteProduct(productId) {
    if (!confirm('정말 삭제하시겠습니까?')) {
        return;
    }

    fetch(contextPath + '/api/admin/product/delete/' + productId, {
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
        showAlert('요청 중 오류가 발생했습니다', 'error');
    });
}

function showAlert(message, type) {
    const alertContainer = document.querySelector('.alert-container');
    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.textContent = message;
    alertContainer.innerHTML = '';
    alertContainer.appendChild(alert);
}