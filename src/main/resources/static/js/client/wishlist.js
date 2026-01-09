// 위시리스트 페이지 JS

document.addEventListener('DOMContentLoaded', function() {
    loadWishlist();
});

/**
 * 위시리스트 목록 로드
 */
async function loadWishlist() {
    const loading = document.getElementById('loading');
    const emptyWishlist = document.getElementById('emptyWishlist');
    const wishlistContent = document.getElementById('wishlistContent');
    const wishlistGrid = document.getElementById('wishlistGrid');
    const totalCount = document.getElementById('totalCount');

    try {
        const response = await fetch(contextPath + '/api/wishlist');
        const data = await response.json();

        loading.style.display = 'none';

        if (!data.success || data.items.length === 0) {
            emptyWishlist.style.display = 'block';
            wishlistContent.style.display = 'none';
            return;
        }

        emptyWishlist.style.display = 'none';
        wishlistContent.style.display = 'block';
        totalCount.textContent = data.items.length;

        wishlistGrid.innerHTML = data.items.map(item => renderWishlistItem(item)).join('');

    } catch (error) {
        console.error('위시리스트 로드 오류:', error);
        loading.style.display = 'none';
        emptyWishlist.style.display = 'block';
    }
}

/**
 * 위시리스트 아이템 렌더링
 */
function renderWishlistItem(item) {
    const isSoldOut = item.productStock === 0;
    const hasDiscount = item.productDiscount > 0;

    return `
        <div class="wishlist-item" data-wishlist-id="${item.wishlistId}" data-product-id="${item.productId}">
            <button class="btn-remove" onclick="removeFromWishlist(${item.wishlistId})" title="삭제">
                ✕
            </button>
            <a href="${contextPath}/product/${item.productId}">
                <div class="item-image ${!item.thumbnailUrl ? 'no-image' : ''}">
                    ${item.thumbnailUrl
                        ? `<img src="${contextPath}${item.thumbnailUrl}" alt="${item.productName}">`
                        : '이미지 없음'
                    }
                    ${isSoldOut ? '<div class="sold-out-overlay">품절</div>' : ''}
                </div>
            </a>
            <div class="item-info">
                ${item.categoryName ? `<div class="item-category">${item.categoryName}</div>` : ''}
                <div class="item-name">
                    <a href="${contextPath}/product/${item.productId}">${item.productName}</a>
                </div>
                <div class="item-price">
                    ${hasDiscount ? `
                        <span class="original-price">${formatPrice(item.productPrice)}원</span>
                        <span class="discount-badge">${item.productDiscount}%</span>
                    ` : ''}
                    <span class="final-price">${formatPrice(item.discountedPrice)}원</span>
                </div>
                <div class="item-actions">
                    <button class="btn-cart" onclick="addToCartFromWishlist(${item.productId})" ${isSoldOut ? 'disabled' : ''}>
                        🛒 장바구니
                    </button>
                    <button class="btn-buy" onclick="buyNowFromWishlist(${item.productId})" ${isSoldOut ? 'disabled' : ''}>
                        바로구매
                    </button>
                </div>
                <div class="item-date">
                    ${formatDate(item.createdDate)}에 추가됨
                </div>
            </div>
        </div>
    `;
}

/**
 * 위시리스트에서 제거
 */
async function removeFromWishlist(wishlistId) {
    if (!confirm('위시리스트에서 삭제하시겠습니까?')) {
        return;
    }

    try {
        const response = await fetch(contextPath + '/api/wishlist/' + wishlistId, {
            method: 'DELETE'
        });
        const data = await response.json();

        if (data.success) {
            // 아이템 제거 애니메이션
            const item = document.querySelector(`[data-wishlist-id="${wishlistId}"]`);
            if (item) {
                item.style.opacity = '0';
                item.style.transform = 'scale(0.8)';
                setTimeout(() => {
                    item.remove();
                    updateCount();
                }, 300);
            }

            // 헤더 카운트 업데이트
            updateWishlistCount(data.wishlistCount);
        } else {
            alert(data.message || '삭제에 실패했습니다.');
        }
    } catch (error) {
        console.error('삭제 오류:', error);
        alert('삭제 중 오류가 발생했습니다.');
    }
}

/**
 * 총 개수 업데이트
 */
function updateCount() {
    const items = document.querySelectorAll('.wishlist-item');
    const totalCount = document.getElementById('totalCount');
    const emptyWishlist = document.getElementById('emptyWishlist');
    const wishlistContent = document.getElementById('wishlistContent');

    totalCount.textContent = items.length;

    if (items.length === 0) {
        emptyWishlist.style.display = 'block';
        wishlistContent.style.display = 'none';
    }
}

/**
 * 장바구니에 추가
 */
async function addToCartFromWishlist(productId) {
    try {
        const response = await fetch(contextPath + '/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                productId: productId,
                quantity: 1
            })
        });
        const data = await response.json();

        if (data.success) {
            alert('장바구니에 추가되었습니다.');
            // 헤더 장바구니 카운트 업데이트
            if (typeof loadCartCount === 'function') {
                loadCartCount();
            }
        } else if (data.requireLogin) {
            if (confirm('로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?')) {
                location.href = contextPath + '/login?redirect=/wishlist';
            }
        } else {
            alert(data.message || '장바구니 추가에 실패했습니다.');
        }
    } catch (error) {
        console.error('장바구니 추가 오류:', error);
        alert('장바구니 추가 중 오류가 발생했습니다.');
    }
}

/**
 * 바로구매
 */
function buyNowFromWishlist(productId) {
    // 상품 상세 페이지로 이동 (옵션 선택이 필요할 수 있음)
    location.href = contextPath + '/product/' + productId;
}

/**
 * 헤더 위시리스트 카운트 업데이트
 */
function updateWishlistCount(count) {
    const countEl = document.getElementById('wishlistCount');
    if (countEl) {
        if (count > 0) {
            countEl.textContent = count;
            countEl.style.display = 'inline-flex';
        } else {
            countEl.style.display = 'none';
        }
    }
}

/**
 * 가격 포맷팅
 */
function formatPrice(price) {
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

/**
 * 날짜 포맷팅
 */
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}.${month}.${day}`;
}