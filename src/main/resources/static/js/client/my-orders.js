// 주문 내역 페이지 JavaScript

function cancelOrder(orderId) {
    const reason = prompt('취소 사유를 입력하세요:');
    if (reason === null) return;

    fetch(contextPath + '/mypage/order/cancel/' + orderId, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'cancelReason=' + encodeURIComponent(reason)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message);
            location.reload();
        } else {
            alert(data.message);
        }
    });
}