<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html><html><head><c:set var="pageTitle" value="Tra cứu quản trị"/><%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %></head><body>
<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<main class="container py-4"><h2>Tra cứu quản trị</h2><p class="text-muted">Lọc câu hỏi diễn đàn, lịch hẹn và nhật ký bảo mật theo nhiều tiêu chí.</p>
<c:if test="${not empty errorMessage}"><div class="alert alert-danger"><c:out value="${errorMessage}"/></div></c:if>
<form method="get" class="qaute-card p-3 row g-3 mb-4">
<div class="col-md-3"><label class="form-label">Dữ liệu</label><select name="type" class="form-select"><option value="tickets" ${type=='tickets'?'selected':''}>Câu hỏi diễn đàn</option><option value="appointments" ${type=='appointments'?'selected':''}>Lịch hẹn</option><option value="logs" ${type=='logs'?'selected':''}>Nhật ký bảo mật</option></select></div>
<div class="col-md-3"><label class="form-label">Từ khóa</label><input name="keyword" class="form-control" value="<c:out value='${param.keyword}'/>" maxlength="100"></div>
<div class="col-md-3"><label class="form-label">Trạng thái / Loại sự kiện</label><input name="status" class="form-control" value="<c:out value='${param.status}'/>" maxlength="50"></div>
<div class="col-md-3"><label class="form-label">Đơn vị</label><select name="unitId" class="form-select"><option value="">Tất cả</option><c:forEach var="u" items="${units}"><option value="${u.maDonVi}" ${param.unitId==u.maDonVi?'selected':''}><c:out value="${u.tenDonVi}"/></option></c:forEach></select></div>
<div class="col-md-3"><label class="form-label">Sinh viên / người thực hiện</label><select name="accountId" class="form-select"><option value="">Tất cả</option><c:forEach var="account" items="${searchAccounts}"><option value="${account.maTK}" ${param.accountId == account.maTK ? 'selected' : ''}><c:out value="${account.hoTen}" /> (<c:out value="${account.tenDangNhap}" />)</option></c:forEach></select></div>
<div class="col-md-3"><label class="form-label">Từ ngày</label><input type="date" name="from" class="form-control" value="<c:out value='${param.from}'/>"></div>
<div class="col-md-3"><label class="form-label">Đến ngày</label><input type="date" name="to" class="form-control" value="<c:out value='${param.to}'/>"></div>
<div class="col-md-3 d-flex align-items-end"><button class="btn btn-primary">Tìm kiếm</button></div>
<c:if test="${not empty results}"><div class="col-12 d-flex gap-2 align-items-center"><span><c:out value="${results.totalElements}"/> kết quả · Trang <c:out value="${results.number+1}"/> / <c:out value="${results.totalPages}"/></span><c:if test="${results.number>0}"><button class="btn btn-sm btn-outline-primary" name="page" value="${results.number-1}">Trang trước</button></c:if><c:if test="${results.hasNext()}"><button class="btn btn-sm btn-outline-primary" name="page" value="${results.number+1}">Trang sau</button></c:if></div></c:if>
</form><div class="table-responsive qaute-card p-3"><table class="table"><thead><tr><th>Nội dung</th><th>Trạng thái / Loại</th><th>Ngày tạo</th></tr></thead><tbody>
<c:forEach var="row" items="${results.content}"><tr>
<td><c:choose><c:when test="${type=='tickets'}"><c:out value="${row.tieuDe}"/></c:when><c:when test="${type=='appointments'}"><c:out value="${row.chuDe}"/></c:when><c:otherwise><c:out value="${row.chiTiet}"/></c:otherwise></c:choose></td><td><c:out value="${type=='logs'?row.loaiSuKien:q:statusLabel(row.trangThai)}"/></td><td><c:out value="${q:formatTemporal(row.ngayTao,'dd/MM/yyyy HH:mm')}"/></td></tr></c:forEach>
</tbody></table></div></main><%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %></body></html>
