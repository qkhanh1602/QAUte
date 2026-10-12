<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Quản lý khung giờ tư vấn (Slots)" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4" data-appointment-live-root data-context-path="${pageContext.request.contextPath}">
    <p class="small text-secondary" data-appointment-live-status role="status">Đang kết nối cập nhật lịch…</p>
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
        <div>
            <h2 class="fw-bold text-dark m-0">Quản Lý Khung Giờ Tư Vấn (Slots)</h2>
            <p class="text-muted mb-0 small">Thiết lập các khung giờ mở để sinh viên có thể đăng ký lịch tư vấn trực tiếp hoặc trực tuyến</p>
        </div>
        <div class="d-flex flex-wrap gap-2">
            <form method="post" action="${pageContext.request.contextPath}/advisor/slots/copy-previous-week">
                <sec:csrfInput /><button type="submit" class="btn btn-outline-primary rounded-pill px-4">
                    <i class="bi bi-copy me-1"></i>Sao chép lịch tuần trước
                </button>
            </form>
            <button type="button" class="btn btn-qaute-primary rounded-pill px-4" data-bs-toggle="modal" data-bs-target="#newSlotModal">
                <i class="bi bi-plus-circle-fill me-2"></i>Mở khung giờ mới
            </button>
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
    </c:if><c:if test="${nextWeekSlotCount lt 2}"><div class="alert alert-warning d-flex align-items-center gap-2">
        <i class="bi bi-exclamation-triangle-fill"></i>
        <span>Tuần tới bạn mới có <strong><c:out value="${nextWeekSlotCount}" /></strong>/2 khung giờ rảnh. QĐ24 yêu cầu đăng ký tối thiểu 2 khung giờ trước Chủ nhật.</span>
    </div>

    </c:if><!-- Info -->
    <div class="alert alert-info py-2 small mb-4 d-flex align-items-center gap-2 border-0 bg-info bg-opacity-10 text-info-emphasis">
        <i class="bi bi-info-circle-fill fs-5 text-info"></i>
        <div>
            <strong>Quy định thời lượng (QĐ24):</strong> Mỗi slot có thời lượng chuẩn <strong>45 hoặc 60 phút</strong>; không trùng lặp khung giờ trong cùng ngày.
        </div>
    </div>

    <!-- Slots Table -->
    <div class="qaute-card p-4 border-0 shadow-sm">
        <c:if test="${empty slots}"><div class="text-center py-5 text-muted">
            <i class="bi bi-calendar3 fs-1 text-secondary mb-2"></i>
            <h5>Bạn chưa mở khung giờ tư vấn nào</h5>
            <p class="small text-muted mb-3">Hãy tạo các khung giờ rảnh trong tuần để sinh viên có thể xem và đặt lịch.</p>
            <button type="button" class="btn btn-primary btn-sm rounded-pill px-4" data-bs-toggle="modal" data-bs-target="#newSlotModal">
                Mở khung giờ ngay
            </button>
        </div>

        </c:if><c:if test="${not (empty slots)}"><div class="table-responsive">
            <table class="table table-hover align-middle">
                <thead class="table-light">
                    <tr>
                        <th>Mã</th>
                        <th>Ngày tư vấn</th>
                        <th>Khung thời gian</th>
                        <th>Hình thức</th>
                        <th>Địa điểm / Link họp</th>
                        <th>Trạng thái slot</th><th>Chỉnh sửa</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="s" items="${slots}"><tr>
                        <td><strong class="text-secondary"><c:out value="${'#' += s.maKhungGio}" /></strong></td>
                        <td class="fw-bold text-dark"><c:out value="${q:formatTemporal(s.batDau, 'EEEE, dd/MM/yyyy')}" /></td>
                        <td>
                            <span class="badge bg-primary-subtle text-primary border border-primary-subtle px-2 py-1">
                                <i class="bi bi-clock me-1"></i><span><c:out value="${q:formatTemporal(s.batDau, 'HH:mm') += ' - ' += q:formatTemporal(s.ketThuc, 'HH:mm')}" /></span>
                            </span>
                        </td>
                        <td>
                            <c:if test="${s.hinhThuc == 'TrucTiep'}"><span class="badge bg-secondary-subtle text-secondary">
                                <i class="bi bi-geo-alt me-1"></i>Trực tiếp
                            </span>
                            </c:if><c:if test="${s.hinhThuc == 'TrucTuyen'}"><span class="badge bg-info-subtle text-info">
                                <i class="bi bi-camera-video me-1"></i>Trực tuyến
                            </span>
                        </c:if></td>
                        <td class="small text-secondary"><c:out value="${s.diaDiemLink}" /></td>
                        <td>
                            <c:if test="${s.trangThai == 'Mo'}"><span class="badge bg-success-subtle text-success">Đang mở</span>
                            </c:if><c:if test="${s.trangThai == 'Khoa'}"><span class="badge bg-secondary-subtle text-secondary">Đã khóa</span>
                        </c:if></td>
                        <td><details><summary class="text-primary">Sửa / Ngưng</summary>
                        <form method="post" class="d-grid gap-2 mt-2" action="${pageContext.request.contextPath}/advisor/slots/${s.maKhungGio}/update">
                        <sec:csrfInput /><input type="datetime-local" name="start" class="form-control" value="${fn:substring(s.batDau, 0, 16)}">
                        <select name="minutes" class="form-select"><option value="45">45 phút</option><option value="60" ${s.ketThuc.minusMinutes(60).equals(s.batDau) ? 'selected' : ''}>60 phút</option></select>
                        <select name="type" class="form-select"><option value="TrucTiep">Trực tiếp</option><option value="TrucTuyen" ${s.hinhThuc == 'TrucTuyen' ? 'selected' : ''}>Trực tuyến</option></select>
                        <input name="location" class="form-control" value="<c:out value='${s.diaDiemLink}'/>">
                        <button class="btn btn-sm btn-primary">Lưu</button><button name="close" value="true" class="btn btn-sm btn-outline-danger">Ngưng khung giờ</button>
                        </form></details></td>
                    </tr>
                </c:forEach></tbody>
            </table>
        </div>
    </c:if></div>
</div>

<!-- New Slot Modal -->
<div class="modal fade" id="newSlotModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content border-0 shadow">
            <div class="modal-header">
                <h5 class="modal-title fw-bold text-dark">Mở khung giờ tư vấn mới</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form method="post" action="${pageContext.request.contextPath}/advisor/slots/new">
                <sec:csrfInput /><div class="modal-body text-start">
                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Thời điểm bắt đầu *</label>
                        <input type="datetime-local" name="batDau" class="form-control form-control-sm" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Thời lượng tư vấn (QĐ24) *</label>
                        <select name="thoiLuongPhut" class="form-select form-select-sm" required>
                            <option value="45">45 phút</option>
                            <option value="60" selected>60 phút (1 tiếng)</option>
                        </select>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Hình thức tư vấn *</label>
                        <select name="hinhThuc" class="form-select form-select-sm" required>
                            <option value="TrucTiep" selected>Gặp trực tiếp tại Văn phòng</option>
                            <option value="TrucTuyen">Họp trực tuyến (Google Meet / Zoom)</option>
                        </select>
                    </div>

                    <div class="mb-3">
                        <label class="form-label small fw-semibold">Địa điểm phòng hoặc Link họp *</label>
                        <input type="text" name="diaDiemLink" class="form-control form-control-sm" placeholder="VD: Phòng A1-402 hoặc link Google Meet" required>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary btn-sm rounded-pill" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-primary btn-sm rounded-pill px-4 fw-bold">Mở khung giờ</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7.2.1/bundles/stomp.umd.min.js"></script>
<script src="${pageContext.request.contextPath}/js/qaute-appointments.js?v=20261008-3" defer></script>
<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
