/**
 * 비속어 필터 유틸리티
 * 사용법:
 * - ProfanityFilter.validate(text) : 비속어 포함 여부 검증
 * - ProfanityFilter.filter(text) : 비속어 마스킹 처리
 * - ProfanityFilter.attachValidator(inputElement, options) : 입력 요소에 실시간 검증 추가
 * - ProfanityFilter.validateForm(formElement, fieldNames) : 폼 제출 전 검증
 */
const ProfanityFilter = {

    /**
     * 텍스트 검증
     * @param {string} text - 검증할 텍스트
     * @returns {Promise<Object>} - { isValid, hasProfanity, detectedWords, message }
     */
    validate: async function(text) {
        if (!text || text.trim().length === 0) {
            return { isValid: true, hasProfanity: false };
        }

        try {
            const response = await fetch(`${window.contextPath || ''}/api/profanity/validate`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ text: text })
            });
            return await response.json();
        } catch (error) {
            console.error('Profanity validation error:', error);
            return { isValid: true, hasProfanity: false };
        }
    },

    /**
     * 여러 필드 검증
     * @param {Object} fields - { fieldName: fieldValue, ... }
     * @returns {Promise<Object>} - { isValid, hasAnyProfanity, fieldResults }
     */
    validateFields: async function(fields) {
        try {
            const response = await fetch(`${window.contextPath || ''}/api/profanity/validate-fields`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(fields)
            });
            return await response.json();
        } catch (error) {
            console.error('Profanity validation error:', error);
            return { isValid: true, hasAnyProfanity: false };
        }
    },

    /**
     * 텍스트 필터링 (마스킹)
     * @param {string} text - 필터링할 텍스트
     * @returns {Promise<string>} - 마스킹된 텍스트
     */
    filter: async function(text) {
        if (!text || text.trim().length === 0) {
            return text;
        }

        try {
            const response = await fetch(`${window.contextPath || ''}/api/profanity/filter`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ text: text })
            });
            const result = await response.json();
            return result.filtered;
        } catch (error) {
            console.error('Profanity filter error:', error);
            return text;
        }
    },

    /**
     * input/textarea 요소에 실시간 검증 추가
     * @param {HTMLElement} inputElement - 입력 요소
     * @param {Object} options - 옵션 설정
     */
    attachValidator: function(inputElement, options = {}) {
        if (!inputElement) return;

        const defaults = {
            showAlert: false,          // blur 시 alert 표시 여부
            preventSubmit: true,       // 제출 방지 여부
            errorClass: 'profanity-error',
            errorMessage: '부적절한 표현이 포함되어 있습니다.',
            debounceMs: 500,           // 디바운스 시간
            minLength: 2               // 최소 검사 길이
        };
        const settings = { ...defaults, ...options };

        let debounceTimer;

        // 입력 시 실시간 검사 (debounce)
        inputElement.addEventListener('input', async function() {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(async () => {
                const text = this.value;
                if (text.length < settings.minLength) {
                    ProfanityFilter._hideError(this, settings);
                    return;
                }

                const result = await ProfanityFilter.validate(text);

                if (result.hasProfanity) {
                    ProfanityFilter._showError(this, settings, result.detectedWords);
                } else {
                    ProfanityFilter._hideError(this, settings);
                }
            }, settings.debounceMs);
        });

        // 포커스 아웃 시 검사
        inputElement.addEventListener('blur', async function() {
            const text = this.value;
            if (!text || text.length < settings.minLength) return;

            const result = await ProfanityFilter.validate(text);

            if (result.hasProfanity) {
                ProfanityFilter._showError(this, settings, result.detectedWords);
                if (settings.showAlert) {
                    alert(settings.errorMessage);
                }
            }
        });

        // 데이터 속성에 설정 저장
        inputElement.dataset.profanitySettings = JSON.stringify(settings);
    },

    /**
     * 에러 표시 (내부 함수)
     */
    _showError: function(element, settings, detectedWords) {
        element.classList.add(settings.errorClass);

        // 에러 메시지 표시
        let errorSpan = element.nextElementSibling;
        if (!errorSpan || !errorSpan.classList.contains('profanity-error-msg')) {
            errorSpan = document.createElement('span');
            errorSpan.className = 'profanity-error-msg';
            errorSpan.style.color = '#dc3545';
            errorSpan.style.fontSize = '0.85rem';
            errorSpan.style.display = 'block';
            errorSpan.style.marginTop = '5px';
            element.parentNode.insertBefore(errorSpan, element.nextSibling);
        }

        let message = '⚠️ ' + settings.errorMessage;
        if (detectedWords && detectedWords.length > 0) {
            message += ' (' + detectedWords.slice(0, 3).join(', ') + ')';
        }
        errorSpan.textContent = message;
        errorSpan.style.display = 'block';
    },

    /**
     * 에러 숨김 (내부 함수)
     */
    _hideError: function(element, settings) {
        element.classList.remove(settings.errorClass);

        const errorSpan = element.nextElementSibling;
        if (errorSpan && errorSpan.classList.contains('profanity-error-msg')) {
            errorSpan.style.display = 'none';
        }
    },

    /**
     * 폼 제출 전 검증
     * @param {HTMLFormElement} formElement - 폼 요소
     * @param {Array<string>} fieldNames - 검증할 필드명 배열
     * @returns {Promise<boolean>} - 유효성 여부
     */
    validateForm: async function(formElement, fieldNames) {
        const fields = {};
        let firstInvalidField = null;

        fieldNames.forEach(name => {
            const input = formElement.querySelector(`[name="${name}"]`);
            if (input) {
                fields[name] = input.value;
            }
        });

        const result = await this.validateFields(fields);

        if (result.hasAnyProfanity) {
            const invalidFields = Object.keys(result.fieldResults);

            // 첫 번째 유효하지 않은 필드에 포커스
            for (const fieldName of invalidFields) {
                const input = formElement.querySelector(`[name="${fieldName}"]`);
                if (input) {
                    input.classList.add('profanity-error');
                    if (!firstInvalidField) {
                        firstInvalidField = input;
                    }
                }
            }

            if (firstInvalidField) {
                firstInvalidField.focus();
            }

            const fieldLabels = invalidFields.map(name => {
                const input = formElement.querySelector(`[name="${name}"]`);
                const label = formElement.querySelector(`label[for="${input ? input.id : name}"]`);
                return label ? label.textContent.replace('*', '').trim() : name;
            });

            alert(`다음 필드에 부적절한 표현이 포함되어 있습니다:\n${fieldLabels.join(', ')}`);
            return false;
        }

        return true;
    },

    /**
     * HTML 태그 제거하여 순수 텍스트 추출
     * @param {string} html - HTML 문자열
     * @returns {string} - 순수 텍스트
     */
    stripHtml: function(html) {
        if (!html) return '';
        return html.replace(/<[^>]*>/g, ' ').replace(/&nbsp;/g, ' ').replace(/\s+/g, ' ').trim();
    },

    /**
     * 여러 요소에 일괄 검증 추가
     * @param {string} selector - CSS 선택자
     * @param {Object} options - 옵션 설정
     */
    attachToAll: function(selector, options = {}) {
        const elements = document.querySelectorAll(selector);
        elements.forEach(el => this.attachValidator(el, options));
    }
};

// 전역 스타일 추가
(function() {
    const style = document.createElement('style');
    style.textContent = `
        .profanity-error {
            border-color: #dc3545 !important;
            background-color: #fff5f5 !important;
        }
        .profanity-error-msg {
            color: #dc3545;
            font-size: 0.85rem;
            margin-top: 5px;
            display: block;
        }
        [data-theme="dark"] .profanity-error {
            background-color: #3d2020 !important;
        }
    `;
    document.head.appendChild(style);
})();