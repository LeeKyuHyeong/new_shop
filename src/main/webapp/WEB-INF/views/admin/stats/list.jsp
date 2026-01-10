<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>통계 관리 - KH SHOP Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/stats.css">
</head>
<body>
    <div class="admin-container">
        <%@ include file="../common/sidebar.jsp" %>

        <main class="main-content">
            <header class="top-bar">
                <h1>통계 관리</h1>
                <div class="user-info">
                    <%= session.getAttribute("loggedInUser") %>님
                </div>
            </header>

            <div class="content">
            <div class="search-box">
                <div class="form-group">
                    <label for="startDate">시작일</label>
                    <input type="date" id="startDate" class="form-control">
                </div>
                <div class="form-group">
                    <label for="endDate">종료일</label>
                    <input type="date" id="endDate" class="form-control">
                </div>
                <button type="button" onclick="loadAllStats()" class="btn btn-primary">조회</button>
            </div>

            <div class="stats-container">
                <div class="stats-section">
                    <h2>카테고리별 판매 통계</h2>
                    <div class="table-responsive">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>카테고리</th>
                                    <th>판매 수량</th>
                                    <th>판매 금액</th>
                                </tr>
                            </thead>
                            <tbody id="categoryStatsBody">
                                <tr>
                                    <td colspan="3" class="text-center">조회 버튼을 클릭해주세요</td>
                                </tr>
                            </tbody>
                            <tfoot id="categoryStatsFooter" style="display:none;">
                                <tr>
                                    <th>합계</th>
                                    <th id="categoryTotalQty">0</th>
                                    <th id="categoryTotalAmount">0원</th>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </div>

                <div class="stats-section">
                    <h2>색상별 판매 통계</h2>
                    <div class="table-responsive">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>색상</th>
                                    <th>판매 수량</th>
                                    <th>판매 금액</th>
                                </tr>
                            </thead>
                            <tbody id="colorStatsBody">
                                <tr>
                                    <td colspan="3" class="text-center">조회 버튼을 클릭해주세요</td>
                                </tr>
                            </tbody>
                            <tfoot id="colorStatsFooter" style="display:none;">
                                <tr>
                                    <th>합계</th>
                                    <th id="colorTotalQty">0</th>
                                    <th id="colorTotalAmount">0원</th>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </div>

                <div class="stats-section">
                    <h2>사이즈별 판매 통계</h2>
                    <div class="table-responsive">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>사이즈</th>
                                    <th>판매 수량</th>
                                    <th>판매 금액</th>
                                </tr>
                            </thead>
                            <tbody id="sizeStatsBody">
                                <tr>
                                    <td colspan="3" class="text-center">조회 버튼을 클릭해주세요</td>
                                </tr>
                            </tbody>
                            <tfoot id="sizeStatsFooter" style="display:none;">
                                <tr>
                                    <th>합계</th>
                                    <th id="sizeTotalQty">0</th>
                                    <th id="sizeTotalAmount">0원</th>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </main>
    </div>

    <script src="${pageContext.request.contextPath}/js/common/theme.js"></script>
    <script src="${pageContext.request.contextPath}/js/admin/stats.js"></script>
</body>
</html>
