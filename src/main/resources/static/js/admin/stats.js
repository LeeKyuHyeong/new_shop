const contextPath = window.contextPath || '';

// 페이지 로드 시 오늘 날짜로 초기화
document.addEventListener('DOMContentLoaded', function() {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);

    document.getElementById('startDate').valueAsDate = firstDay;
    document.getElementById('endDate').valueAsDate = today;
});

// 모든 통계 조회
function loadAllStats() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!startDate || !endDate) {
        alert('시작일과 종료일을 선택해주세요.');
        return;
    }

    if (new Date(startDate) > new Date(endDate)) {
        alert('시작일은 종료일보다 이전이어야 합니다.');
        return;
    }

    loadCategoryStats(startDate, endDate);
    loadColorStats(startDate, endDate);
    loadSizeStats(startDate, endDate);
}

// 카테고리별 통계 조회
function loadCategoryStats(startDate, endDate) {
    fetch(`${contextPath}/admin/stats/api/category?startDate=${startDate}&endDate=${endDate}`)
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                renderCategoryStats(result.data);
            } else {
                alert('통계 조회 중 오류가 발생했습니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('통계 조회 중 오류가 발생했습니다.');
        });
}

// 색상별 통계 조회
function loadColorStats(startDate, endDate) {
    fetch(`${contextPath}/admin/stats/api/color?startDate=${startDate}&endDate=${endDate}`)
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                renderColorStats(result.data);
            } else {
                alert('통계 조회 중 오류가 발생했습니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('통계 조회 중 오류가 발생했습니다.');
        });
}

// 사이즈별 통계 조회
function loadSizeStats(startDate, endDate) {
    fetch(`${contextPath}/admin/stats/api/size?startDate=${startDate}&endDate=${endDate}`)
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                renderSizeStats(result.data);
            } else {
                alert('통계 조회 중 오류가 발생했습니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('통계 조회 중 오류가 발생했습니다.');
        });
}

// 카테고리 통계 렌더링
function renderCategoryStats(data) {
    const tbody = document.getElementById('categoryStatsBody');
    const footer = document.getElementById('categoryStatsFooter');

    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center">조회된 데이터가 없습니다.</td></tr>';
        footer.style.display = 'none';
        return;
    }

    let totalQty = 0;
    let totalAmount = 0;

    tbody.innerHTML = data.map(item => {
        totalQty += item.totalQuantity;
        totalAmount += item.totalAmount;

        return `
            <tr>
                <td>${item.categoryName}</td>
                <td class="text-right">${item.totalQuantity.toLocaleString()}개</td>
                <td class="text-right">${item.totalAmount.toLocaleString()}원</td>
            </tr>
        `;
    }).join('');

    document.getElementById('categoryTotalQty').textContent = totalQty.toLocaleString() + '개';
    document.getElementById('categoryTotalAmount').textContent = totalAmount.toLocaleString() + '원';
    footer.style.display = 'table-footer-group';
}

// 색상 통계 렌더링
function renderColorStats(data) {
    const tbody = document.getElementById('colorStatsBody');
    const footer = document.getElementById('colorStatsFooter');

    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center">조회된 데이터가 없습니다.</td></tr>';
        footer.style.display = 'none';
        return;
    }

    let totalQty = 0;
    let totalAmount = 0;

    tbody.innerHTML = data.map(item => {
        totalQty += item.totalQuantity;
        totalAmount += item.totalAmount;

        return `
            <tr>
                <td>${item.color}</td>
                <td class="text-right">${item.totalQuantity.toLocaleString()}개</td>
                <td class="text-right">${item.totalAmount.toLocaleString()}원</td>
            </tr>
        `;
    }).join('');

    document.getElementById('colorTotalQty').textContent = totalQty.toLocaleString() + '개';
    document.getElementById('colorTotalAmount').textContent = totalAmount.toLocaleString() + '원';
    footer.style.display = 'table-footer-group';
}

// 사이즈 통계 렌더링
function renderSizeStats(data) {
    const tbody = document.getElementById('sizeStatsBody');
    const footer = document.getElementById('sizeStatsFooter');

    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-center">조회된 데이터가 없습니다.</td></tr>';
        footer.style.display = 'none';
        return;
    }

    let totalQty = 0;
    let totalAmount = 0;

    tbody.innerHTML = data.map(item => {
        totalQty += item.totalQuantity;
        totalAmount += item.totalAmount;

        return `
            <tr>
                <td>${item.size}</td>
                <td class="text-right">${item.totalQuantity.toLocaleString()}개</td>
                <td class="text-right">${item.totalAmount.toLocaleString()}원</td>
            </tr>
        `;
    }).join('');

    document.getElementById('sizeTotalQty').textContent = totalQty.toLocaleString() + '개';
    document.getElementById('sizeTotalAmount').textContent = totalAmount.toLocaleString() + '원';
    footer.style.display = 'table-footer-group';
}