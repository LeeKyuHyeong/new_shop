// client-main.js

(function() {
    let currentSlide = 0;
    let slideTimer = null;
    let slides = [];
    let dots = [];
    let slideDuration = 5000; // 기본값 5초

    function init() {
        slides = document.querySelectorAll('.slide');
        dots = document.querySelectorAll('.slide-dot');

        // data-duration 속성에서 슬라이드 지속시간 가져오기 (siteSetting 값)
        const container = document.querySelector('.slide-container');
        if (container && container.dataset.duration) {
            slideDuration = parseInt(container.dataset.duration) * 1000;
        }

        if (slides.length > 0) {
            startAutoSlide();
        }
    }

    function showSlide(index) {
        if (slides.length === 0) return;

        // 인덱스 범위 조정
        if (index >= slides.length) {
            currentSlide = 0;
        } else if (index < 0) {
            currentSlide = slides.length - 1;
        } else {
            currentSlide = index;
        }

        // 모든 슬라이드 비활성화 (fade-out)
        slides.forEach(function(slide) {
            slide.classList.remove('active');
        });

        // 모든 dot 비활성화
        dots.forEach(function(dot) {
            dot.classList.remove('active');
        });

        // 현재 슬라이드 활성화 (fade-in)
        slides[currentSlide].classList.add('active');

        if (dots[currentSlide]) {
            dots[currentSlide].classList.add('active');
        }

        // 자동 슬라이드 타이머 재설정
        resetTimer();
    }

    function changeSlide(direction) {
        showSlide(currentSlide + direction);
    }

    function goToSlide(index) {
        showSlide(index);
    }

    function startAutoSlide() {
        slideTimer = setTimeout(function() {
            changeSlide(1);
        }, slideDuration);
    }

    function resetTimer() {
        if (slideTimer) {
            clearTimeout(slideTimer);
        }
        slideTimer = setTimeout(function() {
            changeSlide(1);
        }, slideDuration);
    }

    // 전역 함수로 노출 (onclick에서 호출)
    window.changeSlide = changeSlide;
    window.goToSlide = goToSlide;

    // DOM 로드 완료 시 초기화
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();