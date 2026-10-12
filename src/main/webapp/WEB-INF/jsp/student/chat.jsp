<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Phòng tư vấn trực tuyến" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>
<div class="alert alert-info mx-3 mt-3 small">Cố vấn trực chat thứ Hai–Sáu, 08:00–12:00 và 13:00–17:00 (trừ ngày nghỉ). Sinh viên có thể để lại tin nhắn chờ ngoài giờ.</div>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4">
    <!-- Student Nav -->


    <div class="row g-4">
        <!-- Department selector (Sidebar) -->
        <div class="col-lg-4">
            <div class="qaute-card border-0 shadow-sm mb-4 overflow-hidden">
                <div class="p-3 text-white fw-bold d-flex align-items-center justify-content-between" style="background-color: var(--qaute-sidebar);">
                    <span class="d-flex align-items-center gap-2"><i class="bi bi-diagram-3"></i>Chọn Đơn vị kết nối</span>
                    <span class="badge" style="background-color: var(--qaute-secondary);"><c:out value="${fn:length(donViList)}" /></span>
                </div>
                <div class="p-3">
                    <div class="list-group list-group-flush" style="max-height: 380px; overflow-y: auto;">
                        <c:forEach var="dv" items="${donViList}"><a href="${pageContext.request.contextPath}/student/chat?maDonVi=${dv.maDonVi}" class="${selectedDonVi == dv.maDonVi ? 'list-group-item list-group-item-action active rounded-3 mb-1 fw-semibold' : 'list-group-item list-group-item-action rounded-3 mb-1 text-secondary'}">
                            <i class="bi bi-building me-2"></i>
                            <span><c:out value="${dv.tenDonVi}" /></span>
                        </a>
                    </c:forEach></div>
                </div>
            </div>

            <div class="qaute-card p-3 border-0 small text-secondary" style="background-color: var(--qaute-bg); border: 1px solid var(--qaute-border) !important;">
                <h6 class="fw-bold text-dark mb-2 d-flex align-items-center gap-1">
                    <i class="bi bi-shield-check text-primary"></i>Quy định tư vấn trực tuyến
                </h6>
                <p class="mb-1">• Khung giờ hỗ trợ: <strong>08:00 - 11:30</strong> và <strong>13:30 - 16:30</strong> (Thứ 2 - Thứ 6).</p>
                <p class="mb-1">• Vui lòng chuẩn bị sẵn MSSV, thông tin chi tiết câu hỏi.</p>
                <p class="mb-0">• Mọi phiên trao đổi được mã hóa và bảo mật thông tin sinh viên.</p>
            </div>
        </div>

        <!-- Chat Area -->
        <c:if test="${currentSession != null}"><div class="col-lg-8">
            <div class="qaute-card border-0 shadow-sm d-flex flex-column" style="height: 580px;">
                <!-- Chat Header -->
                <div class="p-3 border-bottom d-flex justify-content-between align-items-center bg-light rounded-top-4">
                    <div class="d-flex align-items-center gap-2">
                        <div class="user-avatar user-avatar-md">
                            <c:if test="${currentSession.nguoiTuVan != null and currentSession.nguoiTuVan.taiKhoan.avatarUrl != null and not empty currentSession.nguoiTuVan.taiKhoan.avatarUrl}"><img alt="Ảnh cố vấn" src="${currentSession.nguoiTuVan.taiKhoan.avatarUrl}">
                            </c:if><c:if test="${currentSession.nguoiTuVan != null and (currentSession.nguoiTuVan.taiKhoan.avatarUrl == null or empty currentSession.nguoiTuVan.taiKhoan.avatarUrl)}"><span><c:out value="${currentSession.nguoiTuVan.taiKhoan.avatarInitial}" /></span>
                            </c:if><c:if test="${currentSession.nguoiTuVan == null}"><i class="bi bi-headset"></i>
                        </c:if></div>
                        <div>
                            <div class="fw-bold text-dark"><c:out value="${currentSession.donVi.tenDonVi}" /></div>
                            <c:if test="${currentSession.nguoiTuVan != null}"><small class="text-success fw-semibold">
                                <i class="bi bi-person-check-fill me-1"></i>Cố vấn: <span><c:out value="${currentSession.nguoiTuVan.taiKhoan.hoTen}" /></span>
                                · <span id="advisorPresence" data-username="${currentSession.nguoiTuVan.taiKhoan.tenDangNhap}" data-busy="${currentSession.trangThai == 'DangHoatDong'}"><c:out value="${advisorPresence}" /></span>
                            </small>
                            </c:if><c:if test="${not (currentSession.nguoiTuVan != null)}"><small class="text-warning fw-semibold">
                                <i class="bi bi-hourglass-split me-1"></i>Đang chờ Cố vấn tiếp nhận...
                            </small>
                        </c:if></div>
                    </div>
                    <div class="d-flex align-items-center gap-2"><span class="${'status-badge status-' += currentSession.trangThai}"><c:out value="${q:chatStatusLabel(currentSession.trangThai)}" /></span>
<c:if test="${currentSession.trangThai != 'KetThuc'}"><form method="post" action="${pageContext.request.contextPath}/student/chat/${currentSession.maPhienChat}/end"><sec:csrfInput /><button class="btn btn-outline-secondary btn-sm" type="submit">Kết thúc chat</button></form></c:if></div>
                </div>

                <!-- Messages Stream -->
                <div class="p-3 flex-grow-1 overflow-auto bg-slate-50 d-flex flex-column gap-3" data-context-path="${pageContext.request.contextPath}" id="messageStream" style="background: var(--qaute-bg);" data-session-id="${currentSession.maPhienChat}" data-user-id="${student.taiKhoan.maTK}">
                    <c:if test="${empty messages}"><div class="text-center my-auto text-muted small">
                        <i class="bi bi-chat-text fs-2 mb-2 d-block text-secondary"></i>
                        Chưa có tin nhắn nào trong phiên này. Hãy gửi tin nhắn đầu tiên!
                    </div>

                    </c:if><c:forEach var="m" items="${messages}"><div data-message-id="${m.maTinNhan}" class="${m.taiKhoanGui.maTK == student.taiKhoan.maTK ? 'd-flex justify-content-end' : 'd-flex justify-content-start'}">
                        <div class="${m.taiKhoanGui.maTK == student.taiKhoan.maTK ? 'chat-bubble-sender' : 'chat-bubble-receiver'}" >
                            <c:if test="${not empty m.taiKhoanGui}"><div class="small fw-bold mb-1 opacity-75"><c:out value="${m.taiKhoanGui.hoTen}" /></div></c:if>
                            <div style="white-space: pre-line;"><c:out value="${m.noiDungHienThi}" /></div>
                            <c:if test="${messageAttachments != null and messageAttachments[m.maTinNhan] != null}"><div class="mt-2"><c:forEach var="file" items="${messageAttachments[m.maTinNhan]}"><a class="btn btn-sm btn-light border" target="_blank" rel="noopener" href="${pageContext.request.contextPath}/attachments/${file.maTep}"><i class="bi bi-paperclip"></i> <span><c:out value="${file.tenGoc}" /></span></a></c:forEach></div>
                            </c:if><div class="text-end mt-1" style="font-size: 0.7rem; opacity: 0.7;"><c:out value="${q:formatTemporal(m.thoiGianGui, 'HH:mm')}" /></div>
                        </div>
                    </div>
                </c:forEach></div>

                <!-- Quick Suggestions -->
                <div class="px-3 pt-2 pb-1 bg-white border-top d-flex gap-1 flex-wrap align-items-center">
                    <span class="text-muted small me-1"><i class="bi bi-lightbulb-fill text-warning me-1"></i>Gợi ý hỏi nhanh:</span>
                    <c:forEach var="suggestion" items="${quickSuggestions}">
                        <button type="button" class="btn btn-sm btn-light rounded-pill border small px-2 py-0 text-secondary" data-quick-question="<c:out value='${suggestion.question}' />"><c:out value="${suggestion.label}" /></button>
                    </c:forEach>
                </div>

                <!-- Chat Input Form -->
                <div class="p-3 bg-white rounded-bottom-4">
                    <form id="chatSendForm" method="post" enctype="multipart/form-data" class="d-flex gap-2 align-items-center" action="${pageContext.request.contextPath}/student/chat/${currentSession.maPhienChat}/send">
                        <sec:csrfInput /><label class="btn btn-outline-secondary rounded-circle mb-0" title="Đính kèm ảnh hoặc PDF"><i class="bi bi-paperclip"></i><input type="file" name="attachment" accept=".jpg,.jpeg,.png,.pdf" hidden></label>
                        <input type="text" id="chatInput" name="noiDung" class="form-control rounded-pill px-3 shadow-none" placeholder="Nhập tin nhắn hoặc gửi tệp..." autocomplete="off">
                        <button type="submit" class="btn btn-primary rounded-pill px-4 fw-bold">
                            <i class="bi bi-send-fill"></i>
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </c:if></div>
</div>

<script>
function fillMessage(text) {
    const input = document.getElementById('chatInput');
    if (input) {
        input.value = text;
        input.focus();
    }
}

window.addEventListener('DOMContentLoaded', () => {
    const stream = document.getElementById('messageStream');
    if (stream) {
        stream.scrollTop = stream.scrollHeight;
    }
});
</script>
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7.2.1/bundles/stomp.umd.min.js"></script>
<script src="${pageContext.request.contextPath}/js/qaute-chat.js?v=20261013-1" defer></script>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
