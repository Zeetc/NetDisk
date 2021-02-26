package com.catchiz.interceptor;

import com.catchiz.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ManagerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        Claims claim;
        try {
            claim = JwtUtils.getClaim(token);
        }catch (Exception e){
            response.sendRedirect(request.getContextPath()+"/pages/managerLogin.jsp");
            return false;
        }
        Boolean isManager= (Boolean) claim.get("manager");
        if(isManager==null||!isManager){
            response.sendRedirect(request.getContextPath()+"/pages/managerLogin.jsp");
            return false;
        }
        return true;
    }
}
