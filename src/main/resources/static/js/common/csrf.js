/**
 * CSRF 토큰 처리
 * - 모든 AJAX 요청에 CSRF 토큰 자동 추가
 * - 폼 제출 시 CSRF 토큰 자동 추가
 */
(function() {
    'use strict';

    // 메타 태그에서 CSRF 토큰 정보 가져오기
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    const csrfParam = document.querySelector('meta[name="_csrf_param"]')?.content || '_csrf';

    // jQuery가 있으면 AJAX 기본 설정
    if (typeof $ !== 'undefined' && csrfToken) {
        $.ajaxSetup({
            beforeSend: function(xhr, settings) {
                // GET, HEAD, OPTIONS, TRACE는 제외
                if (!/^(GET|HEAD|OPTIONS|TRACE)$/i.test(settings.type)) {
                    xhr.setRequestHeader(csrfHeader, csrfToken);
                    xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
                }
            }
        });
    }

    // fetch API 래퍼
    if (csrfToken) {
        const originalFetch = window.fetch;
        window.fetch = function(url, options = {}) {
            options = options || {};
            options.headers = options.headers || {};

            // GET, HEAD, OPTIONS, TRACE는 제외
            const method = (options.method || 'GET').toUpperCase();
            if (!['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)) {
                if (options.headers instanceof Headers) {
                    options.headers.set(csrfHeader, csrfToken);
                    options.headers.set('X-Requested-With', 'XMLHttpRequest');
                } else {
                    options.headers[csrfHeader] = csrfToken;
                    options.headers['X-Requested-With'] = 'XMLHttpRequest';
                }
            }

            return originalFetch(url, options);
        };
    }

    // 폼에 CSRF 토큰 자동 추가
    document.addEventListener('DOMContentLoaded', function() {
        if (!csrfToken) return;

        // 모든 폼에 CSRF 토큰 hidden input 추가
        document.querySelectorAll('form').forEach(function(form) {
            // GET 폼은 제외
            if (form.method && form.method.toUpperCase() === 'GET') return;

            // 이미 CSRF 토큰이 있으면 제외
            if (form.querySelector('input[name="' + csrfParam + '"]')) return;

            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = csrfParam;
            input.value = csrfToken;
            form.appendChild(input);
        });
    });

    // 전역 접근용
    window.CSRF = {
        token: csrfToken,
        header: csrfHeader,
        param: csrfParam,
        getToken: function() {
            return csrfToken;
        },
        addToForm: function(form) {
            if (!csrfToken || !form) return;
            let input = form.querySelector('input[name="' + csrfParam + '"]');
            if (!input) {
                input = document.createElement('input');
                input.type = 'hidden';
                input.name = csrfParam;
                form.appendChild(input);
            }
            input.value = csrfToken;
        }
    };
})();
