// 결제 페이지 JavaScript

let selectedCardCompany = '';

// 카드사 코드 매핑 (포트원 기준)
const cardCodes = {
    'samsung': '04',
    'shinhan': '06',
    'kb': '11',
    'hyundai': '01',
    'lotte': '07',
    'bc': '03',
    'hana': '21',
    'woori': '33',
    'nh': '34',
    'citi': '02',
    'kakao': '15'
};

// PG사 채널키 (포트원 콘솔에서 확인한 값)
const channelKeys = {
    'CARD': 'channel-key-88ebdcc0-cca6-45b6-af23-266b2bf69e89',  // KCP
    'BANK': 'channel-key-88ebdcc0-cca6-45b6-af23-266b2bf69e89',  // KCP
    'KAKAO': 'channel-key-ddadf9a7-9974-466c-995d-e3f01f88482f'  // 카카오페이
};

// 결제 수단 선택
document.querySelectorAll('.payment-method').forEach(method => {
    method.addEventListener('click', function() {
        document.querySelectorAll('.payment-method').forEach(m => m.classList.remove('selected'));
        this.classList.add('selected');
        this.querySelector('input').checked = true;

        // 카드 선택 시 카드사 선택 영역 표시
        const paymentMethod = this.querySelector('input').value;
        const cardSection = document.getElementById('cardCompanySection');
        if (paymentMethod === 'CARD') {
            cardSection.classList.add('active');
        } else {
            cardSection.classList.remove('active');
        }
    });
});

// 카드사 선택
document.querySelectorAll('.card-company').forEach(card => {
    card.addEventListener('click', function() {
        document.querySelectorAll('.card-company').forEach(c => c.classList.remove('selected'));
        this.classList.add('selected');
        selectedCardCompany = this.dataset.card;
        document.getElementById('cardCompany').value = selectedCardCompany;
    });
});

// 주소 검색
function searchAddress() {
    new daum.Postcode({
        oncomplete: function(data) {
            document.getElementById('postalCode').value = data.zonecode;
            document.getElementById('receiverAddress').value = data.roadAddress || data.jibunAddress;
        }
    }).open();
}

// 폼 제출
// 흐름: Order 먼저 서버에 미리 생성(PENDING) -> 그 orderNumber 로 Portone 결제 -> 서버 검증 -> 완료 페이지
document.getElementById('orderForm').addEventListener('submit', function(e) {
    e.preventDefault();

    if (!document.getElementById('agreeAll').checked) {
        alert('결제에 동의해주세요.');
        return;
    }

    // 필수 입력 검증
    const receiverName = document.querySelector('[name="receiverName"]').value;
    const receiverPhone = document.querySelector('[name="receiverPhone"]').value;
    const postalCode = document.getElementById('postalCode').value;

    if (!receiverName || !receiverPhone || !postalCode) {
        alert('배송 정보를 모두 입력해주세요.');
        return;
    }

    // 선택된 결제 수단 확인
    const paymentMethod = document.querySelector('[name="paymentMethod"]:checked').value;

    // 1) Order PENDING 으로 미리 생성
    prepareOrder(paymentMethod);
});

// 1단계: 서버에 Order 생성 요청 (PENDING) -> server-generated orderNumber 받기
function prepareOrder(paymentMethod) {
    const form = document.getElementById('orderForm');
    const formData = new FormData(form);
    // 결제 수단 보장
    formData.set('paymentMethod', paymentMethod);

    fetch(contextPath + '/api/order/prepare', {
        method: 'POST',
        body: formData
    })
    .then(r => r.json())
    .then(data => {
        if (!data.success) {
            alert(data.message || '주문 생성에 실패했습니다.');
            return;
        }
        // 2단계: server orderNumber 를 merchant_uid 로 사용하여 결제
        requestPayment(paymentMethod, data.orderId, data.orderNumber, data.finalPrice);
    })
    .catch(err => {
        console.error('주문 생성 실패:', err);
        alert('주문 생성 중 오류가 발생했습니다.');
    });
}

// 2단계: 포트원 결제 요청
function requestPayment(method, orderId, orderNumber, finalPriceFromServer) {
    fetch(contextPath + '/api/payment/config')
        .then(response => response.json())
        .then(config => {
            const IMP = window.IMP;
            IMP.init(config.impCode);

            // 결제 요청 데이터 (V2 방식 - channelKey 사용)
            // 주의: merchant_uid 와 amount 는 서버에서 받은 값을 사용 (클라이언트 변조 방지)
            let payData = {
                channelKey: getChannelKey(method),
                pay_method: getPayMethod(method),
                merchant_uid: orderNumber,
                name: getOrderName(),
                amount: finalPriceFromServer,
                buyer_name: document.querySelector('[name="receiverName"]').value,
                buyer_tel: document.querySelector('[name="receiverPhone"]').value,
                buyer_addr: document.getElementById('receiverAddress').value,
                buyer_postcode: document.getElementById('postalCode').value
            };

            // 카드사 지정 (카드 결제 시)
            if (method === 'CARD' && selectedCardCompany && cardCodes[selectedCardCompany]) {
                payData.card = { direct: { code: cardCodes[selectedCardCompany] } };
            }

            if (config.testMode) {
                console.log('테스트 모드로 결제를 진행합니다.');
                console.log('결제 데이터:', payData);
            }

            IMP.request_pay(payData, function(response) {
                if (response.success) {
                    // 3단계: 서버 검증
                    verifyPayment(response.imp_uid, orderNumber, orderId);
                } else {
                    // 결제 실패/취소 -> PENDING Order 정리 (재고 복구)
                    cancelPreparedOrder(orderId);
                    alert('결제가 취소되었습니다.\n' + (response.error_msg || ''));
                }
            });
        })
        .catch(error => {
            console.error('결제 설정 로드 실패:', error);
            // 결제 진행 불가 -> 미리 생성한 Order 취소
            cancelPreparedOrder(orderId);
            alert('결제 설정을 불러올 수 없습니다.');
        });
}

// PG사 채널키 반환
function getChannelKey(method) {
    return channelKeys[method] || channelKeys['CARD'];
}

// 결제 수단 결정
function getPayMethod(method) {
    switch(method) {
        case 'CARD': return 'card';
        case 'BANK': return 'trans';
        case 'KAKAO': return 'kakaopay';
        case 'NAVER': return 'naverpay';
        default: return 'card';
    }
}

// 주문명 생성
function getOrderName() {
    const firstItem = document.querySelector('.order-item-name');
    if (firstItem) {
        const itemCount = document.querySelectorAll('.order-item').length;
        if (itemCount > 1) {
            return firstItem.textContent.trim() + ' 외 ' + (itemCount - 1) + '건';
        }
        return firstItem.textContent.trim();
    }
    return 'KH SHOP 주문';
}

// 3단계: 결제 검증 (서버가 DB Order 와 포트원 응답을 양쪽 검증)
function verifyPayment(impUid, merchantUid, orderId) {
    fetch(contextPath + '/api/payment/verify', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            imp_uid: impUid,
            merchant_uid: merchantUid
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // 검증 통과 -> 완료 페이지로 이동
            window.location.href = contextPath + '/order/complete/' + data.orderId;
        } else {
            // 서버가 이미 환불/취소 처리함 (catch 로 우회 불가)
            alert('결제 검증에 실패했습니다: ' + data.message);
            window.location.href = contextPath + '/cart';
        }
    })
    .catch(error => {
        // 네트워크 오류 등 - 서버 응답 확인 불가. 사용자가 다시 시도하도록 안내.
        // (서버는 백그라운드에서 미결제 Order 자동 정리 스케줄러가 처리)
        console.error('결제 검증 통신 오류:', error);
        alert('결제 검증 통신 중 오류가 발생했습니다. 잠시 후 주문 내역에서 상태를 확인해주세요.');
        window.location.href = contextPath + '/my-orders';
    });
}

// 결제 실패/취소 시 미리 생성된 Order 정리 (재고 복구)
function cancelPreparedOrder(orderId) {
    if (!orderId) return;
    fetch(contextPath + '/api/order/cancel-pending/' + orderId, {
        method: 'POST'
    }).catch(err => console.warn('미결제 주문 정리 실패:', err));
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closePaymentModal();
    }
});

// 기존 모달 관련 함수 (호환성 유지)
function openPaymentModal() {
    document.getElementById('paymentModal').classList.add('active');
    document.body.style.overflow = 'hidden';
}

function closePaymentModal() {
    document.getElementById('paymentModal').classList.remove('active');
    document.body.style.overflow = '';
}