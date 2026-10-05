<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Quản trị Người dùng & Tài khoản" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4">
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
        <div>
            <h2 class="fw-bold text-dark m-0">Quản Lý Người Dùng &amp; Tài Khoản</h2>
            <p class="text-muted mb-0 small">Theo dõi danh sách tài khoản, phân quyền, khóa/mở tài khoản và đặt lại mật khẩu</p>
        </div>
        <div class="d-flex flex-wrap gap-2">
            <button class="btn btn-outline-primary rounded-pill" data-bs-toggle="modal" data-bs-target="#importStudentsModal"><i class="bi bi-file-earmark-excel me-1"></i>Nhập Excel</button>
            <button class="btn btn-outline-primary rounded-pill" data-bs-toggle="modal" data-bs-target="#createStaffModal"><i class="bi bi-person-badge me-1"></i>Thêm cố vấn/Admin</button>
            <button class="btn btn-primary rounded-pill" data-bs-toggle="modal" data-bs-target="#createStudentModal"><i class="bi bi-person-plus me-1"></i>Thêm sinh viên</button>
        </div>
    </div>

    <!-- Admin Nav -->


    <!-- Alerts -->
    <c:if test="${not empty successMessage}"><div class="alert alert-success py-2 small mb-3">
        <i class="bi bi-check-circle-fill me-1"></i><span><c:out value="${successMessage}" /></span>
    </div>
    </c:if><c:if test="${not empty errorMessage}"><div class="alert alert-danger py-2 small mb-3">
        <i class="bi bi-exclamation-triangle-fill me-1"></i><span><c:out value="${errorMessage}" /></span>
    </div>

    </c:if><!-- Role Filter -->
    <div class="d-flex gap-2 mb-4">
        <a href="${pageContext.request.contextPath}/admin/users" class="${selectedRole == null ? 'btn btn-primary btn-sm rounded-pill px-3' : 'btn btn-outline-secondary btn-sm rounded-pill px-3'}">Tất cả</a>
        <a href="${pageContext.request.contextPath}/admin/users?role=SinhVien" class="${selectedRole == 'SinhVien' ? 'btn btn-primary btn-sm rounded-pill px-3' : 'btn btn-outline-secondary btn-sm rounded-pill px-3'}">Sinh viên</a>
        <a href="${pageContext.request.contextPath}/admin/users?role=CoVan" class="${selectedRole == 'CoVan' ? 'btn btn-primary btn-sm rounded-pill px-3' : 'btn btn-outline-secondary btn-sm rounded-pill px-3'}">Cố vấn học tập</a>
        <a href="${pageContext.request.contextPath}/admin/users?role=Admin" class="${selectedRole == 'Admin' ? 'btn btn-primary btn-sm rounded-pill px-3' : 'btn btn-outline-secondary btn-sm rounded-pill px-3'}">Quản trị viên</a>
    </div>

    <!-- Users Table -->
    <div class="qaute-card p-4 border-0 shadow-sm">
        <div class="table-responsive">
            <table class="table table-hover align-middle">
                <thead class="table-light">
                    <tr>
                        <th>Mã</th>
                        <th>Tên đăng nhập</th>
                        <th>Họ và tên</th>
                        <th>Email &amp; SĐT</th>
                        <th>Vai trò</th>
                        <th>Trạng thái</th>
                        <th>Ngày tạo</th>
                        <th style="width: 220px;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="u" items="${users}"><tr>
                        <td><strong class="text-secondary"><c:out value="${'#' += u.maTK}" /></strong></td>
                        <td><code class="fw-bold text-dark"><c:out value="${u.tenDangNhap}" /></code></td>
                        <td>
                            <div class="fw-semibold text-dark"><c:out value="${u.hoTen}" /></div>
                            <c:if test="${u.vaiTro == 'CoVan' and advisorProfiles[u.maTK] != null}"><div class="small text-muted mt-1">
                                <c:set var="advisor" value="${advisorProfiles[u.maTK]}" /><div><i class="bi bi-person-badge me-1"></i><span><c:out value="${advisor.maCanBo}" /></span></div>
                                <div><i class="bi bi-building me-1"></i><span><c:out value="${advisor.donVi.tenDonVi}" /></span></div>
                                <c:if test="${advisor.hocHamHocVi != null and not empty advisor.hocHamHocVi}"><div><c:out value="${advisor.hocHamHocVi += ' · ' += advisor.chucVu}" /></div>
                                </c:if><div><c:out value="${advisor.chuyenMon}" /></div>
                            </div>
                        </c:if></td>
                        <td class="small">
                            <div><c:out value="${u.email}" /></div>
                            <small class="text-muted"><c:out value="${u.soDienThoai != null ? u.soDienThoai : '-'}" /></small>
                        </td>
                        <td>
                            <c:if test="${u.vaiTro == 'Admin'}"><span class="badge bg-danger-subtle text-danger border border-danger-subtle">Admin</span>
                            </c:if><c:if test="${u.vaiTro == 'CoVan'}"><span class="badge bg-primary-subtle text-primary border border-primary-subtle">Cố vấn</span>
                            </c:if><c:if test="${u.vaiTro == 'SinhVien'}"><span class="badge bg-success-subtle text-success border border-success-subtle">Sinh viên</span>
                        </c:if></td>
                        <td>
                            <c:if test="${u.trangThai == 'HoatDong'}"><span class="badge bg-success-subtle text-success">Hoạt động</span>
                            </c:if><c:if test="${u.trangThai == 'Khoa'}"><span class="badge bg-danger-subtle text-danger">Bị khóa</span>
                            </c:if><c:if test="${u.trangThai == 'ChoDuyet'}"><span class="badge bg-warning-subtle text-warning">Chờ duyệt</span>
                        </c:if></td>
                        <td class="small text-muted"><c:out value="${q:formatTemporal(u.ngayTao, 'dd/MM/yyyy')}" /></td>
                        <td>
                            <c:if test="${u.vaiTro == 'CoVan' and u.trangThai == 'ChoDuyet'}"><div class="d-flex gap-1 flex-wrap">
                                <form method="post" class="m-0" action="${pageContext.request.contextPath}/admin/users/${u.maTK}/approve-advisor">
                                    <sec:csrfInput /><button type="submit" class="btn btn-success btn-sm rounded-pill"><i class="bi bi-check2 me-1"></i>Phê duyệt</button>
                                </form>
                                <form method="post" class="m-0" onsubmit="return confirm('Từ chối hồ sơ cố vấn này?');" action="${pageContext.request.contextPath}/admin/users/${u.maTK}/reject-advisor">
                                    <sec:csrfInput /><button type="submit" class="btn btn-outline-danger btn-sm rounded-pill"><i class="bi bi-x-lg me-1"></i>Từ chối</button>
                                </form>
                            </div>
                            </c:if><c:if test="${not (u.vaiTro == 'CoVan' and u.trangThai == 'ChoDuyet')}"><div class="d-flex gap-1">
                                <c:if test="${u.vaiTro != 'Admin'}"><!-- Lock/Unlock Button -->
                                <form method="post" class="m-0" action="${pageContext.request.contextPath}/admin/users/${u.maTK}/toggle-status">
                                    <sec:csrfInput /><c:if test="${u.trangThai == 'HoatDong'}"><button type="submit" class="btn btn-outline-danger btn-sm rounded-pill" title="Khóa tài khoản">
                                        <i class="bi bi-lock me-1"></i>Khóa
                                    </button>
                                    </c:if><c:if test="${u.trangThai == 'Khoa'}"><button type="submit" class="btn btn-outline-success btn-sm rounded-pill" title="Mở khóa tài khoản">
                                        <i class="bi bi-unlock me-1"></i>Mở khóa
                                    </button>
                                </c:if></form>

                                </c:if><!-- Reset Password Button -->
                                <form method="post" class="m-0" onsubmit="return confirm('Tạo mật khẩu tạm thời mới cho tài khoản này?');" action="${pageContext.request.contextPath}/admin/users/${u.maTK}/reset-password">
                                    <sec:csrfInput /><button type="submit" class="btn btn-light btn-sm rounded-pill text-secondary border" title="Tạo mật khẩu tạm thời">
                                        <i class="bi bi-key me-1"></i>Reset MK
                                    </button>
                                </form>
                            </div>
                        </c:if>
                        <details class="mt-2"><summary class="text-primary">Sửa tài khoản / Phân vai trò</summary>
                        <form method="post" class="d-grid gap-2 mt-2" action="${pageContext.request.contextPath}/admin/users/${u.maTK}/edit">
                        <sec:csrfInput /><input name="fullName" class="form-control" value="<c:out value='${u.hoTen}'/>" maxlength="100" required>
                        <input type="email" name="email" class="form-control" value="<c:out value='${u.email}'/>" maxlength="254" required>
                        <input name="phone" class="form-control" value="<c:out value='${u.soDienThoai}'/>">
                        <select name="role" class="form-select"><option value="${u.vaiTro}"><c:out value="${u.vaiTro}" /></option><option value="SinhVien">Sinh viên</option><option value="CoVan">Cố vấn</option><option value="Admin">Admin</option></select>
                        <select name="unitId" class="form-select"><option value="">Đơn vị cố vấn</option><c:forEach var="unit" items="${units}"><option value="${unit.maDonVi}" ${advisorProfiles[u.maTK].donVi.maDonVi == unit.maDonVi ? 'selected' : ''}><c:out value="${unit.tenDonVi}" /></option></c:forEach></select>
                        <button class="btn btn-sm btn-primary">Lưu thay đổi</button></form></details>
                        </td>
                    </tr>
                </c:forEach></tbody>
            </table>
        </div>
    </div>
</div>

<div class="modal fade" id="createStudentModal" tabindex="-1"><div class="modal-dialog modal-lg"><form class="modal-content" method="post" action="${pageContext.request.contextPath}/admin/users/students/create"><sec:csrfInput /><div class="modal-header"><h5 class="modal-title">Tạo tài khoản sinh viên</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div><div class="modal-body"><div class="row g-3"><div class="col-md-6"><input class="form-control" name="username" placeholder="Tên đăng nhập" required></div><div class="col-md-6"><input class="form-control" type="password" name="password" placeholder="Mật khẩu tạm có chữ và số" minlength="8" required></div><div class="col-md-6"><input class="form-control" name="fullName" placeholder="Họ và tên" required></div><div class="col-md-6"><input class="form-control" type="email" name="email" placeholder="Email" required></div><div class="col-md-6"><input class="form-control" name="phone" placeholder="Số điện thoại"></div><div class="col-md-6"><input class="form-control" name="studentCode" placeholder="Mã sinh viên" required></div><div class="col-md-6"><input class="form-control" name="faculty" placeholder="Khoa" required></div><div class="col-md-6"><input class="form-control" name="major" placeholder="Ngành" required></div><div class="col-md-6"><input class="form-control" name="className" placeholder="Lớp" required></div><div class="col-md-6"><input class="form-control" name="course" placeholder="Khóa học" required></div></div></div><div class="modal-footer"><button class="btn btn-primary">Tạo tài khoản</button></div></form></div></div>
<div class="modal fade" id="createStaffModal" tabindex="-1"><div class="modal-dialog modal-lg"><form class="modal-content" method="post" action="${pageContext.request.contextPath}/admin/users/staff/create"><sec:csrfInput /><div class="modal-header"><h5 class="modal-title">Tạo tài khoản cố vấn</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div><div class="modal-body"><div class="row g-3"><div class="col-md-6"><select class="form-select" name="role" id="staffRole" onchange="document.getElementById('advisorFields').classList.toggle('d-none',this.value!=='CoVan')" required><option value="CoVan">Cố vấn</option></select></div><div class="col-md-6"><input class="form-control" name="username" placeholder="Tên đăng nhập" required></div><div class="col-md-6"><input class="form-control" type="password" name="password" minlength="8" placeholder="Mật khẩu tạm có chữ và số" required></div><div class="col-md-6"><input class="form-control" name="fullName" placeholder="Họ và tên" required></div><div class="col-md-6"><input class="form-control" name="email" type="email" placeholder="Email" required></div><div class="col-md-6"><input class="form-control" name="phone" placeholder="Số điện thoại"></div></div><div id="advisorFields" class="row g-3 mt-0"><div class="col-md-6"><input class="form-control" name="officerCode" placeholder="Mã cán bộ"></div><div class="col-md-6"><select class="form-select" name="unitId"><option value="">Chọn đơn vị</option><c:forEach var="unit" items="${units}"><option value="${unit.maDonVi}"><c:out value="${unit.tenDonVi}" /></option></c:forEach></select></div><div class="col-md-4"><input class="form-control" name="academicTitle" placeholder="Học hàm/học vị"></div><div class="col-md-4"><input class="form-control" name="position" placeholder="Chức vụ"></div><div class="col-md-4"><input class="form-control" name="expertise" placeholder="Chuyên môn"></div></div></div><div class="modal-footer"><button class="btn btn-primary">Tạo tài khoản</button></div></form></div></div>
<div class="modal fade" id="importStudentsModal" tabindex="-1"><div class="modal-dialog"><form class="modal-content" method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/admin/users/students/import"><sec:csrfInput /><div class="modal-header"><h5 class="modal-title">Nhập sinh viên từ Excel</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div><div class="modal-body"><p class="small text-muted">Dùng đúng 10 cột trong file mẫu. Nếu một dòng lỗi, toàn bộ đợt nhập sẽ được hoàn tác để tránh dữ liệu dở dang.</p><a class="btn btn-sm btn-outline-secondary mb-3" href="${pageContext.request.contextPath}/admin/users/students/import-template">Tải file mẫu</a><input class="form-control" type="file" name="file" accept=".xlsx" required></div><div class="modal-footer"><button class="btn btn-success"><i class="bi bi-upload me-1"></i>Nhập danh sách</button></div></form></div></div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
