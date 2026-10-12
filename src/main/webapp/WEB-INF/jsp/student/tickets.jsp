<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Câu hỏi của tôi" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4">
    <!-- Header -->
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
        <div>
            <h2 class="fw-bold text-dark m-0">Câu hỏi của tôi</h2>
            <p class="text-muted mb-0 small">Theo dõi tiến độ giải quyết câu hỏi và trao đổi cùng cố vấn</p>
        </div>
        <a class="btn btn-qaute-primary rounded-pill px-4" href="${pageContext.request.contextPath}/forum/ask">
            <i class="bi bi-plus-circle-fill me-2"></i>Đặt câu hỏi
        </a>
    </div>

    <!-- Student Nav -->


    <!-- Tickets Table -->
    <div class="qaute-card p-4 border-0 shadow-sm">
        <c:if test="${empty tickets}"><div class="text-center py-5 text-muted">
            <i class="bi bi-inbox fs-1 mb-2 text-secondary"></i>
            <h5>Bạn chưa có câu hỏi nào</h5>
            <p class="small text-muted mb-3">Mọi thắc mắc của bạn sẽ được tiếp nhận và giải quyết theo quy trình SLA chuẩn.</p>
            <a class="btn btn-primary btn-sm rounded-pill px-4" href="${pageContext.request.contextPath}/forum/ask">Gửi câu hỏi ngay</a>
        </div>

        </c:if><c:if test="${not (empty tickets)}"><div class="table-responsive">
            <table class="table table-hover align-middle">
                <thead class="table-light">
                    <tr>
                        <th style="width: 80px;">Mã</th>
                        <th>Tiêu đề yêu cầu</th>
                        <th>Đơn vị &amp; Lĩnh vực</th>
                        <th>Cố vấn xử lý</th>
                        <th>Thời gian tạo</th>
                        <th>Hạn SLA</th>
                        <th>Trạng thái</th>
                        <th style="width: 100px;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="t" items="${tickets}"><tr>
                        <td><strong class="text-secondary"><c:out value="${'#' += t.maYeuCau}" /></strong></td>
                        <td>
                            <a class="fw-bold text-dark text-decoration-none hover-primary d-block" href="${pageContext.request.contextPath}/forum/${t.maYeuCau}"><c:out value="${t.tieuDe}" /></a>
                            <c:if test="${t.baoMatDanhTinh}"><small class="badge bg-secondary-subtle text-secondary" style="font-size: 0.7rem;">
                                <i class="bi bi-shield-lock me-1"></i>Ẩn danh
                            </small>
                        </c:if></td>
                        <td>
                            <div class="fw-semibold small"><c:out value="${t.donVi.tenDonVi}" /></div>
                            <small class="text-muted"><c:out value="${t.linhVuc.tenLinhVuc}" /></small>
                        </td>
                        <td>
                            <c:if test="${t.coVanPhuTrach != null}"><span class="fw-semibold small text-primary"><c:out value="${t.coVanPhuTrach.taiKhoan.hoTen}" /></span>
                            </c:if><c:if test="${not (t.coVanPhuTrach != null)}"><span class="badge bg-warning-subtle text-warning-emphasis">Chờ tiếp nhận</span>
                        </c:if></td>
                        <td class="small text-muted"><c:out value="${q:formatTemporal(t.ngayTao, 'dd/MM/yyyy HH:mm')}" /></td>
                        <td>
                            <c:if test="${t.trangThai != 'Dong'}"><small class="text-muted"><c:out value="${q:formatTemporal(t.hanSLA, 'dd/MM HH:mm')}" /></small>
                            </c:if><c:if test="${t.trangThai == 'Dong'}"><small class="text-success fw-semibold">Đã hoàn thành</small>
                        </c:if></td>
                        <td>
                            <span class="${'status-badge status-' += t.trangThai}"><c:out value="${t.trangThai}" /></span>
                        </td>
                        <td>
                            <a class="btn btn-outline-primary btn-sm rounded-pill px-3" href="${pageContext.request.contextPath}/forum/${t.maYeuCau}">
                                Chi tiết
                            </a>
                        </td>
                    </tr>
                </c:forEach></tbody>
            </table>
        </div>
    </c:if></div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
