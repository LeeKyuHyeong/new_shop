<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%@ include file="/WEB-INF/views/common/security-headers.jsp" %>
    <title>주문서 작성 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/client/checkout.css">
</head>
<body>
    <%@ include file="common/header.jsp" %>

    <div class="checkout-container">
        <h1 class="page-title">주문서 작성</h1>

        <noscript>
            <div style="background:#fdecea;color:#b71c1c;padding:12px;border-radius:6px;margin-bottom:16px;">
                결제를 진행하려면 JavaScript를 활성화해주세요.
            </div>
        </noscript>

        <form id="orderForm" method="post" onsubmit="return false;">
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
                <input type="hidden" name="color" value="${selectedColor}">
                <input type="hidden" name="size" value="${selectedSize}">
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
                                            <!-- 옵션 표시 -->
                                            <c:if test="${not empty selectedColor || not empty selectedSize}">
                                                <div class="order-item-option">
                                                    옵션: 
                                                    <c:if test="${not empty selectedColor}">${selectedColor}</c:if>
                                                    <c:if test="${not empty selectedColor && not empty selectedSize}"> / </c:if>
                                                    <c:if test="${not empty selectedSize}">${selectedSize}</c:if>
                                                </div>
                                            </c:if>
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
                                                <!-- 옵션 표시 -->
                                                <c:if test="${not empty cart.optionText}">
                                                    <div class="order-item-option">옵션: ${cart.optionText}</div>
                                                </c:if>
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
                                <div class="card-company" data-card="samsung"><div>삼성</div></div>
                                <div class="card-company" data-card="shinhan"><div>신한</div></div>
                                <div class="card-company" data-card="kb"><div>KB국민</div></div>
                                <div class="card-company" data-card="hyundai"><div>현대</div></div>
                                <div class="card-company" data-card="lotte"><div>롯데</div></div>
                                <div class="card-company" data-card="bc"><div>BC</div></div>
                                <div class="card-company" data-card="hana"><div>하나</div></div>
                                <div class="card-company" data-card="woori"><div>우리</div></div>
                                <div class="card-company" data-card="nh"><div>NH농협</div></div>
                                <div class="card-company" data-card="citi"><div>씨티</div></div>
                                <div class="card-company" data-card="kakao"><div>카카오뱅크</div></div>
                                <div class="card-company" data-card="etc"><div>기타</div></div>
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
                        
                        <div class="agree-section">
                            <div class="agree-item">
                                <input type="checkbox" id="agreeAll" required>
                                <span>주문 내용을 확인하였으며, 결제에 동의합니다.</span>
                            </div>
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

    <!-- 다음 주소 API -->
    <script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
    <!-- 포트원(아임포트) SDK -->
    <script src="https://cdn.iamport.kr/v1/iamport.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';
        const finalPrice = ${finalPrice};
    </script>
    <script src="${pageContext.request.contextPath}/js/client/checkout.js"></script>
</body>
</html>
