<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html><html><head><c:set var="pageTitle" value="Email cần xử lý"/><%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %></head><body>
<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4"><h2>Email chưa gửi thành công</h2><p>Chỉ thử lại những thông báo vẫn còn cần gửi. Hệ thống sẽ gửi khi ứng dụng đang hoạt động.</p>
<c:if test="${not empty successMessage}"><div class="alert alert-success"><c:out value="${successMessage}"/></div></c:if>
<c:if test="${not empty errorMessage}"><div class="alert alert-danger"><c:out value="${errorMessage}"/></div></c:if>
<table class="table"><thead><tr><th>Ngày tạo</th><th>Số lần thử</th><th>Lỗi gần nhất</th><th>Thao tác</th></tr></thead><tbody>
<c:forEach var="email" items="${emails}"><tr><td><c:out value="${email.NgayTao}"/></td><td>${email.SoLanThu}</td><td><c:out value="${email.LoiCuoi}"/></td><td><form method="post" action="${pageContext.request.contextPath}/admin/email-queue/${email.MaSuKien}/retry"><sec:csrfInput/><button class="btn btn-outline-primary btn-sm">Thử gửi lại</button></form></td></tr></c:forEach>
<c:if test="${empty emails}"><tr><td colspan="4">Không có email thất bại ở trang này.</td></tr></c:if></tbody></table>
<c:if test="${queuePage>0}"><a class="btn btn-outline-primary" href="?page=${queuePage-1}">Trang trước</a></c:if>
<c:if test="${emails.size()==20}"><a class="btn btn-outline-primary" href="?page=${queuePage+1}">Trang sau</a></c:if></div>
<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %></body></html>
