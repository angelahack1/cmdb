<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Assets &ndash; CMDB</title>
    <style>
        body { font-family: system-ui, sans-serif; max-width: 960px; margin: 2rem auto; padding: 0 1rem; }
        table { border-collapse: collapse; width: 100%; margin-top: 1rem; }
        th, td { border: 1px solid #ccc; padding: .5rem .75rem; text-align: left; }
        th { background: #f4f4f4; }
        form.inline { display: flex; gap: .5rem; flex-wrap: wrap; margin-top: 1.5rem; }
        form.inline input, form.inline select, form.inline button { padding: .4rem .6rem; }
        a { color: #0969da; }
    </style>
</head>
<body>
    <h1><a href="${pageContext.request.contextPath}/">CMDB</a> &rsaquo; Assets</h1>

    <table>
        <thead>
            <tr><th>ID</th><th>Name</th><th>Type</th><th>Owner</th><th>Created</th></tr>
        </thead>
        <tbody>
            <c:forEach var="a" items="${assets}">
                <tr>
                    <td><c:out value="${a.id}" /></td>
                    <td><c:out value="${a.name}" /></td>
                    <td><c:out value="${a.type}" /></td>
                    <td><c:out value="${a.owner}" /></td>
                    <td><c:out value="${a.createdAt}" /></td>
                </tr>
            </c:forEach>
            <c:if test="${empty assets}">
                <tr><td colspan="5">No assets found.</td></tr>
            </c:if>
        </tbody>
    </table>

    <h2>Add Asset</h2>
    <form class="inline" method="post" action="${pageContext.request.contextPath}/assets">
        <input name="name"  placeholder="Name"  required />
        <input name="type"  placeholder="Type" />
        <input name="owner" placeholder="Owner" />
        <button type="submit">Create</button>
    </form>
</body>
</html>
