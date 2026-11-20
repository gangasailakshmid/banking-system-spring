<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Personal Details</title>
</head>
<body>
<main>
    <h1>Personal Details</h1>
    <form action="${pageContext.request.contextPath}/submit" method="post">
<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <p><label for="firstName">First name</label><br>
            <input id="firstName" name="firstName" autocomplete="given-name" maxlength="100" required></p>
        <p><label for="middleName">Middle name (optional)</label><br>
            <input id="middleName" name="middleName" autocomplete="additional-name" maxlength="100"></p>
        <p><label for="lastName">Last name</label><br>
            <input id="lastName" name="lastName" autocomplete="family-name" maxlength="100" required></p>
        <p><label for="gender">Gender</label><br>
            <select id="gender" name="gender" required>
                <option value="">Select gender</option>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
                <option value="Other">Other</option>
            </select></p>
        <button type="submit">Submit</button>
    </form>
</main>
</body>
</html>
