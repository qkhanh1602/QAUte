<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Điều phối phiếu tư vấn" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <div><h2 class="fw-bold mb-1">Điều phối phiếu tư vấn</h2><p class="text-muted mb-0">Phân công phiếu mới cho cố vấn đúng đơn vị và còn hạn ngạch.</p></div>
        <a class="btn btn-outline-secondary rounded-pill" href="${pageContext.request.contextPath}/admin/dashboard">Quay lại tổng quan</a>
    </div>
    <c:if test="${successMessage}"><div class="alert alert-success"><c:out value="${successMessage}" /></div>
    </c:if><c:if test="${errorMessage}"><div class="alert alert-danger"><c:out value="${errorMessage}" /></div>
    </c:if><div class="qaute-card p-4 shadow-sm border-0">
        <div class="table-responsive">
            <table class="table align-middle">
                <thead><tr><th>Phiếu</th><th>Đơn vị</th><th>Trạng thái</th><th>Cố vấn</th><th>Điều phối</th></tr></thead>
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
                </tr>
                </c:forEach><c:if test="${empty tickets}"><tr><td colspan="5" class="text-center text-muted py-4">Chưa có phiếu tư vấn.</td></tr>
                </c:if></tbody>
            </table>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>
</body>
</html>
