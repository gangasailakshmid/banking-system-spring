<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Log in | Banking System Spring</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css"><link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css"></head>
<body><main class="login-shell"><section class="panel login-panel"><span class="eyebrow">BANKING SYSTEM SPRING</span><h1>Welcome back</h1><p class="subtitle">Log in to manage registrations.</p>
<c:if test="${param.error != null}"><div class="notice error" role="alert">Invalid username or password.</div></c:if>
<c:if test="${param.logout != null}"><div class="notice" role="status">You have been logged out.</div></c:if>
<form method="post" action="${pageContext.request.contextPath}/login" class="user-form">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
<label for="username">Username</label><input id="username" name="username" autocomplete="username" required autofocus maxlength="100">
<label for="password">Password</label><input id="password" type="password" name="password" autocomplete="current-password" required>
<button class="primary" type="submit">Log in</button></form></section></main></body></html>
