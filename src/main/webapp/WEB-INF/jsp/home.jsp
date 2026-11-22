<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Banking System Spring</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css?v=grid-reload"><script src="${pageContext.request.contextPath}/js/theme.js?v=20261022-skin-menu" defer></script></head>
<body><header><div><span class="eyebrow">Registration workspace</span><h1>Banking System Spring</h1><p>Create profiles and manage your registered users in one place.</p></div><div class="header-tools"><form method="post" action="${pageContext.request.contextPath}/logout"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button type="submit" class="secondary">Log out</button></form><span class="badge">User management</span><details class="skin-picker"><summary aria-label="Select interface skin"><span class="skin-preview" aria-hidden="true"></span><span class="skin-trigger-copy"><span class="skin-caption">INTERFACE SKIN</span><strong data-skin-name>Navy</strong></span><svg class="skin-chevron" aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m6 9 6 6 6-6"/></svg></summary><div class="skin-menu" role="group" aria-label="Choose interface skin"><div class="skin-menu-title">Make it yours<span>Choose a color for your workspace</span></div><button type="button" class="skin-option" data-theme-choice="navy" data-skin-label="Navy" style="--swatch:#3567df" aria-pressed="true"><span class="skin-chip" aria-hidden="true"></span><span class="skin-option-copy"><strong>Navy</strong><span>Classic &amp; confident</span></span><span class="skin-check" aria-hidden="true">✓</span></button><button type="button" class="skin-option" data-theme-choice="teal" data-skin-label="Teal" style="--swatch:#0f766e" aria-pressed="false"><span class="skin-chip" aria-hidden="true"></span><span class="skin-option-copy"><strong>Teal</strong><span>Fresh &amp; focused</span></span><span class="skin-check" aria-hidden="true">✓</span></button><button type="button" class="skin-option" data-theme-choice="forest" data-skin-label="Forest" style="--swatch:#287044" aria-pressed="false"><span class="skin-chip" aria-hidden="true"></span><span class="skin-option-copy"><strong>Forest</strong><span>Calm &amp; grounded</span></span><span class="skin-check" aria-hidden="true">✓</span></button><button type="button" class="skin-option" data-theme-choice="slate" data-skin-label="Slate" style="--swatch:#526277" aria-pressed="false"><span class="skin-chip" aria-hidden="true"></span><span class="skin-option-copy"><strong>Slate</strong><span>Clean &amp; understated</span></span><span class="skin-check" aria-hidden="true">✓</span></button><button type="button" class="skin-option" data-theme-choice="plum" data-skin-label="Plum" style="--swatch:#7850a0" aria-pressed="false"><span class="skin-chip" aria-hidden="true"></span><span class="skin-option-copy"><strong>Plum</strong><span>Refined &amp; expressive</span></span><span class="skin-check" aria-hidden="true">✓</span></button><button type="button" class="skin-option" data-theme-choice="burgundy" data-skin-label="Burgundy" style="--swatch:#a33e56" aria-pressed="false"><span class="skin-chip" aria-hidden="true"></span><span class="skin-option-copy"><strong>Burgundy</strong><span>Warm &amp; distinctive</span></span><span class="skin-check" aria-hidden="true">✓</span></button></div></details></div></header>
<main>
<c:if test="${not empty message}"><div class="notice" role="status"><c:out value="${message}"/></div></c:if>
<div class="split"><section class="panel" aria-labelledby="form-title">
<div class="panel-head"><h2 id="form-title">${empty editId ? 'Create new user' : 'Edit user'}</h2><p class="subtitle">${empty editId ? 'Add personal, contact, and bank details.' : 'Update this profile and save your changes.'}</p></div>
<form:form method="post" action="${pageContext.request.contextPath}/users/save" modelAttribute="registration" cssClass="user-form">

<c:if test="${not empty editId}"><input type="hidden" name="editId" value="<c:out value='${editId}'/>"></c:if>
<c:if test="${not empty error}"><div class="notice error" role="alert"><c:out value="${error}"/></div></c:if>
<ol class="step-progress" aria-label="Registration progress" hidden>
<li><span class="step-number" aria-hidden="true">1</span><span>Personal</span><span class="step-state">Current</span></li>
<li><span class="step-number" aria-hidden="true">2</span><span>Contact</span><span class="step-state">Upcoming</span></li>
<li><span class="step-number" aria-hidden="true">3</span><span>Bank</span><span class="step-state">Upcoming</span></li>
</ol>
<p class="step-caption" aria-live="polite" hidden></p>
<fieldset><legend>01 / PERSONAL DETAILS</legend><div class="fields">
<div class=""><label for="firstName">First name</label><form:input path="firstName" id="firstName" maxlength="100" required="required"/></div>
<div class=""><label for="lastName">Last name</label><form:input path="lastName" id="lastName" maxlength="100" required="required"/></div>
<div class="wide"><label for="middleName">Middle name (optional)</label><form:input path="middleName" id="middleName" maxlength="100"/></div>
<div class="wide"><label for="gender">Gender</label><form:select path="gender" id="gender" required="required"><form:option value="" label="Select gender"/><form:option value="Male"/><form:option value="Female"/><form:option value="Other"/></form:select></div>
</div></fieldset><fieldset><legend>02 / ADDRESS &amp; CONTACT</legend><div class="fields">
<div class="wide"><label for="address">Street address</label><form:textarea path="address" id="address" maxlength="500" required="required"/></div>
<div><label for="city">City</label><form:input path="city" id="city" maxlength="100" required="required"/></div>
<div><label for="state">State</label><form:input path="state" id="state" maxlength="100" required="required"/></div>
<div><label for="country">Country</label><form:input path="country" id="country" maxlength="100" required="required"/></div>
<div><label for="phone">Phone</label><form:input path="phone" id="phone" maxlength="30" required="required"/></div>
</div></fieldset><fieldset><legend>03 / BANK DETAILS</legend><div class="fields">
<div class="wide"><label for="bankName">Bank name</label><form:input path="bankName" id="bankName" maxlength="100" required="required"/></div>
<div class="wide"><label for="accountNumber">Account number</label><input type="password" name="accountNumber" id="accountNumber" inputmode="numeric" autocomplete="new-password" pattern="[0-9]{1,34}" maxlength="34" ${empty editId ? 'required' : ''}></div>
<div class="wide"><label for="ssn">SSN</label><input type="password" name="ssn" id="ssn" inputmode="numeric" autocomplete="new-password" pattern="([0-9]{9}|[0-9]{3}-[0-9]{2}-[0-9]{4})" maxlength="11" ${empty editId ? 'required' : ''}><small>${empty editId ? 'Enter nine digits, with or without hyphens.' : 'Enter any missing bank details. Leave saved account numbers and SSNs blank to keep their values.'}</small></div>
</div></fieldset><div class="actions wizard-actions"><button class="secondary" type="button" data-back hidden>Back</button><button class="primary" type="button" data-next hidden>Next step</button><button class="primary" type="submit" data-finish>${empty editId ? 'Create user' : 'Save changes'}</button><c:if test="${not empty editId}"><a class="button secondary" href="${pageContext.request.contextPath}/">Cancel</a></c:if></div>
</form:form></section>
<section class="panel" aria-labelledby="users-title" data-user-grid><div class="panel-head directory-heading"><div><h2 id="users-title">Registered users <span class="subtitle" data-user-count>(${users.size()})</span></h2><p class="subtitle">Your directory of existing registrations.</p></div>
<div class="grid-tools"><div class="grid-search"><label class="sr-only" for="user-search">Search registered users</label><svg aria-hidden="true" width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/></svg><input type="search" id="user-search" placeholder="Search users…" aria-controls="user-grid-results" aria-describedby="user-search-status" autocomplete="off"></div><button type="button" class="secondary" data-reload-users data-url="${pageContext.request.contextPath}/" aria-controls="user-grid-results" hidden>Reload</button></div></div>
<p class="search-status" data-reload-status role="status"></p>
<p id="user-search-status" class="search-status" role="status" aria-live="polite" aria-atomic="true"></p>
<div id="user-grid-results"><c:choose><c:when test="${empty users}"><div class="empty"><strong>No registered users yet</strong>Create your first user using the form on the left.</div></c:when><c:otherwise>
<div class="table-scroll"><table id="users-table"><thead><tr><th scope="col">User</th><th scope="col">Contact</th><th scope="col">Location</th><th scope="col">Bank status</th><th scope="col">Actions</th></tr></thead><tbody>
<c:forEach items="${users}" var="user"><tr data-user-row>
<td><span class="person"><c:out value="${user.firstName}"/> <c:out value="${user.middleName}"/> <c:out value="${user.lastName}"/></span><span class="muted">#<c:out value="${user.id}"/> · <c:out value="${user.gender}"/></span></td>
<td><c:out value="${user.phone}" default="Missing"/></td>
<td><c:choose><c:when test="${empty user.city and empty user.state and empty user.country}">Missing address</c:when><c:otherwise><c:out value="${user.city}"/>, <c:out value="${user.state}"/><span class="muted"><c:out value="${user.country}"/></span></c:otherwise></c:choose></td>
<td><c:choose>
<c:when test="${user.bankStatus eq 'IN_PROGRESS'}">In progress</c:when>
<c:when test="${user.bankStatus eq 'ACTIVATED'}">Activated</c:when>
<c:when test="${user.bankStatus eq 'FAILED'}">Failed</c:when>
<c:when test="${user.bankStatus eq 'INACTIVE'}">Inactive</c:when>
<c:otherwise>Missing bank details</c:otherwise>
</c:choose></td>
<td><a class="edit" href="${pageContext.request.contextPath}/users/${user.id}/edit">Edit</a><form class="inline" method="post" action="${pageContext.request.contextPath}/users/${user.id}/delete" data-delete-user data-user-id="${user.id}" data-user-name="<c:out value='${user.firstName}'/> <c:out value='${user.middleName}'/> <c:out value='${user.lastName}'/>" data-user-phone="<c:out value='${user.phone}'/>" data-user-location="<c:out value='${user.city}'/>, <c:out value='${user.state}'/>, <c:out value='${user.country}'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="delete" type="submit" aria-label="Delete user ${user.id}">Delete</button></form></td>
</tr></c:forEach><tr id="search-empty" hidden><td colspan="5" class="empty"><strong>No matching users</strong>Try a different name, ID, phone number, or location.</td></tr></tbody></table></div></c:otherwise></c:choose></div><div class="footnote">Bank account numbers and SSNs are kept out of the directory.</div></section>
</div></main>
<dialog id="delete-user-dialog" class="delete-dialog" aria-labelledby="delete-dialog-title" aria-describedby="delete-dialog-description">
    <div class="delete-dialog-body">
        <button type="button" class="dialog-close" data-dismiss-delete aria-label="Close delete confirmation">&times;</button>
        <div class="delete-icon" aria-hidden="true"><svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18M9 6V4h6v2M5 6l1 14h12l1-14M10 10v6M14 10v6"/></svg></div>
        <span class="dialog-eyebrow">REMOVE REGISTRATION</span>
        <h2 id="delete-dialog-title">Delete this user?</h2>
        <p id="delete-dialog-description">This will permanently remove the user and their saved registration details. This action cannot be undone.</p>
        <div class="delete-user-card">
            <div class="delete-avatar" aria-hidden="true" data-delete-avatar></div>
            <div><strong data-delete-name></strong><span class="muted">Registration #<span data-delete-id></span></span></div>
            <dl><div><dt>Phone</dt><dd data-delete-phone></dd></div><div><dt>Location</dt><dd data-delete-location></dd></div></dl>
        </div>
    </div>
    <div class="delete-dialog-actions"><button type="button" class="secondary" data-dismiss-delete autofocus>Keep user</button><button type="button" class="danger" data-confirm-delete>Yes, delete user</button></div>
</dialog>
<script src="${pageContext.request.contextPath}/js/user-search.js?v=grid-reload" defer></script><script src="${pageContext.request.contextPath}/js/delete-user-dialog.js?v=grid-reload" defer></script><script src="${pageContext.request.contextPath}/js/registration-wizard.js" defer></script></body></html>
