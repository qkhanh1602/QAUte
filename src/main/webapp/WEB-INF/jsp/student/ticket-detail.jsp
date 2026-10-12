<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Chi tiết câu hỏi" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4">
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a class="text-decoration-none" href="${pageContext.request.contextPath}/student/dashboard">Cổng sinh viên</a></li>
            <li class="breadcrumb-item"><a class="text-decoration-none" href="${pageContext.request.contextPath}/student/tickets">Câu hỏi của tôi</a></li>
            <li class="breadcrumb-item active">Chi tiết câu hỏi</li>
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
        <!-- Main Conversation Area -->
        <div class="col-lg-8">
            <!-- Ticket Info Card -->
            <div class="qaute-card p-4 mb-4 border-0 shadow-sm">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <span class="text-muted small">Câu hỏi</span>
                    <span class="${'status-badge status-' += ticket.trangThai}"><c:out value="${ticket.trangThai}" /></span>
                </div>

                <h3 class="fw-bold text-dark mb-3"><c:out value="${ticket.tieuDe}" /></h3>

                <div class="d-flex flex-wrap gap-3 small text-muted mb-4 pb-3 border-bottom">
                    <span><i class="bi bi-building me-1"></i><span><c:out value="${ticket.donVi.tenDonVi}" /></span></span>
                    <span><i class="bi bi-tag me-1"></i><span><c:out value="${ticket.linhVuc.tenLinhVuc}" /></span></span>
                    <span><i class="bi bi-clock me-1"></i>Gửi lúc: <span><c:out value="${q:formatTemporal(ticket.ngayTao, 'dd/MM/yyyy HH:mm')}" /></span></span>
                    <c:if test="${ticket.baoMatDanhTinh}"><span class="badge bg-secondary-subtle text-secondary">
                        <i class="bi bi-shield-lock me-1"></i>Chế độ ẩn danh
                    </span>
                </c:if></div>

                <!-- Original Student Question -->
                <div class="p-3 bg-light rounded-3 mb-2 border">
                    <div class="fw-bold text-dark mb-2 d-flex align-items-center gap-2">
                        <i class="bi bi-person-circle text-primary"></i>
                        <span>Nội dung câu hỏi ban đầu của bạn:</span>
                    </div>
                    <div class="text-secondary" style="white-space: pre-line; line-height: 1.7;"><c:out value="${ticket.noiDung}" /></div>
                    <c:if test="${not empty ticketAttachments}"><div class="mt-3 d-flex flex-wrap gap-2">
                        <c:forEach var="file" items="${ticketAttachments}"><a class="btn btn-sm btn-outline-primary" target="_blank" rel="noopener" href="${pageContext.request.contextPath}/attachments/${file.maTep}">
                            <i class="bi bi-paperclip"></i> <span><c:out value="${file.tenGoc}" /></span>
                        </a>
                    </c:forEach></div>
                </c:if></div>
            </div>

            <!-- Conversation Responses Stream -->
            <h5 class="fw-bold text-dark mb-3"><i class="bi bi-chat-square-dots-fill text-primary me-2"></i>Tiến trình trao đổi</h5>
            <a class="btn btn-sm btn-outline-danger mb-3" href="${pageContext.request.contextPath}/student/tickets/${ticket.maYeuCau}/record.pdf"><i class="bi bi-file-earmark-pdf"></i> In biên bản PDF</a>

            <c:if test="${empty ticket.phanHoiList}"><div class="qaute-card p-4 text-center text-muted mb-4 border-0 shadow-sm">
                <i class="bi bi-hourglass-split fs-2 text-warning mb-2"></i>
                <h6 class="fw-bold text-dark">Đang chờ phản hồi từ Cố vấn</h6>
                <p class="small text-muted mb-0">Cố vấn chuyên trách đang xem xét nội dung câu hỏi của bạn. Thời hạn xử lý tối đa là 48 giờ liên tục.</p>
            </div>

            </c:if><c:if test="${not (empty ticket.phanHoiList)}"><div class="d-flex flex-column gap-3 mb-4">
                <c:forEach var="ph" items="${ticket.phanHoiList}"><div class="${ph.loaiPhanHoi == 'TraLoi' ? 'qaute-card p-4 border-start border-4 border-success shadow-sm' : 'qaute-card p-4 border-start border-4 border-primary shadow-sm'}">

                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <div class="d-flex align-items-center gap-2">
                            <c:if test="${ph.loaiPhanHoi == 'TraLoi'}"><span class="badge bg-success-subtle text-success border border-success-subtle">
                                <i class="bi bi-check-circle me-1"></i>Phản hồi từ Cố vấn
                            </span>
                            </c:if><c:if test="${ph.loaiPhanHoi == 'HoiThem'}"><span class="badge bg-primary-subtle text-primary border border-primary-subtle">
                                <i class="bi bi-question-circle me-1"></i>Sinh viên hỏi thêm
                            </span>
                            </c:if><strong class="text-dark small"><c:out value="${ph.taiKhoanGui.hoTen}" /></strong>
                        </div>
                        <small class="text-muted"><i class="bi bi-clock me-1"></i><span><c:out value="${q:formatTemporal(ph.ngayTraLoi, 'dd/MM/yyyy HH:mm')}" /></span></small>
                    </div>

                    <div class="text-secondary" style="white-space: pre-line; line-height: 1.8;"><c:out value="${ph.noiDungTraLoi}" /></div>
                    <c:if test="${replyAttachments[ph.maPhanHoi] != null and not empty replyAttachments[ph.maPhanHoi]}"><div class="mt-2 d-flex flex-wrap gap-2">
                        <c:forEach var="file" items="${replyAttachments[ph.maPhanHoi]}"><a class="btn btn-sm btn-outline-primary" target="_blank" rel="noopener" href="${pageContext.request.contextPath}/attachments/${file.maTep}">
                            <i class="bi bi-paperclip"></i> <span><c:out value="${file.tenGoc}" /></span>
                        </a>
                    </c:forEach></div>
                </c:if></div>
            </c:forEach></div>

            </c:if><!-- Follow-up Question Box (if ticket not closed) -->
            <c:if test="${ticket.trangThai == 'DaPhanHoi'}"><div class="qaute-card p-4 border-0 shadow-sm mb-4">
                <h5 class="fw-bold text-dark mb-2"><i class="bi bi-reply-fill text-primary me-1"></i>Bạn có câu hỏi thêm?</h5>
                <p class="text-muted small mb-3">Nếu cần làm rõ câu trả lời, hãy nhập câu hỏi thêm tại đây để tiếp tục trao đổi với cố vấn.</p>
                <form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/student/tickets/${ticket.maYeuCau}/follow-up">
                    <sec:csrfInput /><div class="mb-3">
                        <textarea name="noiDung" rows="4" class="form-control" placeholder="Nhập câu hỏi bổ sung..." required></textarea>
                    </div>
                    <div class="mb-3"><input type="file" name="attachment" class="form-control" accept=".jpg,.jpeg,.png,.pdf"><div class="form-text">JPG, PNG hoặc PDF; nhỏ hơn 10 MB.</div></div>
                    <div class="d-flex justify-content-between align-items-center">
                        <button type="button" class="btn btn-outline-secondary btn-sm rounded-pill" data-bs-toggle="modal" data-bs-target="#closeTicketModal">
                            <i class="bi bi-check2-circle me-1"></i>Đã hiểu và muốn đóng câu hỏi
                        </button>
                        <button type="submit" class="btn btn-primary btn-sm rounded-pill px-4 fw-bold">
                            <i class="bi bi-send me-1"></i>Gửi câu hỏi thêm
                        </button>
                    </div>
                </form>
            </div>

            </c:if><!-- Rating Section (if answered or closed) -->
            <c:if test="${ticket.trangThai == 'DaPhanHoi' || ticket.trangThai == 'Dong'}"><div class="qaute-card p-4 border-0 shadow-sm">
                <c:if test="${ticket.danhGia == null}"><div>
                    <h5 class="fw-bold text-dark mb-2"><i class="bi bi-star-fill text-warning me-2"></i>Đánh giá chất lượng tư vấn</h5>
                    <p class="text-muted small mb-3">Đóng góp ý kiến của bạn giúp Nhà trường nâng cao chất lượng hỗ trợ sinh viên.</p>
                    <form method="post" action="${pageContext.request.contextPath}/student/tickets/${ticket.maYeuCau}/rate">
                        <sec:csrfInput /><div class="mb-3">
                            <label class="form-label small fw-semibold">Số sao đánh giá (1 - 5 sao) *</label>
                            <select name="soSao" class="form-select form-select-sm" style="max-width: 200px;" required>
                                <option value="5" selected>★★★★★ - Rất hài lòng (5 sao)</option>
                                <option value="4">★★★★☆ - Hài lòng (4 sao)</option>
                                <option value="3">★★★☆☆ - Bình thường (3 sao)</option>
                                <option value="2">★★☆☆☆ - Chưa hài lòng (2 sao)</option>
                                <option value="1">★☆☆☆☆ - Rất không hài lòng (1 sao)</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label class="form-label small fw-semibold">Nhận xét thêm (nếu có)</label>
                            <textarea name="nhanXet" rows="2" class="form-control form-control-sm" placeholder="Chia sẻ cảm nghĩ về tốc độ và chất lượng câu trả lời..."></textarea>
                        </div>
                        <button type="submit" class="btn btn-warning text-dark fw-bold btn-sm rounded-pill px-4">
                            Gửi đánh giá
                        </button>
                    </form>
                </div>

                </c:if><c:if test="${ticket.danhGia != null}"><div>
                    <div class="d-flex align-items-center gap-2 mb-2">
                        <i class="bi bi-patch-check-fill text-success fs-5"></i>
                        <h6 class="fw-bold text-dark mb-0">Bạn đã đánh giá câu hỏi này</h6>
                    </div>
                    <div class="rating-stars mb-1">
                        <c:forEach var="i" items="${q:sequence(1, ticket.danhGia.soSao)}"><span>★</span>
                        </c:forEach><c:forEach var="i" items="${q:sequence(ticket.danhGia.soSao + 1, 5)}"><c:if test="${ticket.danhGia.soSao lt 5}"><span class="text-secondary">☆</span>
                        </c:if></c:forEach><strong class="text-dark small ms-2"><c:out value="${ticket.danhGia.soSao += '/5 sao'}" /></strong>
                    </div>
                    <p class="text-secondary small mb-0 fst-italic"><c:out value="${ticket.danhGia.nhanXet}" /></p>
                </div>
            </c:if></div>
        </c:if></div>

        <!-- Sidebar Meta Info -->
        <div class="col-lg-4">
            <div class="qaute-card p-4 mb-4 border-0 shadow-sm">
                <h6 class="fw-bold mb-3 border-bottom pb-2">Thông tin tiếp nhận &amp; SLA</h6>
                <div class="d-flex flex-column gap-3 small">
                    <div>
                        <div class="text-muted">Cố vấn phụ trách:</div>
                        <div class="fw-bold text-primary"><c:out value="${ticket.coVanPhuTrach != null ? ticket.coVanPhuTrach.taiKhoan.hoTen : 'Chưa phân công'}" /></div>
                    </div>
                    <div>
                        <div class="text-muted">Hạn tiếp nhận (24h làm việc):</div>
                        <div class="fw-semibold text-dark"><c:out value="${q:formatTemporal(ticket.hanTiepNhan, 'dd/MM/yyyy HH:mm')}" /></div>
                    </div>
                    <div>
                        <div class="text-muted">Hạn phản hồi chuẩn (48 giờ liên tục):</div>
                        <div class="fw-semibold text-dark"><c:out value="${q:formatTemporal(ticket.hanSLA, 'dd/MM/yyyy HH:mm')}" /></div>
                    </div>
                    <c:if test="${ticket.hanTuDongDong != null}"><div>
                        <div class="text-muted">Hạn tự động đóng:</div>
                        <div class="fw-semibold text-warning"><c:out value="${q:formatTemporal(ticket.hanTuDongDong, 'dd/MM/yyyy')}" /></div>
                        <small class="text-muted" style="font-size: 0.75rem;">(Nếu sau 7 ngày không có câu hỏi thêm)</small>
                    </div>
                </c:if></div>
            </div>

            <!-- Action shortcut -->
            <div class="qaute-card p-4 bg-light border-0 text-center">
                <h6 class="fw-bold mb-2">Cần trao đổi trực tiếp?</h6>
                <p class="small text-muted mb-3">Bạn có thể đặt lịch hẹn tư vấn 1-1 với Cố vấn để thảo luận kỹ hơn.</p>
                <a class="btn btn-outline-success btn-sm rounded-pill w-100 fw-semibold" href="${pageContext.request.contextPath}/student/appointments">
                    <i class="bi bi-calendar-plus me-1"></i>Đặt lịch hẹn gặp
                </a>
            </div>
        </div>
    </div>
</div>

<!-- Close Ticket Modal -->
<div class="modal fade" id="closeTicketModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content border-0 shadow">
            <div class="modal-header">
                <h5 class="modal-title fw-bold text-dark">Xác nhận đóng câu hỏi</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form method="post" action="${pageContext.request.contextPath}/student/tickets/${ticket.maYeuCau}/close">
                <sec:csrfInput /><div class="modal-body">
                    <p class="text-secondary small">
                        Khi đóng câu hỏi, bạn xác nhận rằng câu hỏi đã được giải đáp thỏa đáng và không cần hỏi thêm.
                    </p>
                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Ghi chú kết luận (tùy chọn)</label>
                        <textarea name="ketLuan" rows="3" class="form-control form-control-sm" placeholder="VD: Đã hiểu rõ quy định rút học phần..."></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary btn-sm rounded-pill" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-success btn-sm rounded-pill px-4 fw-bold">Xác nhận đóng</button>
                </div>
            </form>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
