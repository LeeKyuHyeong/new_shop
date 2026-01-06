<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- 
    공통 페이징 컴포넌트
    필수 파라미터:
    - result: PageResponseDTO 객체
    - pageRequestDTO: PageRequestDTO 객체
    선택 파라미터:
    - theme: 테마 (admin/client, 기본값: client)
--%>

<c:set var="theme" value="${empty param.theme ? 'client' : param.theme}" />

<c:if test="${result.totalCount > 0}">
<div class="pagination-wrapper ${theme}-pagination" data-theme="${theme}">
    <div class="pagination-info">
        <span class="total-count">
            총 <strong>${result.totalCount}</strong>개
        </span>
        <c:if test="${theme eq 'admin'}">
            <span class="page-info">
                (${result.page} / ${result.totalPage} 페이지)
            </span>
        </c:if>
    </div>
    
    <nav class="pagination" aria-label="페이지 네비게이션">
        <ul class="pagination-list">
            <%-- 처음으로 --%>
            <li class="page-item ${result.page == 1 ? 'disabled' : ''}">
                <a class="page-link page-first" href="javascript:void(0);" 
                   data-page="1" aria-label="처음">
                    <span aria-hidden="true">«</span>
                </a>
            </li>
            
            <%-- 이전 그룹 --%>
            <li class="page-item ${!result.prev ? 'disabled' : ''}">
                <a class="page-link page-prev-group" href="javascript:void(0);" 
                   data-page="${result.start - 1}" aria-label="이전">
                    <span aria-hidden="true">‹</span>
                </a>
            </li>
            
            <%-- 페이지 번호 --%>
            <c:forEach var="pageNum" items="${result.pageList}">
                <li class="page-item ${pageNum == result.page ? 'active' : ''}">
                    <a class="page-link page-number" href="javascript:void(0);" 
                       data-page="${pageNum}">
                        ${pageNum}
                    </a>
                </li>
            </c:forEach>
            
            <%-- 다음 그룹 --%>
            <li class="page-item ${!result.next ? 'disabled' : ''}">
                <a class="page-link page-next-group" href="javascript:void(0);" 
                   data-page="${result.end + 1}" aria-label="다음">
                    <span aria-hidden="true">›</span>
                </a>
            </li>
            
            <%-- 마지막으로 --%>
            <li class="page-item ${result.page == result.totalPage ? 'disabled' : ''}">
                <a class="page-link page-last" href="javascript:void(0);" 
                   data-page="${result.totalPage}" aria-label="마지막">
                    <span aria-hidden="true">»</span>
                </a>
            </li>
        </ul>
    </nav>
    
    <%-- 페이지 직접 이동 (Admin용) --%>
    <c:if test="${theme eq 'admin' && result.totalPage > 10}">
        <div class="page-jump">
            <input type="number" class="page-jump-input" 
                   min="1" max="${result.totalPage}" 
                   placeholder="${result.page}"
                   aria-label="페이지 번호 입력">
            <button type="button" class="btn-page-jump">이동</button>
        </div>
    </c:if>
</div>

<%-- 페이징용 히든 폼 --%>
<form id="paginationForm" method="get">
    <input type="hidden" name="page" value="${pageRequestDTO.page}">
    <input type="hidden" name="size" value="${pageRequestDTO.size}">
    <c:if test="${not empty pageRequestDTO.searchType}">
        <input type="hidden" name="searchType" value="${pageRequestDTO.searchType}">
    </c:if>
    <c:if test="${not empty pageRequestDTO.searchKeyword}">
        <input type="hidden" name="searchKeyword" value="${pageRequestDTO.searchKeyword}">
    </c:if>
    <c:if test="${not empty pageRequestDTO.sortField}">
        <input type="hidden" name="sortField" value="${pageRequestDTO.sortField}">
    </c:if>
    <c:if test="${not empty pageRequestDTO.sortDirection}">
        <input type="hidden" name="sortDirection" value="${pageRequestDTO.sortDirection}">
    </c:if>
    <c:if test="${not empty pageRequestDTO.categoryId}">
        <input type="hidden" name="categoryId" value="${pageRequestDTO.categoryId}">
    </c:if>
</form>
</c:if>

<c:if test="${result.totalCount == 0}">
<div class="pagination-empty">
    <p>검색 결과가 없습니다.</p>
</div>
</c:if>
