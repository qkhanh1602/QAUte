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

    </c:if><!-- Role Filter & Search -->
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <div class="d-flex gap-2 flex-wrap">
            <a href="${pageContext.request.contextPath}/admin/users<c:if test='${not empty kw}'>?kw=${kw}</c:if>" class="${selectedRole == null ? 'btn btn-primary btn-sm rounded-pill px-3' : 'btn btn-outline-secondary btn-sm rounded-pill px-3'}">Tất cả</a>
            <a href="${pageContext.request.contextPath}/admin/users?role=SinhVien<c:if test='${not empty kw}'>&kw=${kw}</c:if>" class="${selectedRole == 'SinhVien' ? 'btn btn-primary btn-sm rounded-pill px-3' : 'btn btn-outline-secondary btn-sm rounded-pill px-3'}">Sinh viên</a>
            <a href="${pageContext.request.contextPath}/admin/users?role=CoVan<c:if test='${not empty kw}'>&kw=${kw}</c:if>" class="${selectedRole == 'CoVan' ? 'btn btn-primary btn-sm rounded-pill px-3' : 'btn btn-outline-secondary btn-sm rounded-pill px-3'}">Cố vấn học tập</a>
            <a href="${pageContext.request.contextPath}/admin/users?role=Admin<c:if test='${not empty kw}'>&kw=${kw}</c:if>" class="${selectedRole == 'Admin' ? 'btn btn-primary btn-sm rounded-pill px-3' : 'btn btn-outline-secondary btn-sm rounded-pill px-3'}">Quản trị viên</a>
        </div>
        <form method="get" action="${pageContext.request.contextPath}/admin/users" class="d-flex gap-2" style="max-width: 420px; width: 100%;">
            <c:if test="${not empty selectedRole}">
                <input type="hidden" name="role" value="${selectedRole}" />
            </c:if>
            <div class="input-group">
                <span class="input-group-text bg-white border-end-0"><i class="bi bi-search text-muted"></i></span>
                <input type="text" name="kw" value="<c:out value='${kw}'/>" class="form-control border-start-0 ps-0" placeholder="Tìm tên, username, email, SĐT..." />
                <c:if test="${not empty kw}">
                    <a href="${pageContext.request.contextPath}/admin/users<c:if test='${not empty selectedRole}'>?role=${selectedRole}</c:if>" class="btn btn-outline-secondary" title="Xóa tìm kiếm"><i class="bi bi-x"></i></a>
                </c:if>
                <button type="submit" class="btn btn-primary px-3">Tìm kiếm</button>
            </div>
        </form>
    </div>

    <!-- Users Table -->
    <div class="qaute-card p-4 border-0 shadow-sm">
        <div class="table-responsive">
            <table class="table table-hover align-middle">
                <thead class="table-light">
                    <tr>
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
                        <td><code class="fw-bold text-dark"><c:out value="${u.tenDangNhap}" /></code></td>
                        <td>
                            <div class="fw-semibold text-dark"><c:out value="${u.hoTen}" /></div>
                            <c:set var="st" value="${studentProfiles[u.maTK]}" />
                            <c:set var="advisor" value="${advisorProfiles[u.maTK]}" />
                            <c:if test="${st != null}"><div class="small text-muted mt-1">
                                <div><i class="bi bi-mortarboard me-1"></i><span>MSSV: <c:out value="${st.mssv}" /></span><c:if test="${not empty st.lop}"> · <span><c:out value="${st.lop}" /></span></c:if></div>
                                <c:if test="${not empty st.khoa}"><div><c:out value="${st.khoa}" /></div></c:if>
                            </div></c:if>
                            <c:if test="${advisor != null and (u.vaiTro == 'CoVan' or advisor.nhanTuVan)}"><div class="small text-muted mt-1">
                                <div><i class="bi bi-person-badge me-1"></i><span><c:out value="${advisor.donVi.tenDonVi}" /> (<c:out value="${advisor.chucVu}" />)</span></div>
                                <c:if test="${advisor.hocHamHocVi != null and not empty advisor.hocHamHocVi}"><div><c:out value="${advisor.hocHamHocVi}" /></div></c:if>
                                <div><c:out value="${advisor.chuyenMon}" /></div>
                            </div></c:if>
                        </td>
                        <td class="small">
                            <div><c:out value="${u.email}" /></div>
                            <small class="text-muted"><c:out value="${u.soDienThoai != null ? u.soDienThoai : '-'}" /></small>
                        </td>
                        <td>
                            <c:set var="hasStudentRole" value="${st != null or u.vaiTro == 'SinhVien'}" />
                            <c:set var="hasAdvisorRole" value="${advisor != null and (u.vaiTro == 'CoVan' or advisor.nhanTuVan)}" />
                            <c:choose>
                                <c:when test="${u.vaiTro == 'Admin'}">
                                    <span class="badge bg-danger-subtle text-danger border border-danger-subtle">Admin</span>
                                </c:when>
                                <c:when test="${hasStudentRole and hasAdvisorRole}">
                                    <div class="d-flex flex-column gap-1">
                                        <span class="badge bg-success-subtle text-success border border-success-subtle">Sinh viên</span>
                                        <span class="badge bg-primary-subtle text-primary border border-primary-subtle">Cố vấn</span>
                                    </div>
                                </c:when>
                                <c:when test="${hasAdvisorRole or u.vaiTro == 'CoVan'}">
                                    <span class="badge bg-primary-subtle text-primary border border-primary-subtle">Cố vấn</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-success-subtle text-success border border-success-subtle">Sinh viên</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
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
                        <select name="role" class="form-select">
                            <c:if test="${u.vaiTro == 'Admin'}"><option value="Admin" selected>Admin</option></c:if>
                            <option value="SinhVien" ${u.vaiTro == 'SinhVien' ? 'selected' : ''}>Sinh viên</option>
                            <option value="CoVan" ${u.vaiTro == 'CoVan' ? 'selected' : ''}>Cố vấn</option>
                        </select>
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

<%@ include file="/WEB-INF/jsp/fragments/admin-pagination.jspf" %>
<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
