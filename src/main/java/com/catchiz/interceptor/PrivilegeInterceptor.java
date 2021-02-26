package com.catchiz.interceptor;

import com.catchiz.domain.User;
import com.catchiz.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class PrivilegeInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        Claims claim;
        try {
            claim = JwtUtils.getClaim(token);
        }catch (Exception e){
            return false;
        }
        User user=(User) claim.get("user");
        if(user==null){
            response.sendRedirect(request.getContextPath()+"/pages/login.jsp");
            return false;
        }
        return true;
    }
}
