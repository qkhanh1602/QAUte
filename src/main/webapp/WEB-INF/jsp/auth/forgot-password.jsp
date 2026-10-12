<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html><html><head><c:set var="pageTitle" value="Khôi phục mật khẩu"/>
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %></head><body>
<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<main class="container py-5" style="max-width:640px"><div class="qaute-card p-4">
<h1 class="h3 mb-3">Khôi phục mật khẩu</h1>
<c:if test="${not empty infoMessage}"><div class="alert alert-info"><c:out value="${infoMessage}"/></div></c:if>
<c:if test="${not empty errorMessage}"><div class="alert alert-danger"><c:out value="${errorMessage}"/></div></c:if>
<form method="post" action="${pageContext.request.contextPath}/forgot-password" class="d-grid gap-3 mb-4">
<sec:csrfInput/><input type="hidden" name="action" value="request">
<label>Email tài khoản<input class="form-control" type="email" name="email" maxlength="254" value="<c:out value='${resetEmail}'/>" autocomplete="email" required></label>
<button class="btn btn-outline-primary">Gửi mã xác minh</button></form>
<form method="post" action="${pageContext.request.contextPath}/forgot-password" class="d-grid gap-3">
<sec:csrfInput/><input type="hidden" name="action" value="reset">
<label>Email nhận mã<input class="form-control" type="email" name="email" maxlength="254" value="<c:out value='${resetEmail}'/>" autocomplete="email" required></label>
<label>Mã OTP 6 chữ số<input class="form-control" name="otp" inputmode="numeric" pattern="[0-9]{6}" maxlength="6" autocomplete="one-time-code" required></label>
<label>Mật khẩu mới<input class="form-control" type="password" name="password" minlength="8" maxlength="72" autocomplete="new-password" required></label>
<p class="small text-muted mb-0">Mật khẩu dài 8–72 ký tự, có chữ và số, tối đa 72 byte UTF-8.</p>
<label>Nhập lại mật khẩu mới<input class="form-control" type="password" name="confirm" minlength="8" maxlength="72" autocomplete="new-password" required></label>
<button class="btn btn-primary">Đặt lại mật khẩu</button></form>
<a class="d-inline-block mt-3" href="${pageContext.request.contextPath}/login">Quay lại đăng nhập</a>
</div></main><%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %></body></html>
