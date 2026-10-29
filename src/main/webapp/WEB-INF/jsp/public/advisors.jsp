<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Đội ngũ Cố vấn học tập" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-5">
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a class="text-decoration-none" href="${pageContext.request.contextPath}/">Trang chủ</a></li>
            <li class="breadcrumb-item active" aria-current="page">Đội ngũ Cố vấn</li>
        </ol>
    </nav>

    <div class="text-center mb-5">
        <h1 class="fw-bold text-dark tracking-tight mb-2">Đội Ngũ Cố Vấn &amp; Cán Bộ Tư Vấn</h1>
        <p class="text-muted">Các thầy cô phụ trách học vụ, công tác sinh viên và định hướng chuyên môn nghề nghiệp</p>
    </div>

    <div class="row g-4">
        <c:forEach var="cv" items="${advisors}"><div class="col-md-6 col-lg-4">
            <div class="qaute-card qaute-card-hover p-4 text-center h-100 d-flex flex-column">
                <div class="user-avatar user-avatar-lg mx-auto mb-3">
                    <c:if test="${cv.taiKhoan.avatarUrl != null and not empty cv.taiKhoan.avatarUrl}"><img alt="Ảnh cố vấn" src="${cv.taiKhoan.avatarUrl}">
                    </c:if><c:if test="${not (cv.taiKhoan.avatarUrl != null and not empty cv.taiKhoan.avatarUrl)}"><span><c:out value="${cv.taiKhoan.avatarInitial}" /></span>
                </c:if></div>
                <h5 class="fw-bold mb-1 text-dark"><c:out value="${cv.taiKhoan.hoTen}" /></h5>
                <c:choose>
                    <c:when test="${not empty cv.hocHamHocVi}">
                        <div class="text-primary small fw-semibold mb-1"><c:out value="${cv.hocHamHocVi}" /> - <c:out value="${cv.chucVu}" /></div>
                    </c:when>
                    <c:otherwise>
                        <div class="text-primary small fw-semibold mb-1"><c:out value="${cv.chucVu}" /></div>
                    </c:otherwise>
                </c:choose>
                <div class="text-muted small mb-3"><i class="bi bi-building me-1"></i><span><c:out value="${cv.donVi.tenDonVi}" /></span></div>

                <div class="bg-light p-3 rounded-3 text-start small mb-3 flex-grow-1">
                    <div class="fw-bold text-dark mb-1">Chuyên môn tư vấn:</div>
                    <div class="text-secondary"><c:out value="${cv.chuyenMon}" /></div>
                    <div class="mt-2 text-muted">
                        <i class="bi bi-envelope me-1"></i><span><c:out value="${cv.taiKhoan.email}" /></span>
                    </div>
                </div>

                <div class="pt-2 border-top">
                    <c:choose>
                        <c:when test="${cv.chucVu eq 'Cố vấn sinh viên'}">
                            <a class="btn btn-outline-primary w-100 py-2 rounded-pill fw-semibold" href="${pageContext.request.contextPath}/student/chat?maDonVi=${cv.donVi.maDonVi}">
                                <i class="bi bi-chat-dots me-1"></i>Chat tư vấn trực tuyến
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a class="btn btn-qaute-primary w-100 py-2 rounded-pill fw-semibold" href="${pageContext.request.contextPath}/student/appointments?maCV=${cv.maCV}">
                                <i class="bi bi-calendar-check me-1"></i>Đặt lịch hẹn tư vấn
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </c:forEach></div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
