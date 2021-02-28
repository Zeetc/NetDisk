<%--
  Created by IntelliJ IDEA.
  User: Alan
  Date: 2021/2/1
  Time: 21:44
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<form action="${pageContext.request.contextPath}/user/login" method="post">
    用户名：<input type="text" name="username"><br>
    密码：<input type="password" name="password"><br>
    <input type="submit" value="登录">
</form>
<br>
<a href="${pageContext.request.contextPath}/pages/register.jsp">注册</a>
<br>
<a href="${pageContext.request.contextPath}/manager/loginUI">后台工作人员</a>
</body>

<img src="http://localhost:8888/file/images/45">
</html>
