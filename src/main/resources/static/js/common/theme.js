/**
 * 테마 관리 JS
 *
 * 1. 비로그인 사용자: localStorage 기반 테마 저장/적용
 * 2. 로그인 사용자: DB(UserSetting) 기반 테마 저장/적용 + localStorage 동기화
 * 3. admin/client 모두 동일한 user-setting 값 사용
 */

document.addEventListener('DOMContentLoaded', function() {
    initTheme();
});

/**
 * 테마 초기화
 * - 먼저 localStorage에서 빠르게 적용 (깜빡임 방지)
 * - 로그인 사용자는 서버에서 DB 테마 가져와서 동기화
 */
function initTheme() {
    // 1. localStorage에서 먼저 적용 (화면 깜빡임 방지)
    const localTheme = localStorage.getItem('theme') || 'light';
    applyTheme(localTheme);

    // 2. 서버에서 사용자 테마 확인 (로그인 상태 체크)
    const contextPath = window.contextPath || '';
    fetch(contextPath + '/api/setting/theme')
        .then(response => response.json())
        .then(data => {
            if (data.success && data.loggedIn) {
                // 로그인한 사용자는 DB 설정을 기준으로 적용
                const dbTheme = data.theme ? data.theme.toLowerCase() : 'light';

                // localStorage와 DB가 다르면 DB 값으로 동기화
                if (localTheme !== dbTheme) {
                    localStorage.setItem('theme', dbTheme);
                    applyTheme(dbTheme);
                }
            }
            // 비로그인 사용자는 localStorage 값 그대로 유지
        })
        .catch(err => {
            // API 호출 실패 시 localStorage 유지
            console.log('Theme API not available, using localStorage');
        });
}

/**
 * 테마 적용
 * @param {string} theme - 'light' 또는 'dark'
 */
function applyTheme(theme) {
    if (theme === 'dark') {
        document.body.classList.add('dark-mode');
    } else {
        document.body.classList.remove('dark-mode');
    }
    updateThemeButton();
}

/**
 * 테마 토글 (클릭 시)
 * - 비로그인: localStorage만 저장
 * - 로그인: localStorage + DB 저장
 */
function toggleTheme() {
    document.body.classList.toggle('dark-mode');
    const isDarkMode = document.body.classList.contains('dark-mode');
    const theme = isDarkMode ? 'dark' : 'light';

    // localStorage 저장 (비로그인/로그인 모두)
    localStorage.setItem('theme', theme);
    updateThemeButton();

    // 서버에 테마 저장 시도 (로그인한 경우에만 DB에 저장됨)
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
        // 저장 성공/실패 로그 (비로그인은 saved:false)
        if (data.saved) {
            console.log('Theme saved to DB:', theme);
        }
    })
    .catch(err => {
        console.log('Theme save to server failed, localStorage saved');
    });
}

/**
 * 테마 버튼 UI 업데이트
 */
function updateThemeButton() {
    const themeText = document.getElementById('themeText');
    const themeIcon = document.getElementById('themeIcon');
    const isDarkMode = document.body.classList.contains('dark-mode');

    if (themeText && themeIcon) {
        // 현재 모드 표시: 다크모드면 "라이트" 버튼, 라이트모드면 "다크" 버튼
        themeText.textContent = isDarkMode ? '라이트' : '다크';
        themeIcon.textContent = isDarkMode ? '☀️' : '🌙';
    }
}

/* ========================================
   모바일 사이드바 토글 (admin용)
   ======================================== */
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