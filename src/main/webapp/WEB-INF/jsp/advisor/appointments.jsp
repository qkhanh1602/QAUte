<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Phê duyệt lịch hẹn tư vấn" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4">
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
        <div>
            <h2 class="fw-bold text-dark m-0">Phê Duyệt &amp; Quản Lý Lịch Hẹn</h2>
            <p class="text-muted mb-0 small">Xét duyệt yêu cầu đặt hẹn, cập nhật phòng/link họp và ghi nhận kết luận buổi tư vấn</p>
        </div>
    </div>

    <!-- Advisor Nav -->


    <!-- Alerts -->
    <c:if test="${not empty successMessage}"><div class="alert alert-success py-2 small mb-3">
        <i class="bi bi-check-circle-fill me-1"></i><span><c:out value="${successMessage}" /></span>
    </div>
    </c:if><c:if test="${not empty errorMessage}"><div class="alert alert-danger py-2 small mb-3">
        <i class="bi bi-exclamation-triangle-fill me-1"></i><span><c:out value="${errorMessage}" /></span>
    </div>

    </c:if><!-- Appointments Table -->
    <div class="qaute-card p-4 border-0 shadow-sm">
        <c:if test="${empty appointments}"><div class="text-center py-5 text-muted">
            <i class="bi bi-calendar-check fs-1 text-secondary mb-2"></i>
            <h5>Chưa có lịch hẹn nào được đăng ký</h5>
            <p class="small text-muted mb-0">Khi sinh viên đặt khung giờ tư vấn của bạn, yêu cầu sẽ xuất hiện tại đây.</p>
        </div>

        </c:if><c:if test="${not (empty appointments)}"><div class="table-responsive">
            <table class="table table-hover align-middle">
                <thead class="table-light">
                    <tr>
                        <th>Mã</th>
                        <th>Sinh viên</th>
                        <th>Chủ đề &amp; Nội dung</th>
                        <th>Thời gian</th>
                        <th>Hình thức &amp; Địa điểm</th>
                        <th>Trạng thái</th>
                        <th style="width: 180px;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="a" items="${appointments}"><tr>
                        <td><strong class="text-secondary"><c:out value="${'#' += a.maLichHen}" /></strong></td>
                        <td>
                            <div class="fw-bold text-dark"><c:out value="${a.sinhVien.taiKhoan.hoTen}" /></div>
                            <small class="text-muted"><c:out value="${a.sinhVien.mssv += ' - ' += a.sinhVien.lop}" /></small>
                        </td>
                        <td>
                            <div class="fw-bold text-dark small"><c:out value="${a.chuDe}" /></div>
                            <small class="text-secondary"><c:out value="${a.noiDungHen}" /></small>
                            <c:if test="${a.ketLuan != null}"><div class="small text-success mt-1">
                                <i class="bi bi-check2-all me-1"></i><strong>Kết luận:</strong> <span><c:out value="${a.ketLuan}" /></span>
                            </div>
                        </c:if></td>
                        <td>
                            <div class="small fw-bold text-dark"><c:out value="${q:formatTemporal(a.khungGio.batDau, 'dd/MM/yyyy')}" /></div>
                            <small class="text-primary fw-semibold"><c:out value="${q:formatTemporal(a.khungGio.batDau, 'HH:mm') += ' - ' += q:formatTemporal(a.khungGio.ketThuc, 'HH:mm')}" /></small>
                        </td>
                        <td>
                            <c:if test="${a.khungGio.hinhThuc == 'TrucTiep'}"><span class="badge bg-secondary-subtle text-secondary mb-1">Trực tiếp</span>
                            </c:if><c:if test="${a.khungGio.hinhThuc == 'TrucTuyen'}"><span class="badge bg-info-subtle text-info mb-1">Trực tuyến</span>
                            </c:if><div class="small text-secondary"><c:out value="${a.linkHopPhong != null ? a.linkHopPhong : a.khungGio.diaDiemLink}" /></div>
                        </td>
                        <td>
                            <span class="${'status-badge status-' += a.trangThai}"><c:out value="${a.trangThai}" /></span>
                            <c:if test="${a.lyDo != null}"><div class="small text-danger mt-1"><c:out value="${'Lý do: ' += a.lyDo}" /></div>
                        </c:if></td>
                        <td>
                            <!-- If ChoDuyet: Approve & Reject buttons -->
                            <c:if test="${a.trangThai == 'ChoDuyet'}"><div class="d-flex gap-1">
                                <button type="button" class="btn btn-success btn-sm rounded-pill px-3 fw-bold" data-bs-toggle="modal" data-bs-target="${'#approveModal' += a.maLichHen}">
                                    Duyệt
                                </button>
                                <button type="button" class="btn btn-outline-danger btn-sm rounded-pill px-2" data-bs-toggle="modal" data-bs-target="${'#rejectModal' += a.maLichHen}">
                                    Từ chối
                                </button>
                            </div>

                            </c:if><!-- If DaXacNhan: Complete button -->
                            <c:if test="${a.trangThai == 'DaXacNhan'}"><div>
                                <button type="button" class="btn btn-primary btn-sm rounded-pill px-3 fw-bold" data-bs-toggle="modal" data-bs-target="${'#completeModal' += a.maLichHen}">
                                    Hoàn thành
                                </button>
                            </div>

                            </c:if><!-- If HoanThanh: Rating badge -->
                            <c:if test="${a.trangThai == 'HoanThanh'}"><div>
                                <c:if test="${a.danhGia != null}"><span class="text-warning fw-bold small">
                                    ★ <span><c:out value="${a.danhGia.soSao}" /></span>/5 sao
                                </span>
                                </c:if><c:if test="${not (a.danhGia != null)}"><span class="text-muted small">Đang chờ SV đánh giá</span>
                            </c:if></div>

                            </c:if><!-- Approve Modal -->
                            <div class="modal fade" tabindex="-1" id="${'approveModal' += a.maLichHen}">
                                <div class="modal-dialog">
                                    <div class="modal-content border-0 shadow">
                                        <div class="modal-header">
                                            <h5 class="modal-title fw-bold text-dark">Phê duyệt lịch hẹn tư vấn</h5>
                                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                        </div>
                                        <form method="post" action="${pageContext.request.contextPath}/advisor/appointments/${a.maLichHen}/approve">
                                            <sec:csrfInput /><div class="modal-body text-start">
                                                <div class="mb-3">
                                                    <label class="form-label small fw-semibold">Địa điểm phòng tư vấn hoặc Link họp Google Meet *</label>
                                                    <input type="text" name="linkHopPhong" class="form-control form-control-sm" required value="${a.khungGio.diaDiemLink}">
                                                    <small class="text-muted">Thông tin này sẽ được gửi thông báo đến sinh viên.</small>
                                                </div>
                                            </div>
                                            <div class="modal-footer">
                                                <button type="button" class="btn btn-secondary btn-sm rounded-pill" data-bs-dismiss="modal">Đóng</button>
                                                <button type="submit" class="btn btn-success btn-sm rounded-pill px-4 fw-bold">Xác nhận duyệt</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </div>

                            <!-- Reject Modal -->
                            <div class="modal fade" tabindex="-1" id="${'rejectModal' += a.maLichHen}">
                                <div class="modal-dialog">
                                    <div class="modal-content border-0 shadow">
                                        <div class="modal-header">
                                            <h5 class="modal-title fw-bold text-dark">Từ chối lịch hẹn tư vấn</h5>
                                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                        </div>
                                        <form method="post" action="${pageContext.request.contextPath}/advisor/appointments/${a.maLichHen}/reject">
                                            <sec:csrfInput /><div class="modal-body text-start">
                                                <div class="mb-3">
                                                    <label class="form-label small fw-semibold">Lý do từ chối *</label>
                                                    <textarea name="lyDo" rows="3" class="form-control form-control-sm" placeholder="VD: Thầy bị trùng lịch họp đột xuất của Khoa..." required></textarea>
                                                </div>
                                            </div>
                                            <div class="modal-footer">
                                                <button type="button" class="btn btn-secondary btn-sm rounded-pill" data-bs-dismiss="modal">Đóng</button>
                                                <button type="submit" class="btn btn-danger btn-sm rounded-pill px-4 fw-bold">Xác nhận từ chối</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </div>

                            <!-- Complete Modal -->
                            <div class="modal fade" tabindex="-1" id="${'completeModal' += a.maLichHen}">
                                <div class="modal-dialog">
                                    <div class="modal-content border-0 shadow">
                                        <div class="modal-header">
                                            <h5 class="modal-title fw-bold text-dark">Ghi nhận hoàn tất buổi tư vấn</h5>
                                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                        </div>
                                        <form method="post" action="${pageContext.request.contextPath}/advisor/appointments/${a.maLichHen}/complete">
                                            <sec:csrfInput /><div class="modal-body text-start">
                                                <div class="mb-3">
                                                    <label class="form-label small fw-semibold">Kết luận buổi tư vấn &amp; Hướng giải quyết *</label>
                                                    <textarea name="ketLuan" rows="4" class="form-control form-control-sm" placeholder="VD: Đã định hướng cho sinh viên lựa chọn đề tài AI trong Y tế, giới thiệu tài liệu nghiên cứu..." required></textarea>
                                                </div>
                                            </div>
                                            <div class="modal-footer">
                                                <button type="button" class="btn btn-secondary btn-sm rounded-pill" data-bs-dismiss="modal">Đóng</button>
                                                <button type="submit" class="btn btn-primary btn-sm rounded-pill px-4 fw-bold">Hoàn tất buổi hẹn</button>
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

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
