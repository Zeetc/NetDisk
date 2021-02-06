<%--
  Created by IntelliJ IDEA.
  User: Alan
  Date: 2021/2/1
  Time: 21:38
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>注册结果页面</title>
</head>
<body>

<c:choose>
    <c:when test="${registerFail}">
        注册失败
    </c:when>
    <c:otherwise>
        你的id号码是：${userId}
    </c:otherwise>
</c:choose>

<br>
<a href="${pageContext.request.contextPath}/file/subFile">去home</a>
</body>
</html>
