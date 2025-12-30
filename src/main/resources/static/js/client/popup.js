// 팝업 초기화
document.addEventListener('DOMContentLoaded', function() {
    const popups = document.querySelectorAll('.popup-container');

    popups.forEach(popup => {
        const popupId = popup.id.replace('popup-', '');

        // 쿠키 확인
        if (getCookie('hidePopup_' + popupId)) {
            popup.style.display = 'none';
        }
    });
});

// 팝업 닫기
function closePopup(popupId) {
    const popup = document.getElementById('popup-' + popupId);
    if (popup) {
        popup.style.display = 'none';
    }
}

// 오늘 하루 보지 않기
function closePopupToday(popupId) {
    const popup = document.getElementById('popup-' + popupId);
    if (popup) {
        popup.style.display = 'none';

        // 쿠키 설정 (popupDuration일 동안 유지)
        const duration = typeof popupDuration !== 'undefined' ? popupDuration : 1;
        setCookie('hidePopup_' + popupId, 'true', duration);
    }
}

// 쿠키 설정
function setCookie(name, value, days) {
    const expires = new Date();
    expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000);
    document.cookie = name + '=' + value + ';expires=' + expires.toUTCString() + ';path=/';
}

// 쿠키 가져오기
function getCookie(name) {
    const value = '; ' + document.cookie;
    const parts = value.split('; ' + name + '=');
    if (parts.length === 2) {
        return parts.pop().split(';').shift();
    }
    return null;
}