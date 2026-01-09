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

// ==================== 위시리스트 기능 ====================

// 페이지 로드 시 위시리스트 상태 확인
document.addEventListener('DOMContentLoaded', function() {
    checkWishlistStatus();
});

// 위시리스트 상태 확인
async function checkWishlistStatus() {
    try {
        const response = await fetch(contextPath + '/api/wishlist/check/' + productId);
        const data = await response.json();

        updateWishlistButton(data.isWished);
    } catch (error) {
        console.log('위시리스트 상태 확인 실패:', error);
    }
}

// 위시리스트 토글
async function toggleWishlist() {
    try {
        const response = await fetch(contextPath + '/api/wishlist/toggle/' + productId, {
            method: 'POST'
        });
        const data = await response.json();

        if (data.requireLogin) {
            if (confirm('로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?')) {
                location.href = contextPath + '/login?redirect=/product/' + productId;
            }
            return;
        }

        if (data.success) {
            updateWishlistButton(data.isWished);

            // 헤더 위시리스트 카운트 업데이트
            const countEl = document.getElementById('wishlistCount');
            if (countEl) {
                if (data.wishlistCount > 0) {
                    countEl.textContent = data.wishlistCount;
                    countEl.style.display = 'inline-flex';
                } else {
                    countEl.style.display = 'none';
                }
            }

            // 토스트 메시지 (간단한 알림)
            showToast(data.message);
        } else {
            alert(data.message || '처리에 실패했습니다.');
        }
    } catch (error) {
        console.error('위시리스트 토글 오류:', error);
        alert('오류가 발생했습니다.');
    }
}

// 위시리스트 버튼 상태 업데이트
function updateWishlistButton(isWished) {
    const btn = document.getElementById('btnWishlist');
    const icon = document.getElementById('wishlistIcon');
    const text = document.getElementById('wishlistText');

    if (!btn || !icon || !text) return;

    if (isWished) {
        btn.classList.add('active');
        icon.textContent = '❤️';
        text.textContent = '찜취소';
    } else {
        btn.classList.remove('active');
        icon.textContent = '🤍';
        text.textContent = '찜하기';
    }
}

// 토스트 메시지 표시
function showToast(message) {
    // 기존 토스트 제거
    const existingToast = document.querySelector('.toast-message');
    if (existingToast) {
        existingToast.remove();
    }

    // 토스트 생성
    const toast = document.createElement('div');
    toast.className = 'toast-message';
    toast.textContent = message;
    toast.style.cssText = `
        position: fixed;
        bottom: 100px;
        left: 50%;
        transform: translateX(-50%);
        background: rgba(0, 0, 0, 0.8);
        color: white;
        padding: 12px 24px;
        border-radius: 8px;
        font-size: 14px;
        z-index: 10000;
        animation: toastFadeIn 0.3s ease;
    `;

    document.body.appendChild(toast);

    // 3초 후 제거
    setTimeout(() => {
        toast.style.animation = 'toastFadeOut 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 2000);
}

// 토스트 애니메이션 스타일 추가
const toastStyle = document.createElement('style');
toastStyle.textContent = `
    @keyframes toastFadeIn {
        from { opacity: 0; transform: translateX(-50%) translateY(20px); }
        to { opacity: 1; transform: translateX(-50%) translateY(0); }
    }
    @keyframes toastFadeOut {
        from { opacity: 1; transform: translateX(-50%) translateY(0); }
        to { opacity: 0; transform: translateX(-50%) translateY(20px); }
    }
`;
document.head.appendChild(toastStyle);