<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Đặt câu hỏi trên diễn đàn" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4">
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a class="text-decoration-none" href="${pageContext.request.contextPath}/student/dashboard">Cổng sinh viên</a></li>
            <li class="breadcrumb-item"><a class="text-decoration-none" href="${pageContext.request.contextPath}/forum">Diễn đàn</a></li>
            <li class="breadcrumb-item active">Đặt câu hỏi</li>
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
                        <h4 class="fw-bold text-dark mb-1">Đặt câu hỏi trên diễn đàn</h4>
                        <p class="text-muted small mb-0">Câu hỏi và nội dung trao đổi sẽ được hiển thị công khai trên diễn đàn.</p>
                    </div>
                </div>

                <c:if test="${not empty errorMessage}"><div class="alert alert-danger py-2 small mb-4">
                    <i class="bi bi-exclamation-triangle-fill me-1"></i><span><c:out value="${errorMessage}" /></span>
                </div>

                </c:if><!-- Guidance Info Box -->
                <div class="alert alert-info py-2 small mb-4 d-flex align-items-center gap-2 border-0 bg-info bg-opacity-10 text-info-emphasis">
                    <i class="bi bi-info-circle-fill fs-5 text-info"></i>
                    <div>
                        <strong>Cam kết dịch vụ SLA:</strong> Cố vấn sẽ tiếp nhận xử lý trong vòng <strong>24 giờ liên tục</strong> và có phản hồi chính thức trước <strong>48 giờ liên tục</strong>.
                    </div>
                </div>

                <form method="post" enctype="multipart/form-data" id="ticketForm" action="${pageContext.request.contextPath}/forum/ask">
                    <sec:csrfInput /><!-- Department & Field Selector -->
                    <div class="row g-3 mb-3">
                        <div class="col-md-6">
                            <label class="form-label small fw-semibold">1. Chọn Đơn vị phụ trách *</label>
                            <select name="maDonVi" id="donViSelect" class="form-select" required onchange="loadLinhVuc(this.value)">
                                <option value="" disabled selected>-- Chọn phòng/khoa --</option>
                                <c:forEach var="dv" items="${donViList}"><option value="${dv.maDonVi}" ${selectedUnit == dv.maDonVi ? 'selected' : ''}><c:out value="${dv.tenDonVi}" /></option>
                            </c:forEach></select>
                        </div>
                        <div class="col-md-6">
                            <label for="linhVucInput" class="form-label small fw-semibold">2. Lĩnh vực cụ thể *</label>
                            <div class="position-relative" id="fieldPicker">
                                <div class="input-group">
                                    <input name="tenLinhVuc" id="linhVucInput" class="form-control" required maxlength="100" autocomplete="off" placeholder="Nhập hoặc chọn lĩnh vực" aria-expanded="false" aria-controls="linhVucSuggestions" value="<c:out value='${enteredField}' />">
                                    <button type="button" id="fieldToggle" class="btn btn-outline-secondary" aria-label="Chọn lĩnh vực" aria-expanded="false" aria-controls="linhVucSuggestions"><i class="bi bi-chevron-down" aria-hidden="true"></i></button>
                                </div>
                                <div id="linhVucSuggestions" class="position-absolute bg-white border rounded shadow-sm w-100 p-1" style="z-index: 1050; max-height: 240px; overflow-y: auto;" hidden></div>
                            </div>
                        </div>
                    </div>

                    <!-- Title -->
                    <div class="mb-3">
                        <div class="d-flex justify-content-between">
                            <label class="form-label small fw-semibold">3. Tiêu đề câu hỏi / vấn đề cần tư vấn *</label>
                            <span id="titleCounter" class="text-muted small">0/10 ký tự tối thiểu</span>
                        </div>
                        <input type="text" name="tieuDe" id="tieuDeInput" value="<c:out value='${enteredTitle}' />" class="form-control" placeholder="Tóm tắt ngắn gọn vấn đề (tối thiểu 10 ký tự)..." required minlength="10" oninput="updateTitleCount()">
                    </div>

                    <!-- Body -->
                    <div class="mb-4">
                        <div class="d-flex justify-content-between">
                            <label class="form-label small fw-semibold">4. Nội dung chi tiết câu hỏi *</label>
                            <span id="bodyCounter" class="text-muted small">0/20 ký tự tối thiểu</span>
                        </div>
                        <textarea name="noiDung" id="noiDungInput" rows="6" class="form-control" placeholder="Mô tả cụ thể hoàn cảnh, câu hỏi và mong muốn hỗ trợ từ cố vấn (tối thiểu 20 ký tự)..." required minlength="20" oninput="updateBodyCount()"><c:out value="${enteredContent}" /></textarea>
                    </div>

                    <div class="mb-4">
                        <label class="form-label small fw-semibold">5. Tệp minh chứng (không bắt buộc)</label>
                        <input type="file" name="attachment" class="form-control" accept=".jpg,.jpeg,.png,.pdf,image/jpeg,image/png,application/pdf">
                        <div class="form-text">Hỗ trợ JPG, PNG hoặc PDF; dung lượng nhỏ hơn 10 MB.</div>
                    </div>

                    <div class="alert alert-warning small">Câu hỏi và các trao đổi tiếp theo sẽ được công khai. Không đưa mật khẩu, số điện thoại hay thông tin nhạy cảm vào nội dung. Tệp đính kèm chỉ người có quyền xử lý xem được; cần trao đổi riêng, hãy dùng Chat trực tuyến.</div>
                    <!-- Anonymous Checkbox -->
                    <div class="p-3 bg-light rounded-3 mb-4 border">
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" name="baoMatDanhTinh" value="true" id="baoMatCheck" ${enteredAnonymous ? 'checked' : ''}>
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
                        <a class="btn btn-outline-secondary rounded-pill px-4" href="${pageContext.request.contextPath}/forum">Hủy bỏ</a>
                        <button type="submit" class="btn btn-qaute-primary rounded-pill px-5 fw-bold">
                            <i class="bi bi-send-fill me-1"></i>Đăng câu hỏi
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

let fieldRequest;
let fieldNames = [];
let fieldMessage = 'Chọn đơn vị để xem danh sách lĩnh vực.';
const fieldInput = document.getElementById('linhVucInput');
const fieldList = document.getElementById('linhVucSuggestions');
const fieldToggle = document.getElementById('fieldToggle');
function closeFields() {
    fieldList.hidden = true;
    fieldInput.setAttribute('aria-expanded', 'false');
    fieldToggle.setAttribute('aria-expanded', 'false');
}
function showFields(filter = '') {
    fieldList.replaceChildren();
    const names = fieldNames.filter(name => name.toLocaleLowerCase('vi').includes(filter.trim().toLocaleLowerCase('vi')));
    names.forEach(name => {
        const option = document.createElement('button');
        option.type = 'button';
        option.className = 'dropdown-item rounded text-wrap';
        option.textContent = name;
        option.addEventListener('click', () => {
            fieldInput.value = name;
            fieldInput.focus();
            closeFields();
        });
        fieldList.appendChild(option);
    });
    if (!names.length) {
        const empty = document.createElement('div');
        empty.className = 'small text-muted p-2';
        empty.textContent = fieldNames.length ? 'Không có gợi ý phù hợp. Bạn có thể nhập lĩnh vực khác.' : fieldMessage;
        fieldList.appendChild(empty);
    }
    fieldList.hidden = false;
    fieldInput.setAttribute('aria-expanded', 'true');
    fieldToggle.setAttribute('aria-expanded', 'true');
}
fieldToggle.addEventListener('click', () => fieldList.hidden ? showFields() : closeFields());
fieldInput.addEventListener('focus', () => showFields());
fieldInput.addEventListener('input', () => showFields(fieldInput.value));
document.addEventListener('click', event => {
    if (!document.getElementById('fieldPicker').contains(event.target)) closeFields();
});
document.getElementById('fieldPicker').addEventListener('focusout', event => {
    if (!event.currentTarget.contains(event.relatedTarget)) closeFields();
});
document.getElementById('fieldPicker').addEventListener('keydown', event => {
    if (event.key === 'Escape') { closeFields(); fieldInput.focus(); closeFields(); return; }
    if (event.key !== 'ArrowDown' && event.key !== 'ArrowUp') return;
    event.preventDefault();
    if (fieldList.hidden) showFields();
    const options = Array.from(fieldList.querySelectorAll('button'));
    if (!options.length) return;
    const index = options.indexOf(document.activeElement);
    const next = index < 0 ? (event.key === 'ArrowDown' ? 0 : options.length - 1)
        : (index + (event.key === 'ArrowDown' ? 1 : -1) + options.length) % options.length;
    options[next].focus();
});
async function loadLinhVuc(maDonVi, preserve = false) {
    fieldRequest?.abort();
    const input = document.getElementById('linhVucInput');
    const list = document.getElementById('linhVucSuggestions');
    list.replaceChildren();
    closeFields();
    fieldNames = [];
    fieldMessage = 'Chọn đơn vị để xem danh sách lĩnh vực.';
    if (!preserve) input.value = '';
    if (!maDonVi) return;
    fieldRequest = new AbortController();
    fieldMessage = 'Đang tải danh sách lĩnh vực…';
    try {
        const response = await fetch('${pageContext.request.contextPath}/api/public/linh-vuc-by-don-vi?maDonVi=' + encodeURIComponent(maDonVi), {signal: fieldRequest.signal});
        if (!response.ok) throw new Error('load');
        const data = await response.json();
        fieldNames = data.map(item => item.tenLinhVuc);
        fieldMessage = 'Đơn vị chưa có lĩnh vực gợi ý. Bạn có thể nhập lĩnh vực khác.';
        if (!list.hidden) showFields(input.value);
    } catch (error) {
        if (error.name !== 'AbortError') {
            fieldMessage = 'Chưa tải được danh sách. Bạn vẫn có thể nhập lĩnh vực.';
            if (!list.hidden) showFields(input.value);
        }
    }
}
loadLinhVuc(document.getElementById('donViSelect').value, true);
updateTitleCount();
updateBodyCount();
</script>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
