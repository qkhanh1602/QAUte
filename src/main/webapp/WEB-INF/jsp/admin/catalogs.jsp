<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Quản lý Đơn vị & Lĩnh vực" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4">
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
        <div>
            <h2 class="fw-bold text-dark m-0">Quản Lý Danh Mục Đơn Vị &amp; Lĩnh Vực</h2>
            <p class="text-muted mb-0 small">Cơ cấu tổ chức phòng ban, khoa và các lĩnh vực tiếp nhận tư vấn</p>
        </div>
        <div class="d-flex gap-2">
            <button class="btn btn-outline-primary rounded-pill" data-bs-toggle="modal" data-bs-target="#unitModal"><i class="bi bi-building-add me-1"></i>Thêm đơn vị</button>
            <button class="btn btn-primary rounded-pill" data-bs-toggle="modal" data-bs-target="#fieldModal"><i class="bi bi-tag me-1"></i>Thêm lĩnh vực</button>
        </div>
    </div>

    <c:if test="${not empty successMessage}"><div class="alert alert-success"><c:out value="${successMessage}" /></div>
    </c:if><c:if test="${not empty errorMessage}"><div class="alert alert-danger"><c:out value="${errorMessage}" /></div>

    </c:if><!-- Admin Nav -->


    <div class="row g-4">
        <!-- Departments Table -->
        <div class="col-lg-5">
            <div class="qaute-card p-4 border-0 shadow-sm h-100">
                <h5 class="fw-bold text-dark mb-3"><i class="bi bi-building text-primary me-2"></i>Danh sách Đơn vị</h5>
                <div class="table-responsive">
                    <table class="table table-hover align-middle small">
                        <thead class="table-light">
                            <tr>
                                <th>Tên Đơn vị</th>
                                <th>Loại</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="d" items="${donViList}"><tr>
                                <td class="fw-bold"><c:out value="${d.tenDonVi}" /></td>
                                <td><span class="badge bg-secondary-subtle text-secondary"><c:out value="${q:unitTypeLabel(d.loaiDonVi)}" /></span></td>
                                <td>
                                    <c:if test="${d.hoatDong}"><span class="badge bg-success-subtle text-success">Đang hoạt động</span>
                                    </c:if><c:if test="${not (d.hoatDong)}"><span class="badge bg-danger-subtle text-danger">Tạm dừng</span>
                                </c:if></td>
                                <td>
                                    <button class="btn btn-light btn-sm border" data-bs-toggle="collapse" data-bs-target="${'#edit-unit-' += d.maDonVi}">Sửa</button>
                                    <form method="post" class="d-inline" action="${pageContext.request.contextPath}/admin/catalogs/units/${d.maDonVi}/toggle"><sec:csrfInput /><button class="btn btn-outline-warning btn-sm" type="submit"><c:out value="${d.hoatDong ? 'Ngưng' : 'Bật'}" /></button></form>
                                    <form class="collapse mt-2" method="post" style="min-width:240px" action="${pageContext.request.contextPath}/admin/catalogs/units/save" id="${'edit-unit-' += d.maDonVi}">
                                        <sec:csrfInput /><input type="hidden" name="id" value="${d.maDonVi}"><input type="hidden" name="code" value="${d.maDonViCode}"><input class="form-control form-control-sm mb-1" name="name" required value="${d.tenDonVi}">
                                        <select class="form-select form-select-sm mb-1" name="type">
                                            <option value="Khoa" ${d.loaiDonVi == 'Khoa' ? 'selected="selected"' : ''}>Khoa</option>
                                            <option value="PhongBan" ${d.loaiDonVi == 'PhongBan' ? 'selected="selected"' : ''}>Phòng ban</option>
                                            <option value="TrungTam" ${d.loaiDonVi == 'TrungTam' ? 'selected="selected"' : ''}>Trung tâm</option>
                                        </select>
                                        <input class="form-control form-control-sm mb-1" name="email" placeholder="Email" value="${d.email}"><input class="form-control form-control-sm mb-1" name="phone" placeholder="Số điện thoại" value="${d.soDienThoai}"><button class="btn btn-primary btn-sm w-100">Lưu</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach></tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Fields Table -->
        <div class="col-lg-7">
            <div class="qaute-card p-4 border-0 shadow-sm h-100">
                <h5 class="fw-bold text-dark mb-3"><i class="bi bi-tag text-info me-2"></i>Lĩnh vực Tư vấn</h5>
                <div class="table-responsive">
                    <table class="table table-hover align-middle small">
                        <thead class="table-light">
                            <tr>
                                <th>Tên lĩnh vực</th>
                                <th>Đơn vị phụ trách</th>
                                <th>Đặc thù</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="l" items="${linhVucList}"><tr>
                                <td class="fw-bold text-dark"><c:out value="${l.tenLinhVuc}" /></td>
                                <td><c:out value="${l.donVi.tenDonVi}" /></td>
                                <td>
                                    <c:if test="${l.nhayCam}"><span class="badge bg-warning-subtle text-warning-emphasis">Nhạy cảm / Bảo mật</span>
                                    </c:if><c:if test="${not (l.nhayCam)}"><span class="text-muted">Thông thường</span>
                                </c:if></td>
                                <td>
                                    <c:if test="${l.trangThai == 'HoatDong'}"><span class="badge bg-success-subtle text-success">Hoạt động</span>
                                    </c:if><c:if test="${not (l.trangThai == 'HoatDong')}"><span class="badge bg-secondary-subtle text-secondary">Tạm dừng</span>
                                </c:if></td>
                                <td>
                                    <button class="btn btn-light btn-sm border" data-bs-toggle="collapse" data-bs-target="${'#edit-field-' += l.maLinhVuc}">Sửa</button>
                                    <form method="post" class="d-inline" action="${pageContext.request.contextPath}/admin/catalogs/fields/${l.maLinhVuc}/toggle"><sec:csrfInput /><button class="btn btn-outline-warning btn-sm" type="submit"><c:out value="${l.trangThai == 'HoatDong' ? 'Ngưng' : 'Bật'}" /></button></form>
                                    <form class="collapse mt-2" method="post" style="min-width:260px" action="${pageContext.request.contextPath}/admin/catalogs/fields/save" id="${'edit-field-' += l.maLinhVuc}">
                                        <sec:csrfInput /><input type="hidden" name="id" value="${l.maLinhVuc}"><input type="hidden" name="code" value="${l.maLinhVucCode}"><input class="form-control form-control-sm mb-1" name="name" required value="${l.tenLinhVuc}"><textarea class="form-control form-control-sm mb-1" name="description" placeholder="Mô tả"><c:out value="${l.moTa}" /></textarea>
                                        <select class="form-select form-select-sm mb-1" name="unitId" required><c:forEach var="unit" items="${donViList}"><c:choose><c:when test="${unit.maDonVi == l.donVi.maDonVi}"><option value="${unit.maDonVi}" selected><c:out value="${unit.tenDonVi}" /></option></c:when><c:otherwise><option value="${unit.maDonVi}"><c:out value="${unit.tenDonVi}" /></option></c:otherwise></c:choose></c:forEach></select><label class="small"><c:choose><c:when test="${l.nhayCam}"><input type="checkbox" name="sensitive" value="true" checked></c:when><c:otherwise><input type="checkbox" name="sensitive" value="true"></c:otherwise></c:choose> Nội dung nhạy cảm</label><button class="btn btn-primary btn-sm w-100 mt-1">Lưu</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="unitModal" tabindex="-1"><div class="modal-dialog"><form class="modal-content" method="post" action="${pageContext.request.contextPath}/admin/catalogs/units/save"><sec:csrfInput /><div class="modal-header"><h5 class="modal-title">Thêm đơn vị</h5><button class="btn-close" data-bs-dismiss="modal" type="button"></button></div><div class="modal-body d-grid gap-2"><input type="hidden" name="code" value=""><input class="form-control" name="name" placeholder="Tên đơn vị" required><select class="form-select" name="type"><option value="Khoa">Khoa</option><option value="PhongBan">Phòng ban</option><option value="TrungTam">Trung tâm</option></select><input class="form-control" name="email" type="email" placeholder="Email"><input class="form-control" name="phone" placeholder="Số điện thoại"></div><div class="modal-footer"><button class="btn btn-primary">Lưu đơn vị</button></div></form></div></div>
<div class="modal fade" id="fieldModal" tabindex="-1"><div class="modal-dialog"><form class="modal-content" method="post" action="${pageContext.request.contextPath}/admin/catalogs/fields/save"><sec:csrfInput /><div class="modal-header"><h5 class="modal-title">Thêm lĩnh vực</h5><button class="btn-close" data-bs-dismiss="modal" type="button"></button></div><div class="modal-body d-grid gap-2"><input type="hidden" name="code" value=""><input class="form-control" name="name" placeholder="Tên lĩnh vực" required><textarea class="form-control" name="description" placeholder="Mô tả"></textarea><select class="form-select" name="unitId" required><option value="">Chọn đơn vị</option><c:forEach var="unit" items="${donViList}"><option value="${unit.maDonVi}"><c:out value="${unit.tenDonVi}" /></option></c:forEach></select><label><input type="checkbox" name="sensitive" value="true"> Nội dung nhạy cảm</label></div><div class="modal-footer"><button class="btn btn-primary">Lưu lĩnh vực</button></div></form></div></div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
