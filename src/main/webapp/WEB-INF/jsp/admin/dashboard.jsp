<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Bảng điều khiển Quản trị viên" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4"><a class="btn btn-outline-primary mb-3" href="${pageContext.request.contextPath}/admin/search">Tra cứu câu hỏi, lịch hẹn, nhật ký</a>
    <!-- Thông báo nếu vừa bị chặn khi vào link sinh viên -->
    <c:if test="${param.denied}"><div class="alert alert-warning alert-dismissible fade show d-flex align-items-center gap-2 mb-4 rounded-3 shadow-sm border-warning" role="alert">
        <i class="bi bi-info-circle-fill text-warning fs-5 flex-shrink-0"></i>
        <span>Tài khoản <strong>Quản trị viên</strong> không có hồ sơ sinh viên để mở các trang dành riêng cho sinh viên (như chat trực tuyến SV, gửi câu hỏi, đặt lịch). Hệ thống đã tự động đưa bạn về Bảng điều khiển Quản trị.</span>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>

    </c:if><!-- Admin Header Banner -->
    <div class="qaute-card dashboard-hero p-4 p-md-5 mb-4 text-white border-0 shadow-sm" style="border-radius: 1.5rem;">
        <div class="row align-items-center">
            <div class="col-md-8">
                <span class="badge bg-danger text-white px-3 py-1 rounded-pill mb-2 fw-bold">
                    <i class="bi bi-shield-lock me-1"></i> Trung tâm Quản trị Hệ thống QAUTE
                </span>
                <h2 class="fw-bold mb-1">Báo Cáo Thống Kê &amp; Giám Sát SLA</h2>
                <p class="text-white-50 mb-3 small">
                    Hệ thống đo lường hiệu suất tư vấn, chỉ số hoàn tất theo chuẩn QĐ22 và kiểm soát an toàn vận hành
                </p>
                <div class="d-flex flex-wrap gap-2">
                    <a class="btn btn-light text-dark fw-semibold btn-sm rounded-pill px-3" href="${pageContext.request.contextPath}/admin/users">
                        <i class="bi bi-people-fill me-1"></i>Quản lý người dùng
                    </a>
                    <a class="btn btn-warning text-dark fw-bold btn-sm rounded-pill px-3" href="${pageContext.request.contextPath}/admin/content">
                        <i class="bi bi-check-circle me-1"></i>Duyệt FAQ &amp; Cẩm nang
                    </a>
                    <a class="btn btn-outline-light btn-sm rounded-pill px-3 fw-semibold" href="${pageContext.request.contextPath}/admin/catalogs">
                        <i class="bi bi-folder2-open me-1"></i>Danh mục đơn vị
                    </a>
                    <a class="btn btn-outline-light btn-sm rounded-pill px-3 fw-semibold" href="${pageContext.request.contextPath}/admin/tickets">
                        <i class="bi bi-diagram-2 me-1"></i>Điều phối câu hỏi
                    </a>
                </div>
                <form class="row g-2 mt-3" method="get" action="${pageContext.request.contextPath}/admin/reports/export">
                    <div class="col-auto"><input class="form-control form-control-sm" type="date" name="from" aria-label="Từ ngày"></div>
                    <div class="col-auto"><input class="form-control form-control-sm" type="date" name="to" aria-label="Đến ngày"></div>
                    <div class="col-auto"><select class="form-select form-select-sm" name="unitId"><option value="">Tất cả đơn vị</option><c:forEach var="unit" items="${units}"><option value="${unit.maDonVi}"><c:out value="${unit.tenDonVi}" /></option></c:forEach></select></div>
                    <div class="col-auto"><button class="btn btn-success btn-sm" name="format" value="xlsx"><i class="bi bi-file-earmark-excel me-1"></i>Excel</button></div>
                    <div class="col-auto"><button class="btn btn-danger btn-sm" name="format" value="pdf"><i class="bi bi-file-earmark-pdf me-1"></i>PDF</button></div>
                </form>
            </div>

            <div class="col-md-4 text-md-end mt-4 mt-md-0">
                <div class="bg-white bg-opacity-10 p-3 rounded-4 text-center border border-white border-opacity-10 d-inline-block" style="min-width: 220px;">
                    <small class="text-white-50 d-block mb-1">Tỷ lệ hoàn tất (QĐ22)</small>
                    <span class="fs-1 fw-extrabold text-success"><c:out value="${forumSummary.completionRate += '%'}" /></span>
                    <div class="text-white-50 small mt-1">(Đã trả lời + Đã đóng) / Câu hỏi hiển thị</div>
                </div>
            </div>
        </div>
    </div>

    <!-- Admin Sub-nav -->


    <!-- KPI Metric Cards -->
    <div class="row g-3 mb-4">
        <div class="col-md-3">
            <div class="qaute-card p-3 border-0 shadow-sm">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <small class="text-muted fw-medium d-block mb-1">Tổng câu hỏi diễn đàn</small>
                        <h3 class="fw-bold text-dark mb-0"><c:out value="${forumSummary.total}" /></h3>
                    </div>
                    <div class="bg-primary bg-opacity-10 text-primary rounded-3 p-3">
                        <i class="bi bi-ticket-perforated fs-4"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="qaute-card p-3 border-0 shadow-sm">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <small class="text-muted fw-medium d-block mb-1">Câu hỏi chờ tiếp nhận</small>
                        <h3 class="fw-bold text-warning mb-0"><c:out value="${forumSummary.pending}" /></h3>
                    </div>
                    <div class="bg-warning bg-opacity-10 text-warning rounded-3 p-3">
                        <i class="bi bi-hourglass-split fs-4"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="qaute-card p-3 border-0 shadow-sm">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <small class="text-muted fw-medium d-block mb-1">Tổng lịch hẹn tư vấn</small>
                        <h3 class="fw-bold text-info mb-0"><c:out value="${summary.tongLichHen}" /></h3>
                    </div>
                    <div class="bg-info bg-opacity-10 text-info rounded-3 p-3">
                        <i class="bi bi-calendar2-week fs-4"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="qaute-card p-3 border-0 shadow-sm">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <small class="text-muted fw-medium d-block mb-1">Cảnh báo trễ SLA</small>
                        <h3 class="fw-bold text-danger mb-0"><c:out value="${summary.canhbSla}" /></h3>
                    </div>
                    <div class="bg-danger bg-opacity-10 text-danger rounded-3 p-3">
                        <i class="bi bi-exclamation-octagon fs-4"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Charts Section -->
    <div class="row g-4 mb-4">
        <!-- Doughnut Chart: Status -->
        <div class="col-lg-6">
            <div class="qaute-card p-4 border-0 shadow-sm h-100">
                <h5 class="fw-bold text-dark mb-3"><i class="bi bi-pie-chart text-primary me-2"></i>Trạng thái câu hỏi diễn đàn</h5>
                <div style="max-height: 300px; position: relative;">
                    <canvas id="statusChart"></canvas>
                </div>
            </div>
        </div>

        <!-- Bar Chart: Departments -->
        <div class="col-lg-6">
            <div class="qaute-card p-4 border-0 shadow-sm h-100">
                <h5 class="fw-bold text-dark mb-3"><i class="bi bi-bar-chart-line text-success me-2"></i>Câu hỏi diễn đàn theo khoa / đơn vị</h5>
                <div style="height: ${fn:length(forumSummary.units) * 32 + 70}px; min-height: 300px; position: relative;">
                    <canvas id="deptChart"></canvas>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Chart.js CDN -->
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>

const choTiepNhan = ${forumSummary.pending};
const dangXuLy = ${forumSummary.processing};
const daPhanHoi = ${forumSummary.answered};
const dong = ${forumSummary.closed};

// 1. Status Chart
const ctxStatus = document.getElementById('statusChart').getContext('2d');
new Chart(ctxStatus, {
    type: 'doughnut',
    data: {
        labels: ['Chờ tiếp nhận', 'Đang xử lý', 'Đã trả lời', 'Đã đóng'],
        datasets: [{
            data: [choTiepNhan, dangXuLy, daPhanHoi, dong],
            backgroundColor: ['#b91c1c', '#123b76', '#194984', '#64748B']
        }]
    },
    options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: { position: 'bottom' }
        }
    }
});

// 2. Department Chart
const deptData = ${q:toJson(forumSummary.units)};
const deptLabels = Object.keys(deptData);
const deptValues = Object.values(deptData);

const ctxDept = document.getElementById('deptChart').getContext('2d');
new Chart(ctxDept, {
    type: 'bar',
    data: {
        labels: deptLabels,
        datasets: [{
            label: 'Số câu hỏi diễn đàn',
            data: deptValues,
            backgroundColor: '#123b76',
            borderRadius: 6
        }]
    },
    options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
            x: { beginAtZero: true, ticks: { precision: 0 } },
            y: { ticks: { autoSkip: false } }
        }
    }
});

</script>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
