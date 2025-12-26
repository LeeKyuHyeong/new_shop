let isUserIdChecked = false;
let isEmailChecked = false;

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

function checkEmail() {
    const email = document.getElementById('email').value;
    const emailMsg = document.getElementById('emailMsg');

    if (!isValidEmail(email)) {
        emailMsg.textContent = '유효한 이메일을 입력하세요';
        emailMsg.className = 'error';
        isEmailChecked = false;
        return;
    }

    fetch(contextPath + '/api/check-email?email=' + encodeURIComponent(email))
        .then(response => response.json())
        .then(data => {
            if (data.duplicate) {
                emailMsg.textContent = '이미 사용 중인 이메일입니다';
                emailMsg.className = 'error';
                isEmailChecked = false;
            } else {
                emailMsg.textContent = '사용 가능한 이메일입니다';
                emailMsg.className = 'success';
                isEmailChecked = true;
            }
        })
        .catch(error => {
            emailMsg.textContent = '중복확인 중 오류가 발생했습니다';
            emailMsg.className = 'error';
        });
}

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

//    const password = document.getElementById('userPassword').value;
//    const confirmPassword = document.getElementById('confirmPassword').value;
//
//    if (password.length < 8) {
//        showAlert('비밀번호는 8자 이상이어야 합니다', 'error');
//        return;
//    }
//
//    if (password !== confirmPassword) {
//        showAlert('비밀번호가 일치하지 않습니다', 'error');
//        return;
//    }
//
//    if (!isUserIdChecked) {
//        showAlert('아이디 중복확인을 진행해주세요', 'error');
//        return;
//    }
//
//    if (!isEmailChecked) {
//        showAlert('이메일 중복확인을 진행해주세요', 'error');
//        return;
//    }

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
    const alertContainer = document.querySelector('.alert-container');
    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.textContent = message;

    if (!alertContainer) {
        const container = document.createElement('div');
        container.className = 'alert-container';
        document.querySelector('.signup-box').insertBefore(container, document.querySelector('.signup-header').nextElementSibling);
        container.appendChild(alert);
    } else {
        alertContainer.innerHTML = '';
        alertContainer.appendChild(alert);
    }

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