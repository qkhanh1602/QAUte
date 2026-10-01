<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Hỏi đáp Câu hỏi thường gặp FAQ" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-5">
    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a class="text-decoration-none" href="${pageContext.request.contextPath}/">Trang chủ</a></li>
            <li class="breadcrumb-item active" aria-current="page">Hỏi đáp FAQ</li>
        </ol>
    </nav>

    <!-- Header & Search -->
    <div class="row align-items-center mb-5">
        <div class="col-lg-6">
            <h1 class="fw-bold text-dark tracking-tight mb-2">Trung tâm Giải đáp FAQ</h1>
            <p class="text-muted">Tra cứu nhanh câu hỏi thường gặp theo lĩnh vực hoặc từ khóa</p>
        </div>
        <div class="col-lg-6">
            <form method="get" class="d-flex gap-2" action="${pageContext.request.contextPath}/faq">
                <div class="input-group">
                    <span class="input-group-text bg-white border-end-0"><i class="bi bi-search text-muted"></i></span>
                    <input type="text" name="kw" class="form-control border-start-0 ps-0" placeholder="Nhập từ khóa cần tìm..." value="${kw}">
                    <button type="submit" class="btn btn-primary fw-semibold px-4">Tìm kiếm</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Category filter pills -->
    <div class="d-flex flex-wrap gap-2 mb-4 pb-2 border-bottom">
        <a href="${pageContext.request.contextPath}/faq" class="${selectedLinhVuc == null ? 'btn btn-primary btn-sm rounded-pill px-3' : 'btn btn-outline-secondary btn-sm rounded-pill px-3'}">
            Tất cả lĩnh vực
        </a>
        <c:forEach var="lv" items="${linhVucList}"><a href="${pageContext.request.contextPath}/faq?linhVuc=${lv.maLinhVuc}" class="${selectedLinhVuc == lv.maLinhVuc ? 'btn btn-primary btn-sm rounded-pill px-3' : 'btn btn-outline-secondary btn-sm rounded-pill px-3'}"><c:out value="${lv.tenLinhVuc}" /></a>
    </c:forEach></div>

    <!-- FAQ List -->
    <div class="row">
        <div class="col-lg-8">
            <c:if test="${empty faqs}"><div class="qaute-card p-5 text-center text-muted">
                <i class="bi bi-inbox fs-1 mb-3 text-secondary"></i>
                <h5>Không tìm thấy câu hỏi FAQ nào</h5>
                <p class="small">Hãy thử tìm kiếm với từ khóa khác hoặc gửi câu hỏi mới cho Cố vấn.</p>
                <a class="btn btn-primary btn-sm rounded-pill mt-2" href="${pageContext.request.contextPath}/student/tickets/new">Gửi câu hỏi tư vấn</a>
            </div>

            </c:if><c:if test="${not (empty faqs)}"><div class="accordion" id="faqAccordionPage">
                <c:forEach var="faq" items="${faqs}"><div class="qaute-card mb-3 p-3 border-0 shadow-sm">
                    <h2 class="accordion-header" id="${'heading' += faq.maFAQ}">
                        <button class="accordion-button collapsed fw-bold py-2 text-dark bg-transparent shadow-none" type="button" data-bs-toggle="collapse" data-bs-target="${'#collapse' += faq.maFAQ}">
                            <i class="bi bi-question-circle-fill text-primary me-2"></i>
                            <span><c:out value="${faq.cauHoi}" /></span>
                        </button>
                    </h2>
                    <div class="accordion-collapse collapse" data-bs-parent="#faqAccordionPage" id="${'collapse' += faq.maFAQ}">
                        <div class="accordion-body text-secondary ps-4 pt-3 pb-2" style="line-height: 1.7;"><c:out value="${faq.cauTraLoi}" /></div>
                        <div class="d-flex justify-content-between align-items-center pt-2 ps-4 text-muted small border-top mt-2">
                            <span><i class="bi bi-tag me-1"></i><span><c:out value="${faq.linhVuc.tenLinhVuc}" /></span></span>
                            <span><i class="bi bi-eye me-1"></i><span><c:out value="${faq.luotXem}" /></span> lượt xem</span>
                        </div>
                    </div>
                </div>
            </c:forEach></div>
        </c:if></div>

        <!-- Sidebar Help Box -->
        <div class="col-lg-4">
            <div class="qaute-card public-page-hero p-4 text-white mb-4 border-0 shadow-sm" style="border-radius: 1.25rem;">
                <h5 class="fw-bold mb-2">Chưa tìm thấy câu trả lời?</h5>
                <p class="small text-white-50 mb-3">
                    Đừng ngần ngại gửi câu hỏi trực tiếp cho Cố vấn học tập phụ trách. Bạn sẽ nhận được phản hồi chi tiết trong vòng 24–48h.
                </p>
                <a class="btn btn-warning text-dark fw-bold w-100 rounded-pill py-2" href="${pageContext.request.contextPath}/student/tickets/new">
                    <i class="bi bi-pencil-square me-1"></i>Tạo Phiếu Tư Vấn Mới
                </a>
            </div>

            <div class="qaute-card p-4">
                <h6 class="fw-bold mb-3 border-bottom pb-2">Đơn vị tiếp nhận giải đáp</h6>
                <div class="d-flex flex-column gap-3 small">
                    <div class="d-flex gap-2">
                        <i class="bi bi-building text-primary fs-5"></i>
                        <div>
                            <div class="fw-bold">Phòng Đào tạo</div>
                            <div class="text-muted">Học vụ, đăng ký môn học, điểm số</div>
                        </div>
                    </div>
                    <div class="d-flex gap-2">
                        <i class="bi bi-heart-pulse text-danger fs-5"></i>
                        <div>
                            <div class="fw-bold">Phòng Công tác Sinh viên</div>
                            <div class="text-muted">Học bổng, học phí, BHYT, KTX, kỷ luật</div>
                        </div>
                    </div>
                    <div class="d-flex gap-2">
                        <i class="bi bi-people text-success fs-5"></i>
                        <div>
                            <div class="fw-bold">Trung tâm Hỗ trợ Sinh viên</div>
                            <div class="text-muted">Ký túc xá, tâm lý, kỹ năng và đời sống sinh viên</div>
                        </div>
                    </div>
                    <div class="d-flex gap-2">
                        <i class="bi bi-mortarboard text-info fs-5"></i>
                        <div class="w-100 min-w-0">
                            <div class="fw-bold">Các Khoa / Viện chuyên môn</div>
                            <div class="text-muted mb-2">Ngành học, đồ án, thực tập, việc làm và cố vấn học tập</div>
                            <div class="receiving-faculty-list" aria-label="Danh sách khoa và viện tiếp nhận tư vấn">
                                <c:forEach var="dv" items="${donViList}"><c:if test="${dv.loaiDonVi == 'Khoa'}"><div class="receiving-faculty-item">
                                    <i class="bi bi-check-circle-fill" aria-hidden="true"></i>
                                    <span><c:out value="${dv.tenDonVi}" /></span>
                                </div>
                            </c:if></c:forEach></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
