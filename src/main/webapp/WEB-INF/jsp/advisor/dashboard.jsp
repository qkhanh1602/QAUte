<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Bàn làm việc Cố vấn" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4">
    <!-- Thông báo nếu vừa bị chặn -->
    <c:if test="${param.denied}"><div class="alert alert-warning alert-dismissible fade show d-flex align-items-center gap-2 mb-4 rounded-3 shadow-sm border-warning" role="alert">
        <i class="bi bi-info-circle-fill text-warning fs-5 flex-shrink-0"></i>
        <span>Bạn đang đăng nhập bằng tài khoản <strong>Cán bộ / Cố vấn</strong>. Mọi tác vụ tư vấn trực tiếp của bạn được quản lý tại Bàn làm việc Cố vấn.</span>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>

    </c:if><!-- Advisor Header Card -->
    <div class="qaute-card dashboard-hero p-4 p-md-5 mb-4 text-white border-0 shadow-sm" style="border-radius: 1.5rem;">
        <div class="row align-items-center">
            <div class="col-md-8">
                <span class="badge bg-warning text-dark px-3 py-1 rounded-pill mb-2 fw-bold">
                    <i class="bi bi-briefcase me-1"></i> Bàn làm việc Cố vấn học tập
                </span>
                <h2 class="fw-bold mb-1"><c:out value="${'Kính chào, ' += advisor.taiKhoan.hoTen}" /></h2>
                <p class="text-white-50 mb-3 small">
                    <span><c:out value="${advisor.hocHamHocVi += ' - ' += advisor.chucVu}" /></span> |
                    Đơn vị: <strong class="text-white"><c:out value="${advisor.donVi.tenDonVi}" /></strong> |
                    Hạn ngạch tiếp nhận: <span class="badge bg-white bg-opacity-20 text-white"><c:out value="${advisor.hanNgachTiepNhan += ' câu hỏi'}" /></span>
                </p>
                <div class="d-flex flex-wrap gap-2">
                    <a class="btn btn-warning text-dark fw-bold btn-sm rounded-pill px-3 shadow-sm" href="${pageContext.request.contextPath}/advisor/tickets">
                        <i class="bi bi-inbox-fill me-1"></i>Hàng đợi tiếp nhận câu hỏi
                    </a>
                    <a class="btn btn-outline-light btn-sm rounded-pill px-3 fw-semibold" href="${pageContext.request.contextPath}/advisor/slots">
                        <i class="bi bi-calendar-plus me-1"></i>Mở khung giờ tư vấn
                    </a>
                    <a class="btn btn-outline-light btn-sm rounded-pill px-3 fw-semibold" href="${pageContext.request.contextPath}/advisor/appointments">
                        <i class="bi bi-calendar-check me-1"></i>Duyệt lịch hẹn sinh viên
                    </a>
                </div>
            </div>

            <div class="col-md-4 text-md-end mt-4 mt-md-0">
                <div class="d-flex gap-2 justify-content-md-end">
                    <div class="bg-white bg-opacity-10 p-3 rounded-4 text-center border border-white border-opacity-10" style="min-width: 130px;">
                        <small class="text-white-50 d-block mb-1">Đang phụ trách</small>
                        <span class="fs-2 fw-bold text-white"><c:out value="${fn:length(assignedTickets)}" /></span>
                    </div>
                    <div class="bg-white bg-opacity-10 p-3 rounded-4 text-center border border-white border-opacity-10" style="min-width: 130px;">
                        <small class="text-white-50 d-block mb-1">Hàng đợi khoa</small>
                        <span class="fs-2 fw-bold text-warning"><c:out value="${fn:length(queueTickets)}" /></span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Advisor Sub-nav -->


    <div class="row g-4">
        <!-- Assigned Tickets -->
        <div class="col-lg-7">
            <div class="qaute-card p-4 h-100 border-0 shadow-sm">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="fw-bold text-dark m-0"><i class="bi bi-person-check-fill text-primary me-2"></i>Câu hỏi đang được giao cho bạn</h5>
                    <a class="text-primary text-decoration-none small fw-semibold" href="${pageContext.request.contextPath}/advisor/tickets">Xem chi tiết</a>
                </div>

                <c:if test="${empty assignedTickets}"><div class="text-center py-5 text-muted">
                    <i class="bi bi-check2-circle fs-1 text-success mb-2"></i>
                    <p class="small mb-0">Bạn đã xử lý hết các câu hỏi được giao. Hãy kiểm tra hàng đợi để nhận câu hỏi mới!</p>
                </div>

                </c:if><c:if test="${not (empty assignedTickets)}"><div class="list-group list-group-flush">
                    <c:forEach var="t" items="${assignedTickets}"><div class="list-group-item px-0 py-3 border-bottom">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <span class="${'status-badge status-' += t.trangThai}"><c:out value="${q:statusLabel(t.trangThai)}" /></span>
                            <small class="text-muted"><i class="bi bi-alarm me-1"></i>Hạn SLA: <span><c:out value="${q:formatTemporal(t.hanSLA, 'dd/MM HH:mm')}" /></span></small>
                        </div>
                        <h6 class="fw-bold mb-1">
                            <a class="text-dark text-decoration-none hover-primary" href="${pageContext.request.contextPath}/advisor/tickets/${t.maYeuCau}"><c:out value="${t.tieuDe}" /></a>
                        </h6>
                        <div class="small text-muted d-flex gap-3">
                            <span><i class="bi bi-person me-1"></i>Sinh viên: <strong><c:out value="${t.baoMatDanhTinh ? 'Ẩn danh' : t.sinhVien.taiKhoan.hoTen}" /></strong></span>
                            <span><i class="bi bi-tag me-1"></i><span><c:out value="${t.linhVuc.tenLinhVuc}" /></span></span>
                        </div>
                    </div>
                </c:forEach></div>
            </c:if></div>
        </div>

        <!-- Department Queue & Today's Appointments -->
        <div class="col-lg-5">
            <!-- Queue -->
            <div class="qaute-card p-4 mb-4 border-0 shadow-sm">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="fw-bold text-dark m-0"><i class="bi bi-hourglass-split text-warning me-2"></i>Hàng đợi đơn vị cần tiếp nhận</h5>
                    <span class="badge bg-warning-subtle text-warning-emphasis fw-bold"><c:out value="${fn:length(queueTickets) += ' câu hỏi chờ'}" /></span>
                </div>

                <c:if test="${empty queueTickets}"><div class="text-center py-4 text-muted">
                    <i class="bi bi-inbox fs-2 text-secondary mb-1"></i>
                    <p class="small mb-0">Hàng đợi của đơn vị hiện đang trống.</p>
                </div>

                </c:if><c:if test="${not (empty queueTickets)}"><div class="d-flex flex-column gap-3">
                    <c:forEach var="q" items="${queueTickets}"><div class="p-3 bg-light rounded-3 border-start border-4 border-warning">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <span class="fw-bold text-dark small"><c:out value="${q.tieuDe}" /></span>
                            <small class="text-muted"><c:out value="${q:formatTemporal(q.ngayTao, 'dd/MM HH:mm')}" /></small>
                        </div>
                        <div class="small text-muted mb-2">
                            Lĩnh vực: <strong><c:out value="${q.linhVuc.tenLinhVuc}" /></strong>
                        </div>
                        <form method="post" action="${pageContext.request.contextPath}/advisor/tickets/${q.maYeuCau}/claim">
                            <sec:csrfInput /><button type="submit" class="btn btn-warning btn-sm rounded-pill fw-bold text-dark w-100">
                                <i class="bi bi-hand-index-thumb me-1"></i>Tiếp nhận xử lý
                            </button>
                        </form>
                    </div>
                </c:forEach></div>
            </c:if></div>

            <!-- Appointments summary -->
            <div class="qaute-card p-4 border-0 shadow-sm">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="fw-bold text-dark m-0"><i class="bi bi-calendar2-check text-success me-2"></i>Lịch hẹn gần đây</h5>
                    <a class="text-success text-decoration-none small fw-semibold" href="${pageContext.request.contextPath}/advisor/appointments">Quản lý</a>
                </div>

                <c:if test="${empty appointments}"><div class="text-center py-4 text-muted small">
                    Chưa có lịch hẹn nào.
                </div>

                </c:if><c:if test="${not (empty appointments)}"><div class="d-flex flex-column gap-2">
                    <c:forEach var="a" items="${appointments}"><div class="p-2 border-bottom small">
                        <div class="d-flex justify-content-between align-items-center">
                            <strong class="text-dark"><c:out value="${a.chuDe}" /></strong>
                            <span class="${'status-badge status-' += a.trangThai}"><c:out value="${q:appointmentStatusLabel(a.trangThai)}" /></span>
                        </div>
                        <div class="text-muted">
                            <i class="bi bi-clock me-1"></i><span><c:out value="${q:formatTemporal(a.khungGio.batDau, 'dd/MM HH:mm')}" /></span> - SV: <span><c:out value="${a.sinhVien.taiKhoan.hoTen}" /></span>
                        </div>
                    </div>
                </c:forEach></div>
            </c:if></div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
