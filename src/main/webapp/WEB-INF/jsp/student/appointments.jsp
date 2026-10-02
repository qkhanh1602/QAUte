<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Lịch hẹn tư vấn của tôi" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4">
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
        <div>
            <h2 class="fw-bold text-dark m-0">Lịch Hẹn Tư Vấn Của Tôi</h2>
            <p class="text-muted mb-0 small">Quản lý các buổi hẹn trực tiếp và trực tuyến cùng Cố vấn</p>
        </div>
    </div>

    <!-- Student Nav -->


    <!-- Alerts -->
    <c:if test="${successMessage}"><div class="alert alert-success py-2 small mb-3">
        <i class="bi bi-check-circle-fill me-1"></i><span><c:out value="${successMessage}" /></span>
    </div>
    </c:if><c:if test="${errorMessage}"><div class="alert alert-danger py-2 small mb-3">
        <i class="bi bi-exclamation-triangle-fill me-1"></i><span><c:out value="${errorMessage}" /></span>
    </div>

    </c:if><!-- Navigation Tabs -->
    <ul class="nav nav-pills mb-4 gap-2" id="appointmentTab" role="tablist">
        <li class="nav-item">
            <button class="nav-link active rounded-pill px-4 fw-bold" id="my-tab" data-bs-toggle="tab" data-bs-target="#my-appointments" type="button">
                <i class="bi bi-calendar-event me-1"></i>Lịch hẹn đã đặt
            </button>
        </li>
        <li class="nav-item">
            <button class="nav-link rounded-pill px-4 fw-bold" id="book-tab" data-bs-toggle="tab" data-bs-target="#book-appointment" type="button">
                <i class="bi bi-plus-circle me-1"></i>Đặt lịch hẹn mới (Khung giờ mở)
            </button>
        </li>
    </ul>

    <div class="tab-content" id="appointmentTabContent">
        <!-- TAB 1: My Appointments -->
        <div class="tab-pane fade show active" id="my-appointments" role="tabpanel">
            <div class="qaute-card p-4 border-0 shadow-sm">
                <c:if test="${empty appointments}"><div class="text-center py-5 text-muted">
                    <i class="bi bi-calendar-x fs-1 text-secondary mb-2"></i>
                    <h5>Bạn chưa có lịch hẹn nào</h5>
                    <p class="small text-muted mb-3">Chuyển sang tab "Đặt lịch hẹn mới" để xem các khung giờ trống của Cố vấn.</p>
                </div>

                </c:if><c:if test="${not (empty appointments)}"><div class="table-responsive">
                    <table class="table table-hover align-middle">
                        <thead class="table-light">
                            <tr>
                                <th>Chủ đề buổi hẹn</th>
                                <th>Cố vấn</th>
                                <th>Thời gian</th>
                                <th>Hình thức &amp; Địa điểm</th>
                                <th>Trạng thái</th>
                                <th style="width: 150px;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="a" items="${appointments}"><tr>
                                <td>
                                    <div class="fw-bold text-dark"><c:out value="${a.chuDe}" /></div>
                                    <small class="text-muted"><c:out value="${a.noiDungHen}" /></small>
                                </td>
                                <td>
                                    <div class="fw-semibold small text-primary"><c:out value="${a.nguoiTuVan.taiKhoan.hoTen}" /></div>
                                    <small class="text-muted"><c:out value="${a.nguoiTuVan.donVi.tenDonVi}" /></small>
                                </td>
                                <td>
                                    <div class="fw-semibold small"><c:out value="${q:formatTemporal(a.khungGio.batDau, 'dd/MM/yyyy')}" /></div>
                                    <small class="text-muted"><c:out value="${q:formatTemporal(a.khungGio.batDau, 'HH:mm') += ' - ' += q:formatTemporal(a.khungGio.ketThuc, 'HH:mm')}" /></small>
                                </td>
                                <td>
                                    <c:if test="${a.khungGio.hinhThuc == 'TrucTiep'}"><span class="badge bg-secondary-subtle text-secondary mb-1">
                                        <i class="bi bi-geo-alt me-1"></i>Trực tiếp
                                    </span>
                                    </c:if><c:if test="${a.khungGio.hinhThuc == 'TrucTuyen'}"><span class="badge bg-info-subtle text-info mb-1">
                                        <i class="bi bi-camera-video me-1"></i>Trực tuyến
                                    </span>
                                    </c:if><c:if test="${a.linkHopPhong != null}"><div class="small fw-semibold text-success"><c:out value="${a.linkHopPhong}" /></div>
                                </c:if></td>
                                <td>
                                    <span class="${'status-badge status-' += a.trangThai}"><c:out value="${a.trangThai}" /></span>
                                    <c:if test="${a.lyDo != null}"><div class="small text-danger mt-1"><c:out value="${'Lý do: ' += a.lyDo}" /></div>
                                </c:if></td>
                                <td>
                                    <!-- Cancel Button (if ChoDuyet or DaXacNhan) -->
                                    <c:if test="${a.trangThai == 'ChoDuyet' || a.trangThai == 'DaXacNhan'}"><button type="button" class="btn btn-outline-danger btn-sm rounded-pill px-3" data-bs-toggle="modal" data-bs-target="${'#cancelModal' += a.maLichHen}">
                                        Hủy hẹn
                                    </button>

                                    </c:if><!-- Rate Button (if HoanThanh and not rated yet) -->
                                    <c:if test="${a.trangThai == 'HoanThanh' and a.danhGia == null}"><button type="button" class="btn btn-warning btn-sm rounded-pill px-3 fw-bold text-dark" data-bs-toggle="modal" data-bs-target="${'#rateModal' += a.maLichHen}">
                                        Đánh giá
                                    </button>
                                    </c:if><c:if test="${a.danhGia != null}"><span class="badge bg-success-subtle text-success">
                                        ★ <span><c:out value="${a.danhGia.soSao}" /></span>/5 sao
                                    </span>

                                    </c:if><!-- Cancel Modal -->
                                    <div class="modal fade" tabindex="-1" id="${'cancelModal' += a.maLichHen}">
                                        <div class="modal-dialog">
                                            <div class="modal-content border-0 shadow">
                                                <div class="modal-header">
                                                    <h5 class="modal-title fw-bold text-dark">Hủy lịch hẹn tư vấn</h5>
                                                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                </div>
                                                <form method="post" action="${pageContext.request.contextPath}/student/appointments/${a.maLichHen}/cancel">
                                                    <sec:csrfInput /><div class="modal-body text-start">
                                                        <p class="small text-secondary mb-2">
                                                            <strong>Lưu ý:</strong> Theo quy định, việc hủy hẹn phải được thực hiện <strong>trước ít nhất 12 giờ</strong> so với giờ bắt đầu buổi tư vấn.
                                                        </p>
                                                        <div class="mb-3">
                                                            <label class="form-label small fw-semibold">Lý do hủy lịch hẹn *</label>
                                                            <textarea name="lyDo" rows="3" class="form-control form-control-sm" placeholder="VD: Em bị trùng lịch thi đột xuất..." required></textarea>
                                                        </div>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <button type="button" class="btn btn-secondary btn-sm rounded-pill" data-bs-dismiss="modal">Đóng</button>
                                                        <button type="submit" class="btn btn-danger btn-sm rounded-pill px-4 fw-bold">Xác nhận hủy</button>
                                                    </div>
                                                </form>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Rate Modal -->
                                    <div class="modal fade" tabindex="-1" id="${'rateModal' += a.maLichHen}">
                                        <div class="modal-dialog">
                                            <div class="modal-content border-0 shadow">
                                                <div class="modal-header">
                                                    <h5 class="modal-title fw-bold text-dark">Đánh giá buổi tư vấn</h5>
                                                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                </div>
                                                <form method="post" action="${pageContext.request.contextPath}/student/appointments/${a.maLichHen}/rate">
                                                    <sec:csrfInput /><div class="modal-body text-start">
                                                        <div class="mb-3">
                                                            <label class="form-label small fw-semibold">Chất lượng tư vấn (1 - 5 sao) *</label>
                                                            <select name="soSao" class="form-select form-select-sm" required>
                                                                <option value="5" selected>★★★★★ - Rất hài lòng (5 sao)</option>
                                                                <option value="4">★★★★☆ - Hài lòng (4 sao)</option>
                                                                <option value="3">★★★☆☆ - Bình thường (3 sao)</option>
                                                                <option value="2">★★☆☆☆ - Chưa hài lòng (2 sao)</option>
                                                                <option value="1">★☆☆☆☆ - Rất không hài lòng (1 sao)</option>
                                                            </select>
                                                        </div>
                                                        <div class="mb-3">
                                                            <label class="form-label small fw-semibold">Nhận xét thêm (nếu có)</label>
                                                            <textarea name="nhanXet" rows="3" class="form-control form-control-sm" placeholder="Ý kiến đóng góp giúp nâng cao chất lượng..."></textarea>
                                                        </div>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <button type="button" class="btn btn-secondary btn-sm rounded-pill" data-bs-dismiss="modal">Đóng</button>
                                                        <button type="submit" class="btn btn-warning btn-sm rounded-pill px-4 fw-bold text-dark">Gửi đánh giá</button>
                                                    </div>
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach></tbody>
                    </table>
                </div>
            </c:if></div>
        </div>

        <!-- TAB 2: Book Appointment (Available Slots) -->
        <div class="tab-pane fade" id="book-appointment" role="tabpanel">
            <div class="qaute-card p-4 border-0 shadow-sm">
                <!-- Rules Alert -->
                <div class="alert alert-info py-2 small mb-4 d-flex align-items-center gap-2 border-0 bg-info bg-opacity-10 text-info-emphasis">
                    <i class="bi bi-info-circle-fill fs-5 text-info"></i>
                    <div>
                        <strong>Quy tắc đặt lịch QAUTE:</strong> Mỗi sinh viên được giữ tối đa <strong>2 lịch chờ duyệt</strong>; các khung giờ có thời lượng chuẩn <strong>45 hoặc 60 phút</strong> trong vòng <strong>7 ngày tới</strong>.
                    </div>
                </div>

                <!-- Advisor filter -->
                <form method="get" class="row g-3 align-items-center mb-4" action="${pageContext.request.contextPath}/student/appointments">
                    <div class="col-md-5">
                        <label class="form-label small fw-semibold">Lọc theo Cố vấn học tập:</label>
                        <select name="maCV" class="form-select form-select-sm" onchange="this.form.submit()">
                            <option value="">-- Tất cả Cố vấn --</option>
                            <c:forEach var="cv" items="${advisors}">
                                <c:choose>
                                    <c:when test="${selectedCv == cv.maCV}"><option value="${cv.maCV}" selected><c:out value="${cv.taiKhoan.hoTen += ' (' += cv.donVi.tenDonVi += ')'}" /></option></c:when>
                                    <c:otherwise><option value="${cv.maCV}"><c:out value="${cv.taiKhoan.hoTen += ' (' += cv.donVi.tenDonVi += ')'}" /></option></c:otherwise>
                                </c:choose>
                            </c:forEach></select>
                    </div>
                </form>

                <c:if test="${empty availableSlots}"><div class="text-center py-5 text-muted">
                    <i class="bi bi-calendar-x fs-1 text-secondary mb-2"></i>
                    <h5>Hiện chưa có khung giờ tư vấn nào khả dụng</h5>
                    <p class="small text-muted">Cố vấn chưa mở thêm slot trong 7 ngày tới hoặc các slot đều đã kín chỗ.</p>
                </div>

                </c:if><!-- Available Slots Grid -->
                <c:if test="${not (empty availableSlots)}"><div class="row g-3">
                    <c:forEach var="slot" items="${availableSlots}"><div class="col-md-6 col-lg-4">
                        <div class="qaute-card p-3 border h-100 d-flex flex-column justify-content-between">
                            <div>
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <c:if test="${slot.hinhThuc == 'TrucTiep'}"><span class="badge bg-secondary-subtle text-secondary">
                                        <i class="bi bi-geo-alt me-1"></i>Trực tiếp
                                    </span>
                                    </c:if><c:if test="${slot.hinhThuc == 'TrucTuyen'}"><span class="badge bg-info-subtle text-info">
                                        <i class="bi bi-camera-video me-1"></i>Trực tuyến
                                    </span>
                                    </c:if><span class="badge bg-success-subtle text-success">Còn trống</span>
                                </div>

                                <h6 class="fw-bold text-dark mb-1"><c:out value="${slot.nguoiTuVan.taiKhoan.hoTen}" /></h6>
                                <p class="text-muted small mb-2"><c:out value="${slot.nguoiTuVan.donVi.tenDonVi}" /></p>

                                <div class="p-2 bg-light rounded-3 small mb-2">
                                    <div><i class="bi bi-calendar3 me-1 text-primary"></i><span><c:out value="${q:formatTemporal(slot.batDau, 'EEEE, dd/MM/yyyy')}" /></span></div>
                                    <div><i class="bi bi-clock me-1 text-primary"></i><span class="fw-bold text-dark"><c:out value="${q:formatTemporal(slot.batDau, 'HH:mm') += ' - ' += q:formatTemporal(slot.ketThuc, 'HH:mm')}" /></span></div>
                                </div>
                                <small class="text-muted d-block mb-3"><c:out value="${slot.diaDiemLink}" /></small>
                            </div>

                            <button type="button" class="btn btn-outline-primary btn-sm rounded-pill w-100 fw-semibold" data-bs-toggle="modal" data-bs-target="${'#bookModal' += slot.maKhungGio}">
                                <i class="bi bi-calendar-plus me-1"></i>Đặt khung giờ này
                            </button>

                            <!-- Booking Modal -->
                            <div class="modal fade" tabindex="-1" id="${'bookModal' += slot.maKhungGio}">
                                <div class="modal-dialog">
                                    <div class="modal-content border-0 shadow">
                                        <div class="modal-header">
                                            <h5 class="modal-title fw-bold text-dark">Đăng ký lịch hẹn tư vấn</h5>
                                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                        </div>
                                        <form method="post" action="${pageContext.request.contextPath}/student/appointments/book">
                                            <sec:csrfInput /><input type="hidden" name="maKhungGio" value="${slot.maKhungGio}">
                                            <div class="modal-body text-start">
                                                <div class="p-3 bg-light rounded-3 mb-3 small">
                                                    <div><strong>Cố vấn:</strong> <span><c:out value="${slot.nguoiTuVan.taiKhoan.hoTen}" /></span></div>
                                                    <div><strong>Thời gian:</strong> <span><c:out value="${q:formatTemporal(slot.batDau, 'dd/MM/yyyy HH:mm') += ' - ' += q:formatTemporal(slot.ketThuc, 'HH:mm')}" /></span></div>
                                                    <div><strong>Hình thức:</strong> <span><c:out value="${slot.hinhThuc}" /></span></div>
                                                </div>

                                                <div class="mb-3">
                                                    <label class="form-label small fw-semibold">Chủ đề cần tư vấn *</label>
                                                    <input type="text" name="chuDe" class="form-control form-control-sm" placeholder="VD: Tư vấn chọn hướng đồ án tốt nghiệp" required>
                                                </div>

                                                <div class="mb-3">
                                                    <label class="form-label small fw-semibold">Nội dung chi tiết / Câu hỏi chuẩn bị trước *</label>
                                                    <textarea name="noiDungHen" rows="3" class="form-control form-control-sm" placeholder="Mô tả tóm tắt nội dung bạn muốn trao đổi cùng cố vấn..." required></textarea>
                                                </div>
                                            </div>
                                            <div class="modal-footer">
                                                <button type="button" class="btn btn-secondary btn-sm rounded-pill" data-bs-dismiss="modal">Đóng</button>
                                                <button type="submit" class="btn btn-primary btn-sm rounded-pill px-4 fw-bold">Xác nhận đặt lịch</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach></div>
            </c:if></div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
