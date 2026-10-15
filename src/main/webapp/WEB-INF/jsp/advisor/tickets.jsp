<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Quản lý câu hỏi diễn đàn & Hàng đợi" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4">
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
        <div>
            <h2 class="fw-bold text-dark m-0">Câu Hỏi Cần Xử Lý &amp; Hàng Đợi Khoa/Phòng</h2>
            <p class="text-muted mb-0 small">Ưu tiên câu hỏi cần trả lời có hạn SLA sớm nhất. SLA tính 48 giờ liên tục từ lúc sinh viên gửi câu hỏi.</p>
        </div>
    </div>

    <!-- Advisor Nav -->


    <!-- Alerts -->
    <c:if test="${not empty successMessage}"><div class="alert alert-success py-2 small mb-3">
        <i class="bi bi-check-circle-fill me-1"></i><span><c:out value="${successMessage}" /></span>
    </div>
    </c:if><c:if test="${not empty errorMessage}"><div class="alert alert-danger py-2 small mb-3">
        <i class="bi bi-exclamation-triangle-fill me-1"></i><span><c:out value="${errorMessage}" /></span>
    </div>

    </c:if><!-- Tabs -->
    <ul class="nav nav-pills mb-4 gap-2" id="advisorTicketTab" role="tablist">
        <li class="nav-item">
            <button class="nav-link active rounded-pill px-4 fw-bold" id="assigned-tab" data-bs-toggle="tab" data-bs-target="#assigned-tickets" type="button">
                <i class="bi bi-person-check-fill me-1"></i>Câu hỏi đang phụ trách (<span><c:out value="${fn:length(assignedTickets)}" /></span>)
            </button>
        </li>
        <li class="nav-item">
            <button class="nav-link rounded-pill px-4 fw-bold" id="queue-tab" data-bs-toggle="tab" data-bs-target="#queue-tickets" type="button">
                <i class="bi bi-inbox-fill me-1"></i>Hàng đợi chưa tiếp nhận (<span><c:out value="${fn:length(queueTickets)}" /></span>)
            </button>
        </li>
        <li class="nav-item"><button class="nav-link rounded-pill px-4 fw-bold" data-bs-toggle="tab" data-bs-target="#closed-tickets" type="button">Đã đóng (<c:out value="${fn:length(closedTickets)}" />)</button></li>
    </ul>

    <div class="tab-content" id="advisorTicketTabContent">
        <div class="tab-pane fade" id="closed-tickets" role="tabpanel">
            <div class="qaute-card p-4 border-0 shadow-sm">
                <c:if test="${empty closedTickets}"><p class="text-muted mb-0">Chưa có câu hỏi đã đóng.</p></c:if>
                <c:forEach var="t" items="${closedTickets}"><div class="d-flex justify-content-between align-items-center gap-3 py-3 border-bottom">
                    <div><strong><c:out value="${t.tieuDe}" /></strong><div class="small text-muted">Đã đóng · <c:out value="${q:formatTemporal(t.ngayDong, 'dd/MM/yyyy HH:mm')}" /></div></div>
                    <a class="btn btn-outline-primary btn-sm" href="${pageContext.request.contextPath}/advisor/tickets/${t.maYeuCau}">Xem lại</a>
                </div></c:forEach>
            </div>
        </div>
        <!-- TAB 1: Assigned Tickets -->
        <div class="tab-pane fade show active" id="assigned-tickets" role="tabpanel">
            <div class="qaute-card p-4 border-0 shadow-sm">
                <c:if test="${empty assignedTickets}"><div class="text-center py-5 text-muted">
                    <i class="bi bi-inbox fs-1 text-secondary mb-2"></i>
                    <h5>Bạn chưa có câu hỏi diễn đàn nào cần xử lý</h5>
                    <p class="small text-muted mb-0">Chuyển sang tab "Hàng đợi chưa tiếp nhận" để nhận thêm câu hỏi từ sinh viên.</p>
                </div>

                </c:if><c:if test="${not (empty assignedTickets)}"><div class="table-responsive">
                    <table class="table table-hover align-middle">
                        <thead class="table-light">
                            <tr>
                                <th>Tiêu đề yêu cầu</th>
                                <th>Sinh viên</th>
                                <th>Lĩnh vực</th>
                                <th>Ngày tiếp nhận</th>
                                <th>Hạn SLA</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="t" items="${assignedTickets}"><tr>
                                <td>
                                    <a class="fw-bold text-dark text-decoration-none hover-primary d-block" href="${pageContext.request.contextPath}/advisor/tickets/${t.maYeuCau}"><c:out value="${t.tieuDe}" /></a>
                                    <small class="text-muted">Ngày gửi: <c:out value="${q:formatTemporal(t.ngayTao, 'dd/MM/yyyy HH:mm')}" /></small>
                                </td>
                                <td>
                                    <c:if test="${t.baoMatDanhTinh}"><span class="badge bg-secondary-subtle text-secondary">
                                        <i class="bi bi-shield-lock me-1"></i>Ẩn danh
                                    </span>
                                    </c:if><c:if test="${not (t.baoMatDanhTinh)}"><div>
                                        <div class="fw-semibold small"><c:out value="${t.sinhVien.taiKhoan.hoTen}" /></div>
                                        <small class="text-muted"><c:out value="${t.sinhVien.mssv}" /></small>
                                    </div>
                                </c:if></td>
                                <td class="small"><c:out value="${t.linhVuc.tenLinhVuc}" /></td>
                                <td class="small text-muted"><c:out value="${t.ngayTiepNhan != null ? q:formatTemporal(t.ngayTiepNhan, 'dd/MM HH:mm') : '-'}" /></td>
                                <td>
                                    <small class="text-danger fw-semibold"><c:out value="${q:formatTemporal(t.hanSLA, 'dd/MM HH:mm')}" /></small>
                                </td>
                                <td>
                                    <span class="${'status-badge status-' += t.trangThai}"><c:out value="${q:statusLabel(t.trangThai)}" /></span>
                                </td>
                                <td>
                                    <a class="btn btn-primary btn-sm rounded-pill px-3 fw-semibold" href="${pageContext.request.contextPath}/advisor/tickets/${t.maYeuCau}">
                                        Trả lời
                                    </a>
                                </td>
                            </tr>
                        </c:forEach></tbody>
                    </table>
                </div>
            </c:if></div>
        </div>

        <!-- TAB 2: Queue Tickets -->
        <div class="tab-pane fade" id="queue-tickets" role="tabpanel">
            <div class="qaute-card p-4 border-0 shadow-sm">
                <c:if test="${empty queueTickets}"><div class="text-center py-5 text-muted">
                    <i class="bi bi-check-circle fs-1 text-success mb-2"></i>
                    <h5>Hàng đợi của đơn vị hiện không có câu hỏi mới</h5>
                    <p class="small text-muted mb-0">Tất cả các câu hỏi của sinh viên đã được tiếp nhận xử lý.</p>
                </div>

                </c:if><c:if test="${not (empty queueTickets)}"><div class="table-responsive">
                    <table class="table table-hover align-middle">
                        <thead class="table-light">
                            <tr>
                                <th>Tiêu đề yêu cầu</th>
                                <th>Lĩnh vực</th>
                                <th>Thời gian gửi</th>
                                <th>Hạn tiếp nhận (48h)</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="q" items="${queueTickets}"><tr>
                                <td class="fw-bold text-dark"><c:out value="${q.tieuDe}" /></td>
                                <td class="small"><c:out value="${q.linhVuc.tenLinhVuc}" /></td>
                                <td class="small text-muted"><c:out value="${q:formatTemporal(q.ngayTao, 'dd/MM/yyyy HH:mm')}" /></td>
                                <td>
                                    <span class="badge bg-warning-subtle text-warning-emphasis"><c:out value="${q:formatTemporal(q.hanTiepNhan, 'dd/MM HH:mm')}" /></span>
                                </td>
                                <td>
                                    <form method="post" class="m-0" action="${pageContext.request.contextPath}/advisor/tickets/${q.maYeuCau}/claim">
                                        <sec:csrfInput /><button type="submit" class="btn btn-warning btn-sm rounded-pill px-4 fw-bold text-dark">
                                            <i class="bi bi-hand-index-thumb me-1"></i>Tiếp nhận
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach></tbody>
                    </table>
                </div>
            </c:if></div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
