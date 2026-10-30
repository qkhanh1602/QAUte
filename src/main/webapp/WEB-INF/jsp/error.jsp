<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Không thể xử lý yêu cầu</title></head>
<body style="font-family:Arial,sans-serif;max-width:680px;margin:80px auto;padding:24px;color:#172b4d">
<h1><c:out value="${errorTitle}" default="Chưa thể xử lý yêu cầu"/></h1>
<p><c:out value="${errorMessage}" default="Vui lòng tải lại trang hoặc quay lại trang chủ và thử lại."/></p>
<c:url var="returnUrl" value="${empty backLink ? '/' : backLink}"/>
<a href="<c:out value="${returnUrl}"/>">Quay lại</a>
</body></html>
