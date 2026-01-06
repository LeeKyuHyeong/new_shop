/**
 * 공통 페이징 모듈
 * Admin/Client 양쪽에서 사용 가능한 범용 페이징 기능
 */

(function() {
    'use strict';

    // DOM 로드 완료 시 초기화
    document.addEventListener('DOMContentLoaded', function() {
        initPagination();
    });

    /**
     * 페이징 초기화
     */
    function initPagination() {
        bindPageLinkEvents();
        bindPageJumpEvents();
    }

    /**
     * 페이지 링크 클릭 이벤트 바인딩
     */
    function bindPageLinkEvents() {
        document.addEventListener('click', function(e) {
            const pageLink = e.target.closest('.page-link');
            if (pageLink) {
                e.preventDefault();

                const pageItem = pageLink.closest('.page-item');
                if (pageItem && pageItem.classList.contains('disabled')) {
                    return;
                }

                const page = pageLink.dataset.page;
                if (page) {
                    goToPage(parseInt(page));
                }
            }
        });
    }

    /**
     * 페이지 직접 이동 이벤트 바인딩
     */
    function bindPageJumpEvents() {
        // Enter 키 이벤트
        document.addEventListener('keypress', function(e) {
            if (e.target.classList.contains('page-jump-input') && e.key === 'Enter') {
                e.preventDefault();
                jumpToPage(e.target);
            }
        });

        // 이동 버튼 클릭
        document.addEventListener('click', function(e) {
            if (e.target.classList.contains('btn-page-jump')) {
                const wrapper = e.target.closest('.pagination-wrapper');
                const input = wrapper.querySelector('.page-jump-input');
                if (input) {
                    jumpToPage(input);
                }
            }
        });
    }

    /**
     * 특정 페이지로 이동
     * @param {number} page - 페이지 번호
     */
    function goToPage(page) {
        const form = document.getElementById('paginationForm');
        if (!form) {
            console.error('Pagination form not found');
            return;
        }

        // 페이지 값 설정
        const pageInput = form.querySelector('input[name="page"]');
        if (pageInput) {
            pageInput.value = page;
        }

        // 폼 제출
        form.submit();
    }

    /**
     * 페이지 직접 입력 이동
     * @param {HTMLInputElement} input - 페이지 입력 요소
     */
    function jumpToPage(input) {
        const page = parseInt(input.value);
        const max = parseInt(input.max);
        const min = parseInt(input.min) || 1;

        if (isNaN(page)) {
            alert('페이지 번호를 입력해주세요.');
            input.focus();
            return;
        }

        if (page < min || page > max) {
            alert('페이지는 ' + min + '에서 ' + max + ' 사이의 값이어야 합니다.');
            input.value = '';
            input.focus();
            return;
        }

        goToPage(page);
    }

    // 전역 함수로 노출
    window.goToPage = goToPage;
})();

/**
 * 페이지 사이즈 변경
 * @param {number|string} size - 페이지 사이즈
 */
function changePageSize(size) {
    const form = document.getElementById('paginationForm') || document.getElementById('searchForm');
    if (!form) return;

    let sizeInput = form.querySelector('input[name="size"]');
    if (!sizeInput) {
        sizeInput = document.createElement('input');
        sizeInput.type = 'hidden';
        sizeInput.name = 'size';
        form.appendChild(sizeInput);
    }
    sizeInput.value = size;

    // 페이지 1로 리셋
    const pageInput = form.querySelector('input[name="page"]');
    if (pageInput) {
        pageInput.value = 1;
    }

    form.submit();
}

/**
 * 정렬 변경
 * @param {string} sortValue - 정렬 값 (field,direction 형식)
 */
function changeSort(sortValue) {
    const form = document.getElementById('paginationForm') || document.getElementById('searchForm');
    if (!form) return;

    const [field, direction] = sortValue.split(',');

    // sortField 설정
    let sortFieldInput = form.querySelector('input[name="sortField"]');
    if (!sortFieldInput) {
        sortFieldInput = document.createElement('input');
        sortFieldInput.type = 'hidden';
        sortFieldInput.name = 'sortField';
        form.appendChild(sortFieldInput);
    }
    sortFieldInput.value = field;

    // sortDirection 설정
    let sortDirInput = form.querySelector('input[name="sortDirection"]');
    if (!sortDirInput) {
        sortDirInput = document.createElement('input');
        sortDirInput.type = 'hidden';
        sortDirInput.name = 'sortDirection';
        form.appendChild(sortDirInput);
    }
    sortDirInput.value = direction;

    // 페이지 1로 리셋
    const pageInput = form.querySelector('input[name="page"]');
    if (pageInput) {
        pageInput.value = 1;
    }

    form.submit();
}

/**
 * 검색 초기화
 */
function resetSearch() {
    const form = document.getElementById('searchForm');
    if (!form) {
        window.location.href = window.location.pathname;
        return;
    }

    // 모든 입력 필드 초기화
    form.querySelectorAll('input[type="text"], input[type="number"]').forEach(function(input) {
        input.value = '';
    });

    form.querySelectorAll('select').forEach(function(select) {
        select.selectedIndex = 0;
    });

    // 기본 URL로 이동
    window.location.href = form.action || window.location.pathname;
}