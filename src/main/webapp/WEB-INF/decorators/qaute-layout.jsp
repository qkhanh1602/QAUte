<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="requestPath" value="${pageContext.request.requestURI}" />
<c:set var="layoutVariant" value="qaute-content-layout" />
<c:if test="${fn:contains(requestPath, '/login')
              or fn:contains(requestPath, '/register')
              or fn:contains(requestPath, '/verify-otp')}" >
    <c:set var="layoutVariant" value="qaute-auth-layout" />
</c:if>
<c:set var="skipLabel" value="Skip to main content" />
<c:if test="${pageContext.request.locale.language eq 'vi'}">
    <c:set var="skipLabel" value="Đi đến nội dung chính" />
</c:if>
<!DOCTYPE html>
<html lang="<c:out value='${pageContext.request.locale.language}' default='vi'/>"
      data-view-engine="jsp-jstl">
<head>
    <meta charset="UTF-8">
    <title><sitemesh:write property="title"/></title>
    <sitemesh:write property="head"/>
    <style>
        .qaute-skip-link {
            position: fixed;
            top: 0.75rem;
            left: 0.75rem;
            z-index: 10000;
            padding: 0.65rem 1rem;
            border-radius: 0.5rem;
            background: #123b76;
            color: #fff;
            font-weight: 600;
            text-decoration: none;
            transform: translateY(-160%);
            transition: transform 0.15s ease;
        }

        .qaute-skip-link:focus {
            color: #fff;
            transform: translateY(0);
        }
    </style>
</head>
<body class="qaute-sitemesh-shell <c:out value='${layoutVariant}'/>"
      data-layout-engine="sitemesh">
    <a class="qaute-skip-link" href="#qaute-main-content"><c:out value="${skipLabel}" /></a>
    <span id="qaute-main-content" tabindex="-1"></span>
    <sitemesh:write property="body"/>
</body>
</html>
