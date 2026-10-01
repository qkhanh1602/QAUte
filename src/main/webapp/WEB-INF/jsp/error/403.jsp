<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Truy cập bị từ chối - 403" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body class="d-flex flex-column min-vh-100" style="background-color: var(--qaute-bg);">

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container my-auto py-5">
    <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
            <div class="qaute-card p-5 text-center shadow-lg border-0">
                <div class="rounded-circle d-inline-flex align-items-center justify-content-center mb-3" style="width: 80px; height: 80px; background-color: #FEE2E2; color: #DC2626;">
                    <i class="bi bi-shield-x fs-1"></i>
                </div>

                <h3 class="fw-extrabold text-dark mb-2">Truy cập bị từ chối</h3>
                <span class="badge bg-danger-subtle text-danger px-3 py-1 rounded-pill mb-3 fw-semibold">Mã lỗi: 403 Forbidden</span>

                <p class="text-secondary small mb-4" style="line-height: 1.6;">
                    Tài khoản hiện tại của bạn không có quyền truy cập vào đường dẫn này hoặc chức năng này dành riêng cho vai trò người dùng khác (Sinh viên / Cố vấn / Quản trị viên).
                </p>

                <div class="d-flex flex-wrap justify-content-center gap-2">
                    <!-- Nút điều hướng theo vai trò -->
                    <sec:authorize access="hasRole('Admin')"><div>
                        <a class="btn btn-primary rounded-pill px-4 fw-semibold" href="${pageContext.request.contextPath}/admin/dashboard">
                            <i class="bi bi-speedometer2 me-1"></i>Về Bảng Quản trị
                        </a>
                    </div>
                    </sec:authorize><sec:authorize access="hasRole('CoVan')"><div>
                        <a class="btn btn-warning text-dark rounded-pill px-4 fw-bold" href="${pageContext.request.contextPath}/advisor/dashboard">
                            <i class="bi bi-briefcase me-1"></i>Về Bàn Cố vấn
                        </a>
                    </div>
                    </sec:authorize><sec:authorize access="hasRole('SinhVien')"><div>
                        <a class="btn btn-primary rounded-pill px-4 fw-semibold" href="${pageContext.request.contextPath}/student/dashboard">
                            <i class="bi bi-mortarboard me-1"></i>Về Cổng Sinh viên
                        </a>
                    </div>
                    </sec:authorize><a class="btn btn-outline-secondary rounded-pill px-4 fw-semibold" href="${pageContext.request.contextPath}/">
                        <i class="bi bi-house me-1"></i>Về Trang chủ
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
