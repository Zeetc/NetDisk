package com.catchiz.interceptor;

import com.catchiz.domain.User;
import com.catchiz.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    private String refreshToken(Claims claim) {
        Date nowDate = new Date();
        Date expiration = claim.getExpiration();
        //如果还有有效时间的话，刷新token
        if ((expiration.getTime() - nowDate.getTime()) >= 0) {
            //要刷新token
            Object user = claim.get("user");
            Map<String, Object> map = new HashMap<>();
            map.put("user", user);
            return JwtUtils.generate(map);
        }
        return null;
    }
}
