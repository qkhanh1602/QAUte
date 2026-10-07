<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html><html><head><c:set var="pageTitle" value="Diễn đàn tư vấn sinh viên" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head><body><%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<main class="container py-4">

<a class="text-decoration-none d-inline-block mb-3" href="${pageContext.request.contextPath}/forum"><i class="bi bi-arrow-left"></i> Diễn đàn</a>
<article class="qaute-card p-4 mb-4">
 <h1 class="h3"><c:out value="${question.title}" /></h1>
 <div class="small text-secondary d-flex flex-wrap gap-3 mb-3"><span><c:out value="${question.authorName}" /></span><time><c:out value="${question.createdAt}" /></time><span><c:out value="${question.views}" /> lượt xem</span><span><c:out value="${question.statusLabel}" /></span></div>
 <div class="small text-primary mb-3"><c:out value="${question.unitName}" /> · <c:out value="${question.fieldName}" /></div>
 <div style="white-space:pre-wrap;overflow-wrap:anywhere"><c:out value="${question.body}" /></div>
</article>
<section class="qaute-card p-4"><h2 class="h5 mb-4">Trao đổi và trả lời</h2>
 <c:if test="${empty replies}"><p class="text-secondary">Câu hỏi đang chờ cố vấn trả lời.</p></c:if>
 <c:forEach var="reply" items="${replies}"><article class="border-top pt-3 mb-4">
  <div class="d-flex flex-wrap gap-2 mb-2"><strong><c:out value="${reply.authorName}" /></strong><span class="badge bg-primary"><c:out value="${reply.kind}" /></span><time class="small text-secondary"><c:out value="${reply.createdAt}" /></time></div>
  <div style="white-space:pre-wrap;overflow-wrap:anywhere"><c:out value="${reply.body}" /></div>
 </article></c:forEach>
 <p class="small text-secondary mb-0">Người gửi câu hỏi và cố vấn trao đổi trong mục Phiếu tư vấn. Tệp đính kèm chỉ người có quyền xử lý xem được.</p>
</section>
</main><%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %></body></html>