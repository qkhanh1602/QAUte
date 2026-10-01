<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Cẩm nang & Hướng dẫn" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-5">
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a class="text-decoration-none" href="${pageContext.request.contextPath}/">Trang chủ</a></li>
            <li class="breadcrumb-item active" aria-current="page">Cẩm nang sinh viên</li>
        </ol>
    </nav>

    <div class="row align-items-center mb-5">
        <div class="col-lg-6">
            <h1 class="fw-bold text-dark tracking-tight mb-2">Cẩm Nang &amp; Hướng Dẫn</h1>
            <p class="text-muted">Kho tài liệu, quy chế đào tạo và hướng dẫn học tập chính thức</p>
        </div>
        <div class="col-lg-6">
            <form method="get" class="d-flex gap-2" action="${pageContext.request.contextPath}/articles">
                <div class="input-group">
                    <span class="input-group-text bg-white border-end-0"><i class="bi bi-search text-muted"></i></span>
                    <input type="text" name="kw" class="form-control border-start-0 ps-0" placeholder="Tìm bài viết hướng dẫn..." value="${kw}">
                    <button type="submit" class="btn btn-primary fw-semibold px-4">Tìm kiếm</button>
                </div>
            </form>
        </div>
    </div>

    <div class="d-flex flex-wrap gap-2 mb-4 pb-2 border-bottom">
        <a href="${pageContext.request.contextPath}/articles" class="${selectedLinhVuc == null ? 'btn btn-primary btn-sm rounded-pill px-3' : 'btn btn-outline-secondary btn-sm rounded-pill px-3'}">
            Tất cả chủ đề
        </a>
        <c:forEach var="lv" items="${linhVucList}"><a href="${pageContext.request.contextPath}/articles?linhVuc=${lv.maLinhVuc}" class="${selectedLinhVuc == lv.maLinhVuc ? 'btn btn-primary btn-sm rounded-pill px-3' : 'btn btn-outline-secondary btn-sm rounded-pill px-3'}"><c:out value="${lv.tenLinhVuc}" /></a>
    </c:forEach></div>

    <div class="row g-4">
        <c:forEach var="article" items="${articles}"><div class="col-md-6 col-lg-4">
            <div class="qaute-card qaute-card-hover p-4 h-100 d-flex flex-column">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <span class="badge bg-primary-subtle text-primary border border-primary-subtle"><c:out value="${article.linhVuc.tenLinhVuc}" /></span>
                    <small class="text-muted"><i class="bi bi-eye me-1"></i><span><c:out value="${article.luotXem}" /></span> xem</small>
                </div>
                <h5 class="fw-bold mb-2">
                    <a class="text-dark text-decoration-none hover-primary" href="${pageContext.request.contextPath}/articles/${article.maBaiViet}"><c:out value="${article.tieuDe}" /></a>
                </h5>
                <p class="text-secondary small flex-grow-1"><c:out value="${article.tomTat}" /></p>
                <div class="mt-3 pt-3 border-top d-flex justify-content-between align-items-center">
                    <small class="text-muted"><i class="bi bi-clock me-1"></i><span><c:out value="${q:formatTemporal(article.ngayDang, 'dd/MM/yyyy')}" /></span></small>
                    <a class="text-primary fw-semibold small text-decoration-none" href="${pageContext.request.contextPath}/articles/${article.maBaiViet}">
                        Đọc chi tiết <i class="bi bi-arrow-right"></i>
                    </a>
                </div>
            </div>
        </div>
    </c:forEach></div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
