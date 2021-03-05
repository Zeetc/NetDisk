package com.catchiz.handler;

import com.catchiz.pojo.CommonResult;
import com.catchiz.pojo.CommonStatus;
import com.catchiz.pojo.User;
import com.catchiz.service.UserService;
import com.catchiz.utils.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component("authenticationSuccessHandler")
public class AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        logger.info("登录成功");
        response.setContentType("application/json;charset=UTF-8");
        String username=authentication.getName();
        User user= userService.getUserById(Integer.parseInt(username));
        String token = JwtTokenUtil.generateToken(username,user.getIsManager()==1);
        user.setPassword(null);
        response.getWriter().write(objectMapper.writeValueAsString(new CommonResult(CommonStatus.OK,"登录成功", Arrays.asList(token,user))));
    }
}