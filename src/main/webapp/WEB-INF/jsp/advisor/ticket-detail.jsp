<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Chi tiết câu hỏi diễn đàn #${ticket.maYeuCau}" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4">
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a class="text-decoration-none" href="${pageContext.request.contextPath}/advisor/dashboard">Bàn cố vấn</a></li>
            <li class="breadcrumb-item"><a class="text-decoration-none" href="${pageContext.request.contextPath}/advisor/tickets">Câu hỏi diễn đàn</a></li>
            <li class="breadcrumb-item active"><c:out value="${'#' += ticket.maYeuCau}" /></li>
        </ol>
    </nav>

    <!-- Alerts -->
    <c:if test="${not empty successMessage}"><div class="alert alert-success py-2 small mb-3">
        <i class="bi bi-check-circle-fill me-1"></i><span><c:out value="${successMessage}" /></span>
    </div>
    </c:if><c:if test="${not empty errorMessage}"><div class="alert alert-danger py-2 small mb-3">
        <i class="bi bi-exclamation-triangle-fill me-1"></i><span><c:out value="${errorMessage}" /></span>
    </div>

    </c:if><div class="row g-4">
        <!-- Main Processing Area -->
        <div class="col-lg-8">
            <!-- Ticket Info Card -->
            <div class="qaute-card p-4 mb-4 border-0 shadow-sm">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <span class="text-muted small">Câu hỏi diễn đàn <strong><c:out value="${'#' += ticket.maYeuCau}" /></strong></span>
                    <span class="${'status-badge status-' += ticket.trangThai}"><c:out value="${ticket.trangThai}" /></span>
                </div>

                <h3 class="fw-bold text-dark mb-3"><c:out value="${ticket.tieuDe}" /></h3>

                <div class="d-flex flex-wrap gap-3 small text-muted mb-4 pb-3 border-bottom">
                    <span><i class="bi bi-building me-1"></i><span><c:out value="${ticket.donVi.tenDonVi}" /></span></span>
                    <span><i class="bi bi-tag me-1"></i><span><c:out value="${ticket.linhVuc.tenLinhVuc}" /></span></span>
                    <span><i class="bi bi-clock me-1"></i>Gửi lúc: <span><c:out value="${q:formatTemporal(ticket.ngayTao, 'dd/MM/yyyy HH:mm')}" /></span></span>
                    <c:if test="${ticket.baoMatDanhTinh}"><span class="badge bg-secondary-subtle text-secondary">
                        <i class="bi bi-shield-lock me-1"></i>Sinh viên yêu cầu bảo mật danh tính
                    </span>
                </c:if></div>

                <!-- Original Student Question -->
                <div class="p-3 bg-light rounded-3 mb-2 border">
                    <div class="fw-bold text-dark mb-2 d-flex align-items-center gap-2">
                        <i class="bi bi-question-circle-fill text-primary"></i>
                        <span>Nội dung câu hỏi của Sinh viên:</span>
                    </div>
                    <div class="text-secondary" style="white-space: pre-line; line-height: 1.7;"><c:out value="${ticket.noiDung}" /></div>
                    <c:if test="${not empty ticketAttachments}"><div class="mt-3 d-flex flex-wrap gap-2">
                        <c:forEach var="file" items="${ticketAttachments}"><a class="btn btn-sm btn-outline-primary" target="_blank" rel="noopener" href="${pageContext.request.contextPath}/attachments/${file.maTep}"><i class="bi bi-paperclip"></i> <span><c:out value="${file.tenGoc}" /></span></a>
                    </c:forEach></div>
                </c:if></div>
            </div>

            <!-- Conversation History -->
            <h5 class="fw-bold text-dark mb-3"><i class="bi bi-chat-square-dots-fill text-primary me-2"></i>Lịch sử trao đổi</h5>
            <a class="btn btn-sm btn-outline-danger mb-3" href="${pageContext.request.contextPath}/advisor/tickets/${ticket.maYeuCau}/record.pdf"><i class="bi bi-file-earmark-pdf"></i> In biên bản PDF</a>
            <div class="d-flex flex-column gap-3 mb-4">
                <c:forEach var="ph" items="${ticket.phanHoiList}"><div class="${ph.loaiPhanHoi == 'TraLoi' ? 'qaute-card p-4 border-start border-4 border-success shadow-sm' : 'qaute-card p-4 border-start border-4 border-primary shadow-sm'}">

                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <div class="d-flex align-items-center gap-2">
                            <c:if test="${ph.loaiPhanHoi == 'TraLoi'}"><span class="badge bg-success-subtle text-success">
                                <i class="bi bi-check-circle me-1"></i>Câu trả lời của bạn
                            </span>
                            </c:if><c:if test="${ph.loaiPhanHoi == 'HoiThem'}"><span class="badge bg-primary-subtle text-primary">
                                <i class="bi bi-question-circle me-1"></i>Sinh viên hỏi thêm
                            </span>
                            </c:if><strong class="text-dark small"><c:out value="${ph.taiKhoanGui.hoTen}" /></strong>
                        </div>
                        <small class="text-muted"><i class="bi bi-clock me-1"></i><span><c:out value="${q:formatTemporal(ph.ngayTraLoi, 'dd/MM/yyyy HH:mm')}" /></span></small>
                    </div>

                    <div class="text-secondary" style="white-space: pre-line; line-height: 1.8;"><c:out value="${ph.noiDungTraLoi}" /></div>
                    <c:if test="${replyAttachments[ph.maPhanHoi] != null and not empty replyAttachments[ph.maPhanHoi]}"><div class="mt-2 d-flex flex-wrap gap-2">
                        <c:forEach var="file" items="${replyAttachments[ph.maPhanHoi]}"><a class="btn btn-sm btn-outline-primary" target="_blank" rel="noopener" href="${pageContext.request.contextPath}/attachments/${file.maTep}"><i class="bi bi-paperclip"></i> <span><c:out value="${file.tenGoc}" /></span></a>
                    </c:forEach></div>
                </c:if></div>
            </c:forEach></div>

            <!-- Reply Form -->
            <div class="qaute-card p-4 border-0 shadow-sm mb-4">
                <h5 class="fw-bold text-dark mb-2"><i class="bi bi-reply-all-fill text-success me-1"></i>Soạn câu trả lời tư vấn</h5>
                <p class="text-muted small mb-3">Câu trả lời sẽ được lưu trữ và thông báo ngay đến tài khoản của sinh viên.</p>

                <!-- Canned Responses dropdown -->
                <c:if test="${not (empty mauTraLoiList)}"><div class="mb-3">
                    <label class="form-label small fw-semibold text-secondary">
                        <i class="bi bi-lightning-charge me-1 text-warning"></i>Sử dụng Mẫu câu trả lời có sẵn:
                    </label>
                    <select class="form-select form-select-sm" onchange="applyCannedResponse(this.value)">
                        <option value="" selected>-- Chọn mẫu câu trả lời gợi ý --</option>
                        <c:forEach var="m" items="${mauTraLoiList}"><option value="${m.noiDung}"><c:out value="${m.tieuDe}" /></option>
                    </c:forEach></select>
                </div>

                </c:if><form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/advisor/tickets/${ticket.maYeuCau}/reply">
                    <sec:csrfInput /><div class="mb-3">
                        <label class="form-label small fw-semibold">Nội dung giải đáp chi tiết *</label>
                        <textarea name="noiDung" id="replyTextarea" rows="6" class="form-control" placeholder="Nhập câu trả lời giải đáp căn cứ theo quy định của Trường..." required></textarea>
                    </div>
                    <div class="mb-3"><label class="form-label small fw-semibold">Tệp minh chứng (không bắt buộc)</label><input type="file" name="attachment" class="form-control" accept=".jpg,.jpeg,.png,.pdf"><div class="form-text">JPG, PNG hoặc PDF; nhỏ hơn 10 MB.</div></div>

                    <div class="form-check mb-4">
                        <input class="form-check-input" type="checkbox" name="deXuatFaq" value="true" id="deXuatCheck">
                        <label class="form-check-label small fw-semibold text-dark" for="deXuatCheck">
                            <i class="bi bi-lightbulb text-warning me-1"></i>Đề xuất đưa câu hỏi &amp; câu trả lời này vào mục <strong>Hỏi đáp FAQ chung</strong>
                        </label>
                        <div class="text-muted" style="font-size: 0.78rem;">
                            (Sẽ chuyển vào danh sách chờ Ban Quản trị phê duyệt trước khi công khai)
                        </div>
                    </div>

                    <div class="d-flex justify-content-between align-items-center">
                        <button type="button" class="btn btn-outline-danger btn-sm rounded-pill" data-bs-toggle="modal" data-bs-target="#transferModal">
                            <i class="bi bi-arrow-left-right me-1"></i>Chuyển tiếp đơn vị khác
                        </button>
                        <button type="submit" class="btn btn-success btn-sm rounded-pill px-4 fw-bold">
                            <i class="bi bi-send-check me-1"></i>Gửi phản hồi tư vấn
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- Sidebar Info -->
        <div class="col-lg-4">
            <!-- Student Information Card -->
            <div class="qaute-card p-4 mb-4 border-0 shadow-sm">
                <h6 class="fw-bold mb-3 border-bottom pb-2">Thông tin Sinh viên</h6>
                <div class="d-flex flex-column gap-2 small">
                    <div>
                        <span class="text-muted">Họ và tên:</span>
                        <strong class="d-block text-dark"><c:out value="${ticket.sinhVien.taiKhoan.hoTen}" /></strong>
                    </div>
                    <div>
                        <span class="text-muted">Mã số sinh viên:</span>
                        <span class="d-block fw-semibold text-primary"><c:out value="${ticket.sinhVien.mssv}" /></span>
                    </div>
                    <div>
                        <span class="text-muted">Lớp &amp; Ngành:</span>
                        <span class="d-block"><c:out value="${ticket.sinhVien.lop += ' - ' += ticket.sinhVien.nganh}" /></span>
                    </div>
                    <div>
                        <span class="text-muted">Email liên hệ:</span>
                        <span class="d-block text-truncate"><c:out value="${ticket.sinhVien.taiKhoan.email}" /></span>
                    </div>
                    <div>
                        <span class="text-muted">Số điện thoại:</span>
                        <span class="d-block"><c:out value="${ticket.sinhVien.taiKhoan.soDienThoai != null ? ticket.sinhVien.taiKhoan.soDienThoai : 'Chưa cập nhật'}" /></span>
                    </div>
                </div>
            </div>

            <!-- SLA Card -->
            <div class="qaute-card p-4 border-0 shadow-sm">
                <h6 class="fw-bold mb-3 border-bottom pb-2">Mốc thời gian &amp; SLA</h6>
                <div class="d-flex flex-column gap-2 small">
                    <div>
                        <span class="text-muted">Ngày gửi:</span>
                        <span class="d-block"><c:out value="${q:formatTemporal(ticket.ngayTao, 'dd/MM/yyyy HH:mm')}" /></span>
                    </div>
                    <div>
                        <span class="text-muted">Hạn SLA phản hồi đầu:</span>
                        <span class="d-block fw-bold text-danger"><c:out value="${q:formatTemporal(ticket.hanSLA, 'dd/MM/yyyy HH:mm')}" /></span>
                    </div>
                    <c:if test="${ticket.ngayPhanHoiDau != null}"><div>
                        <span class="text-muted">Đã phản hồi lần đầu lúc:</span>
                        <span class="d-block text-success fw-semibold"><c:out value="${q:formatTemporal(ticket.ngayPhanHoiDau, 'dd/MM/yyyy HH:mm')}" /></span>
                    </div>
                </c:if></div>
            </div>
        </div>
    </div>
</div>

<!-- Transfer Modal -->
<div class="modal fade" id="transferModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content border-0 shadow">
            <div class="modal-header">
                <h5 class="modal-title fw-bold text-dark">Chuyển tiếp câu hỏi diễn đàn</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form method="post" action="${pageContext.request.contextPath}/advisor/tickets/${ticket.maYeuCau}/transfer">
                <sec:csrfInput /><div class="modal-body">
                    <p class="small text-secondary mb-3">
                        Nếu câu hỏi thuộc phạm vi thẩm quyền của đơn vị khác, hãy chuyển tiếp để đơn vị phụ trách tiếp nhận và xử lý.
                    </p>
                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Đơn vị mới tiếp nhận *</label>
                        <select name="maDonViMoi" class="form-select form-select-sm" required>
                            <option value="" disabled selected>-- Chọn đơn vị chuyển tiếp --</option>
                            <c:forEach var="dv" items="${donViList}"><c:if test="${dv.maDonVi != ticket.donVi.maDonVi}"><option value="${dv.maDonVi}"><c:out value="${dv.tenDonVi}" /></option>
                        </c:if></c:forEach></select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Lĩnh vực chuyển tiếp *</label>
                        <select name="maLinhVucMoi" class="form-select form-select-sm" required>
                            <option value="" disabled selected>-- Chọn lĩnh vực --</option>
                            <c:forEach var="lv" items="${linhVucList}"><option value="${lv.maLinhVuc}"><c:out value="${lv.tenLinhVuc}" /></option>
                        </c:forEach></select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Lý do chuyển tiếp *</label>
                        <textarea name="lyDo" rows="3" class="form-control form-control-sm" placeholder="VD: Vấn đề thuộc thẩm quyền giải quyết học phí của Phòng CTSV..." required></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary btn-sm rounded-pill" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-danger btn-sm rounded-pill px-4 fw-bold">Xác nhận chuyển</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
function applyCannedResponse(text) {
    if (text) {
        document.getElementById('replyTextarea').value = text;
    }
}
</script>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
