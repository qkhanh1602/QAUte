<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="${article.tieuDe}" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-5">
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a class="text-decoration-none" href="${pageContext.request.contextPath}/">Trang chủ</a></li>
            <li class="breadcrumb-item"><a class="text-decoration-none" href="${pageContext.request.contextPath}/articles">Cẩm nang</a></li>
            <li class="breadcrumb-item active text-truncate" style="max-width: 300px;"><c:out value="${article.tieuDe}" /></li>
        </ol>
    </nav>

    <div class="row g-5">
        <div class="col-lg-8">
            <div class="qaute-card p-4 p-md-5 border-0 shadow-sm">
                <div class="d-flex align-items-center gap-2 mb-3">
                    <span class="badge bg-primary-subtle text-primary border border-primary-subtle"><c:out value="${article.linhVuc.tenLinhVuc}" /></span>
                    <span class="text-muted small">•</span>
                    <small class="text-muted"><i class="bi bi-calendar3 me-1"></i><span><c:out value="${q:formatTemporal(article.ngayDang, 'dd/MM/yyyy')}" /></span></small>
                    <span class="text-muted small">•</span>
                    <small class="text-muted"><i class="bi bi-eye me-1"></i><span><c:out value="${article.luotXem}" /></span> lượt xem</small>
                </div>

                <h1 class="fw-bold text-dark mb-4 lh-sm"><c:out value="${article.tieuDe}" /></h1>

                <div class="p-3 bg-light rounded-3 border-start border-4 border-primary text-secondary fst-italic mb-4"><c:out value="${article.tomTat}" /></div>

                <div class="article-content text-dark" style="line-height: 1.9; font-size: 1.05rem;"><c:out value="${article.noiDung}" escapeXml="false" /></div>

                <c:if test="${not empty attachments}"><div class="mt-4 p-3 bg-light rounded-3">
                    <div class="fw-semibold mb-2"><i class="bi bi-paperclip"></i> Tài liệu đính kèm</div>
                    <div class="d-flex flex-wrap gap-2"><c:forEach var="file" items="${attachments}"><a class="btn btn-sm btn-outline-primary" target="_blank" rel="noopener" href="${pageContext.request.contextPath}/attachments/${file.maTep}"><c:out value="${file.tenGoc}" /></a></c:forEach></div>
                </div>

                </c:if><div class="mt-5 pt-4 border-top d-flex justify-content-between align-items-center">
                    <div class="text-muted small">
                        <i class="bi bi-tags me-1"></i>Từ khóa: <span class="fw-semibold"><c:out value="${article.tuKhoa}" /></span>
                    </div>
                    <a class="btn btn-outline-secondary btn-sm rounded-pill" href="${pageContext.request.contextPath}/articles">
                        <i class="bi bi-arrow-left me-1"></i>Quay lại danh sách
                    </a>
                </div>
            </div>
        </div>

        <div class="col-lg-4">
            <div class="qaute-card p-4 mb-4">
                <h6 class="fw-bold mb-3 border-bottom pb-2">Bài viết cùng chuyên mục</h6>
                <div class="d-flex flex-column gap-3">
                    <c:forEach var="recent" items="${recentArticles}"><c:if test="${recent.maBaiViet != article.maBaiViet}"><div>
                        <a class="text-dark text-decoration-none fw-semibold d-block mb-1 hover-primary" href="${pageContext.request.contextPath}/articles/${recent.maBaiViet}"><c:out value="${recent.tieuDe}" /></a>
                        <small class="text-muted"><i class="bi bi-clock me-1"></i><span><c:out value="${q:formatTemporal(recent.ngayDang, 'dd/MM/yyyy')}" /></span></small>
                    </div>
                </c:if></c:forEach></div>
            </div>

            <div class="qaute-card p-4 bg-light text-center">
                <i class="bi bi-chat-heart text-primary fs-1 mb-2"></i>
                <h6 class="fw-bold">Bạn vẫn có điều thắc mắc?</h6>
                <p class="small text-muted mb-3">Hãy đặt câu hỏi trên diễn đàn để được thầy cô giải đáp cụ thể theo trường hợp của bạn.</p>
                <a class="btn btn-primary btn-sm rounded-pill w-100 py-2 fw-semibold" href="${pageContext.request.contextPath}/forum/ask">
                    Đặt câu hỏi
                </a>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
