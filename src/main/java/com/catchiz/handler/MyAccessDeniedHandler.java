package com.catchiz.handler;

import com.catchiz.pojo.CommonResult;
import com.catchiz.pojo.CommonStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * 授权异常处理（主要用户权限校验异常）
 */
@Component
public class MyAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(mapper.writeValueAsString(new CommonResult(CommonStatus.FORBIDDEN,"密码/验证码错误或权限不足",
                Arrays.asList(accessDeniedException.getMessage(),request.getServletPath(),System.currentTimeMillis())
                ))
        );
    }
}