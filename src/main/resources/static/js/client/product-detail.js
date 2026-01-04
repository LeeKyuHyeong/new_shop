// 상품 상세 페이지 JavaScript

// 이미지 변경
function changeImage(src, element) {
    document.getElementById('mainImg').src = src;
    document.querySelectorAll('.thumb-item').forEach(item => item.classList.remove('active'));
    element.classList.add('active');
}

// 옵션 선택
function selectOption(type, value, element) {
    // 같은 타입의 다른 버튼들 비활성화
    const container = element.parentElement;
    container.querySelectorAll('.option-btn').forEach(btn => btn.classList.remove('selected'));

    // 현재 버튼 활성화
    element.classList.add('selected');

    // hidden input 업데이트
    if (type === 'color') {
        document.getElementById('selectedColor').value = value;
    } else if (type === 'size') {
        document.getElementById('selectedSize').value = value;
    }
}

// 옵션 검증
function validateOptions() {
    // 색상 옵션이 있는데 선택 안 한 경우
    if (hasColorOption) {
        const selectedColor = document.getElementById('selectedColor')?.value;
        if (!selectedColor) {
            alert('색상을 선택해주세요.');
            return false;
        }
    }

    // 사이즈 옵션이 있는데 선택 안 한 경우
    if (hasSizeOption) {
        const selectedSize = document.getElementById('selectedSize')?.value;
        if (!selectedSize) {
            alert('사이즈를 선택해주세요.');
            return false;
        }
    }

    return true;
}

// 선택한 옵션 값 가져오기
function getSelectedOptions() {
    const color = document.getElementById('selectedColor')?.value || '';
    const size = document.getElementById('selectedSize')?.value || '';
    return { color, size };
}

// 수량 변경
function changeQty(delta) {
    const qtyInput = document.getElementById('quantity');
    let qty = parseInt(qtyInput.value) + delta;
    if (qty < 1) qty = 1;
    if (qty > maxStock) qty = maxStock;
    qtyInput.value = qty;
    updateTotalPrice();
}

// 총 금액 업데이트
function updateTotalPrice() {
    const qty = parseInt(document.getElementById('quantity').value);
    const total = productPrice * qty;
    document.getElementById('totalPriceDisplay').textContent = total.toLocaleString();
}

// 수량 입력 이벤트
document.getElementById('quantity')?.addEventListener('change', function() {
    let qty = parseInt(this.value);
    if (isNaN(qty) || qty < 1) qty = 1;
    if (qty > maxStock) qty = maxStock;
    this.value = qty;
    updateTotalPrice();
});

// 장바구니 추가
function addToCart() {
    // 옵션 검증
    if (!validateOptions()) {
        return;
    }

    const qty = parseInt(document.getElementById('quantity')?.value || 1);
    const options = getSelectedOptions();

    const params = new URLSearchParams();
    params.append('productId', productId);
    params.append('quantity', qty);
    if (options.color) params.append('color', options.color);
    if (options.size) params.append('size', options.size);

    fetch(contextPath + '/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: params.toString()
    })
    .then(response => response.json())
    .then(data => {
        if (data.needLogin) {
            if (confirm('로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?')) {
                location.href = contextPath + '/login?redirect=/product/' + productId;
            }
        } else if (data.success) {
            // 장바구니 카운트 업데이트
            const countEl = document.getElementById('cartCount');
            if (countEl) {
                countEl.textContent = data.cartCount;
                countEl.style.display = 'inline-flex';
            }

            if (confirm('장바구니에 추가되었습니다.\n장바구니로 이동하시겠습니까?')) {
                location.href = contextPath + '/cart';
            }
        } else {
            alert(data.message);
        }
    })
    .catch(error => {
        alert('오류가 발생했습니다.');
    });
}

// 바로구매
function buyNow() {
    // 옵션 검증
    if (!validateOptions()) {
        return;
    }

    const qty = parseInt(document.getElementById('quantity')?.value || 1);
    const options = getSelectedOptions();

    let url = contextPath + '/order/direct?productId=' + productId + '&quantity=' + qty;
    if (options.color) url += '&color=' + encodeURIComponent(options.color);
    if (options.size) url += '&size=' + encodeURIComponent(options.size);

    location.href = url;
}