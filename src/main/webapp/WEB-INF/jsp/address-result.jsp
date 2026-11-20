<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Bank Details</title>
</head>
<body>
<main>
    <h1>Bank Details</h1>
    <form action="${pageContext.request.contextPath}/register" method="post">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <p><label for="bankName">Bank name</label><br>
            <input id="bankName" name="bankName" maxlength="100" required></p>
        <p><label for="accountNumber">Account number</label><br>
            <input type="text" id="accountNumber" name="accountNumber" inputmode="numeric"
                   autocomplete="off" pattern="[0-9]{1,34}" maxlength="34" required></p>
        <p><label for="ssn">SSN</label><br>
            <input type="password" id="ssn" name="ssn" inputmode="numeric" autocomplete="off"
                   pattern="([0-9]{9}|[0-9]{3}-[0-9]{2}-[0-9]{4})" maxlength="11"
                   aria-describedby="ssnHint" required><br>
            <small id="ssnHint">Enter 9 digits, with or without hyphens.</small></p>
        <button type="submit">Submit</button>
    </form>
    <a href="${pageContext.request.contextPath}/">Back to form</a>
</main>
</body>
</html>
