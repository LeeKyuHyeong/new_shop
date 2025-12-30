const contextPath = window.contextPath || '';

function deletePopup(popupId) {
    if (!confirm('이 팝업을 삭제하시겠습니까?')) {
        return;
    }

    fetch(`${contextPath}/api/admin/popup/delete/${popupId}`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message);
            location.reload();
        } else {
            alert(data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('팝업 삭제 중 오류가 발생했습니다');
    });
}