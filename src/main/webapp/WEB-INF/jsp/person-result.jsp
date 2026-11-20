<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Address and Contact Details</title>
</head>
<body>
<main>
    <h1>Address and Contact Details</h1>
    <form action="${pageContext.request.contextPath}/submit-address" method="post">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <p><label for="address">Address</label><br>
            <textarea id="address" name="address" autocomplete="street-address" maxlength="500" required></textarea></p>
        <p><label for="city">City</label><br>
            <input id="city" name="city" autocomplete="address-level2" maxlength="100" required></p>
        <p><label for="state">State</label><br>
            <input id="state" name="state" autocomplete="address-level1" maxlength="100" required></p>
        <p><label for="country">Country</label><br>
            <input id="country" name="country" autocomplete="country-name" maxlength="100" required></p>
        <p><label for="phone">Phone</label><br>
            <input type="tel" id="phone" name="phone" autocomplete="tel" maxlength="30" required></p>
        <button type="submit">Submit</button>
    </form>
    <a href="${pageContext.request.contextPath}/">Back to form</a>
</main>
</body>
</html>
