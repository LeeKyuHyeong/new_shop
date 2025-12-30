// 소셜 회원가입 페이지 JavaScript

let userIdChecked = false;
let emailChecked = false;
let originalEmail = '';

// 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 소셜에서 가져온 이메일이 있으면 자동 체크
    originalEmail = document.getElementById('email').value;
    if (originalEmail) {
        emailChecked = true;
        checkEmail();
    }
});

// 아이디 중복 체크
function checkUserId() {
    const userId = document.getElementById('userId').value.trim();
    const msgEl = document.getElementById('userIdMsg');
    const inputEl = document.getElementById('userId');

    if (!userId) {
        msgEl.textContent = '아이디를 입력해주세요.';
        msgEl.className = 'validation-msg error';
        return;
    }

    if (!/^[a-zA-Z0-9_]{4,20}$/.test(userId)) {
        msgEl.textContent = '영문, 숫자, 언더스코어(_) 조합 4~20자로 입력해주세요.';
        msgEl.className = 'validation-msg error';
        inputEl.className = 'error';
        return;
    }

    fetch(contextPath + '/oauth/check-userid?userId=' + encodeURIComponent(userId))
        .then(response => response.json())
        .then(data => {
            if (data.available) {
                msgEl.textContent = '사용 가능한 아이디입니다.';
                msgEl.className = 'validation-msg success';
                inputEl.className = 'success';
                userIdChecked = true;
            } else {
                msgEl.textContent = '이미 사용 중인 아이디입니다.';
                msgEl.className = 'validation-msg error';
                inputEl.className = 'error';
                userIdChecked = false;
            }
            updateSubmitButton();
        });
}

// 이메일 중복 체크
function checkEmail() {
    const email = document.getElementById('email').value.trim();
    const msgEl = document.getElementById('emailMsg');
    const inputEl = document.getElementById('email');

    if (!email) {
        msgEl.textContent = '이메일을 입력해주세요.';
        msgEl.className = 'validation-msg error';
        return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        msgEl.textContent = '올바른 이메일 형식이 아닙니다.';
        msgEl.className = 'validation-msg error';
        inputEl.className = 'error';
        return;
    }

    fetch(contextPath + '/oauth/check-email?email=' + encodeURIComponent(email))
        .then(response => response.json())
        .then(data => {
            if (data.available) {
                msgEl.textContent = '사용 가능한 이메일입니다.';
                msgEl.className = 'validation-msg success';
                inputEl.className = 'success';
                emailChecked = true;
            } else {
                msgEl.textContent = '이미 등록된 이메일입니다.';
                msgEl.className = 'validation-msg error';
                inputEl.className = 'error';
                emailChecked = false;
            }
            updateSubmitButton();
        });
}

// 아이디 입력 시 체크 상태 초기화
document.getElementById('userId').addEventListener('input', function() {
    userIdChecked = false;
    this.className = '';
    document.getElementById('userIdMsg').textContent = '';
    updateSubmitButton();
});

// 이메일 입력 시 체크 상태 초기화
document.getElementById('email').addEventListener('input', function() {
    // 원래 소셜에서 가져온 이메일과 같으면 체크 유지
    if (this.value === originalEmail && originalEmail) {
        emailChecked = true;
    } else {
        emailChecked = false;
        this.className = '';
        document.getElementById('emailMsg').textContent = '';
    }
    updateSubmitButton();
});

// 전체 동의
function toggleAllAgree() {
    const agreeAll = document.getElementById('agreeAll').checked;
    document.getElementById('agreeTerms').checked = agreeAll;
    document.getElementById('agreePrivacy').checked = agreeAll;
    document.getElementById('agreeMarketing').checked = agreeAll;
    updateSubmitButton();
}

// 개별 동의 체크
function checkAgree() {
    const terms = document.getElementById('agreeTerms').checked;
    const privacy = document.getElementById('agreePrivacy').checked;
    const marketing = document.getElementById('agreeMarketing').checked;

    document.getElementById('agreeAll').checked = terms && privacy && marketing;
    updateSubmitButton();
}

// 제출 버튼 상태 업데이트
function updateSubmitButton() {
    const userId = document.getElementById('userId').value.trim();
    const userName = document.getElementById('userName').value.trim();
    const email = document.getElementById('email').value.trim();
    const terms = document.getElementById('agreeTerms').checked;
    const privacy = document.getElementById('agreePrivacy').checked;

    const isValid = userId && userName && email &&
                   userIdChecked && emailChecked &&
                   terms && privacy;

    document.getElementById('submitBtn').disabled = !isValid;
}

// 폼 제출 전 최종 검증
document.getElementById('signupForm').addEventListener('submit', function(e) {
    if (!userIdChecked) {
        e.preventDefault();
        alert('아이디 중복확인을 해주세요.');
        return;
    }

    if (!emailChecked) {
        e.preventDefault();
        alert('이메일 중복확인을 해주세요.');
        return;
    }
});