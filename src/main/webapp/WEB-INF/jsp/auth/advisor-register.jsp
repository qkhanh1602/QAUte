<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Đăng ký tài khoản Cố vấn" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body class="auth-page py-5">
<div class="container">
    <div class="row justify-content-center">
        <div class="col-lg-8 col-xl-7">
            <div class="text-center mb-4">
                <a class="fs-2 fw-bold text-decoration-none text-dark" href="${pageContext.request.contextPath}/">QAUTE</a>
                <p class="text-muted small mb-0">Đăng ký dành cho giảng viên và cán bộ tư vấn</p>
            </div>

            <div class="qaute-card p-4 p-md-5 shadow-lg border-0">
                <h1 class="h4 text-center mb-2">Đăng ký làm cố vấn</h1>
                <p class="text-secondary text-center small mb-4">Email sẽ được xác thực bằng OTP. Admin cần kiểm tra hồ sơ trước khi tài khoản được phép đăng nhập.</p>

                <c:if test="${errorMessage}"><div class="alert alert-danger py-2 small">
                    <i class="bi bi-exclamation-triangle-fill me-1"></i><span><c:out value="${errorMessage}" /></span>
                </div>

                </c:if><form method="post" action="${pageContext.request.contextPath}/advisor-register">
                    <sec:csrfInput /><h2 class="h6 text-primary border-bottom pb-2 mb-3">1. Tài khoản và liên hệ</h2>
                    <div class="row g-3 mb-3">
                        <div class="col-md-6"><label class="form-label small fw-semibold">Tên đăng nhập *</label><input name="tenDangNhap" class="form-control" maxlength="50" required></div>
                        <div class="col-md-6"><label class="form-label small fw-semibold">Mật khẩu *</label><input type="password" name="matKhau" class="form-control" minlength="8" maxlength="72" placeholder="8–72 ký tự, gồm chữ và số" required></div>
                        <div class="col-md-6"><label class="form-label small fw-semibold">Họ và tên *</label><input name="hoTen" class="form-control" maxlength="100" required></div>
                        <div class="col-md-6"><label class="form-label small fw-semibold">Email công vụ *</label><input type="email" name="email" class="form-control" maxlength="254" required></div>
                        <div class="col-md-6"><label class="form-label small fw-semibold">Số điện thoại</label><input name="soDienThoai" class="form-control" maxlength="15"></div>
                        <div class="col-md-6"><label class="form-label small fw-semibold">Mã cán bộ *</label><input name="maCanBo" class="form-control" maxlength="30" required></div>
                    </div>

                    <h2 class="h6 text-primary border-bottom pb-2 mb-3 pt-2">2. Hồ sơ chuyên môn</h2>
                    <div class="row g-3 mb-3">
                        <div class="col-md-6"><label class="form-label small fw-semibold">Học hàm / học vị</label><input name="hocHamHocVi" class="form-control" maxlength="50" placeholder="Ví dụ: Thạc sĩ, Tiến sĩ"></div>
                        <div class="col-md-6"><label class="form-label small fw-semibold">Chức vụ *</label><input name="chucVu" class="form-control" maxlength="100" required></div>
                        <div class="col-12">
                            <label class="form-label small fw-semibold">Khoa / phòng / trung tâm *</label>
                            <select name="maDonVi" class="form-select" required>
                                <option value="" selected disabled>-- Chọn đơn vị công tác --</option>
                                <c:forEach var="dv" items="${donViList}"><option value="${dv.maDonVi}"><c:out value="${dv.tenDonVi}" /></option>
                            </c:forEach></select>
                        </div>
                        <div class="col-12"><label class="form-label small fw-semibold">Chuyên môn tư vấn *</label><textarea name="chuyenMon" class="form-control" rows="3" maxlength="255" placeholder="Các lĩnh vực có thể hỗ trợ sinh viên" required></textarea></div>
                    </div>

                    <div class="alert alert-info small d-flex gap-2">
                        <i class="bi bi-shield-check fs-5"></i>
                        <span>Sau khi xác thực OTP, hồ sơ chuyển tới Admin. Bạn chỉ đăng nhập được sau khi hồ sơ được phê duyệt.</span>
                    </div>
                    <button class="btn btn-primary w-100 py-2 fw-bold" type="submit"><i class="bi bi-send-check me-1"></i>Gửi hồ sơ và nhận OTP</button>
                    <div class="text-center small mt-3">Bạn là sinh viên? <a class="fw-semibold text-decoration-none" href="${pageContext.request.contextPath}/register">Đăng ký tài khoản sinh viên</a></div>
                </form>
            </div>
        </div>
    </div>
</div>
</body>
</html>
