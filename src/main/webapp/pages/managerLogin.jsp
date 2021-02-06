<%--
  Created by IntelliJ IDEA.
  User: Alan
  Date: 2021/2/5
  Time: 20:05
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<form action="${pageContext.request.contextPath}/manager/login" method="post">
    用户名：<input type="text" name="id"><br>
    密码：<input type="password" name="password"><br>
    <input type="submit" value="登录">
</form>
</body>
</html>
