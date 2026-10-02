<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Không tìm thấy trang - 404" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body class="d-flex flex-column min-vh-100" style="background-color: var(--qaute-bg);">

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container my-auto py-5">
    <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
            <div class="qaute-card p-5 text-center shadow-lg border-0">
                <div class="rounded-circle d-inline-flex align-items-center justify-content-center mb-3" style="width: 80px; height: 80px; background-color: #fee2e2; color: #b91c1c;">
                    <i class="bi bi-exclamation-octagon fs-1"></i>
                </div>

                <h3 class="fw-extrabold text-dark mb-2">Không tìm thấy trang</h3>
                <span class="badge bg-warning-subtle text-warning px-3 py-1 rounded-pill mb-3 fw-semibold">Mã lỗi: 404 Not Found</span>

                <p class="text-secondary small mb-4" style="line-height: 1.6;">
                    Đường dẫn bạn yêu cầu không tồn tại, đã bị xóa hoặc tạm thời không khả dụng.
                </p>

                <div class="d-flex justify-content-center gap-2">
                    <a class="btn btn-primary rounded-pill px-4 fw-semibold" href="${pageContext.request.contextPath}/">
                        <i class="bi bi-house me-1"></i>Về Trang chủ
                    </a>
                    <a class="btn btn-outline-secondary rounded-pill px-4 fw-semibold" href="${pageContext.request.contextPath}/faq">
                        <i class="bi bi-question-circle me-1"></i>Trung tâm FAQ
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
