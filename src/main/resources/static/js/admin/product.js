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

// 상위 카테고리 변경 시 하위 카테고리 필터링
function onParentCategoryChange() {
    const parentSelect = document.getElementById('parentCategoryId');
    const childSelect = document.getElementById('categoryId');
    const selectedParentId = parentSelect.value;

    // 모든 하위 카테고리 옵션
    const options = childSelect.querySelectorAll('option');

    options.forEach(option => {
        if (option.value === '') {
            // '전체' 옵션은 항상 표시
            option.style.display = '';
        } else if (!selectedParentId) {
            // 상위 카테고리가 '전체'이면 모든 하위 카테고리 표시
            option.style.display = '';
        } else if (option.dataset.parent === selectedParentId) {
            // 선택한 상위 카테고리의 하위만 표시
            option.style.display = '';
        } else {
            option.style.display = 'none';
        }
    });

    // 하위 카테고리 선택 초기화
    childSelect.value = '';
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 초기 필터링 적용
    const parentSelect = document.getElementById('parentCategoryId');
    if (parentSelect) {
        onParentCategoryChange();
    }
});

// 검색 초기화
function resetSearch() {
    window.location.href = contextPath + '/admin/product';
}

// 페이지 사이즈 변경
function changePageSize(size) {
    const form = document.getElementById('searchForm');
    const sizeInput = form.querySelector('input[name="size"]');
    sizeInput.value = size;
    form.submit();
}

// 정렬 변경
function changeSort(value) {
    const [field, direction] = value.split(',');
    const form = document.getElementById('searchForm');

    // hidden input 추가 또는 업데이트
    let sortFieldInput = form.querySelector('input[name="sortField"]');
    let sortDirectionInput = form.querySelector('input[name="sortDirection"]');

    if (!sortFieldInput) {
        sortFieldInput = document.createElement('input');
        sortFieldInput.type = 'hidden';
        sortFieldInput.name = 'sortField';
        form.appendChild(sortFieldInput);
    }
    if (!sortDirectionInput) {
        sortDirectionInput = document.createElement('input');
        sortDirectionInput.type = 'hidden';
        sortDirectionInput.name = 'sortDirection';
        form.appendChild(sortDirectionInput);
    }

    sortFieldInput.value = field;
    sortDirectionInput.value = direction;
    form.submit();
}