<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Tạo phiếu tư vấn mới" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4">
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a class="text-decoration-none" href="${pageContext.request.contextPath}/student/dashboard">Cổng sinh viên</a></li>
            <li class="breadcrumb-item"><a class="text-decoration-none" href="${pageContext.request.contextPath}/student/tickets">Phiếu tư vấn</a></li>
            <li class="breadcrumb-item active">Gửi câu hỏi mới</li>
        </ol>
    </nav>

    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="qaute-card p-4 p-md-5 border-0 shadow-sm">
                <div class="d-flex align-items-center gap-3 mb-4 pb-3 border-bottom">
                    <div class="bg-primary bg-opacity-10 text-primary rounded-circle d-flex align-items-center justify-content-center" style="width: 54px; height: 54px;">
                        <i class="bi bi-pencil-square fs-3"></i>
                    </div>
                    <div>
                        <h4 class="fw-bold text-dark mb-1">Gửi Phiếu Tư Vấn Mới</h4>
                        <p class="text-muted small mb-0">Biểu mẫu BM02 - Tiếp nhận yêu cầu tư vấn học đường</p>
                    </div>
                </div>

                <c:if test="${not empty errorMessage}"><div class="alert alert-danger py-2 small mb-4">
                    <i class="bi bi-exclamation-triangle-fill me-1"></i><span><c:out value="${errorMessage}" /></span>
                </div>

                </c:if><!-- Guidance Info Box -->
                <div class="alert alert-info py-2 small mb-4 d-flex align-items-center gap-2 border-0 bg-info bg-opacity-10 text-info-emphasis">
                    <i class="bi bi-info-circle-fill fs-5 text-info"></i>
                    <div>
                        <strong>Cam kết dịch vụ SLA:</strong> Cố vấn sẽ tiếp nhận xử lý trong vòng <strong>24 giờ làm việc</strong> và có phản hồi chính thức trước <strong>48 giờ làm việc</strong>.
                    </div>
                </div>

                <form method="post" enctype="multipart/form-data" id="ticketForm" action="${pageContext.request.contextPath}/student/tickets/new">
                    <sec:csrfInput /><!-- Department & Field Selector -->
                    <div class="row g-3 mb-3">
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold">1. Chọn Đơn vị phụ trách *</label>
                            <select name="maDonVi" id="donViSelect" class="form-select" required onchange="loadLinhVuc(this.value)">
                                <option value="" disabled selected>-- Chọn phòng/khoa --</option>
                                <c:forEach var="dv" items="${donViList}"><option value="${dv.maDonVi}"><c:out value="${dv.tenDonVi}" /></option>
                            </c:forEach></select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold">2. Chọn Lĩnh vực cụ thể *</label>
                            <select name="maLinhVuc" id="linhVucSelect" class="form-select" required>
                                <option value="" disabled selected>-- Chọn lĩnh vực --</option>
                                <c:forEach var="lv" items="${linhVucList}"><option value="${lv.maLinhVuc}"><c:out value="${lv.tenLinhVuc}" /></option>
                            </c:forEach></select>
                        </div>
                    </div>

                    <!-- Title -->
                    <div class="mb-3">
                        <div class="d-flex justify-content-between">
                            <label class="form-label small fw-semibold">3. Tiêu đề câu hỏi / vấn đề cần tư vấn *</label>
                            <span id="titleCounter" class="text-muted small">0/10 ký tự tối thiểu</span>
                        </div>
                        <input type="text" name="tieuDe" id="tieuDeInput" class="form-control" placeholder="Tóm tắt ngắn gọn vấn đề (tối thiểu 10 ký tự)..." required minlength="10" oninput="updateTitleCount()">
                    </div>

                    <!-- Body -->
                    <div class="mb-4">
                        <div class="d-flex justify-content-between">
                            <label class="form-label small fw-semibold">4. Nội dung chi tiết câu hỏi *</label>
                            <span id="bodyCounter" class="text-muted small">0/20 ký tự tối thiểu</span>
                        </div>
                        <textarea name="noiDung" id="noiDungInput" rows="6" class="form-control" placeholder="Mô tả cụ thể hoàn cảnh, câu hỏi và mong muốn hỗ trợ từ cố vấn (tối thiểu 20 ký tự)..." required minlength="20" oninput="updateBodyCount()"></textarea>
                    </div>

                    <div class="mb-4">
                        <label class="form-label small fw-semibold">5. Tệp minh chứng (không bắt buộc)</label>
                        <input type="file" name="attachment" class="form-control" accept=".jpg,.jpeg,.png,.pdf,image/jpeg,image/png,application/pdf">
                        <div class="form-text">Hỗ trợ JPG, PNG hoặc PDF; dung lượng nhỏ hơn 10 MB.</div>
                    </div>

                    <!-- Anonymous Checkbox -->
                    <div class="p-3 bg-light rounded-3 mb-4 border">
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" name="baoMatDanhTinh" value="true" id="baoMatCheck">
                            <label class="form-check-label small fw-semibold text-dark" for="baoMatCheck">
                                <i class="bi bi-shield-lock-fill text-primary me-1"></i>Bảo mật danh tính (Ẩn danh đối với sinh viên khác và màn hình chung)
                            </label>
                            <div class="text-muted" style="font-size: 0.78rem; margin-top: 2px;">
                                Chỉ có Cố vấn trực tiếp thụ lý mới xem được thông tin liên hệ của bạn để hỗ trợ.
                            </div>
                        </div>
                    </div>

                    <!-- Buttons -->
                    <div class="d-flex justify-content-between align-items-center">
                        <a class="btn btn-outline-secondary rounded-pill px-4" href="${pageContext.request.contextPath}/student/tickets">Hủy bỏ</a>
                        <button type="submit" class="btn btn-qaute-primary rounded-pill px-5 fw-bold">
                            <i class="bi bi-send-fill me-1"></i>Gửi phiếu tư vấn
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
function updateTitleCount() {
    const len = document.getElementById('tieuDeInput').value.trim().length;
    const counter = document.getElementById('titleCounter');
    counter.innerText = len + '/10 ký tự tối thiểu';
    if (len >= 10) {
        counter.className = 'text-success small fw-semibold';
    } else {
        counter.className = 'text-danger small';
    }
}

function updateBodyCount() {
    const len = document.getElementById('noiDungInput').value.trim().length;
    const counter = document.getElementById('bodyCounter');
    counter.innerText = len + '/20 ký tự tối thiểu';
    if (len >= 20) {
        counter.className = 'text-success small fw-semibold';
    } else {
        counter.className = 'text-danger small';
    }
}

function loadLinhVuc(maDonVi) {
    if (!maDonVi) return;
    fetch('/api/public/linh-vuc-by-don-vi?maDonVi=' + maDonVi)
        .then(response => response.json())
        .then(data => {
            const select = document.getElementById('linhVucSelect');
            select.innerHTML = '<option value="" disabled selected>-- Chọn lĩnh vực --</option>';
            data.forEach(item => {
                const opt = document.createElement('option');
                opt.value = item.maLinhVuc;
                opt.textContent = item.tenLinhVuc;
                select.appendChild(opt);
            });
        })
        .catch(err => console.error(err));
}
</script>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
