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
    <script language="JavaScript">
        function refresh(obj) {
            obj.src = "getVerifyCode?" + Math.random();
        }
        function mouseover(obj) {
            obj.style.cursor = "pointer";
        }
    </script>
</head>
<body>
<form action="${pageContext.request.contextPath}/auth/login" method="post">
    用户名：<input type="text" name="id"><br>
    密码：<input type="password" name="password"><br>
    <input type="text" style="width: 120px;" class="form-control" name="verifyCode" required="required" placeholder="验证码">
    <img src="/getVerifyCode" title="刷新" onclick="refresh(this)" onmouseover="mouseover(this)" />
    <input type="submit" value="登录">
</form>
<br>
<a href="${pageContext.request.contextPath}/pages/register.jsp">注册</a>
<br>
<a href="${pageContext.request.contextPath}/manager/loginUI">后台工作人员</a>
</body>
<%--<img src="http://localhost:8888/file/images/45">--%>
</html>
