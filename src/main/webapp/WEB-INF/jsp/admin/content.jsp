<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Phê duyệt nội dung FAQ & Cẩm nang" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4">
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
        <div>
            <h2 class="fw-bold text-dark m-0">Kiểm Duyệt &amp; Xuất Bản Nội Dung</h2>
            <p class="text-muted mb-0 small">Phê duyệt câu hỏi FAQ và bài viết hướng dẫn trước khi công khai lên cổng thông tin</p>
        </div>
        <div class="d-flex gap-2">
            <button class="btn btn-outline-primary rounded-pill" data-bs-toggle="modal" data-bs-target="#faqEditor"><i class="bi bi-question-circle me-1"></i>Tạo FAQ</button>
            <button class="btn btn-primary rounded-pill" data-bs-toggle="modal" data-bs-target="#articleEditor"><i class="bi bi-pencil-square me-1"></i>Soạn bài viết</button>
        </div>
    </div>

    <!-- Admin Nav -->


    <!-- Alerts -->
    <c:if test="${not empty successMessage}"><div class="alert alert-success py-2 small mb-3">
        <i class="bi bi-check-circle-fill me-1"></i><span><c:out value="${successMessage}" /></span>
    </div>
    </c:if><c:if test="${not empty errorMessage}"><div class="alert alert-danger py-2 small mb-3"><c:out value="${errorMessage}" /></div>

    </c:if><!-- FAQs Section -->
    <div class="qaute-card p-4 border-0 shadow-sm mb-4">
        <h5 class="fw-bold text-dark mb-3"><i class="bi bi-question-circle-fill text-warning me-2"></i>1. Danh sách Câu hỏi FAQ</h5>
        <div class="table-responsive">
            <table class="table table-hover align-middle small">
                <thead class="table-light">
                    <tr>
                        <th>Mã</th>
                        <th>Câu hỏi</th>
                        <th>Lĩnh vực</th>
                        <th>Người đề xuất</th>
                        <th>Trạng thái</th>
                        <th style="width: 130px;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="f" items="${faqs}"><tr>
                        <td><strong><c:out value="${'#' += f.maFAQ}" /></strong></td>
                        <td>
                            <div class="fw-bold text-dark"><c:out value="${f.cauHoi}" /></div>
                            <small class="text-secondary text-truncate d-block" style="max-width: 450px;"><c:out value="${f.cauTraLoi}" /></small>
                        </td>
                        <td><c:out value="${f.linhVuc.tenLinhVuc}" /></td>
                        <td><c:out value="${f.taiKhoanDeXuat.hoTen}" /></td>
                        <td>
                            <c:if test="${f.trangThai == 'HienThi'}"><span class="badge bg-success-subtle text-success">Đã hiển thị</span>
                            </c:if><c:if test="${f.trangThai == 'ChoDuyet'}"><span class="badge bg-warning-subtle text-warning-emphasis">Chờ duyệt</span>
                            </c:if><c:if test="${f.trangThai == 'BanNhao'}"><span class="badge bg-secondary-subtle text-secondary">Bản nháp</span>
                        </c:if></td>
                        <td>
                            <c:if test="${f.trangThai != 'HienThi'}"><form method="post" class="m-0" action="${pageContext.request.contextPath}/admin/content/faq/${f.maFAQ}/approve">
                                <sec:csrfInput /><button type="submit" class="btn btn-success btn-sm rounded-pill px-3 fw-bold">
                                    <i class="bi bi-check2 me-1"></i>Duyệt
                                </button>
                            </form>
                            </c:if><c:if test="${f.trangThai == 'HienThi'}"><span class="text-muted small">Đã công khai</span>
                        </c:if></td>
                    </tr>
                </c:forEach></tbody>
            </table>
        </div>
    </div>

    <!-- Articles Section -->
    <div class="qaute-card p-4 border-0 shadow-sm">
        <h5 class="fw-bold text-dark mb-3"><i class="bi bi-journal-text text-primary me-2"></i>2. Danh sách Bài viết Cẩm nang</h5>
        <div class="table-responsive">
            <table class="table table-hover align-middle small">
                <thead class="table-light">
                    <tr>
                        <th>Mã</th>
                        <th>Tiêu đề bài viết</th>
                        <th>Lĩnh vực</th>
                        <th>Lượt xem</th>
                        <th>Trạng thái</th>
                        <th style="width: 130px;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="b" items="${articles}"><tr>
                        <td><strong><c:out value="${'#' += b.maBaiViet}" /></strong></td>
                        <td class="fw-bold text-dark"><c:out value="${b.tieuDe}" /></td>
                        <td><c:out value="${b.linhVuc.tenLinhVuc}" /></td>
                        <td><c:out value="${b.luotXem}" /></td>
                        <td>
                            <c:if test="${b.trangThai == 'XuatBan'}"><span class="badge bg-success-subtle text-success">Đã xuất bản</span>
                            </c:if><c:if test="${b.trangThai == 'BanNhao'}"><span class="badge bg-secondary-subtle text-secondary">Bản nháp</span>
                        </c:if></td>
                        <td>
                            <c:if test="${b.trangThai != 'XuatBan'}"><form method="post" class="m-0" action="${pageContext.request.contextPath}/admin/content/article/${b.maBaiViet}/approve">
                                <sec:csrfInput /><button type="submit" class="btn btn-primary btn-sm rounded-pill px-3 fw-bold">
                                    <i class="bi bi-check2 me-1"></i>Xuất bản
                                </button>
                            </form>
                            </c:if><c:if test="${b.trangThai == 'XuatBan'}"><span class="text-muted small">Đã công khai</span>
                        </c:if></td>
                    </tr>
                </c:forEach></tbody>
            </table>
        </div>
    </div>
</div>

<div class="modal fade" id="articleEditor" tabindex="-1"><div class="modal-dialog modal-lg"><form class="modal-content" method="post" enctype="multipart/form-data" onsubmit="document.getElementById('articleContent').value=document.getElementById('richEditor').innerHTML" action="${pageContext.request.contextPath}/admin/content/articles/save"><sec:csrfInput /><div class="modal-header"><h5 class="modal-title">Soạn bài viết hướng dẫn</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div><div class="modal-body d-grid gap-3"><input class="form-control" name="title" placeholder="Tiêu đề bài viết" required><select class="form-select" name="fieldId" required><option value="">Chọn lĩnh vực</option><c:forEach var="field" items="${fields}"><option value="${field.maLinhVuc}"><c:out value="${field.tenLinhVuc}" /></option></c:forEach></select><textarea class="form-control" name="summary" maxlength="500" placeholder="Tóm tắt"></textarea><div class="btn-group btn-group-sm" role="group"><button type="button" class="btn btn-outline-secondary" onclick="document.execCommand('bold')"><b>B</b></button><button type="button" class="btn btn-outline-secondary" onclick="document.execCommand('italic')"><i>I</i></button><button type="button" class="btn btn-outline-secondary" onclick="document.execCommand('insertUnorderedList')">Danh sách</button></div><div id="richEditor" class="form-control" contenteditable="true" style="min-height:220px" aria-label="Nội dung bài viết"></div><input type="hidden" id="articleContent" name="content"><input type="file" class="form-control" name="attachment" accept=".jpg,.jpeg,.png,.pdf"><div class="form-text">Đính kèm quy chế JPG, PNG hoặc PDF; nhỏ hơn 10 MB.</div><input class="form-control" name="keywords" placeholder="Từ khóa, phân cách bằng dấu phẩy"><label class="form-check"><input class="form-check-input" type="checkbox" name="publish" value="true"><span class="form-check-label">Xuất bản ngay; bỏ chọn để lưu nháp</span></label></div><div class="modal-footer"><button class="btn btn-primary">Lưu bài viết</button></div></form></div></div>
<div class="modal fade" id="faqEditor" tabindex="-1"><div class="modal-dialog"><form class="modal-content" method="post" action="${pageContext.request.contextPath}/admin/content/faqs/save"><sec:csrfInput /><div class="modal-header"><h5 class="modal-title">Tạo câu hỏi FAQ</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div><div class="modal-body d-grid gap-2"><input class="form-control" name="question" placeholder="Câu hỏi" required><textarea class="form-control" name="answer" rows="5" placeholder="Câu trả lời" required></textarea><select class="form-select" name="fieldId" required><option value="">Chọn lĩnh vực</option><c:forEach var="field" items="${fields}"><option value="${field.maLinhVuc}"><c:out value="${field.tenLinhVuc}" /></option></c:forEach></select><input class="form-control" name="keywords" placeholder="Từ khóa"><label class="form-check"><input class="form-check-input" type="checkbox" name="publish" value="true"><span class="form-check-label">Công khai ngay; bỏ chọn để lưu nháp</span></label></div><div class="modal-footer"><button class="btn btn-primary">Lưu FAQ</button></div></form></div></div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
