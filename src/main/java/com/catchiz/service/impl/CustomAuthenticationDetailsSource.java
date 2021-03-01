package com.catchiz.service.impl;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 该接口用于在Spring Security登录过程中对用户的登录信息的详细信息进行填充
 * 该接口实现了AuthenticationDetailsSource类，所以在securityConfig里面添加权限验证类之后，就会来到该类里
 * 并且返回了CustomWebAuthenticationDetails（实现自WebAuthenticationDetails）
 * 所以具体的实现目的就是重写WebAuthenticationDetails类
 */
@Component
public class CustomAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> {
    @Override
    public WebAuthenticationDetails buildDetails(HttpServletRequest request) {
        return new CustomWebAuthenticationDetails(request);
    }
}