<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Cộng đồng tư vấn sinh viên" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body class="home-shell">
<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<c:if test="${not empty logoutMessage}"><div class="container pt-3">
    <div class="alert alert-success alert-dismissible fade show d-flex align-items-center gap-2 shadow-sm rounded-3 mb-0" role="alert">
        <i class="bi bi-check-circle-fill"></i><span><c:out value="${logoutMessage}" /></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Đóng"></button>
    </div>
</div>

</c:if><main>
    <section class="home-hero">
        <div class="container">
            <div class="hero-copy">
                <div class="hero-content">
                        <div class="hero-eyebrow">CỘNG ĐỒNG HỖ TRỢ SINH VIÊN</div>
                        <h1 class="hero-title">Hỏi – Đáp – Kết nối<br><span>Đồng hành cùng sinh viên</span></h1>
                        <p class="hero-description mb-4">QAUTE giúp bạn tìm câu trả lời, gửi yêu cầu tư vấn, đặt lịch và kết nối với cố vấn phù hợp trong một không gian thân thiện, rõ ràng và bảo mật.</p>
                        <form method="get" class="hero-search d-flex align-items-center gap-2" role="search" action="${pageContext.request.contextPath}/faq">
                            <span class="input-group-text border-0 bg-transparent ps-3 text-secondary"><i class="bi bi-search fs-5"></i></span>
                            <label for="homeSearch" class="visually-hidden">Tìm kiếm câu hỏi</label>
                            <input id="homeSearch" type="search" name="kw" class="form-control" placeholder="Bạn đang thắc mắc điều gì?" autocomplete="off">
                            <button type="submit" class="btn btn-primary px-4 py-3 fw-bold">Tìm kiếm</button>
                        </form>
                        <div class="d-flex flex-wrap align-items-center gap-2 mt-3">
                            <span class="small fw-bold me-1">Gợi ý:</span>
                            <a class="quick-topic" href="${pageContext.request.contextPath}/faq?kw=%C4%91%C4%83ng%20k%C3%BD%20h%E1%BB%8Dc%20ph%E1%BA%A7n">Đăng ký học phần</a>
                            <a class="quick-topic" href="${pageContext.request.contextPath}/faq?kw=h%E1%BB%8Dc%20b%E1%BB%95ng">Học bổng</a>
                            <a class="quick-topic" href="${pageContext.request.contextPath}/faq?kw=%C4%91%E1%BB%93%20%C3%A1n%20t%E1%BB%91t%20nghi%E1%BB%87p">Đồ án tốt nghiệp</a>
                            <a class="quick-topic" href="${pageContext.request.contextPath}/faq?kw=th%E1%BB%B1c%20t%E1%BA%ADp">Thực tập</a>
                            <a class="quick-topic" href="${pageContext.request.contextPath}/faq?kw=k%C3%BD%20t%C3%BAc%20x%C3%A1">Ký túc xá</a>
                            <a class="quick-topic" href="${pageContext.request.contextPath}/faq?kw=h%E1%BB%8Dc%20v%E1%BB%A5">Học vụ</a>
                        </div>
                </div>
            </div>
        </div>
    </section>

    <section class="home-section">
        <div class="container">
            <div class="d-flex justify-content-between align-items-end gap-3 mb-3">
                <div><div class="section-kicker">Khám phá theo nhu cầu</div><h2 class="section-title mb-0">Danh mục tư vấn</h2></div>
                <a class="small fw-semibold text-decoration-none d-none d-sm-inline" href="${pageContext.request.contextPath}/faq">Xem tất cả <i class="bi bi-arrow-right"></i></a>
            </div>
            <div class="row g-3">
                <div class="col-sm-6 col-lg-4"><a class="category-card d-flex gap-3" href="${pageContext.request.contextPath}/faq?kw=h%E1%BB%8Dc%20v%E1%BB%A5"><span class="category-icon"><i class="bi bi-mortarboard fs-4"></i></span><span><strong class="d-block mb-1">Học vụ</strong><small class="text-secondary">Đăng ký học phần, lịch học, thi cử và chuyển ngành.</small></span></a></div>
                <div class="col-sm-6 col-lg-4"><a class="category-card d-flex gap-3" href="${pageContext.request.contextPath}/faq?kw=ch%C6%B0%C6%A1ng%20tr%C3%ACnh%20%C4%91%C3%A0o%20t%E1%BA%A1o"><span class="category-icon"><i class="bi bi-book fs-4"></i></span><span><strong class="d-block mb-1">Chương trình đào tạo</strong><small class="text-secondary">Ngành học, chuyên ngành, chuẩn đầu ra và quy định.</small></span></a></div>
                <div class="col-sm-6 col-lg-4"><a class="category-card d-flex gap-3" href="${pageContext.request.contextPath}/faq?kw=h%E1%BB%8Dc%20b%E1%BB%95ng"><span class="category-icon"><i class="bi bi-coin fs-4"></i></span><span><strong class="d-block mb-1">Học bổng – Tài chính</strong><small class="text-secondary">Học phí, miễn giảm, học bổng và hỗ trợ tài chính.</small></span></a></div>
                <div class="col-sm-6 col-lg-4"><a class="category-card d-flex gap-3" href="${pageContext.request.contextPath}/faq?kw=th%E1%BB%B1c%20t%E1%BA%ADp%20vi%E1%BB%87c%20l%C3%A0m"><span class="category-icon"><i class="bi bi-briefcase fs-4"></i></span><span><strong class="d-block mb-1">Thực tập – Việc làm</strong><small class="text-secondary">Cơ hội thực tập, định hướng nghề nghiệp và việc làm.</small></span></a></div>
                <div class="col-sm-6 col-lg-4"><a class="category-card d-flex gap-3" href="${pageContext.request.contextPath}/faq?kw=%C4%91%E1%BB%9Di%20s%E1%BB%91ng%20sinh%20vi%C3%AAn"><span class="category-icon"><i class="bi bi-people fs-4"></i></span><span><strong class="d-block mb-1">Đời sống sinh viên</strong><small class="text-secondary">Câu lạc bộ, hoạt động, ký túc xá và đời sống.</small></span></a></div>
                <div class="col-sm-6 col-lg-4"><a class="category-card d-flex gap-3" href="${pageContext.request.contextPath}/faq"><span class="category-icon"><i class="bi bi-three-dots fs-4"></i></span><span><strong class="d-block mb-1">Khác</strong><small class="text-secondary">Các thắc mắc khác liên quan đến sinh viên.</small></span></a></div>
            </div>
        </div>
    </section>

    <section class="home-section pt-0">
        <div class="container">
            <div class="row g-4">
                <div class="col-lg-8">
                    <div class="qa-panel">
                        <div class="qa-tabs d-flex align-items-center gap-1">
                            <button type="button" class="qa-tab active"><i class="bi bi-chat-square-text me-1"></i> Mới cập nhật</button>
                            <button type="button" class="qa-tab"><i class="bi bi-fire me-1"></i> Xem nhiều</button>
                            <a class="qa-tab text-decoration-none" href="${pageContext.request.contextPath}/faq"><i class="bi bi-grid me-1"></i> Theo chủ đề</a>
                            <sec:authorize access="hasRole('SinhVien')"><a class="btn btn-primary btn-sm ms-auto px-3 d-none d-md-inline-flex" href="${pageContext.request.contextPath}/student/tickets/new"><i class="bi bi-pencil me-1"></i> Đặt câu hỏi</a>
                            </sec:authorize><sec:authorize access="!isAuthenticated()"><a class="btn btn-primary btn-sm ms-auto px-3 d-none d-md-inline-flex" href="${pageContext.request.contextPath}/login"><i class="bi bi-pencil me-1"></i> Đặt câu hỏi</a>
                        </sec:authorize></div>
                        <c:if test="${empty faqs}"><div class="text-center py-5 text-secondary"><i class="bi bi-chat-square-dots fs-1 d-block mb-2"></i>Chưa có câu hỏi công khai.</div>
                        </c:if><c:forEach var="faq" items="${faqs}" varStatus="stat"><c:if test="${stat.index lt 6}"><article class="question-row">
                            <div class="question-stat"><strong><c:out value="${faq.luotXem != null ? faq.luotXem : 0}" /></strong>lượt xem</div>
                            <div>
                                <a class="question-title d-block mb-2" href="${pageContext.request.contextPath}/faq?kw=${faq.cauHoi}"><c:out value="${faq.cauHoi}" /></a>
                                <div class="d-flex flex-wrap gap-2">
                                    <c:if test="${faq.linhVuc != null}"><span class="badge question-tag"><c:out value="${faq.linhVuc.tenLinhVuc}" /></span>
                                    </c:if><span class="small text-secondary text-truncate" style="max-width: 430px"><c:out value="${faq.tuKhoa}" /></span>
                                </div>
                            </div>
                            <div class="small text-secondary text-end align-self-center"><i class="bi bi-shield-check d-block text-primary fs-5"></i>Đã duyệt</div>
                        </article>
                        </c:if></c:forEach><div class="p-3 border-top text-center"><a class="text-decoration-none fw-semibold" href="${pageContext.request.contextPath}/faq">Xem toàn bộ câu hỏi <i class="bi bi-arrow-right"></i></a></div>
                    </div>
                </div>

                <aside class="col-lg-4">
                    <div class="qaute-card side-cta p-4 mb-3">
                        <div class="category-icon mb-3"><i class="bi bi-lightbulb fs-4"></i></div>
                        <h3 class="h5 mb-2">Bạn có câu hỏi?</h3>
                        <p class="small text-secondary mb-3">Gửi yêu cầu để nhận hỗ trợ phù hợp từ đội ngũ cố vấn.</p>
                        <sec:authorize access="hasRole('SinhVien')"><a class="btn btn-primary w-100" href="${pageContext.request.contextPath}/student/tickets/new"><i class="bi bi-pencil-square me-1"></i> Đặt câu hỏi ngay</a>
                        </sec:authorize><sec:authorize access="!isAuthenticated()"><a class="btn btn-primary w-100" href="${pageContext.request.contextPath}/login"><i class="bi bi-box-arrow-in-right me-1"></i> Đăng nhập để đặt câu hỏi</a>
                        </sec:authorize><sec:authorize access="hasRole('CoVan')"><a class="btn btn-primary w-100" href="${pageContext.request.contextPath}/advisor/dashboard"><i class="bi bi-briefcase me-1"></i> Vào bàn cố vấn</a>
                        </sec:authorize><sec:authorize access="hasRole('Admin')"><a class="btn btn-primary w-100" href="${pageContext.request.contextPath}/admin/dashboard"><i class="bi bi-speedometer2 me-1"></i> Vào trang quản trị</a>
                    </sec:authorize></div>
                    <div class="qaute-card p-4">
                        <div class="d-flex justify-content-between align-items-center mb-3"><h3 class="h6 mb-0"><i class="bi bi-person-check text-primary me-1"></i> Cố vấn đang hỗ trợ</h3><a class="small text-decoration-none" href="${pageContext.request.contextPath}/advisors">Xem thêm</a></div>
                        <c:if test="${empty advisors}"><div class="small text-secondary">Danh sách cố vấn đang được cập nhật.</div>
                        </c:if><c:forEach var="advisor" items="${advisors}" varStatus="stat"><c:if test="${stat.index lt 4}"><div class="d-flex align-items-center gap-3 py-2 border-bottom">
                            <div class="advisor-avatar">
                                <c:if test="${advisor.taiKhoan.avatarUrl != null and not empty advisor.taiKhoan.avatarUrl}"><img alt="Ảnh cố vấn" src="${advisor.taiKhoan.avatarUrl}">
                                </c:if><c:if test="${not (advisor.taiKhoan.avatarUrl != null and not empty advisor.taiKhoan.avatarUrl)}"><span><c:out value="${advisor.taiKhoan.avatarInitial}" /></span>
                            </c:if></div>
                            <div class="min-w-0"><div class="small fw-bold text-truncate"><c:out value="${advisor.taiKhoan.hoTen}" /></div><div class="small text-secondary text-truncate"><c:out value="${advisor.donVi.tenDonVi}" /></div></div>
                        </div>
                    </c:if></c:forEach></div>
                </aside>
            </div>
        </div>
    </section>

    <section class="home-section qaute-section-bg border-top">
        <div class="container">
            <div class="d-flex justify-content-between align-items-end mb-3">
                <div><div class="section-kicker">Kiến thức hữu ích</div><h2 class="section-title mb-0">Bài viết dành cho sinh viên</h2></div>
                <a class="small fw-semibold text-decoration-none" href="${pageContext.request.contextPath}/articles">Xem tất cả <i class="bi bi-arrow-right"></i></a>
            </div>
            <div class="row g-3">
                <c:forEach var="article" items="${articles}" varStatus="stat"><c:if test="${stat.index lt 3}"><div class="col-md-6 col-lg-4">
                    <article class="qaute-card qaute-card-hover article-card h-100"><div class="article-accent"></div><div class="p-4 d-flex flex-column h-100">
                        <div class="small text-primary fw-semibold mb-2"><c:out value="${article.linhVuc != null ? article.linhVuc.tenLinhVuc : 'Hướng dẫn'}" /></div>
                        <h3 class="h6"><a class="text-dark text-decoration-none" href="${pageContext.request.contextPath}/articles/${article.maBaiViet}"><c:out value="${article.tieuDe}" /></a></h3>
                        <p class="small text-secondary flex-grow-1"><c:out value="${article.tomTat}" /></p>
                        <div class="d-flex justify-content-between align-items-center small text-secondary pt-3 border-top"><span><i class="bi bi-eye me-1"></i><span><c:out value="${article.luotXem != null ? article.luotXem : 0}" /></span></span><a class="fw-semibold text-decoration-none" href="${pageContext.request.contextPath}/articles/${article.maBaiViet}">Đọc bài <i class="bi bi-arrow-right"></i></a></div>
                    </div></article>
                </div>
            </c:if></c:forEach></div>
        </div>
    </section>
</main>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>
</body>
</html>
