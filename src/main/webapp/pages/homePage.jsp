<%--
  Created by IntelliJ IDEA.
  User: Alan
  Date: 2021/2/1
  Time: 21:47
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Title</title>


</head>
<body>
homePage
<br>
<a href="${pageContext.request.contextPath}/user/exit">退出登录</a>
<br>
<br>

<form action="${pageContext.request.contextPath}/file/upload" method="post" enctype="multipart/form-data">
    添加文件：<br>
    <input type="file" name="uploadFile"><br>
    <input type="hidden" name="pid" value="${pid}">
    <input type="submit" value="在当前目录下添加该文件">
</form>
<br>

<form action="${pageContext.request.contextPath}/file/upload" method="post" enctype="multipart/form-data">
    添加文件夹：<br>
    <input type="file" name="uploadFile" webkitdirectory mozdirectory><br>
    <input type="hidden" name="pid" value="${pid}">
    <input type="submit" value="在当前目录下添加该文件">
</form>
<br>

<c:forEach items="${myFileList}" var="file">

    <c:choose>
        <c:when test="${file.contentType eq null}">${file.filename}</c:when>
        <c:otherwise>
            <a href="${pageContext.request.contextPath}/file/download?fileId=${file.fileId}">${file.filename}</a>
        </c:otherwise>
    </c:choose>

    <c:choose>
        <c:when test="${file.contentType eq null}">
            <a href="${pageContext.request.contextPath}/file/subFile?pid=${file.fileId}">进入该文件夹</a>
        </c:when>
    </c:choose>


    <a href="${pageContext.request.contextPath}/file/delFile?fileId=${file.fileId}&curPage=${curPage}&fileName=${fileName}">删除该文件</a>
    <br>
</c:forEach>

<a href="${pageContext.request.contextPath}/file/parentFile?pid=${pid}">返回上一级目录</a>

<br>
<form action="${pageContext.request.contextPath}/file/subFile" method="post">
    <input type="text" name="fileName">
    <input type="hidden" value="${pid}">
    <input type="submit" value="搜索">
</form>
<br>

<form action="${pageContext.request.contextPath}/file/addFolder" method="post">
    <input type="text" name="foldName"><br>
    <input type="hidden" name="pid" value="${pid}">
    <input type="submit" value="创建文件夹">
</form>



<br>
<br>
<a href="${pageContext.request.contextPath}/file/subFile?pid=${pid}&curPage=${curPage-1>=1?curPage-1:1}&fileName=${fileName}">上一页</a>
<a href="${pageContext.request.contextPath}/file/subFile?pid=${pid}&curPage=${curPage+1>=totalPage?totalPage:curPage+1}&fileName=${fileName}">下一页</a>

</body>
</html>
