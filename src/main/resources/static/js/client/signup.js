let isUserIdChecked = false;
let isEmailVerified = false;
let verificationTimer = null;

function checkUserId() {
    const userId = document.getElementById('userId').value;
    const userIdMsg = document.getElementById('userIdMsg');

    if (userId.length < 6 || userId.length > 15) {
        userIdMsg.textContent = '6자 이상 15자 이하로 입력하세요';
        userIdMsg.className = 'error';
        isUserIdChecked = false;
        return;
    }

    fetch(contextPath + '/api/check-userid?userId=' + encodeURIComponent(userId))
        .then(response => response.json())
        .then(data => {
            if (data.duplicate) {
                userIdMsg.textContent = '이미 사용 중인 아이디입니다';
                userIdMsg.className = 'error';
                isUserIdChecked = false;
            } else {
                userIdMsg.textContent = '사용 가능한 아이디입니다';
                userIdMsg.className = 'success';
                isUserIdChecked = true;
            }
        })
        .catch(error => {
            userIdMsg.textContent = '중복확인 중 오류가 발생했습니다';
            userIdMsg.className = 'error';
        });
}

function sendVerificationCode() {
    const email = document.getElementById('email').value;
    const emailMsg = document.getElementById('emailMsg');
    const btnSendCode = document.getElementById('btnSendCode');

    if (!isValidEmail(email)) {
        emailMsg.textContent = '유효한 이메일을 입력하세요';
        emailMsg.className = 'error';
        return;
    }

    btnSendCode.disabled = true;
    btnSendCode.textContent = '발송 중...';

    fetch(contextPath + '/api/send-verification-code', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'email=' + encodeURIComponent(email)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            emailMsg.textContent = '인증코드가 발송되었습니다. 이메일을 확인해주세요.';
            emailMsg.className = 'success';
            document.getElementById('verificationGroup').style.display = 'block';
            document.getElementById('verificationCode').value = '';
            document.getElementById('verificationMsg').textContent = '';
            isEmailVerified = false;
            startTimer();
            btnSendCode.textContent = '재발송';
        } else {
            emailMsg.textContent = data.message;
            emailMsg.className = 'error';
            btnSendCode.textContent = '인증코드 발송';
        }
        btnSendCode.disabled = false;
    })
    .catch(error => {
        emailMsg.textContent = '인증코드 발송 중 오류가 발생했습니다';
        emailMsg.className = 'error';
        btnSendCode.disabled = false;
        btnSendCode.textContent = '인증코드 발송';
    });
}

function verifyEmailCode() {
    const email = document.getElementById('email').value;
    const code = document.getElementById('verificationCode').value;
    const verificationMsg = document.getElementById('verificationMsg');

    if (code.length !== 6) {
        verificationMsg.textContent = '6자리 인증코드를 입력해주세요';
        verificationMsg.className = 'error';
        return;
    }

    fetch(contextPath + '/api/verify-email-code', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'email=' + encodeURIComponent(email) + '&code=' + encodeURIComponent(code)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            verificationMsg.textContent = '이메일 인증이 완료되었습니다';
            verificationMsg.className = 'success';
            isEmailVerified = true;
            stopTimer();
            document.getElementById('verificationCode').disabled = true;
            document.getElementById('email').readOnly = true;
            document.getElementById('btnSendCode').style.display = 'none';
        } else {
            verificationMsg.textContent = data.message;
            verificationMsg.className = 'error';
            isEmailVerified = false;
        }
    })
    .catch(error => {
        verificationMsg.textContent = '인증 확인 중 오류가 발생했습니다';
        verificationMsg.className = 'error';
    });
}

function startTimer() {
    stopTimer();
    let remaining = 300; // 5분
    const timerEl = document.getElementById('verificationTimer');

    verificationTimer = setInterval(() => {
        remaining--;
        const min = Math.floor(remaining / 60);
        const sec = remaining % 60;
        timerEl.textContent = '남은 시간: ' + min + ':' + (sec < 10 ? '0' : '') + sec;

        if (remaining <= 0) {
            stopTimer();
            timerEl.textContent = '인증코드가 만료되었습니다. 재발송해주세요.';
            timerEl.className = 'msg error';
        }
    }, 1000);
}

function stopTimer() {
    if (verificationTimer) {
        clearInterval(verificationTimer);
        verificationTimer = null;
    }
    const timerEl = document.getElementById('verificationTimer');
    if (timerEl) timerEl.textContent = '';
}

// 이메일 변경 시 인증 상태 초기화
document.getElementById('email').addEventListener('input', function() {
    if (isEmailVerified) return; // 인증 완료 후에는 readOnly라 여기 안 옴
    document.getElementById('verificationGroup').style.display = 'none';
    document.getElementById('emailMsg').textContent = '';
    stopTimer();
});

document.getElementById('confirmPassword').addEventListener('keyup', function() {
    const password = document.getElementById('userPassword').value;
    const confirmPassword = this.value;
    const passwordMsg = document.getElementById('passwordMsg');

    if (password === confirmPassword && password.length > 0) {
        passwordMsg.textContent = '비밀번호가 일치합니다';
        passwordMsg.className = 'success';
    } else if (confirmPassword.length > 0) {
        passwordMsg.textContent = '비밀번호가 일치하지 않습니다';
        passwordMsg.className = 'error';
    }
});

document.getElementById('signupForm').addEventListener('submit', function(e) {
    e.preventDefault();

    if (!isEmailVerified) {
        showAlert('이메일 인증을 완료해주세요', 'error');
        return;
    }

    const formData = new FormData(this);

    fetch(contextPath + '/signup', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert('회원가입에 성공했습니다. 로그인 페이지로 이동합니다.', 'success');
            setTimeout(() => {
                window.location.href = contextPath + '/login';
            }, 2000);
        } else {
            showAlert(data.message || '회원가입 중 오류가 발생했습니다', 'error');
        }
    })
    .catch(error => {
        showAlert('회원가입 중 오류가 발생했습니다', 'error');
    });
});

function showAlert(message, type) {
    let alertContainer = document.querySelector('.alert-container');
    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.textContent = message;

    if (!alertContainer) {
        alertContainer = document.createElement('div');
        alertContainer.className = 'alert-container';
        const signupBox = document.querySelector('.signup-card');
        if(signupBox) {
            signupBox.prepend(alertContainer);
        }
    } else {
        alertContainer.innerHTML = '';
    }

    alertContainer.appendChild(alert);

    if (type === 'success') {
        setTimeout(() => {
            alert.remove();
        }, 2000);
    }
}

function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}
