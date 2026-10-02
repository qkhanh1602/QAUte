<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Lịch sử tư vấn" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4">
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
        <div>
            <h2 class="fw-bold text-dark m-0">Lịch Sử Tư Vấn Tổng Hợp</h2>
            <p class="text-muted mb-0 small">Tổng hợp toàn bộ phiếu câu hỏi và buổi hẹn tư vấn của bạn</p>
        </div>
        <button type="button" class="btn btn-outline-secondary rounded-pill px-3 btn-sm" onclick="window.print()">
            <i class="bi bi-printer me-1"></i>In biên bản tổng hợp
        </button>
    </div>

    <!-- Student Nav -->


    <!-- Content Tabs -->
    <div class="qaute-card p-4 border-0 shadow-sm mb-4">
        <h5 class="fw-bold text-dark mb-3"><i class="bi bi-ticket-detailed-fill text-primary me-2"></i>1. Lịch sử Phiếu tư vấn</h5>
        <div class="table-responsive">
            <table class="table table-hover align-middle small">
                <thead class="table-light">
                    <tr>
                        <th>Mã</th>
                        <th>Tiêu đề</th>
                        <th>Đơn vị</th>
                        <th>Cố vấn</th>
                        <th>Ngày tạo</th>
                        <th>Trạng thái</th>
                        <th>Đánh giá</th>
                        <th>Chi tiết</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="t" items="${tickets}"><tr>
                        <td><strong><c:out value="${'#' += t.maYeuCau}" /></strong></td>
                        <td class="fw-bold"><c:out value="${t.tieuDe}" /></td>
                        <td><c:out value="${t.donVi.tenDonVi}" /></td>
                        <td><c:out value="${t.coVanPhuTrach != null ? t.coVanPhuTrach.taiKhoan.hoTen : 'Chưa phân công'}" /></td>
                        <td><c:out value="${q:formatTemporal(t.ngayTao, 'dd/MM/yyyy')}" /></td>
                        <td><span class="${'status-badge status-' += t.trangThai}"><c:out value="${t.trangThai}" /></span></td>
                        <td>
                            <c:if test="${t.danhGia != null}"><span class="text-warning fw-bold">
                                ★ <span><c:out value="${t.danhGia.soSao}" /></span>/5
                            </span>
                            </c:if><c:if test="${not (t.danhGia != null)}"><span class="text-muted">Chưa đánh giá</span>
                        </c:if></td>
                        <td>
                            <a class="btn btn-link btn-sm p-0 text-decoration-none" href="${pageContext.request.contextPath}/student/tickets/${t.maYeuCau}">Xem</a>
                        </td>
                    </tr>
                </c:forEach></tbody>
            </table>
        </div>
    </div>

    <div class="qaute-card p-4 border-0 shadow-sm">
        <h5 class="fw-bold text-dark mb-3"><i class="bi bi-calendar2-check-fill text-success me-2"></i>2. Lịch sử Buổi hẹn tư vấn</h5>
        <div class="table-responsive">
            <table class="table table-hover align-middle small">
                <thead class="table-light">
                    <tr>
                        <th>Chủ đề</th>
                        <th>Cố vấn</th>
                        <th>Thời gian</th>
                        <th>Địa điểm / Link</th>
                        <th>Trạng thái</th>
                        <th>Kết luận buổi tư vấn</th>
                        <th>Đánh giá</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="a" items="${appointments}"><tr>
                        <td class="fw-bold"><c:out value="${a.chuDe}" /></td>
                        <td><c:out value="${a.nguoiTuVan.taiKhoan.hoTen}" /></td>
                        <td><c:out value="${q:formatTemporal(a.khungGio.batDau, 'dd/MM/yyyy HH:mm')}" /></td>
                        <td><c:out value="${a.linkHopPhong != null ? a.linkHopPhong : a.khungGio.diaDiemLink}" /></td>
                        <td><span class="${'status-badge status-' += a.trangThai}"><c:out value="${a.trangThai}" /></span></td>
                        <td><c:out value="${a.ketLuan != null ? a.ketLuan : '-'}" /></td>
                        <td>
                            <c:if test="${a.danhGia != null}"><span class="text-warning fw-bold">
                                ★ <span><c:out value="${a.danhGia.soSao}" /></span>/5
                            </span>
                            </c:if><c:if test="${not (a.danhGia != null)}"><span class="text-muted">-</span>
                        </c:if></td>
                    </tr>
                </c:forEach></tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
