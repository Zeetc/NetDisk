package com.catchiz.interceptor;

import com.catchiz.domain.User;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PrivilegeInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session=request.getSession();
        User user=(User) session.getAttribute("user");
        if(user==null){
            response.sendRedirect(request.getContextPath()+"/pages/login.jsp");
            return false;
        }
        return true;
    }
}
