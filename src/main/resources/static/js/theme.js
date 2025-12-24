document.addEventListener('DOMContentLoaded', function() {
    const savedTheme = localStorage.getItem('theme') || 'light';
    if (savedTheme === 'dark') {
        document.body.classList.add('dark-mode');
        updateThemeButton();
    }
});

function toggleTheme() {
    document.body.classList.toggle('dark-mode');
    const isDarkMode = document.body.classList.contains('dark-mode');
    localStorage.setItem('theme', isDarkMode ? 'dark' : 'light');
    updateThemeButton();
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