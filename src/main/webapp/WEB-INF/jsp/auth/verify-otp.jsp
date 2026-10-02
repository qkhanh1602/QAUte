<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Xác thực Email bằng OTP - QAUTE" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body class="auth-page d-flex align-items-center justify-content-center py-5">

<div class="container">
    <div class="row justify-content-center">
        <div class="col-md-7 col-lg-5">
            <div class="text-center mb-4">
                <a class="d-inline-flex align-items-center gap-2 text-decoration-none mb-2" href="${pageContext.request.contextPath}/">
                    <svg width="40" height="40" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg" class="auth-brand-mark flex-shrink-0">
                        <rect width="40" height="40" rx="10" fill="url(#qauteLogoVerifyGrad)"></rect>
                        <path d="M20 9L34 16.5L20 24L6 16.5L20 9Z" fill="#FFFFFF"></path>
                        <path d="M11.5 20V26C11.5 29.2 15.2 31.5 20 31.5C24.8 31.5 28.5 29.2 28.5 26V20L20 24.5L11.5 20Z" fill="#FFFFFF" fill-opacity="0.88"></path>
                        <path d="M34 17.5V26.5" stroke="#dc2626" stroke-width="2.2" stroke-linecap="round"></path>
                        <circle cx="34" cy="27.5" r="1.8" fill="#dc2626"></circle>
                        <defs>
                            <linearGradient id="qauteLogoVerifyGrad" x1="0" y1="0" x2="40" y2="40" gradientUnits="userSpaceOnUse">
                                <stop stop-color="#123b76"></stop>
                                <stop offset="1" stop-color="#dc2626"></stop>
                            </linearGradient>
                        </defs>
                    </svg>
                    <span class="fs-2 fw-extrabold text-dark tracking-tight">QAUTE</span>
                    <span class="brand-badge">UTE ADVISORY</span>
                </a>
                <p class="text-muted small">Cổng Tư Vấn Trực Tuyến Sinh Viên</p>
            </div>

            <div class="qaute-card p-4 p-md-5 shadow-lg border-0 bg-white rounded-4">
                <div class="text-center mb-4">
                    <div class="d-inline-flex align-items-center justify-content-center bg-primary-subtle text-primary rounded-circle mb-3" style="width: 64px; height: 64px;">
                        <i class="bi bi-shield-check fs-2 text-primary"></i>
                    </div>
                    <h4 class="fw-bold text-dark mb-1">Xác thực Mã OTP</h4>
                    <p class="text-muted small mb-0">
                        Mã OTP 6 chữ số đã được gửi tới hộp thư Email:<br>
                        <strong class="text-primary fs-6"><c:out value="${email}" /></strong>
                    </p>
                </div>

                <!-- Flash Messages -->
                <c:if test="${errorMessage}"><div class="alert alert-danger py-2 small mb-3 d-flex align-items-center gap-2">
                    <i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i>
                    <span><c:out value="${errorMessage}" /></span>
                </div>

                </c:if><c:if test="${successMessage}"><div class="alert alert-success py-2 small mb-3 d-flex align-items-center gap-2">
                    <i class="bi bi-check-circle-fill flex-shrink-0"></i>
                    <span><c:out value="${successMessage}" /></span>
                </div>

                </c:if><c:if test="${infoMessage}"><div class="alert alert-info py-2 small mb-3 d-flex align-items-center gap-2">
                    <i class="bi bi-info-circle-fill flex-shrink-0"></i>
                    <span><c:out value="${infoMessage}" /></span>
                </div>

                </c:if><c:if test="${remainingAttempts != null and remainingAttempts gt 0 and remainingAttempts lt 5}"><div class="alert alert-warning py-1 small mb-3 text-center">
                    <i class="bi bi-shield-exclamation me-1"></i>Bạn còn <strong><c:out value="${remainingAttempts}" /></strong> lần thử trước khi mã bị vô hiệu hóa.
                </div>

                </c:if><!-- Form Xác thực OTP -->
                <form method="post" id="verifyForm" action="${pageContext.request.contextPath}/verify-otp">
                    <sec:csrfInput /><input type="hidden" name="email" value="${email}">

                    <div class="mb-3 text-center">
                        <label class="form-label small fw-semibold text-dark mb-2">Nhập mã OTP 6 chữ số</label>
                        <input type="text" name="otp" id="otpInput" maxlength="6" pattern="[0-9]{6}" class="form-control form-control-lg text-center fw-bold tracking-widest fs-3 rounded-3" style="letter-spacing: 12px; font-family: monospace;" placeholder="••••••" autofocus required autocomplete="one-time-code">
                    </div>

                    <!-- Countdown Timer -->
                    <div class="text-center mb-4 small text-muted">
                        <i class="bi bi-clock-history me-1 text-danger"></i>Mã OTP hết hiệu lực sau:
                        <strong id="expiryCountdown" class="text-danger fw-bold">10:00</strong>
                    </div>

                    <button type="submit" class="btn btn-qaute-primary w-100 py-2 fw-bold rounded-3 mb-3">
                        <i class="bi bi-check2-circle me-1"></i>Xác nhận &amp; Hoàn tất
                    </button>
                </form>

                <!-- Form Gửi lại OTP (Email) -->
                <div class="border-top pt-3 text-center">
                    <p class="small text-muted mb-2">Chưa nhận được mã OTP hoặc mã đã hết hạn?</p>
                    <div class="d-flex justify-content-center gap-2 flex-wrap">
                        <form method="post" class="d-inline" action="${pageContext.request.contextPath}/resend-otp">
                            <sec:csrfInput /><input type="hidden" name="email" value="${email}">
                            <button type="submit" id="resendBtn" class="btn btn-outline-secondary btn-sm rounded-pill px-3" disabled>
                                <i class="bi bi-envelope-at me-1"></i>Gửi lại qua Email (<span id="resendSeconds">60</span>s)
                            </button>
                        </form>
                    </div>
                </div>

                <div class="text-center mt-3 small">
                    <a class="text-decoration-none text-muted" href="${advisorRegistration%20?%20'/advisor-register'%20:%20'/register'}">
                        <i class="bi bi-arrow-left me-1"></i>Quay lại trang Đăng ký
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    // 1. Đồng hồ đếm ngược 10 phút hiệu lực OTP
    let totalSeconds = 600; // 10 phút
    const expiryEl = document.getElementById('expiryCountdown');
    const timerInterval = setInterval(function() {
        totalSeconds--;
        if (totalSeconds <= 0) {
            clearInterval(timerInterval);
            expiryEl.textContent = "Hết hạn!";
            expiryEl.classList.add("text-secondary");
        } else {
            const m = Math.floor(totalSeconds / 60);
            const s = totalSeconds % 60;
            expiryEl.textContent = (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
        }
    }, 1000);

    // 2. Bộ đếm ngược cooldown 60 giây nút Gửi lại
    let cooldownSeconds = 60;
    const resendBtn = document.getElementById('resendBtn');
    const resendSecEl = document.getElementById('resendSeconds');

    const cooldownInterval = setInterval(function() {
        cooldownSeconds--;
        if (cooldownSeconds <= 0) {
            clearInterval(cooldownInterval);
            if (resendBtn) {
                resendBtn.removeAttribute('disabled');
                resendBtn.innerHTML = '<i class="bi bi-envelope-at me-1"></i>Gửi lại qua Email';
                resendBtn.classList.remove('btn-outline-secondary');
                resendBtn.classList.add('btn-outline-primary');
            }
        } else {
            if (resendSecEl) resendSecEl.textContent = cooldownSeconds;
        }
    }, 1000);

    // Tự động submit khi nhập đủ 6 số
    const otpInput = document.getElementById('otpInput');
    otpInput.addEventListener('input', function(e) {
        this.value = this.value.replace(/[^0-9]/g, '');
        if (this.value.length === 6) {
            // người dùng có thể bấm xác nhận hoặc enter
        }
    });
</script>

</body>
</html>
