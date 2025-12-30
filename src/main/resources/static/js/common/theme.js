document.addEventListener('DOMContentLoaded', function() {
    initTheme();
});

// 테마 초기화 - 로그인 사용자는 DB, 비로그인은 localStorage
function initTheme() {
    // 먼저 localStorage에서 빠르게 적용 (깜빡임 방지)
    const localTheme = localStorage.getItem('theme') || 'light';
    if (localTheme === 'dark') {
        document.body.classList.add('dark-mode');
    }
    updateThemeButton();

    // 서버에서 사용자 테마 가져오기 시도
    const contextPath = window.contextPath || '';
    fetch(contextPath + '/api/setting/theme')
        .then(response => response.json())
        .then(data => {
            if (data.success && data.loggedIn) {
                // 로그인한 사용자는 DB 설정 적용
                const dbTheme = data.theme ? data.theme.toLowerCase() : 'light';
                localStorage.setItem('theme', dbTheme);

                if (dbTheme === 'dark') {
                    document.body.classList.add('dark-mode');
                } else {
                    document.body.classList.remove('dark-mode');
                }
                updateThemeButton();
            }
        })
        .catch(err => {
            // API 호출 실패 시 localStorage 유지
            console.log('Theme API not available, using localStorage');
        });
}

function toggleTheme() {
    document.body.classList.toggle('dark-mode');
    const isDarkMode = document.body.classList.contains('dark-mode');
    const theme = isDarkMode ? 'dark' : 'light';
    localStorage.setItem('theme', theme);
    updateThemeButton();

    // 서버에 테마 저장 (로그인한 경우)
    const contextPath = window.contextPath || '';
    fetch(contextPath + '/api/setting/theme', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'theme=' + theme.toUpperCase()
    })
    .then(response => response.json())
    .then(data => {
        // 저장 성공/실패 무시 (localStorage는 이미 저장됨)
    })
    .catch(err => {
        console.log('Theme save to server failed, localStorage saved');
    });
}

function updateThemeButton() {
    const themeText = document.getElementById('themeText');
    const themeIcon = document.getElementById('themeIcon');
    const isDarkMode = document.body.classList.contains('dark-mode');

    if (themeText && themeIcon) {
        // 현재 모드에 따라 텍스트와 아이콘 변경
        themeText.textContent = isDarkMode ? '라이트 모드' : '다크 모드';
        themeIcon.textContent = isDarkMode ? '☀️' : '🌙';
    }
}

/* 모바일 사이드바 토글 */
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebarOverlay');
    const menuBtn = document.getElementById('mobileMenuBtn');

    if (sidebar && overlay && menuBtn) {
        sidebar.classList.toggle('open');
        overlay.classList.toggle('active');
        menuBtn.classList.toggle('active');

        // 스크롤 방지
        if (sidebar.classList.contains('open')) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = '';
        }
    }
}

/* ESC 키로 사이드바 닫기 */
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        const sidebar = document.getElementById('sidebar');
        if (sidebar && sidebar.classList.contains('open')) {
            toggleSidebar();
        }
    }
});