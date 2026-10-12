<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Đăng nhập hệ thống" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body class="auth-page d-flex align-items-center justify-content-center py-5">

<div class="container">
    <div class="row justify-content-center">
        <div class="col-md-8 col-lg-5">
            <div class="text-center mb-4">
                <a class="d-inline-flex align-items-center gap-2 text-decoration-none mb-2" href="${pageContext.request.contextPath}/">
                    <svg width="40" height="40" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg" class="auth-brand-mark flex-shrink-0">
                        <rect width="40" height="40" rx="10" fill="url(#qauteLogoLoginGrad)"></rect>
                        <path d="M20 9L34 16.5L20 24L6 16.5L20 9Z" fill="#FFFFFF"></path>
                        <path d="M11.5 20V26C11.5 29.2 15.2 31.5 20 31.5C24.8 31.5 28.5 29.2 28.5 26V20L20 24.5L11.5 20Z" fill="#FFFFFF" fill-opacity="0.88"></path>
                        <path d="M34 17.5V26.5" stroke="#dc2626" stroke-width="2.2" stroke-linecap="round"></path>
                        <circle cx="34" cy="27.5" r="1.8" fill="#dc2626"></circle>
                        <defs>
                            <linearGradient id="qauteLogoLoginGrad" x1="0" y1="0" x2="40" y2="40" gradientUnits="userSpaceOnUse">
                                <stop stop-color="#123b76"></stop>
                                <stop offset="1" stop-color="#dc2626"></stop>
                            </linearGradient>
                        </defs>
                    </svg>
                    <span class="fs-2 fw-extrabold text-dark tracking-tight">QAUTE</span>
                </a>
                <p class="text-muted small">Đăng nhập hệ thống hỏi đáp và tư vấn sinh viên</p>
            </div>

            <div class="qaute-card p-4 p-md-5 shadow-lg border-0">
                <h4 class="fw-bold text-dark mb-3 text-center">Đăng nhập tài khoản</h4>

                <!-- Flash messages -->
                <c:if test="${not empty lockedMessage}"><div class="alert alert-warning py-2 small mb-3 d-flex align-items-center gap-2 border-warning">
                    <i class="bi bi-shield-lock-fill text-danger fs-5 flex-shrink-0"></i>
                    <span class="text-danger fw-bold"><c:out value="${lockedMessage}" /></span>
                </div>
                </c:if><c:if test="${not empty errorMessage}"><div class="alert alert-danger py-2 small mb-3 d-flex align-items-center gap-2">
                    <i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i>
                    <span><c:out value="${errorMessage}" /></span>
                </div>
                </c:if><c:if test="${not empty successMessage}"><div class="alert alert-success py-2 small mb-3 d-flex align-items-center gap-2">
                    <i class="bi bi-check-circle-fill flex-shrink-0"></i>
                    <span><c:out value="${successMessage}" /></span>
                </div>

                </c:if><form method="post" action="${pageContext.request.contextPath}/login">
                    <sec:csrfInput /><div class="mb-3">
                        <label class="form-label small fw-semibold text-dark">Tên đăng nhập hoặc Email</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-person text-muted"></i></span>
                            <input type="text" id="usernameInput" name="username" class="form-control border-start-0 ps-0" placeholder="Nhập tên đăng nhập hoặc email của bạn..." required autocomplete="username">
                        </div>
                    </div>

                    <div class="mb-3">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <label class="form-label small fw-semibold text-dark mb-0">Mật khẩu</label>
                            <a class="text-muted small text-decoration-none" href="${pageContext.request.contextPath}/forgot-password">Quên mật khẩu?</a>
                        </div>
                        <div class="input-group">
                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-key text-muted"></i></span>
                            <input type="password" id="passwordInput" name="password" class="form-control border-start-0 ps-0" placeholder="Nhập mật khẩu..." required autocomplete="current-password">
                        </div>
                    </div>

                    <!-- CAPTCHA sau nhiều lần thất bại (>= 3 lần) -->
                    <c:if test="${requireCaptcha}"><div class="mb-4 p-3 bg-light border rounded-3">
                        <label class="form-label small fw-bold text-dark d-flex align-items-center justify-content-between mb-2">
                            <span><i class="bi bi-shield-check text-primary me-1"></i>Mã bảo vệ CAPTCHA</span>
                            <span class="text-muted small fw-normal">Phát hiện nhiều lần đăng nhập sai</span>
                        </label>
                        <div class="d-flex align-items-center gap-2 mb-2">
                            <img id="captchaImg" alt="Mã bảo vệ CAPTCHA" class="border rounded shadow-sm" style="height: 42px; cursor: pointer;" onclick="refreshCaptcha()" title="Nhấn để đổi mã khác" src="${pageContext.request.contextPath}/captcha">
                            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="refreshCaptcha()" title="Tải lại mã bảo vệ">
                                <i class="bi bi-arrow-clockwise"></i>
                            </button>
                        </div>
                        <input type="text" name="captcha" class="form-control form-control-sm" placeholder="Nhập 5 ký tự trong hình trên..." required autocomplete="off">
                    </div>

                    </c:if><button type="submit" class="btn btn-qaute-primary w-100 py-2 fw-bold rounded-3 mb-3">
                        <i class="bi bi-box-arrow-in-right me-1"></i>Đăng nhập
                    </button>
                </form>

                <div class="text-center small text-muted">
                    Chưa có tài khoản sinh viên? <a class="text-primary fw-semibold text-decoration-none" href="${pageContext.request.contextPath}/register">Đăng ký tại đây</a>
                </div>
            </div>

            <div class="text-center mt-3">
                <a class="text-secondary small text-decoration-none" href="${pageContext.request.contextPath}/"><i class="bi bi-arrow-left me-1"></i>Quay lại Trang chủ</a>
            </div>
        </div>
    </div>
</div>

<script>
function refreshCaptcha() {
    const img = document.getElementById('captchaImg');
    if (img) {
        img.src = '/captcha?' + new Date().getTime();
    }
}
</script>

</body>
</html>
