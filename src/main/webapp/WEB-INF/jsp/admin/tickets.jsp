<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Điều phối câu hỏi diễn đàn" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4"><a class="btn btn-outline-primary mb-3" href="${pageContext.request.contextPath}/admin/search">Tra cứu câu hỏi, lịch hẹn, nhật ký</a>
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div><h2 class="fw-bold mb-1">Điều phối câu hỏi diễn đàn</h2><p class="text-muted mb-0">Phân công cố vấn, ẩn hoặc khôi phục câu hỏi trên diễn đàn. Lịch sử trao đổi vẫn được giữ lại.</p></div>
        <a class="btn btn-outline-secondary rounded-pill" href="${pageContext.request.contextPath}/admin/dashboard">Quay lại tổng quan</a>
    </div>
    <c:if test="${not empty successMessage}"><div class="alert alert-success"><c:out value="${successMessage}" /></div>
    </c:if><c:if test="${not empty errorMessage}"><div class="alert alert-danger"><c:out value="${errorMessage}" /></div>
    </c:if><div class="qaute-card p-4 shadow-sm border-0">
        <div class="table-responsive">
            <table class="table align-middle">
                <thead><tr><th>Câu hỏi</th><th>Đơn vị</th><th>Trạng thái</th><th>Cố vấn</th><th>Điều phối</th><th>Hiển thị diễn đàn</th></tr></thead>
                <tbody>
                <c:forEach var="ticket" items="${tickets}"><tr>
                    <td><strong><c:out value="${'#' += ticket.maYeuCau += ' · ' += ticket.tieuDe}" /></strong><div class="small text-muted"><c:out value="${q:formatTemporal(ticket.ngayTao, 'dd/MM/yyyy HH:mm')}" /></div></td>
                    <td><c:out value="${ticket.donVi.tenDonVi}" /></td>
                    <td><span class="badge bg-secondary-subtle text-secondary"><c:out value="${ticket.trangThai}" /></span></td>
                    <td><c:out value="${ticket.coVanPhuTrach != null ? ticket.coVanPhuTrach.taiKhoan.hoTen : 'Chưa phân công'}" /></td>
                    <td>
                        <c:if test="${ticket.trangThai == 'ChoTiepNhan'}"><form method="post" class="d-flex flex-column gap-2" style="min-width:260px" action="${pageContext.request.contextPath}/admin/tickets/${ticket.maYeuCau}/assign">
                            <sec:csrfInput /><select name="advisorId" class="form-select form-select-sm" required>
                                <option value="">Chọn cố vấn cùng đơn vị</option>
                                <c:forEach var="advisor" items="${advisors}"><c:if test="${advisor.donVi.maDonVi == ticket.donVi.maDonVi and advisor.nhanTuVan and advisor.taiKhoan.trangThai == 'HoatDong'}"><option value="${advisor.maCV}"><c:out value="${advisor.taiKhoan.hoTen += ' · ' += advisor.maCanBo}" /></option>
                            </c:if></c:forEach></select>
                            <input name="note" class="form-control form-control-sm" maxlength="500" placeholder="Ghi chú phân công">
                            <button class="btn btn-primary btn-sm rounded-pill" type="submit">Phân công</button>
                        </form>
                        </c:if><c:if test="${not (ticket.trangThai == 'ChoTiepNhan')}"><span class="small text-muted">Đã xử lý điều phối</span>
                    </c:if></td>
                    <td style="min-width:240px">
                        <c:choose>
                            <c:when test="${forumVisibility[ticket.maYeuCau] eq true}">
                                <span class="badge bg-success mb-2">Đang công khai</span>
                                <form method="post" action="${pageContext.request.contextPath}/admin/tickets/${ticket.maYeuCau}/visibility" class="d-flex flex-column gap-2">
                                    <sec:csrfInput /><input type="hidden" name="visible" value="false">
                                    <label class="visually-hidden" for="hideReason${ticket.maYeuCau}">Lý do ẩn câu hỏi</label>
                                    <input id="hideReason${ticket.maYeuCau}" name="reason" class="form-control form-control-sm" maxlength="500" required placeholder="Lý do ẩn câu hỏi">
                                    <button class="btn btn-outline-danger btn-sm" type="submit"><i class="bi bi-eye-slash me-1"></i>Ẩn câu hỏi</button>
                                </form>
                            </c:when>
                            <c:when test="${forumVisibility[ticket.maYeuCau] eq false}">
                                <span class="badge bg-secondary mb-2">Đã ẩn</span>
                                <form method="post" action="${pageContext.request.contextPath}/admin/tickets/${ticket.maYeuCau}/visibility">
                                    <sec:csrfInput /><input type="hidden" name="visible" value="true">
                                    <button class="btn btn-outline-success btn-sm" type="submit"><i class="bi bi-eye me-1"></i>Khôi phục câu hỏi</button>
                                </form>
                            </c:when>
                            <c:otherwise><span class="text-muted small">Chưa đăng lên diễn đàn</span></c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                </c:forEach><c:if test="${empty tickets}"><tr><td colspan="6" class="text-center text-muted py-4">Chưa có câu hỏi diễn đàn.</td></tr>
                </c:if></tbody>
            </table>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/fragments/admin-pagination.jspf" %>
<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>
</body>
</html>
