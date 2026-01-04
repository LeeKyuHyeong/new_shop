// 리뷰 관련 JavaScript

let currentPage = 0;
let hasMoreReviews = true;
let isLoading = false;
let editingReviewId = null;
let selectedImages = [];

// 페이지 로드 시 리뷰 불러오기
document.addEventListener('DOMContentLoaded', function() {

    const reviewContent = document.querySelector('textarea[name="content"]');
    if (reviewContent) {
        ProfanityFilter.attachValidator(reviewContent, {
            errorMessage: '리뷰에 부적절한 표현이 포함되어 있습니다.'
        });
    }

    loadReviews();
});

// 리뷰 목록 불러오기
function loadReviews(append = false) {
    if (isLoading || (!append && !hasMoreReviews)) return;

    isLoading = true;
    const page = append ? currentPage + 1 : 0;

    fetch(`${contextPath}/api/review/product/${productId}?page=${page}&size=10`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                if (!append) {
                    currentPage = 0;
                    renderReviewStats(data.stats);
                    document.getElementById('reviewList').innerHTML = '';
                } else {
                    currentPage = page;
                }

                renderReviews(data.reviews);
                hasMoreReviews = data.hasNext;

                // 더보기 버튼 표시/숨김
                const loadMoreBtn = document.getElementById('loadMoreBtn');
                if (loadMoreBtn) {
                    loadMoreBtn.style.display = hasMoreReviews ? 'inline-block' : 'none';
                }

                // 리뷰 작성 가능 여부
                updateWriteButton(data.canWrite);

                // 리뷰 개수 업데이트
                document.getElementById('reviewCount').textContent = data.totalElements;
            }
        })
        .finally(() => {
            isLoading = false;
        });
}

// 리뷰 통계 렌더링
function renderReviewStats(stats) {
    const statsContainer = document.getElementById('reviewStats');
    if (!statsContainer) return;

    const avgRating = stats.averageRating || 0;
    const totalCount = stats.totalCount || 0;
    const distribution = stats.ratingDistribution || {};

    // 별점 별 문자열 생성
    let starsHtml = '';
    for (let i = 1; i <= 5; i++) {
        starsHtml += i <= Math.round(avgRating) ? '★' : '☆';
    }

    // 분포 바 생성
    let barsHtml = '';
    for (let i = 5; i >= 1; i--) {
        const count = distribution[i] || 0;
        const percent = totalCount > 0 ? (count / totalCount * 100) : 0;
        barsHtml += `
            <div class="rating-bar">
                <span class="rating-label">${i}점</span>
                <div class="rating-progress">
                    <div class="rating-progress-fill" style="width: ${percent}%"></div>
                </div>
                <span class="rating-count">${count}</span>
            </div>
        `;
    }

    statsContainer.innerHTML = `
        <div class="stats-summary">
            <div class="average-rating">${avgRating.toFixed(1)}</div>
            <div class="average-stars">${starsHtml}</div>
            <div class="total-reviews">${totalCount}개의 리뷰</div>
        </div>
        <div class="stats-distribution">
            ${barsHtml}
        </div>
    `;
}

// 리뷰 목록 렌더링
function renderReviews(reviews) {
    const listContainer = document.getElementById('reviewList');

    if (reviews.length === 0 && currentPage === 0) {
        listContainer.innerHTML = `
            <div class="empty-reviews">
                <div class="icon">📝</div>
                <p>아직 리뷰가 없습니다.<br>첫 번째 리뷰를 작성해보세요!</p>
            </div>
        `;
        return;
    }

    reviews.forEach(review => {
        const reviewHtml = createReviewHtml(review);
        listContainer.insertAdjacentHTML('beforeend', reviewHtml);
    });
}

// 리뷰 HTML 생성
function createReviewHtml(review) {
    // 이미지 HTML
    let imagesHtml = '';
    if (review.images && review.images.length > 0) {
        imagesHtml = '<div class="review-images">';
        review.images.forEach(img => {
            imagesHtml += `
                <div class="review-image" onclick="openImageModal('${contextPath}${img.imageUrl}')">
                    <img src="${contextPath}${img.imageUrl}" alt="리뷰 이미지">
                </div>
            `;
        });
        imagesHtml += '</div>';
    }

    // 관리자 답변 HTML
    let replyHtml = '';
    if (review.adminReply) {
        const replyDate = review.adminReplyDate ? formatDate(review.adminReplyDate) : '';
        replyHtml = `
            <div class="admin-reply">
                <div class="admin-reply-header">
                    <span class="admin-badge">판매자</span>
                    <span class="admin-reply-date">${replyDate}</span>
                </div>
                <div class="admin-reply-content">${escapeHtml(review.adminReply)}</div>
            </div>
        `;
    }

    // 수정/삭제 버튼 (본인 리뷰인 경우)
    let actionsHtml = '';
    if (review.isOwner) {
        actionsHtml = `
            <div class="review-actions">
                <button onclick="editReview(${review.reviewId})">수정</button>
                <button class="btn-delete" onclick="deleteReview(${review.reviewId})">삭제</button>
            </div>
        `;
    }

    const initial = review.maskedUserName ? review.maskedUserName.charAt(0) : '?';
    const createdDate = formatDate(review.createdDate);

    return `
        <div class="review-item" id="review-${review.reviewId}">
            <div class="review-header">
                <div class="review-user">
                    <div class="user-avatar">${initial}</div>
                    <div class="user-info">
                        <div class="user-name">${escapeHtml(review.maskedUserName)}</div>
                        <div class="review-date">${createdDate}</div>
                    </div>
                </div>
                <div class="review-rating">${review.ratingStars}</div>
            </div>
            <div class="review-content">${escapeHtml(review.content || '')}</div>
            ${imagesHtml}
            ${replyHtml}
            ${actionsHtml}
        </div>
    `;
}

// 리뷰 작성 버튼 상태 업데이트
function updateWriteButton(canWrite) {
    const writeBtn = document.getElementById('btnWriteReview');
    if (writeBtn) {
        writeBtn.disabled = !canWrite;
        if (!canWrite) {
            writeBtn.title = '이미 리뷰를 작성하셨습니다.';
        }
    }
}

// 리뷰 작성 폼 토글
function toggleReviewForm() {
    const form = document.getElementById('reviewForm');
    form.classList.toggle('active');

    if (form.classList.contains('active')) {
        editingReviewId = null;
        resetReviewForm();
        document.getElementById('reviewFormTitle').textContent = '리뷰 작성';
    }
}

// 리뷰 폼 초기화
function resetReviewForm() {
    document.getElementById('reviewRating').value = '';
    document.querySelectorAll('.star-rating input').forEach(input => input.checked = false);
    document.getElementById('reviewContent').value = '';
    document.getElementById('imagePreview').innerHTML = '';
    document.getElementById('reviewImages').value = '';
    selectedImages = [];
}

// 이미지 선택 처리
function handleImageSelect(input) {
    const files = input.files;
    const previewContainer = document.getElementById('imagePreview');

    for (let i = 0; i < files.length && selectedImages.length < 5; i++) {
        const file = files[i];

        // 이미지 타입 체크
        if (!file.type.startsWith('image/')) continue;

        // 파일 크기 체크 (5MB)
        if (file.size > 5 * 1024 * 1024) {
            alert('이미지 크기는 5MB 이하만 가능합니다.');
            continue;
        }

        selectedImages.push(file);

        // 미리보기 생성
        const reader = new FileReader();
        reader.onload = function(e) {
            const previewItem = document.createElement('div');
            previewItem.className = 'preview-item';
            previewItem.innerHTML = `
                <img src="${e.target.result}" alt="미리보기">
                <button type="button" class="preview-remove" onclick="removePreviewImage(this, ${selectedImages.length - 1})">×</button>
            `;
            previewContainer.appendChild(previewItem);
        };
        reader.readAsDataURL(file);
    }

    // input 초기화 (같은 파일 다시 선택 가능하도록)
    input.value = '';
}

// 미리보기 이미지 제거
function removePreviewImage(button, index) {
    button.parentElement.remove();
    selectedImages.splice(index, 1);
}

// 리뷰 제출
async function submitReview() {
    const rating = document.querySelector('.star-rating input:checked');
    const content = document.getElementById('reviewContent').value.trim();
    const form = document.getElementById('reviewForm');

    if (!rating) {
        alert('별점을 선택해주세요.');
        return;
    }

    const isValid = await ProfanityFilter.validateForm(form, ['content', 'title']);

    //비속어 필터
    if (!isValid) {
        return false;
    }

    const formData = new FormData();
    formData.append('productId', productId);
    formData.append('rating', rating.value);
    formData.append('content', content);

    // 이미지 추가
    selectedImages.forEach(file => {
        formData.append('images', file);
    });

    const url = editingReviewId
        ? `${contextPath}/api/review/update/${editingReviewId}`
        : `${contextPath}/api/review/create`;

    fetch(url, {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.needLogin) {
            if (confirm('로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?')) {
                location.href = `${contextPath}/login?redirect=/product/${productId}`;
            }
            return;
        }

        if (data.success) {
            alert(data.message);
            toggleReviewForm();
            loadReviews(); // 리뷰 새로고침
        } else {
            alert(data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('오류가 발생했습니다.');
    });
}

// 리뷰 수정
function editReview(reviewId) {
    fetch(`${contextPath}/api/review/${reviewId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const review = data.review;

                // 폼 열기
                const form = document.getElementById('reviewForm');
                form.classList.add('active');

                // 데이터 채우기
                editingReviewId = reviewId;
                document.getElementById('reviewFormTitle').textContent = '리뷰 수정';
                document.getElementById('reviewContent').value = review.content || '';

                // 별점 선택
                const ratingInput = document.querySelector(`.star-rating input[value="${review.rating}"]`);
                if (ratingInput) ratingInput.checked = true;

                // 기존 이미지는 표시하지 않음 (새로 추가만 가능)
                selectedImages = [];
                document.getElementById('imagePreview').innerHTML = '';

                // 스크롤
                form.scrollIntoView({ behavior: 'smooth' });
            }
        });
}

// 리뷰 삭제
function deleteReview(reviewId) {
    if (!confirm('리뷰를 삭제하시겠습니까?')) return;

    fetch(`${contextPath}/api/review/delete/${reviewId}`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message);
            loadReviews(); // 리뷰 새로고침
        } else {
            alert(data.message);
        }
    });
}

// 이미지 모달 열기
function openImageModal(src) {
    const modal = document.getElementById('imageModal');
    const img = modal.querySelector('img');
    img.src = src;
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
}

// 이미지 모달 닫기
function closeImageModal() {
    const modal = document.getElementById('imageModal');
    modal.classList.remove('active');
    document.body.style.overflow = '';
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeImageModal();
    }
});

// 더보기
function loadMoreReviews() {
    loadReviews(true);
}

// 날짜 포맷
function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}.${month}.${day}`;
}

// HTML 이스케이프
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}