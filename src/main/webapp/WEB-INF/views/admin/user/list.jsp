<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="activeMenu" value="user"/>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>사용자 관리 - KH Shop</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/user.css">
</head>
<body>
    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/common/sidebar.jsp"/>

        <main class="main-content">
            <header class="top-bar">
                <h1>사용자 관리</h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
                <div class="alert-container"></div>

                <!-- 검색 필터 -->
                <div class="search-box">
                    <form id="searchForm" method="get" action="${pageContext.request.contextPath}/admin/user">
                        <div class="search-row">
                            <div class="search-group">
                                <label for="userId">아이디</label>
                                <input type="text" id="userId" name="userId" value="${searchDTO.userId}" placeholder="아이디 검색">
                            </div>
                            <div class="search-group">
                                <label for="userName">이름</label>
                                <input type="text" id="userName" name="userName" value="${searchDTO.userName}" placeholder="이름 검색">
                            </div>
                            <div class="search-group">
                                <label for="email">이메일</label>
                                <input type="text" id="email" name="email" value="${searchDTO.email}" placeholder="이메일 검색">
                            </div>
                            <div class="search-group">
                                <label for="gender">성별</label>
                                <select id="gender" name="gender">
                                    <option value="">전체</option>
                                    <option value="M" <c:if test="${searchDTO.gender eq 'M'}">selected</c:if>>남</option>
                                    <option value="F" <c:if test="${searchDTO.gender eq 'F'}">selected</c:if>>여</option>
                                </select>
                            </div>
                        </div>
                        <div class="search-row">
                            <div class="search-group">
                                <label for="userRole">권한</label>
                                <select id="userRole" name="userRole">
                                    <option value="">전체</option>
                                    <option value="ADMIN" <c:if test="${searchDTO.userRole eq 'ADMIN'}">selected</c:if>>관리자</option>
                                    <option value="USER" <c:if test="${searchDTO.userRole eq 'USER'}">selected</c:if>>일반</option>
                                </select>
                            </div>
                            <div class="search-group">
                                <label for="useYn">상태</label>
                                <select id="useYn" name="useYn">
                                    <option value="">전체</option>
                                    <option value="Y" <c:if test="${searchDTO.useYn eq 'Y'}">selected</c:if>>활성</option>
                                    <option value="N" <c:if test="${searchDTO.useYn eq 'N'}">selected</c:if>>비활성</option>
                                </select>
                            </div>
                            <div class="search-group">
                                <label for="startDate">가입일(시작)</label>
                                <input type="date" id="startDate" name="startDate" value="${searchDTO.startDate}">
                            </div>
                            <div class="search-group">
                                <label for="endDate">가입일(종료)</label>
                                <input type="date" id="endDate" name="endDate" value="${searchDTO.endDate}">
                            </div>
                        </div>
                        <div class="search-buttons">
                            <button type="submit" class="btn btn-primary">검색</button>
                            <button type="button" class="btn btn-secondary" onclick="resetSearch()">초기화</button>
                        </div>
                    </form>
                </div>

                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>아이디</th>
                                <th>이름</th>
                                <th>이메일</th>
                                <th>성별</th>
                                <th>권한</th>
                                <th>상태</th>
                                <th>가입일</th>
                                <th>관리</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="user" items="${users}">
                                <tr data-user-id="${user.userId}">
                                    <td data-label="아이디">
                                        <a href="${pageContext.request.contextPath}/admin/user/detail/${user.userId}" class="user-id-link">
                                            ${user.userId}
                                        </a>
                                    </td>
                                    <td data-label="이름">${user.userName}</td>
                                    <td data-label="이메일">${user.email}</td>
                                    <td data-label="성별">
                                        <c:if test="${user.gender eq 'M'}">남</c:if>
                                        <c:if test="${user.gender eq 'F'}">여</c:if>
                                    </td>
                                    <td data-label="권한">
                                        <c:if test="${user.userRole eq 'ADMIN'}">
                                            <span class="badge badge-admin">관리자</span>
                                        </c:if>
                                        <c:if test="${user.userRole eq 'USER'}">
                                            <span class="badge badge-user">일반</span>
                                        </c:if>
                                    </td>
                                    <td data-label="상태">
                                        <c:if test="${user.useYn eq 'Y'}">
                                            <span class="badge badge-active">활성</span>
                                        </c:if>
                                        <c:if test="${user.useYn eq 'N'}">
                                            <span class="badge badge-inactive">비활성</span>
                                        </c:if>
                                    </td>
                                    <td data-label="가입일">
                                        <fmt:parseDate value="${user.createdDate}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both"/>
                                        <fmt:formatDate value="${parsedDate}" pattern="yyyy-MM-dd"/>
                                    </td>
                                    <td class="action-cell">
                                        <a href="${pageContext.request.contextPath}/admin/user/detail/${user.userId}" class="btn btn-small btn-info">상세</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                    <c:if test="${empty users}">
                        <div class="empty-message">등록된 사용자가 없습니다.</div>
                    </c:if>
                </div>
            </div>
        </main>
    </div>

    <script>
        const contextPath = '${pageContext.request.contextPath}';

        function resetSearch() {
            document.getElementById('userId').value = '';
            document.getElementById('userName').value = '';
            document.getElementById('email').value = '';
            document.getElementById('gender').value = '';
            document.getElementById('userRole').value = '';
            document.getElementById('useYn').value = '';
            document.getElementById('startDate').value = '';
            document.getElementById('endDate').value = '';
            window.location.href = contextPath + '/admin/user';
        }
    </script>
    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
</body>
</html>
