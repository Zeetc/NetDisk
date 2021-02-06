<%@ page contentType="text/html;charset=UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

<form action="${pageContext.request.contextPath}/user/register" method="post">
    <label>
        <input type="text"  name="username"
               placeholder="用户名称" value="">

        <input type="password" name="password"
                       placeholder="密码" value="">
        <input type="text" name="email"
                       placeholder="邮箱" value="">
        <input type="submit" value="注册">
    </label>


</form>

</body>
</html>
