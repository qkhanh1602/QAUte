<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<c:set var="pageTitle" value="Đăng xuất" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<main class="container py-5">
    <div class="card mx-auto p-4 shadow-sm" style="max-width: 480px;">
        <h1 class="h4">Đăng xuất khỏi QAUTE?</h1>
        <p class="text-muted">Bạn có thể đăng nhập lại để tiếp tục tư vấn.</p>
        <form method="post" action="${pageContext.request.contextPath}/logout">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <button type="submit" class="btn btn-primary">Đăng xuất</button>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/">Về trang chủ</a>
        </form>
    </div>
</main>
<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>
</body>
</html>
