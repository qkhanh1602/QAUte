<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html><html><head><c:set var="pageTitle" value="Diễn đàn tư vấn sinh viên" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head><body><%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<main class="container py-4">

<div class="d-flex flex-wrap justify-content-between gap-3 mb-4">
 <div><h1 class="h3">Diễn đàn tư vấn sinh viên</h1><p class="text-secondary mb-0">Câu hỏi và trao đổi với cố vấn được công khai để mọi người cùng tham khảo.</p></div>
 <a class="btn btn-primary align-self-center" href="${pageContext.request.contextPath}/student/tickets/new"><i class="bi bi-pencil-square me-1"></i>Đặt câu hỏi</a>
</div>
<form method="get" class="row g-2 mb-4">
 <div class="col-md-8"><label class="visually-hidden" for="forumSearch">Tìm câu hỏi</label><input class="form-control" id="forumSearch" name="kw" maxlength="200" placeholder="Tìm câu hỏi…" value="<c:out value='${kw}' />"></div>
 <div class="col-md-3"><label class="visually-hidden" for="forumSort">Sắp xếp</label><select class="form-select" id="forumSort" name="sort"><option value="latest">Mới nhất</option><option value="views" ${sort eq 'views' ? 'selected' : ''}>Xem nhiều nhất</option></select></div>
 <div class="col-md-1"><button class="btn btn-primary w-100">Tìm</button></div>
</form>
<div class="qaute-card p-3 p-md-4">
 <div class="d-flex justify-content-between border-bottom pb-3"><h2 class="h5 mb-0">Danh sách câu hỏi</h2><span class="text-secondary"><c:out value="${total}" /> câu hỏi</span></div>
 <%@ include file="/WEB-INF/jsp/fragments/forum-questions.jspf" %>
 <nav aria-label="Phân trang câu hỏi" class="d-flex justify-content-center align-items-center gap-3 mt-4">
  <c:if test="${page gt 0}"><c:url var="prev" value="/forum"><c:param name="page" value="${page-1}"/><c:param name="kw" value="${kw}"/><c:param name="sort" value="${sort}"/></c:url><a class="btn btn-outline-primary" href="<c:out value='${prev}' />">Trang trước</a></c:if>
  <span>Trang ${page+1} / ${lastPage+1}</span>
  <c:if test="${page lt lastPage}"><c:url var="next" value="/forum"><c:param name="page" value="${page+1}"/><c:param name="kw" value="${kw}"/><c:param name="sort" value="${sort}"/></c:url><a class="btn btn-outline-primary" href="<c:out value='${next}' />">Trang sau</a></c:if>
 </nav>
</div>
<div class="mt-3"><a href="${pageContext.request.contextPath}/faq">Xem câu hỏi thường gặp (FAQ) đã duyệt</a></div>
</main><%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %></body></html>