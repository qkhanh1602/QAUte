<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Cổng Sinh Viên - Tổng quan" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4">
    <!-- Thông báo nếu vừa bị chặn -->
    <c:if test="${param.denied}"><div class="alert alert-warning alert-dismissible fade show d-flex align-items-center gap-2 mb-4 rounded-3 shadow-sm border-warning" role="alert">
        <i class="bi bi-shield-exclamation text-warning fs-5 flex-shrink-0"></i>
        <span>Bạn không có quyền truy cập vào các chức năng của Cán bộ hoặc Quản trị viên. Hệ thống đã đưa bạn về Cổng Sinh viên.</span>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>

    </c:if><!-- Student Header Banner -->
    <div class="qaute-card dashboard-hero p-4 p-md-5 mb-4 text-white border-0 shadow-sm" style="border-radius: 1.5rem;">
        <div class="row align-items-center">
            <div class="col-md-8">
                <span class="badge bg-white bg-opacity-20 text-white border border-white border-opacity-25 px-3 py-1 rounded-pill mb-2">
                    <i class="bi bi-mortarboard me-1"></i> Cổng thông tin Sinh viên
                </span>
                <h2 class="fw-bold mb-1"><c:out value="${'Xin chào, ' += student.taiKhoan.hoTen += '!'}" /></h2>
                <p class="text-white-50 mb-3 small">
                    MSSV: <strong class="text-white"><c:out value="${student.mssv}" /></strong> |
                    Lớp: <span class="text-white"><c:out value="${student.lop}" /></span> |
                    Ngành: <span class="text-white"><c:out value="${student.nganh}" /></span>
                </p>
                <div class="d-flex flex-wrap gap-2">
                    <a class="btn btn-warning text-dark fw-bold btn-sm rounded-pill px-3 shadow-sm" href="${pageContext.request.contextPath}/student/tickets/new">
                        <i class="bi bi-plus-circle-fill me-1"></i>Gửi câu hỏi tư vấn mới
                    </a>
                    <a class="btn btn-outline-light btn-sm rounded-pill px-3 fw-semibold" href="${pageContext.request.contextPath}/student/appointments">
                        <i class="bi bi-calendar-plus me-1"></i>Đặt lịch hẹn gặp Cố vấn
                    </a>
                    <a class="btn btn-outline-light btn-sm rounded-pill px-3 fw-semibold" href="${pageContext.request.contextPath}/student/chat">
                        <i class="bi bi-chat-dots me-1"></i>Vào phòng chat 1-1
                    </a>
                </div>
            </div>
            <div class="col-md-4 text-md-end mt-4 mt-md-0">
                <div class="bg-white bg-opacity-10 p-3 rounded-4 text-center border border-white border-opacity-10 d-inline-block" style="min-width: 200px;">
                    <small class="text-white-50 d-block mb-1">Phiếu đang xử lý</small>
                    <span class="fs-1 fw-extrabold text-white"><c:out value="${processingTicketsCount}" /></span>
                    <div class="text-white-50 small mt-1">Hạn xử lý chuẩn SLA 48h</div>
                </div>
            </div>
        </div>
    </div>

    <!-- Student Navigation Sub-bar -->


    <div class="row g-4">
        <!-- Recent Tickets -->
        <div class="col-lg-7">
            <div class="qaute-card p-4 h-100 border-0 shadow-sm">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="fw-bold text-dark m-0"><i class="bi bi-chat-left-text text-primary me-2"></i>Phiếu tư vấn gần đây</h5>
                    <a class="text-primary text-decoration-none small fw-semibold" href="${pageContext.request.contextPath}/student/tickets">Xem tất cả</a>
                </div>

                <c:if test="${empty recentTickets}"><div class="text-center py-5 text-muted">
                    <i class="bi bi-chat-square-text fs-1 mb-2 text-secondary"></i>
                    <p class="small mb-2">Bạn chưa gửi phiếu tư vấn nào.</p>
                    <a class="btn btn-outline-primary btn-sm rounded-pill" href="${pageContext.request.contextPath}/student/tickets/new">Gửi câu hỏi ngay</a>
                </div>

                </c:if><c:if test="${not (empty recentTickets)}"><div class="list-group list-group-flush">
                    <c:forEach var="t" items="${recentTickets}"><div class="list-group-item px-0 py-3 border-bottom">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <span class="${'status-badge status-' += t.trangThai}"><c:out value="${t.trangThai}" /></span>
                            <small class="text-muted"><i class="bi bi-clock me-1"></i><span><c:out value="${q:formatTemporal(t.ngayTao, 'dd/MM/yyyy HH:mm')}" /></span></small>
                        </div>
                        <h6 class="fw-bold mb-1">
                            <a class="text-dark text-decoration-none hover-primary" href="${pageContext.request.contextPath}/student/tickets/${t.maYeuCau}"><c:out value="${t.tieuDe}" /></a>
                        </h6>
                        <div class="small text-muted d-flex gap-3">
                            <span><i class="bi bi-building me-1"></i><span><c:out value="${t.donVi.tenDonVi}" /></span></span>
                            <span><i class="bi bi-person me-1"></i><span><c:out value="${t.coVanPhuTrach != null ? t.coVanPhuTrach.taiKhoan.hoTen : 'Đang chờ phân công'}" /></span></span>
                        </div>
                    </div>
                </c:forEach></div>
            </c:if></div>
        </div>

        <!-- Upcoming Appointments & Notifications -->
        <div class="col-lg-5">
            <!-- Appointments -->
            <div class="qaute-card p-4 mb-4 border-0 shadow-sm">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="fw-bold text-dark m-0"><i class="bi bi-calendar-event text-success me-2"></i>Lịch hẹn của bạn</h5>
                    <a class="text-success text-decoration-none small fw-semibold" href="${pageContext.request.contextPath}/student/appointments">Quản lý</a>
                </div>

                <c:if test="${empty recentAppointments}"><div class="text-center py-4 text-muted">
                    <i class="bi bi-calendar-x fs-2 text-secondary mb-1"></i>
                    <p class="small mb-0">Chưa có lịch hẹn nào sắp tới.</p>
                </div>

                </c:if><c:if test="${not (empty recentAppointments)}"><div class="d-flex flex-column gap-3">
                    <c:forEach var="a" items="${recentAppointments}"><div class="p-3 bg-light rounded-3 border-start border-4 border-success">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <span class="fw-bold text-dark"><c:out value="${a.chuDe}" /></span>
                            <span class="${'status-badge status-' += a.trangThai}"><c:out value="${a.trangThai}" /></span>
                        </div>
                        <div class="small text-secondary mb-1">
                            <i class="bi bi-person-badge me-1"></i><span><c:out value="${a.nguoiTuVan.taiKhoan.hoTen}" /></span>
                        </div>
                        <div class="small text-muted">
                            <i class="bi bi-clock me-1"></i><span><c:out value="${q:formatTemporal(a.khungGio.batDau, 'dd/MM/yyyy HH:mm') += ' - ' += q:formatTemporal(a.khungGio.ketThuc, 'HH:mm')}" /></span>
                        </div>
                        <c:if test="${a.linkHopPhong != null}"><div class="mt-2 pt-2 border-top small text-primary fw-semibold">
                            <i class="bi bi-geo-alt me-1"></i><span><c:out value="${a.linkHopPhong}" /></span>
                        </div>
                    </c:if></div>
                </c:forEach></div>
            </c:if></div>

            <!-- Notifications list -->
            <div class="qaute-card p-4 border-0 shadow-sm">
                <h5 class="fw-bold text-dark mb-3"><i class="bi bi-bell text-warning me-2"></i>Thông báo mới nhất</h5>
                <c:if test="${empty notifications}"><div class="text-muted small text-center py-3">
                    Không có thông báo mới nào.
                </div>
                </c:if><c:if test="${not (empty notifications)}"><div class="d-flex flex-column gap-2">
                    <c:forEach var="n" items="${notifications}"><div class="p-2 border-bottom small">
                        <div class="fw-bold text-dark"><c:out value="${n.tieuDe}" /></div>
                        <div class="text-secondary"><c:out value="${n.noiDung}" /></div>
                        <div class="text-muted" style="font-size: 0.75rem;"><c:out value="${q:formatTemporal(n.ngayTao, 'dd/MM/yyyy HH:mm')}" /></div>
                    </div>
                </c:forEach></div>
            </c:if></div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
