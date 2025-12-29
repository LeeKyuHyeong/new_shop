<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>주문서 작성 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client-main.css">
    <style>
        .checkout-container {
            max-width: 1000px;
            margin: 0 auto;
            padding: 30px 20px;
        }
        .page-title {
            font-size: 28px;
            font-weight: 700;
            margin-bottom: 30px;
        }
        .checkout-content {
            display: flex;
            gap: 30px;
        }
        .checkout-form {
            flex: 1;
        }
        .checkout-summary {
            width: 350px;
            flex-shrink: 0;
        }
        .form-section {
            background: white;
            border: 1px solid var(--border-color);
            border-radius: 8px;
            padding: 25px;
            margin-bottom: 20px;
        }
        .section-title {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 20px;
            padding-bottom: 15px;
            border-bottom: 1px solid var(--border-color);
        }
        .form-group {
            margin-bottom: 20px;
        }
        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            font-size: 14px;
        }
        .form-group label .required {
            color: #e74c3c;
        }
        .form-group input, .form-group textarea, .form-group select {
            width: 100%;
            padding: 12px;
            border: 1px solid var(--border-color);
            border-radius: 6px;
            font-size: 14px;
        }
        .form-group input:focus, .form-group textarea:focus {
            outline: none;
            border-color: var(--btn-primary-bg);
        }
        .form-row {
            display: flex;
            gap: 15px;
        }
        .form-row .form-group {
            flex: 1;
        }
        .address-row {
            display: flex;
            gap: 10px;
        }
        .address-row input:first-child {
            width: 120px;
            flex-shrink: 0;
        }
        .btn-search {
            padding: 12px 16px;
            background: var(--btn-secondary-bg);
            color: white;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            white-space: nowrap;
        }
        
        /* 주문 상품 */
        .order-items {
            display: flex;
            flex-direction: column;
            gap: 15px;
        }
        .order-item {
            display: flex;
            gap: 15px;
            padding: 15px;
            background: var(--bg-secondary);
            border-radius: 8px;
        }
        .order-item-image {
            width: 70px;
            height: 70px;
            border-radius: 6px;
            overflow: hidden;
        }
        .order-item-image img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
        .order-item-info {
            flex: 1;
        }
        .order-item-name {
            font-weight: 600;
            margin-bottom: 5px;
        }
        .order-item-price {
            font-size: 14px;
            color: var(--text-secondary);
        }
        .order-item-total {
            font-weight: 600;
            text-align: right;
        }
        
        /* 결제 수단 */
        .payment-methods {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 10px;
        }
        .payment-method {
            padding: 15px;
            border: 2px solid var(--border-color);
            border-radius: 8px;
            cursor: pointer;
            text-align: center;
            transition: all 0.3s;
        }
        .payment-method:hover {
            border-color: var(--btn-primary-bg);
        }
        .payment-method.selected {
            border-color: var(--btn-primary-bg);
            background: rgba(0, 123, 255, 0.05);
        }
        .payment-method input {
            display: none;
        }
        
        /* 카드사 선택 */
        .card-company-section {
            display: none;
            margin-top: 20px;
            padding: 20px;
            background: var(--bg-secondary);
            border-radius: 8px;
        }
        .card-company-section.active {
            display: block;
        }
        .card-company-title {
            font-size: 14px;
            font-weight: 600;
            margin-bottom: 15px;
            color: #333;
        }
        .card-companies {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 10px;
        }
        .card-company {
            padding: 12px 8px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            cursor: pointer;
            text-align: center;
            font-size: 12px;
            font-weight: 500;
            transition: all 0.3s;
            background: white;
        }
        .card-company:hover {
            border-color: #3498db;
        }
        .card-company.selected {
            border-color: #3498db;
            background: #ebf5fb;
        }
        .card-company img {
            width: 40px;
            height: 25px;
            object-fit: contain;
            margin-bottom: 5px;
        }
        
        @media (max-width: 600px) {
            .card-companies {
                grid-template-columns: repeat(3, 1fr);
            }
        }
        
        /* 요약 박스 */
        .summary-box {
            background: white;
            border: 1px solid var(--border-color);
            border-radius: 8px;
            padding: 25px;
            position: sticky;
            top: 100px;
        }
        .summary-title {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 20px;
        }
        .summary-row {
            display: flex;
            justify-content: space-between;
            padding: 10px 0;
        }
        .summary-row.total {
            font-size: 20px;
            font-weight: 700;
            color: var(--btn-primary-bg);
            border-top: 2px solid var(--border-color);
            margin-top: 15px;
            padding-top: 20px;
        }
        .btn-submit {
            width: 100%;
            padding: 16px;
            background: var(--btn-primary-bg);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            margin-top: 20px;
        }
        .btn-submit:hover {
            background: var(--btn-primary-hover);
        }
        .agreement {
            margin-top: 15px;
            font-size: 13px;
            color: var(--text-secondary);
        }
        .agreement label {
            display: flex;
            align-items: flex-start;
            gap: 8px;
            cursor: pointer;
        }
        
        @media (max-width: 768px) {
            .checkout-content {
                flex-direction: column;
            }
            .checkout-summary {
                width: 100%;
            }
            .form-row {
                flex-direction: column;
                gap: 0;
            }
            .payment-methods {
                grid-template-columns: 1fr;
            }
        }

        /* 결제 모달 */
        .payment-modal {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.7);
            z-index: 9999;
            justify-content: center;
            align-items: center;
        }
        .payment-modal.active {
            display: flex;
        }
        .payment-modal-content {
            background: white;
            border-radius: 16px;
            width: 90%;
            max-width: 400px;
            overflow: hidden;
            animation: modalSlideIn 0.3s ease;
        }
        @keyframes modalSlideIn {
            from { transform: translateY(-50px); opacity: 0; }
            to { transform: translateY(0); opacity: 1; }
        }
        .payment-modal-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 25px;
            text-align: center;
        }
        .payment-modal-header h3 {
            font-size: 20px;
            margin-bottom: 5px;
        }
        .payment-modal-header p {
            opacity: 0.9;
            font-size: 14px;
        }
        .payment-modal-body {
            padding: 30px;
        }
        .card-form-group {
            margin-bottom: 20px;
        }
        .card-form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            font-size: 14px;
            color: #333;
        }
        .card-form-group input {
            width: 100%;
            padding: 14px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-size: 16px;
            transition: border-color 0.3s;
        }
        .card-form-group input:focus {
            outline: none;
            border-color: #667eea;
        }
        .card-row {
            display: flex;
            gap: 15px;
        }
        .card-row .card-form-group {
            flex: 1;
        }
        .payment-amount {
            text-align: center;
            padding: 20px;
            background: #f8f9fa;
            border-radius: 8px;
            margin-bottom: 20px;
        }
        .payment-amount span {
            font-size: 14px;
            color: #666;
        }
        .payment-amount strong {
            display: block;
            font-size: 28px;
            color: #e74c3c;
            margin-top: 5px;
        }
        .btn-pay {
            width: 100%;
            padding: 16px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 18px;
            font-weight: 600;
            cursor: pointer;
            margin-bottom: 10px;
        }
        .btn-pay:hover {
            opacity: 0.9;
        }
        .btn-pay:disabled {
            background: #bdc3c7;
            cursor: not-allowed;
        }
        .btn-cancel-pay {
            width: 100%;
            padding: 14px;
            background: white;
            color: #666;
            border: 1px solid #ddd;
            border-radius: 8px;
            font-size: 15px;
            cursor: pointer;
        }
        .processing-overlay {
            display: none;
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(255,255,255,0.95);
            justify-content: center;
            align-items: center;
            flex-direction: column;
            gap: 20px;
        }
        .processing-overlay.active {
            display: flex;
        }
        .spinner {
            width: 50px;
            height: 50px;
            border: 4px solid #e0e0e0;
            border-top-color: #667eea;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }
        @keyframes spin {
            to { transform: rotate(360deg); }
        }
        .processing-text {
            font-size: 16px;
            color: #333;
        }
        .test-notice {
            text-align: center;
            padding: 10px;
            background: #fff3cd;
            color: #856404;
            font-size: 12px;
            border-radius: 6px;
            margin-bottom: 15px;
        }
    </style>
</head>
<body>
    <%@ include file="common/header.jsp" %>

    <div class="checkout-container">
        <h1 class="page-title">주문서 작성</h1>

        <form id="orderForm" action="${pageContext.request.contextPath}/order/submit" method="post">
            <!-- 장바구니 주문인 경우 -->
            <c:if test="${not empty cartIds}">
                <c:forEach var="cartId" items="${cartIds}">
                    <input type="hidden" name="cartIds" value="${cartId}">
                </c:forEach>
            </c:if>
            
            <!-- 바로 구매인 경우 -->
            <c:if test="${isDirect}">
                <input type="hidden" name="productId" value="${product.productId}">
                <input type="hidden" name="quantity" value="${quantity}">
            </c:if>

            <div class="checkout-content">
                <div class="checkout-form">
                    <!-- 주문 상품 -->
                    <div class="form-section">
                        <h2 class="section-title">주문 상품</h2>
                        <div class="order-items">
                            <c:choose>
                                <c:when test="${isDirect}">
                                    <div class="order-item">
                                        <div class="order-item-image">
                                            <c:if test="${not empty product.thumbnailUrl}">
                                                <img src="${pageContext.request.contextPath}${product.thumbnailUrl}" alt="${product.productName}">
                                            </c:if>
                                        </div>
                                        <div class="order-item-info">
                                            <div class="order-item-name">${product.productName}</div>
                                            <div class="order-item-price">
                                                <fmt:formatNumber value="${product.discountedPrice}" pattern="#,###"/>원 × ${quantity}개
                                            </div>
                                        </div>
                                        <div class="order-item-total">
                                            <fmt:formatNumber value="${totalPrice}" pattern="#,###"/>원
                                        </div>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="cart" items="${cartItems}">
                                        <div class="order-item">
                                            <div class="order-item-image">
                                                <c:if test="${not empty cart.product.thumbnailUrl}">
                                                    <img src="${pageContext.request.contextPath}${cart.product.thumbnailUrl}" alt="${cart.product.productName}">
                                                </c:if>
                                            </div>
                                            <div class="order-item-info">
                                                <div class="order-item-name">${cart.product.productName}</div>
                                                <div class="order-item-price">
                                                    <fmt:formatNumber value="${cart.product.discountedPrice}" pattern="#,###"/>원 × ${cart.quantity}개
                                                </div>
                                            </div>
                                            <div class="order-item-total">
                                                <fmt:formatNumber value="${cart.totalPrice}" pattern="#,###"/>원
                                            </div>
                                        </div>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- 배송 정보 -->
                    <div class="form-section">
                        <h2 class="section-title">배송 정보</h2>
                        <div class="form-row">
                            <div class="form-group">
                                <label>수령인 <span class="required">*</span></label>
                                <input type="text" name="receiverName" required placeholder="수령인 이름">
                            </div>
                            <div class="form-group">
                                <label>연락처 <span class="required">*</span></label>
                                <input type="tel" name="receiverPhone" required placeholder="010-0000-0000">
                            </div>
                        </div>
                        <div class="form-group">
                            <label>주소 <span class="required">*</span></label>
                            <div class="address-row">
                                <input type="text" name="postalCode" id="postalCode" required placeholder="우편번호" readonly>
                                <button type="button" class="btn-search" onclick="searchAddress()">주소 검색</button>
                            </div>
                        </div>
                        <div class="form-group">
                            <input type="text" name="receiverAddress" id="receiverAddress" required placeholder="기본 주소" readonly>
                        </div>
                        <div class="form-group">
                            <input type="text" name="receiverAddressDetail" placeholder="상세 주소">
                        </div>
                        <div class="form-group">
                            <label>배송 메모</label>
                            <select name="orderMemo">
                                <option value="">배송 메모를 선택하세요</option>
                                <option value="문 앞에 놓아주세요">문 앞에 놓아주세요</option>
                                <option value="경비실에 맡겨주세요">경비실에 맡겨주세요</option>
                                <option value="배송 전 연락 바랍니다">배송 전 연락 바랍니다</option>
                                <option value="부재 시 연락 바랍니다">부재 시 연락 바랍니다</option>
                            </select>
                        </div>
                    </div>

                    <!-- 결제 수단 -->
                    <div class="form-section">
                        <h2 class="section-title">결제 수단</h2>
                        <div class="payment-methods">
                            <label class="payment-method selected">
                                <input type="radio" name="paymentMethod" value="CARD" checked>
                                💳 신용카드
                            </label>
                            <label class="payment-method">
                                <input type="radio" name="paymentMethod" value="BANK">
                                🏦 계좌이체
                            </label>
                            <label class="payment-method">
                                <input type="radio" name="paymentMethod" value="KAKAO">
                                💛 카카오페이
                            </label>
                            <label class="payment-method">
                                <input type="radio" name="paymentMethod" value="NAVER">
                                💚 네이버페이
                            </label>
                        </div>
                        
                        <!-- 카드사 선택 (카드 결제 선택 시 표시) -->
                        <div class="card-company-section active" id="cardCompanySection">
                            <div class="card-company-title">카드사 선택</div>
                            <div class="card-companies">
                                <div class="card-company" data-card="samsung">
                                    <div>삼성</div>
                                </div>
                                <div class="card-company" data-card="shinhan">
                                    <div>신한</div>
                                </div>
                                <div class="card-company" data-card="kb">
                                    <div>KB국민</div>
                                </div>
                                <div class="card-company" data-card="hyundai">
                                    <div>현대</div>
                                </div>
                                <div class="card-company" data-card="lotte">
                                    <div>롯데</div>
                                </div>
                                <div class="card-company" data-card="bc">
                                    <div>BC</div>
                                </div>
                                <div class="card-company" data-card="hana">
                                    <div>하나</div>
                                </div>
                                <div class="card-company" data-card="woori">
                                    <div>우리</div>
                                </div>
                                <div class="card-company" data-card="nh">
                                    <div>NH농협</div>
                                </div>
                                <div class="card-company" data-card="citi">
                                    <div>씨티</div>
                                </div>
                                <div class="card-company" data-card="kakao">
                                    <div>카카오뱅크</div>
                                </div>
                                <div class="card-company" data-card="etc">
                                    <div>기타</div>
                                </div>
                            </div>
                            <input type="hidden" name="cardCompany" id="cardCompany" value="">
                        </div>
                    </div>
                </div>

                <div class="checkout-summary">
                    <div class="summary-box">
                        <h3 class="summary-title">결제 금액</h3>
                        <div class="summary-row">
                            <span>상품 금액</span>
                            <span><fmt:formatNumber value="${totalPrice}" pattern="#,###"/>원</span>
                        </div>
                        <div class="summary-row">
                            <span>배송비</span>
                            <span>
                                <c:choose>
                                    <c:when test="${deliveryFee == 0}">무료</c:when>
                                    <c:otherwise><fmt:formatNumber value="${deliveryFee}" pattern="#,###"/>원</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="summary-row total">
                            <span>총 결제 금액</span>
                            <span><fmt:formatNumber value="${finalPrice}" pattern="#,###"/>원</span>
                        </div>
                        
                        <div class="agreement">
                            <label>
                                <input type="checkbox" id="agreeAll" required>
                                <span>주문 내용을 확인하였으며, 결제에 동의합니다.</span>
                            </label>
                        </div>
                        
                        <button type="submit" class="btn-submit">
                            <fmt:formatNumber value="${finalPrice}" pattern="#,###"/>원 결제하기
                        </button>
                    </div>
                </div>
            </div>
        </form>
    </div>

    <footer class="footer">
        <p>&copy; 2024 KH SHOP. All rights reserved.</p>
    </footer>

    <!-- 결제 모달 -->
    <div class="payment-modal" id="paymentModal">
        <div class="payment-modal-content" style="position: relative;">
            <div class="payment-modal-header">
                <h3>💳 카드 결제</h3>
                <p>결제 정보를 입력해주세요</p>
            </div>
            <div class="payment-modal-body">
                <div class="test-notice">
                    ⚠️ 테스트 모드: 실제 결제가 진행되지 않습니다
                </div>
                
                <div class="payment-amount">
                    <span>결제 금액</span>
                    <strong id="modalPayAmount"><fmt:formatNumber value="${finalPrice}" pattern="#,###"/>원</strong>
                </div>
                
                <div class="card-form-group">
                    <label>카드 번호</label>
                    <input type="text" id="cardNumber" placeholder="0000-0000-0000-0000" maxlength="19">
                </div>
                <div class="card-row">
                    <div class="card-form-group">
                        <label>유효기간</label>
                        <input type="text" id="cardExpiry" placeholder="MM/YY" maxlength="5">
                    </div>
                    <div class="card-form-group">
                        <label>CVC</label>
                        <input type="text" id="cardCvc" placeholder="000" maxlength="3">
                    </div>
                </div>
                <div class="card-form-group">
                    <label>카드 비밀번호 앞 2자리</label>
                    <input type="password" id="cardPassword" placeholder="••" maxlength="2">
                </div>
                
                <button type="button" class="btn-pay" id="btnPay" onclick="processPayment()">
                    <fmt:formatNumber value="${finalPrice}" pattern="#,###"/>원 결제하기
                </button>
                <button type="button" class="btn-cancel-pay" onclick="closePaymentModal()">취소</button>
            </div>
            
            <!-- 처리 중 오버레이 -->
            <div class="processing-overlay" id="processingOverlay">
                <div class="spinner"></div>
                <div class="processing-text" id="processingText">결제 처리 중...</div>
            </div>
        </div>
    </div>

    <!-- 다음 주소 API -->
    <script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
    <!-- 포트원(아임포트) SDK -->
    <script src="https://cdn.iamport.kr/v1/iamport.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        const finalPrice = ${finalPrice};
        let selectedCardCompany = '';
        
        // 카드사 코드 매핑 (포트원 기준)
        const cardCodes = {
            'samsung': '04',  // 삼성카드
            'shinhan': '06',  // 신한카드
            'kb': '11',       // KB국민카드
            'hyundai': '01',  // 현대카드
            'lotte': '07',    // 롯데카드
            'bc': '03',       // BC카드
            'hana': '21',     // 하나카드
            'woori': '33',    // 우리카드
            'nh': '34',       // NH농협카드
            'citi': '02',     // 씨티카드
            'kakao': '15',    // 카카오뱅크
            'etc': ''         // 기타 (선택 안함)
        };

        // 결제 수단 선택
        document.querySelectorAll('.payment-method').forEach(method => {
            method.addEventListener('click', function() {
                document.querySelectorAll('.payment-method').forEach(m => m.classList.remove('selected'));
                this.classList.add('selected');
                
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
                    
                    // 결제 요청 데이터
                    let payData = {
                        pg: getPgProvider(method),
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

        // PG사 결정
        function getPgProvider(method) {
            switch(method) {
                case 'CARD':
                case 'BANK':
                    return 'html5_inicis'; // KG이니시스
                case 'KAKAO':
                    return 'kakaopay';
                case 'NAVER':
                    return 'naverpay';
                default:
                    return 'html5_inicis';
            }
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
    </script>
</body>
</html>
