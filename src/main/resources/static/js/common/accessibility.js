/**
 * 웹접근성 개선 JavaScript
 * - 키보드 네비게이션 지원
 * - 폼 레이블 자동 연결
 * - ARIA 속성 자동 추가
 */
(function() {
    'use strict';

    // DOM 로드 후 실행
    document.addEventListener('DOMContentLoaded', function() {
        enhanceKeyboardNavigation();
        enhanceFormLabels();
        addAriaAttributes();
        createSkipLink();
    });

    /**
     * 키보드 네비게이션 개선
     */
    function enhanceKeyboardNavigation() {
        // Enter 키로 클릭 가능한 요소 활성화
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' || e.key === ' ') {
                const target = e.target;

                // 버튼 역할을 하는 요소
                if (target.matches('[role="button"], .clickable, [onclick]')) {
                    if (e.key === ' ') {
                        e.preventDefault(); // 스페이스바 스크롤 방지
                    }
                    target.click();
                }

                // 드롭다운 토글
                if (target.matches('[data-toggle="dropdown"]')) {
                    e.preventDefault();
                    target.click();
                }
            }

            // Escape 키로 모달/드롭다운 닫기
            if (e.key === 'Escape') {
                // 열린 모달 닫기
                const openModal = document.querySelector('.modal.show, .modal[style*="display: block"]');
                if (openModal) {
                    const closeBtn = openModal.querySelector('.close, [data-dismiss="modal"], .btn-close');
                    if (closeBtn) closeBtn.click();
                }

                // 열린 드롭다운 닫기
                const openDropdown = document.querySelector('.dropdown-menu.show');
                if (openDropdown) {
                    openDropdown.classList.remove('show');
                }
            }
        });

        // 모달 포커스 트랩
        document.addEventListener('focusin', function(e) {
            const modal = document.querySelector('.modal.show, .modal[style*="display: block"]');
            if (modal && !modal.contains(e.target)) {
                const focusableElements = modal.querySelectorAll(
                    'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
                );
                if (focusableElements.length > 0) {
                    focusableElements[0].focus();
                }
            }
        });
    }

    /**
     * 폼 레이블 개선
     */
    function enhanceFormLabels() {
        // placeholder만 있고 label이 없는 input 찾기
        const inputs = document.querySelectorAll('input[placeholder], textarea[placeholder]');

        inputs.forEach(function(input) {
            const id = input.id;
            const placeholder = input.getAttribute('placeholder');

            // label이 연결되어 있는지 확인
            const hasLabel = id && document.querySelector('label[for="' + id + '"]');

            if (!hasLabel && placeholder) {
                // aria-label 추가
                if (!input.getAttribute('aria-label')) {
                    input.setAttribute('aria-label', placeholder);
                }
            }

            // required 필드에 aria-required 추가
            if (input.required && !input.getAttribute('aria-required')) {
                input.setAttribute('aria-required', 'true');
            }
        });

        // 검색 폼 개선
        const searchInputs = document.querySelectorAll('input[type="search"], input[name*="search"], input[name*="keyword"]');
        searchInputs.forEach(function(input) {
            if (!input.getAttribute('aria-label')) {
                input.setAttribute('aria-label', '검색어 입력');
            }

            // 검색 폼에 role 추가
            const form = input.closest('form');
            if (form && !form.getAttribute('role')) {
                form.setAttribute('role', 'search');
            }
        });
    }

    /**
     * ARIA 속성 자동 추가
     */
    function addAriaAttributes() {
        // 메인 콘텐츠 영역
        const main = document.querySelector('main, .main-content');
        if (main && !main.getAttribute('role')) {
            main.setAttribute('role', 'main');
            if (!main.id) main.id = 'main-content';
        }

        // 네비게이션
        const navs = document.querySelectorAll('nav, .nav, .navigation');
        navs.forEach(function(nav, index) {
            if (!nav.getAttribute('role')) {
                nav.setAttribute('role', 'navigation');
            }
            if (!nav.getAttribute('aria-label')) {
                nav.setAttribute('aria-label', index === 0 ? '메인 네비게이션' : '보조 네비게이션');
            }
        });

        // 푸터
        const footer = document.querySelector('footer, .footer');
        if (footer && !footer.getAttribute('role')) {
            footer.setAttribute('role', 'contentinfo');
        }

        // 버튼에 역할 명시
        const buttonLikeElements = document.querySelectorAll('[onclick]:not(button):not(a)');
        buttonLikeElements.forEach(function(el) {
            if (!el.getAttribute('role')) {
                el.setAttribute('role', 'button');
            }
            if (!el.getAttribute('tabindex')) {
                el.setAttribute('tabindex', '0');
            }
        });

        // 이미지 갤러리/슬라이더
        const sliders = document.querySelectorAll('.slider, .carousel, .swiper');
        sliders.forEach(function(slider) {
            if (!slider.getAttribute('role')) {
                slider.setAttribute('role', 'region');
            }
            if (!slider.getAttribute('aria-label')) {
                slider.setAttribute('aria-label', '이미지 슬라이더');
            }
        });

        // 상품 목록
        const productLists = document.querySelectorAll('.product-list, .product-grid');
        productLists.forEach(function(list) {
            if (!list.getAttribute('role')) {
                list.setAttribute('role', 'list');
            }
            if (!list.getAttribute('aria-label')) {
                list.setAttribute('aria-label', '상품 목록');
            }
        });

        // 상품 카드
        const productCards = document.querySelectorAll('.product-card, .product-item');
        productCards.forEach(function(card) {
            if (!card.getAttribute('role')) {
                card.setAttribute('role', 'listitem');
            }
        });

        // 알림/토스트
        const alerts = document.querySelectorAll('.alert, .toast, .notification');
        alerts.forEach(function(alert) {
            if (!alert.getAttribute('role')) {
                alert.setAttribute('role', 'alert');
            }
            if (!alert.getAttribute('aria-live')) {
                alert.setAttribute('aria-live', 'polite');
            }
        });

        // 로딩 인디케이터
        const loaders = document.querySelectorAll('.loading, .spinner, .loader');
        loaders.forEach(function(loader) {
            if (!loader.getAttribute('role')) {
                loader.setAttribute('role', 'status');
            }
            if (!loader.getAttribute('aria-label')) {
                loader.setAttribute('aria-label', '로딩 중');
            }
        });
    }

    /**
     * 건너뛰기 링크 생성
     */
    function createSkipLink() {
        // 이미 있으면 생성하지 않음
        if (document.querySelector('.skip-link')) return;

        const main = document.querySelector('main, .main-content, #main-content');
        if (!main) return;

        // main에 id 확보
        if (!main.id) main.id = 'main-content';

        // 건너뛰기 링크 생성
        const skipLink = document.createElement('a');
        skipLink.href = '#' + main.id;
        skipLink.className = 'skip-link';
        skipLink.textContent = '본문 바로가기';

        // body 맨 앞에 삽입
        document.body.insertBefore(skipLink, document.body.firstChild);
    }

    // 전역 접근성 유틸리티
    window.A11y = {
        /**
         * 요소에 포커스 (스크롤 없이)
         */
        focusWithoutScroll: function(element) {
            const x = window.scrollX;
            const y = window.scrollY;
            element.focus();
            window.scrollTo(x, y);
        },

        /**
         * 스크린리더에 메시지 전달
         */
        announce: function(message, priority) {
            const liveRegion = document.getElementById('a11y-live-region') || createLiveRegion();
            liveRegion.setAttribute('aria-live', priority === 'assertive' ? 'assertive' : 'polite');
            liveRegion.textContent = message;

            // 잠시 후 비우기
            setTimeout(function() {
                liveRegion.textContent = '';
            }, 1000);
        }
    };

    function createLiveRegion() {
        const region = document.createElement('div');
        region.id = 'a11y-live-region';
        region.className = 'sr-only';
        region.setAttribute('aria-live', 'polite');
        region.setAttribute('aria-atomic', 'true');
        document.body.appendChild(region);
        return region;
    }
})();
