<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Hồ sơ cá nhân" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <h2 class="fw-bold text-dark mb-4">Hồ sơ cá nhân</h2>

            <c:if test="${successMessage}"><div class="alert alert-success py-2 small mb-3">
                <i class="bi bi-check-circle-fill me-1"></i><span><c:out value="${successMessage}" /></span>
            </div>
            </c:if><c:if test="${errorMessage}"><div class="alert alert-danger py-2 small mb-3">
                <i class="bi bi-exclamation-triangle-fill me-1"></i><span><c:out value="${errorMessage}" /></span>
            </div>

            </c:if><!-- Profile Info Card -->
            <div class="qaute-card p-4 mb-4 shadow-sm border-0">
                <div class="d-flex align-items-center gap-3 mb-4 pb-3 border-bottom">
                    <div class="user-avatar user-avatar-xl">
                        <c:if test="${user.avatarUrl != null and not empty user.avatarUrl}"><img alt="Ảnh đại diện" src="${user.avatarUrl}">
                        </c:if><c:if test="${not (user.avatarUrl != null and not empty user.avatarUrl)}"><span><c:out value="${user.avatarInitial}" /></span>
                    </c:if></div>
                    <div>
                        <h4 class="fw-bold mb-1 text-dark"><c:out value="${user.hoTen}" /></h4>
                        <span class="badge bg-primary-subtle text-primary border border-primary-subtle"><c:out value="${user.vaiTro}" /></span>
                        <span class="text-muted small ms-2"><i class="bi bi-calendar3 me-1"></i>Tham gia từ: <span><c:out value="${q:formatTemporal(user.ngayTao, 'dd/MM/yyyy')}" /></span></span>
                    </div>
                </div>

                <!-- If Student, show Student details (MSSV, Khoa, Lop) -->
                <c:if test="${sinhVien != null}"><div class="bg-light p-3 rounded-3 mb-4">
                    <h6 class="fw-bold text-dark mb-2">Thông tin học vụ sinh viên</h6>
                    <div class="row g-2 small">
                        <div class="col-md-6"><strong>MSSV:</strong> <span><c:out value="${sinhVien.mssv}" /></span></div>
                        <div class="col-md-6"><strong>Lớp:</strong> <span><c:out value="${sinhVien.lop}" /></span></div>
                        <div class="col-md-6"><strong>Khoa:</strong> <span><c:out value="${sinhVien.khoa}" /></span></div>
                        <div class="col-md-6"><strong>Ngành:</strong> <span><c:out value="${sinhVien.nganh}" /></span></div>
                    </div>
                </div>

                </c:if><form method="post" enctype="multipart/form-data" class="mb-4" action="${pageContext.request.contextPath}/profile/avatar">
                    <sec:csrfInput /><label class="form-label small fw-semibold">Ảnh đại diện</label>
                    <div class="d-flex gap-2 align-items-center">
                        <input type="file" name="avatar" class="form-control form-control-sm" accept="image/jpeg,image/png,image/webp" required>
                        <button type="submit" class="btn btn-outline-primary btn-sm text-nowrap">Tải ảnh lên</button>
                    </div>
                    <div class="form-text">JPG, PNG hoặc WEBP; tối đa 5 MB. Ảnh được lưu trên Cloudinary.</div>
                </form>

                <form method="post" action="${pageContext.request.contextPath}/profile/update">
                    <sec:csrfInput /><h6 class="fw-bold text-dark mb-3">Cập nhật thông tin liên lạc</h6>
                    <div class="row g-3 mb-3">
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold">Họ và tên</label>
                            <input type="text" name="hoTen" minlength="2" maxlength="100" class="form-control form-control-sm" required value="${user.hoTen}">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold">Số điện thoại</label>
                            <input type="text" name="soDienThoai" class="form-control form-control-sm" value="${user.soDienThoai}">
                        </div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Địa chỉ Email</label>
                            <input type="email" class="form-control form-control-sm" readonly value="${user.email}">
                            <div class="form-text">Email đăng ký chỉ được đổi qua quy trình xác minh OTP.</div>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary btn-sm px-4 fw-semibold rounded-pill">Lưu thông tin</button>
                </form>
            </div>

            <!-- Change Password Card -->
            <div class="qaute-card p-4 shadow-sm border-0">
                <h5 class="fw-bold text-dark mb-3">Đổi mật khẩu</h5>
                <form method="post" action="${pageContext.request.contextPath}/profile/change-password">
                    <sec:csrfInput /><div class="row g-3 mb-3">
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold">Mật khẩu hiện tại</label>
                            <input type="password" name="oldPassword" class="form-control form-control-sm" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold">Mật khẩu mới</label>
                            <input type="password" name="newPassword" class="form-control form-control-sm" minlength="8" maxlength="72" placeholder="8–72 ký tự, gồm chữ và số" required>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-outline-danger btn-sm px-4 fw-semibold rounded-pill">Đổi mật khẩu</button>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
