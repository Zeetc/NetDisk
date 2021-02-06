<%--
  Created by IntelliJ IDEA.
  User: Alan
  Date: 2021/2/2
  Time: 17:52
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>后台管理页面</title>
</head>
<body>

<c:forEach items="${userList}" var="user">
    userID:${user.id}
    &nbsp;&nbsp;
    username:${user.username}
    <c:choose>
        <c:when test="${user.isManager eq 1}">
            <a href="${pageContext.request.contextPath}/manager/subFile?userId=${user.id}">查看该用户所有文件</a>
        </c:when>
        <c:otherwise>
            <a href="${pageContext.request.contextPath}/manager/subFile?userId=${user.id}">查看该用户所有文件</a>
            &nbsp;&nbsp;
            <a href="${pageContext.request.contextPath}/manager/delUser/${user.id}">删除该用户</a>
        </c:otherwise>
    </c:choose>
    <br>
</c:forEach>
<br>
<a href="${pageContext.request.contextPath}/manager/exit">退出登录</a>
</body>
</html>
