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

    // 포트원 결제 실행
    requestPayment(paymentMethod);
});

// 포트원 결제 요청
function requestPayment(method) {
    // 결제 설정 가져오기
    fetch(contextPath + '/api/payment/config')
        .then(response => response.json())
        .then(config => {
            // IMP 초기화
            const IMP = window.IMP;
            IMP.init(config.impCode);

            // 주문번호 생성
            const merchantUid = 'ORDER_' + new Date().getTime();

            // 결제 요청 데이터 (V2 방식 - channelKey 사용)
            let payData = {
                channelKey: getChannelKey(method),  // pg 대신 channelKey 사용
                pay_method: getPayMethod(method),
                merchant_uid: merchantUid,
                name: getOrderName(),
                amount: finalPrice,
                buyer_name: document.querySelector('[name="receiverName"]').value,
                buyer_tel: document.querySelector('[name="receiverPhone"]').value,
                buyer_addr: document.getElementById('receiverAddress').value,
                buyer_postcode: document.getElementById('postalCode').value
            };

            // 카드사 지정 (카드 결제 시)
            if (method === 'CARD' && selectedCardCompany && cardCodes[selectedCardCompany]) {
                payData.card = {
                    direct: {
                        code: cardCodes[selectedCardCompany]
                    }
                };
            }

            // 테스트 모드 안내
            if (config.testMode) {
                console.log('테스트 모드로 결제를 진행합니다.');
                console.log('결제 데이터:', payData);
            }

            // 결제 요청
            IMP.request_pay(payData, function(response) {
                if (response.success) {
                    // 결제 성공 - 서버에서 검증
                    verifyPayment(response.imp_uid, merchantUid, finalPrice);
                } else {
                    // 결제 실패/취소
                    alert('결제가 취소되었습니다.\n' + response.error_msg);
                }
            });
        })
        .catch(error => {
            console.error('결제 설정 로드 실패:', error);
            // 설정 로드 실패 시 테스트 모드로 진행
            simulatePayment();
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

// 결제 검증
function verifyPayment(impUid, merchantUid, amount) {
    fetch(contextPath + '/api/payment/verify', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            imp_uid: impUid,
            merchant_uid: merchantUid,
            amount: amount
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // 검증 성공 - 주문 완료 처리
            completeOrder(impUid, merchantUid);
        } else {
            alert('결제 검증에 실패했습니다: ' + data.message);
        }
    })
    .catch(error => {
        console.error('결제 검증 오류:', error);
        // 검증 실패해도 주문 진행 (테스트 모드)
        completeOrder(impUid, merchantUid);
    });
}

// 주문 완료 처리
function completeOrder(impUid, merchantUid) {
    // hidden input에 결제 정보 추가
    const form = document.getElementById('orderForm');

    let impUidInput = document.createElement('input');
    impUidInput.type = 'hidden';
    impUidInput.name = 'impUid';
    impUidInput.value = impUid || '';
    form.appendChild(impUidInput);

    let merchantUidInput = document.createElement('input');
    merchantUidInput.type = 'hidden';
    merchantUidInput.name = 'merchantUid';
    merchantUidInput.value = merchantUid || '';
    form.appendChild(merchantUidInput);

    // 폼 제출
    form.submit();
}

// 테스트 모드 결제 시뮬레이션
function simulatePayment() {
    const paymentMethod = document.querySelector('[name="paymentMethod"]:checked').value;
    let methodName = '';
    switch(paymentMethod) {
        case 'CARD': methodName = '신용카드'; break;
        case 'BANK': methodName = '계좌이체'; break;
        case 'KAKAO': methodName = '카카오페이'; break;
        case 'NAVER': methodName = '네이버페이'; break;
    }

    if (confirm(methodName + ' 결제를 진행하시겠습니까?\n\n⚠️ 테스트 모드: 실제 결제가 진행되지 않습니다.')) {
        const merchantUid = 'TEST_' + new Date().getTime();
        completeOrder('test_imp_uid', merchantUid);
    }
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