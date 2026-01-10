<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="activeMenu" value="profanity"/>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>비속어 관리 - KH Shop Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/profanity.css">
</head>
<body>
    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/common/sidebar.jsp"/>

        <main class="main-content">
            <header class="top-bar">
                <h1>비속어 관리</h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <!-- 통계 카드 -->
                <div class="stats-cards">
                    <div class="stat-card">
                        <div class="stat-icon">📝</div>
                        <div class="stat-info">
                            <div class="stat-value">${stats.totalCount}</div>
                            <div class="stat-label">전체 단어</div>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon">✅</div>
                        <div class="stat-info">
                            <div class="stat-value">${stats.activeCount}</div>
                            <div class="stat-label">활성화</div>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon">🔒</div>
                        <div class="stat-info">
                            <div class="stat-value">${stats.systemCount}</div>
                            <div class="stat-label">시스템 기본</div>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon">👤</div>
                        <div class="stat-info">
                            <div class="stat-value">${stats.userCount}</div>
                            <div class="stat-label">사용자 추가</div>
                        </div>
                    </div>
                </div>

                <!-- 액션 바 -->
                <div class="action-bar">
                    <div class="action-left">
                        <button class="btn btn-primary" onclick="openAddModal()">
                            ➕ 단어 추가
                        </button>
                        <button class="btn btn-secondary" onclick="openBulkAddModal()">
                            📋 일괄 추가
                        </button>
                        <button class="btn btn-danger" onclick="deleteSelected()" id="btnDeleteSelected" disabled>
                            🗑️ 선택 삭제
                        </button>
                    </div>
                    <div class="action-right">
                        <button class="btn btn-outline" onclick="refreshCache()">
                            🔄 캐시 갱신
                        </button>
                        <button class="btn btn-outline" onclick="openTestModal()">
                            🧪 테스트
                        </button>
                    </div>
                </div>

                <!-- 검색 필터 -->
                <div class="filter-box">
                    <form id="searchForm" method="get" action="${pageContext.request.contextPath}/admin/profanity">
                        <div class="filter-row">
                            <div class="filter-group">
                                <label>카테고리</label>
                                <select name="category" id="filterCategory">
                                    <option value="">전체</option>
                                    <c:forEach var="cat" items="${categories}">
                                        <option value="${cat}" ${category eq cat ? 'selected' : ''}>${cat}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="filter-group">
                                <label>검색어</label>
                                <input type="text" name="keyword" id="filterKeyword" 
                                       placeholder="단어 검색" value="${keyword}">
                            </div>
                            <div class="filter-buttons">
                                <button type="submit" class="btn btn-primary">검색</button>
                                <button type="button" class="btn btn-secondary" onclick="resetFilter()">초기화</button>
                            </div>
                        </div>
                    </form>
                </div>

                <!-- 목록 테이블 -->
                <div class="table-box">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th width="40">
                                    <input type="checkbox" id="checkAll" onchange="toggleCheckAll()">
                                </th>
                                <th width="60">번호</th>
                                <th>단어</th>
                                <th width="100">카테고리</th>
                                <th>설명</th>
                                <th width="80">상태</th>
                                <th width="80">유형</th>
                                <th width="120">등록일</th>
                                <th width="140">관리</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty words}">
                                    <tr>
                                        <td colspan="9" class="empty-message">
                                            <c:choose>
                                                <c:when test="${totalElements == 0}">
                                                    <div class="empty-state">
                                                        <p>등록된 비속어가 없습니다.</p>
                                                        <button class="btn btn-primary" onclick="initializeDefault()">
                                                            기본 비속어 초기화
                                                        </button>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    검색 결과가 없습니다.
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="word" items="${words}" varStatus="status">
                                        <tr data-id="${word.id}">
                                            <td data-label="선택">
                                                <input type="checkbox" class="row-check" value="${word.id}"
                                                       onchange="updateDeleteButton()"
                                                       ${word.isSystem ? 'disabled' : ''}>
                                            </td>
                                            <td data-label="번호">${totalElements - (currentPage * 20) - status.index}</td>
                                            <td class="word-cell" data-label="단어">
                                                <span class="word-text">${word.word}</span>
                                            </td>
                                            <td data-label="카테고리">
                                                <span class="category-badge category-${word.category}">
                                                    ${word.category != null ? word.category : '미분류'}
                                                </span>
                                            </td>
                                            <td class="desc-cell" data-label="설명">${word.description}</td>
                                            <td data-label="상태">
                                                <span class="status-badge ${word.isActive ? 'active' : 'inactive'}"
                                                      onclick="toggleStatus(${word.id})" style="cursor:pointer;">
                                                    ${word.isActive ? '활성' : '비활성'}
                                                </span>
                                            </td>
                                            <td data-label="유형">
                                                <span class="type-badge ${word.isSystem ? 'system' : 'user'}">
                                                    ${word.isSystem ? '시스템' : '사용자'}
                                                </span>
                                            </td>
                                            <td data-label="등록일">
                                                <fmt:parseDate value="${word.createdAt}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both"/>
                                                <fmt:formatDate value="${parsedDate}" pattern="yyyy-MM-dd"/>
                                            </td>
                                            <td class="action-cell">
                                                <div class="action-buttons">
                                                    <button class="btn-icon" onclick="openEditModal(${word.id}, '${word.word}', '${word.category}', '${word.description}', ${word.isActive})" title="수정">
                                                        ✏️
                                                    </button>
                                                    <c:if test="${!word.isSystem}">
                                                        <button class="btn-icon btn-icon-danger" onclick="deleteWord(${word.id})" title="삭제">
                                                            🗑️
                                                        </button>
                                                    </c:if>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>

                <!-- 페이징 -->
                <c:if test="${totalPages > 1}">
                    <div class="pagination">
                        <c:if test="${currentPage > 0}">
                            <a href="?page=0&category=${category}&keyword=${keyword}" class="page-link">«</a>
                            <a href="?page=${currentPage - 1}&category=${category}&keyword=${keyword}" class="page-link">‹</a>
                        </c:if>
                        
                        <c:forEach begin="${Math.max(0, currentPage - 2)}" end="${Math.min(totalPages - 1, currentPage + 2)}" var="i">
                            <a href="?page=${i}&category=${category}&keyword=${keyword}" 
                               class="page-link ${i == currentPage ? 'active' : ''}">${i + 1}</a>
                        </c:forEach>
                        
                        <c:if test="${currentPage < totalPages - 1}">
                            <a href="?page=${currentPage + 1}&category=${category}&keyword=${keyword}" class="page-link">›</a>
                            <a href="?page=${totalPages - 1}&category=${category}&keyword=${keyword}" class="page-link">»</a>
                        </c:if>
                    </div>
                </c:if>
            </div>
        </main>
    </div>

    <!-- 단어 추가/수정 모달 -->
    <div class="modal" id="wordModal">
        <div class="modal-content">
            <div class="modal-header">
                <h3 id="modalTitle">단어 추가</h3>
                <button class="modal-close" onclick="closeModal('wordModal')">&times;</button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="editWordId">
                <div class="form-group">
                    <label for="wordInput">단어 *</label>
                    <input type="text" id="wordInput" placeholder="비속어 입력">
                </div>
                <div class="form-group">
                    <label for="categoryInput">카테고리</label>
                    <select id="categoryInput">
                        <option value="">선택</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat}">${cat}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group">
                    <label for="descInput">설명</label>
                    <input type="text" id="descInput" placeholder="설명 (선택)">
                </div>
                <div class="form-group" id="activeGroup" style="display:none;">
                    <label>
                        <input type="checkbox" id="activeInput" checked>
                        활성화
                    </label>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" onclick="closeModal('wordModal')">취소</button>
                <button class="btn btn-primary" onclick="saveWord()">저장</button>
            </div>
        </div>
    </div>

    <!-- 일괄 추가 모달 -->
    <div class="modal" id="bulkAddModal">
        <div class="modal-content modal-lg">
            <div class="modal-header">
                <h3>일괄 추가</h3>
                <button class="modal-close" onclick="closeModal('bulkAddModal')">&times;</button>
            </div>
            <div class="modal-body">
                <div class="form-group">
                    <label for="bulkCategory">카테고리</label>
                    <select id="bulkCategory">
                        <option value="">선택</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat}">${cat}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group">
                    <label for="bulkWords">단어 목록 (줄바꿈 또는 쉼표로 구분)</label>
                    <textarea id="bulkWords" rows="10" placeholder="단어1&#10;단어2&#10;단어3&#10;또는&#10;단어1, 단어2, 단어3"></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" onclick="closeModal('bulkAddModal')">취소</button>
                <button class="btn btn-primary" onclick="saveBulkWords()">추가</button>
            </div>
        </div>
    </div>

    <!-- 테스트 모달 -->
    <div class="modal" id="testModal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>🧪 비속어 테스트</h3>
                <button class="modal-close" onclick="closeModal('testModal')">&times;</button>
            </div>
            <div class="modal-body">
                <div class="form-group">
                    <label for="testInput">테스트할 텍스트</label>
                    <textarea id="testInput" rows="4" placeholder="비속어 포함 여부를 확인할 텍스트를 입력하세요."></textarea>
                </div>
                <div id="testResult" class="test-result" style="display:none;"></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-secondary" onclick="closeModal('testModal')">닫기</button>
                <button class="btn btn-primary" onclick="runTest()">검사</button>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
    <script src="${pageContext.request.contextPath}/js/admin/profanity.js"></script>
    <script>
        const contextPath = '${pageContext.request.contextPath}';
    </script>
</body>
</html>
