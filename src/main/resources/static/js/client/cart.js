// 장바구니 페이지 JavaScript

function toggleAll() {
    const selectAll = document.getElementById('selectAll');
    document.querySelectorAll('.cart-checkbox').forEach(cb => {
        cb.checked = selectAll.checked;
    });
    calculateTotal();
}

document.querySelectorAll('.cart-checkbox').forEach(cb => {
    cb.addEventListener('change', calculateTotal);
});

function calculateTotal() {
    let total = 0;
    document.querySelectorAll('.cart-item').forEach(item => {
        const checkbox = item.querySelector('.cart-checkbox');
        if (checkbox.checked) {
            const price = parseInt(item.dataset.price);
            const qty = parseInt(item.querySelector('.qty-input').value);
            total += price * qty;
        }
    });

    const deliveryFee = total >= 50000 ? 0 : (total > 0 ? 3000 : 0);
    const finalPrice = total + deliveryFee;

    document.getElementById('totalPrice').textContent = total.toLocaleString() + '원';
    document.getElementById('deliveryFee').textContent = deliveryFee === 0 ? '무료' : deliveryFee.toLocaleString() + '원';
    document.getElementById('finalPrice').textContent = finalPrice.toLocaleString() + '원';

    // 선택된 상품이 없으면 주문 버튼 비활성화
    const checkedCount = document.querySelectorAll('.cart-checkbox:checked').length;
    document.getElementById('orderBtn').disabled = checkedCount === 0;
}

function updateQuantity(cartId, quantity) {
    if (quantity < 1) return;

    fetch(contextPath + '/cart/update', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'cartId=' + cartId + '&quantity=' + quantity
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            const item = document.querySelector('[data-cart-id="' + cartId + '"]');
            item.querySelector('.qty-input').value = quantity;
            item.querySelector('.item-total[data-cart-id="' + cartId + '"]').textContent =
                data.itemTotal.toLocaleString() + '원';
            calculateTotal();
        } else {
            alert(data.message);
        }
    });
}

function removeItem(cartId) {
    if (!confirm('삭제하시겠습니까?')) return;

    fetch(contextPath + '/cart/' + cartId, {
        method: 'DELETE'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            location.reload();
        } else {
            alert(data.message);
        }
    });
}

// 폼 제출 전 선택 확인
document.getElementById('cartForm')?.addEventListener('submit', function(e) {
    const checked = document.querySelectorAll('.cart-checkbox:checked');
    if (checked.length === 0) {
        e.preventDefault();
        alert('주문할 상품을 선택해주세요.');
    }
});