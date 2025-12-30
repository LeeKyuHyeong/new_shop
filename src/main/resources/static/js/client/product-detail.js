// 상품 상세 페이지 JavaScript

function changeImage(src, element) {
    document.getElementById('mainImg').src = src;
    document.querySelectorAll('.thumb-item').forEach(item => item.classList.remove('active'));
    element.classList.add('active');
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
    const qty = parseInt(document.getElementById('quantity')?.value || 1);

    fetch(contextPath + '/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'productId=' + productId + '&quantity=' + qty
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
    const qty = parseInt(document.getElementById('quantity')?.value || 1);
    location.href = contextPath + '/order/direct?productId=' + productId + '&quantity=' + qty;
}