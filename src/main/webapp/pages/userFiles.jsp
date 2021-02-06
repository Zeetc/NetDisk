<%--
  Created by IntelliJ IDEA.
  User: Alan
  Date: 2021/2/2
  Time: 20:52
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Title</title>
</head>
<body>



<c:forEach items="${myFileList}" var="file">
    ${file.filename}
    <a href="${pageContext.request.contextPath}/manager/changeFileValid?fileId=${file.fileId}&isValidFile=${file.isValidFile}&uid=${file.uid}">改变文件合法属性</a>
    <a href="${pageContext.request.contextPath}/manager/delFile?fileId=${file.fileId}&uid=${file.uid}&curPage=${curPage}&fileName=${fileName}">删除该文件</a>
    <a href="${pageContext.request.contextPath}/manager/subFile?pid=${file.fileId}&userId=${userId}">查看该文件</a>
    <br>
</c:forEach>


<br>
<form action="${pageContext.request.contextPath}/manager/subFile" method="post">
    <label>
        <input type="text" name="fileName">
        <input type="hidden" name="userId" value="${userId}">
        <input type="hidden" value="${pid}">
        <input type="submit" value="搜索">
    </label>
</form>
<br>


<br>
<br>
<a href="${pageContext.request.contextPath}/manager/subFile?pid=${pid}&userId=${userId}&curPage=${curPage-1>=1?curPage-1:1}&fileName=${fileName}">上一页</a>
<a href="${pageContext.request.contextPath}/manager/subFile?pid=${pid}&userId=${userId}&curPage=${curPage+1>=totalPage?totalPage:curPage+1}&fileName=${fileName}">下一页</a>

<br>
<a href="${pageContext.request.contextPath}/manager/parentFile?pid=${pid}&userId=${userId}">返回上一级目录</a>
<br>
<a href="${pageContext.request.contextPath}/manager/getAllUser">返回用户列表</a>
</body>
</html>
