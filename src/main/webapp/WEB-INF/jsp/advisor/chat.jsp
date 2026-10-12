<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="q" uri="/WEB-INF/qaute-functions.tld" %>
<!DOCTYPE html>
<html>
<head>
<c:set var="pageTitle" value="Tư vấn trực tuyến 1-1" />
<%@ include file="/WEB-INF/jsp/fragments/common-head.jspf" %>
</head>
<body>
<div class="alert alert-info mx-3 mt-3 small">Cố vấn trực chat thứ Hai–Sáu, 08:00–12:00 và 13:00–17:00 (trừ ngày nghỉ). Sinh viên có thể để lại tin nhắn chờ ngoài giờ.</div>

<%@ include file="/WEB-INF/jsp/fragments/navbar.jspf" %>
<div class="container py-4 advisor-chat-page">
    <!-- Advisor Nav -->


    <!-- Alerts -->
    <c:if test="${not empty errorMessage}"><div class="alert alert-danger py-2 small mb-3">
        <i class="bi bi-exclamation-triangle-fill me-1"></i><span><c:out value="${errorMessage}" /></span>
    </div>

    </c:if><div class="advisor-chat-layout">
        <!-- Sessions and Queue Sidebar -->
        <div class="advisor-chat-session-list">
            <!-- Waiting Queue -->
            <div class="qaute-card mb-4 border-0 shadow-sm overflow-hidden">
                <div class="p-3 text-white fw-bold d-flex justify-content-between align-items-center" style="background-color: var(--qaute-sidebar);">
                    <span class="d-flex align-items-center gap-2">
                        <i class="bi bi-hourglass-split text-warning"></i>Hàng đợi sinh viên
                    </span>
                    <span class="badge" style="background-color: var(--qaute-warning); color: #FFFFFF;"><c:out value="${fn:length(waitingQueue)}" /></span>
                </div>
                <div class="p-3">
                    <c:if test="${empty waitingQueue}"><div class="text-muted small text-center py-2">
                        Không có sinh viên nào đang đợi.
                    </div>
                    </c:if><c:if test="${not (empty waitingQueue)}"><div class="d-flex flex-column gap-2">
                        <c:forEach var="wq" items="${waitingQueue}"><div class="p-2 bg-light rounded-3 d-flex justify-content-between align-items-center border">
                            <div>
                                <strong class="small d-block text-dark"><c:out value="${wq.sinhVien.taiKhoan.hoTen}" /></strong>
                                <small class="text-muted"><c:out value="${wq.donVi.tenDonVi}" /></small>
                            </div>
                            <a class="btn btn-warning btn-sm rounded-pill fw-bold text-dark px-3" href="${pageContext.request.contextPath}/advisor/chat?sessionId=${wq.maPhienChat}">
                                Nhận chat
                            </a>
                        </div>
                    </c:forEach></div>
                </c:if></div>
            </div>

            <!-- My Active Sessions -->
            <div class="qaute-card border-0 shadow-sm overflow-hidden">
                <div class="p-3 text-white fw-bold d-flex justify-content-between align-items-center" style="background-color: var(--qaute-sidebar);">
                    <span class="d-flex align-items-center gap-2">
                        <i class="bi bi-chat-dots" style="color: var(--qaute-teal-light);"></i>Phiên chat của bạn
                    </span>
                    <span class="badge" style="background-color: var(--qaute-teal-light);"><c:out value="${fn:length(mySessions)}" /></span>
                </div>
                <div class="p-3">
                <c:if test="${empty mySessions}"><div class="text-muted small text-center py-2">
                    Chưa có phiên chat nào.
                </div>
                </c:if><c:if test="${not (empty mySessions)}"><div class="list-group list-group-flush">
                    <c:forEach var="s" items="${mySessions}"><a href="${pageContext.request.contextPath}/advisor/chat?sessionId=${s.maPhienChat}" class="${currentSession != null and currentSession.maPhienChat == s.maPhienChat ? 'list-group-item list-group-item-action active rounded-3 mb-1' : 'list-group-item list-group-item-action rounded-3 mb-1'}">
                        <div class="d-flex justify-content-between align-items-center">
                            <strong class="small"><c:out value="${s.sinhVien.taiKhoan.hoTen}" /></strong>
                            <span style="font-size: 0.7rem;" class="${'status-badge status-' += s.trangThai}"><c:out value="${q:chatStatusLabel(s.trangThai)}" /></span>
                        </div>
                        <small class="d-block text-muted"><c:out value="${q:formatTemporal(s.thoiGianBatDau, 'dd/MM HH:mm')}" /></small>
                    </a>
                </c:forEach></div>
            </c:if></div>
        </div>

        </div><!-- advisor-chat-session-list -->

        <!-- Chat Conversation Area -->
        <div class="advisor-chat-conversation">
            <c:if test="${currentSession == null}"><div class="qaute-card p-5 text-center text-muted border-0 shadow-sm" style="min-height: 450px;">
                <i class="bi bi-chat-heart fs-1 text-secondary mb-3"></i>
                <h5>Chọn một phiên chat bên trái hoặc nhận sinh viên từ hàng đợi</h5>
                <p class="small text-muted">Hệ thống hỗ trợ phản hồi giải đáp trực tiếp theo thời gian thực.</p>
            </div>

            </c:if><c:if test="${currentSession != null}"><div class="qaute-card border-0 shadow-sm d-flex flex-column" style="height: clamp(620px, 78vh, 900px);">
                <!-- Header -->
                <div class="p-3 border-bottom d-flex justify-content-between align-items-center bg-light rounded-top-4">
                    <div class="d-flex align-items-center gap-2">
                        <div class="user-avatar user-avatar-md">
                            <c:if test="${currentSession.sinhVien.taiKhoan.avatarUrl != null and not empty currentSession.sinhVien.taiKhoan.avatarUrl}"><img alt="Ảnh sinh viên" src="${currentSession.sinhVien.taiKhoan.avatarUrl}">
                            </c:if><c:if test="${not (currentSession.sinhVien.taiKhoan.avatarUrl != null and not empty currentSession.sinhVien.taiKhoan.avatarUrl)}"><span><c:out value="${currentSession.sinhVien.taiKhoan.avatarInitial}" /></span>
                        </c:if></div>
                        <div>
                            <div class="fw-bold text-dark"><c:out value="${currentSession.sinhVien.taiKhoan.hoTen}" /></div>
                            <small class="text-muted"><c:out value="${currentSession.sinhVien.mssv += ' - ' += currentSession.sinhVien.lop}" /></small>
                        </div>
                    </div>
                    <div class="d-flex align-items-center gap-2"><span class="${'status-badge status-' += currentSession.trangThai}"><c:out value="${q:chatStatusLabel(currentSession.trangThai)}" /></span>
<c:if test="${currentSession.trangThai != 'KetThuc'}"><form method="post" action="${pageContext.request.contextPath}/advisor/chat/${currentSession.maPhienChat}/end"><sec:csrfInput /><button class="btn btn-outline-secondary btn-sm" type="submit">Kết thúc chat</button></form></c:if></div>
                </div>

                <!-- Messages Stream -->
                <div class="p-3 flex-grow-1 overflow-auto bg-slate-50 d-flex flex-column gap-3" data-context-path="${pageContext.request.contextPath}" id="advisorMessageStream" style="background: var(--qaute-bg);" data-session-id="${currentSession.maPhienChat}" data-user-id="${advisor.taiKhoan.maTK}">
                    <c:if test="${empty messages}"><div class="text-center my-auto text-muted small">
                        Chưa có tin nhắn trong phiên này. Hãy gửi lời chào đến sinh viên!
                    </div>

                    </c:if><c:forEach var="m" items="${messages}"><div data-message-id="${m.maTinNhan}" class="${m.taiKhoanGui.maTK == advisor.taiKhoan.maTK ? 'd-flex justify-content-end' : 'd-flex justify-content-start'}">
                        <div class="${m.taiKhoanGui.maTK == advisor.taiKhoan.maTK ? 'chat-bubble-sender' : 'chat-bubble-receiver'}" style="${m.noiDung != null and m.noiDung.contains('[🤖 Trả lời tự động]') ? 'background: #edf2fa; border-left: 4px solid #123b76;' : ''}">
                            <div class="small fw-bold mb-1 opacity-75 d-flex align-items-center">
                                <span><c:out value="${empty m.taiKhoanGui ? 'QAUTE tự động' : m.taiKhoanGui.hoTen}" /></span>
                                <c:if test="${m.noiDung != null and m.noiDung.contains('[🤖 Trả lời tự động]')}"><span class="badge bg-primary-subtle text-primary ms-2 rounded-pill px-2 py-1" style="font-size: 0.65rem;">
                                    <i class="bi bi-robot me-1"></i>Tự động
                                </span>
                            </c:if></div>
                            <div style="white-space: pre-line;"><c:out value="${m.noiDung}" /></div>
                            <c:if test="${messageAttachments != null and messageAttachments[m.maTinNhan] != null}"><div class="mt-2"><c:forEach var="file" items="${messageAttachments[m.maTinNhan]}"><a class="btn btn-sm btn-light border" target="_blank" rel="noopener" href="${pageContext.request.contextPath}/attachments/${file.maTep}"><i class="bi bi-paperclip"></i> <span><c:out value="${file.tenGoc}" /></span></a></c:forEach></div>
                            </c:if><div class="text-end mt-1" style="font-size: 0.7rem; opacity: 0.7;"><c:out value="${q:formatTemporal(m.thoiGianGui, 'HH:mm')}" /></div>
                        </div>
                    </div>
                </c:forEach></div>

                <!-- Chat Input Form -->
                <div class="p-3 border-top bg-white rounded-bottom-4">
                    <form id="advisorChatSendForm" method="post" enctype="multipart/form-data" class="d-flex gap-2 align-items-center" action="${pageContext.request.contextPath}/advisor/chat/${currentSession.maPhienChat}/send">
                        <sec:csrfInput /><label class="btn btn-outline-secondary rounded-circle mb-0" title="Đính kèm ảnh hoặc PDF"><i class="bi bi-paperclip"></i><input type="file" name="attachment" accept=".jpg,.jpeg,.png,.pdf" hidden></label>
                        <input type="text" name="noiDung" class="form-control rounded-pill px-3 shadow-none" placeholder="Nhập câu trả lời hoặc gửi tệp..." autocomplete="off">
                        <button type="submit" class="btn btn-primary rounded-pill px-4 fw-bold">
                            <i class="bi bi-send-fill"></i>
                        </button>
                    </form>
                </div>
            </div>
        </c:if></div>
    </div>
</div>

<script>
window.addEventListener('DOMContentLoaded', () => {
    const stream = document.getElementById('advisorMessageStream');
    if (stream) {
        stream.scrollTop = stream.scrollHeight;
    }
});
</script>
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7.2.1/bundles/stomp.umd.min.js"></script>
<script src="${pageContext.request.contextPath}/js/qaute-chat.js?v=20261003-json-fix" defer></script>

<%@ include file="/WEB-INF/jsp/fragments/footer.jspf" %>

</body>
</html>
