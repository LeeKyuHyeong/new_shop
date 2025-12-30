// 개인설정 페이지 JavaScript

// 테마 설정
function setTheme(theme) {
    // 버튼 상태 업데이트
    document.querySelectorAll('.theme-btn').forEach(btn => {
        btn.classList.remove('active');
        if (btn.dataset.theme === theme) {
            btn.classList.add('active');
        }
    });

    // 바디 클래스 적용
    if (theme === 'DARK') {
        document.body.classList.add('dark-mode');
    } else {
        document.body.classList.remove('dark-mode');
    }

    // localStorage 저장
    localStorage.setItem('theme', theme.toLowerCase());

    // 헤더의 테마 버튼 텍스트 업데이트
    if (typeof updateThemeButton === 'function') {
        updateThemeButton();
    }

    // 서버에 저장
    fetch(contextPath + '/api/setting/theme', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'theme=' + theme
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('테마가 변경되었습니다');
        }
    })
    .catch(err => console.error('Theme save error:', err));
}

// 개별 설정 저장
function saveSetting(settingName, value) {
    const formData = new URLSearchParams();
    formData.append(settingName, value);

    fetch(contextPath + '/api/setting/save', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData.toString()
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('설정이 저장되었습니다');
        } else {
            showToast(data.message, 'error');
        }
    })
    .catch(err => {
        console.error('Setting save error:', err);
        showToast('설정 저장 중 오류가 발생했습니다', 'error');
    });
}

// 비밀번호 변경
document.getElementById('passwordForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (newPassword !== confirmPassword) {
        showToast('새 비밀번호가 일치하지 않습니다', 'error');
        return;
    }

    if (newPassword.length < 4) {
        showToast('비밀번호는 4자 이상이어야 합니다', 'error');
        return;
    }

    const formData = new URLSearchParams();
    formData.append('currentPassword', currentPassword);
    formData.append('newPassword', newPassword);
    formData.append('confirmPassword', confirmPassword);

    fetch(contextPath + '/api/setting/password', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData.toString()
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showToast('비밀번호가 변경되었습니다');
            document.getElementById('passwordForm').reset();
        } else {
            showToast(data.message, 'error');
        }
    })
    .catch(err => {
        console.error('Password change error:', err);
        showToast('비밀번호 변경 중 오류가 발생했습니다', 'error');
    });
});

// 토스트 메시지
function showToast(message, type = 'success') {
    // 기존 토스트 제거
    const existingToast = document.querySelector('.toast');
    if (existingToast) {
        existingToast.remove();
    }

    const toast = document.createElement('div');
    toast.className = 'toast ' + type;
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('show');
    }, 10);

    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// 장바구니 카운트
function loadCartCount() {
    fetch(contextPath + '/cart/count')
        .then(response => response.json())
        .then(data => {
            const countEl = document.getElementById('cartCount');
            if (countEl && data.count > 0) {
                countEl.textContent = data.count;
                countEl.style.display = 'inline-flex';
            }
        })
        .catch(err => console.log('Cart count error:', err));
}

document.addEventListener('DOMContentLoaded', function() {
    loadCartCount();
});