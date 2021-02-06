package com.catchiz.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ManagerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session=request.getSession();
        Boolean isManager= (Boolean) session.getAttribute("manager");
        if(isManager==null||!isManager){
            response.sendRedirect(request.getContextPath()+"/pages/managerLogin.jsp");
            return false;
        }
        return true;
    }
}
