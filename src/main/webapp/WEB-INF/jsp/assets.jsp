<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.xhait.ti.cmdb.assets.Asset" %>
<!doctype html>
<html>
<head>
  <meta charset="utf-8" />
  <title>Assets</title>
  <style>
    body { font-family: sans-serif; margin: 20px; }
    table { border-collapse: collapse; width: 100%; }
    th, td { border: 1px solid #ccc; padding: 8px; }
    th { background: #f5f5f5; text-align: left; }
    .muted { color: #666; }
  </style>
</head>
<body>
  <h1>Assets</h1>
  <p class="muted">Backed by MongoDB collection <b>Assets</b>. Configure <code>MONGODB_URI</code> and <code>MONGODB_DB</code>.</p>

  <h2>Create</h2>
  <form method="post" action="<%= request.getContextPath() %>/assets">
    <label>Name <input name="name" required /></label>
    <label>Type <input name="type" /></label>
    <label>Owner <input name="owner" /></label>
    <button type="submit">Add</button>
  </form>

  <h2>Latest</h2>
  <%
    List<Asset> assets = (List<Asset>) request.getAttribute("assets");
  %>
  <table>
    <thead>
      <tr>
        <th>Id</th>
        <th>Name</th>
        <th>Type</th>
        <th>Owner</th>
        <th>Created</th>
      </tr>
    </thead>
    <tbody>
      <% if (assets == null || assets.isEmpty()) { %>
        <tr><td colspan="5" class="muted">No assets found.</td></tr>
      <% } else {
           for (Asset a : assets) { %>
        <tr>
          <td><%= a.getId() %></td>
          <td><%= a.getName() %></td>
          <td><%= a.getType() %></td>
          <td><%= a.getOwner() %></td>
          <td><%= a.getCreatedAt() %></td>
        </tr>
      <%   }
         } %>
    </tbody>
  </table>

  <p><a href="<%= request.getContextPath() %>/index.jsp">Home</a></p>
</body>
</html>
