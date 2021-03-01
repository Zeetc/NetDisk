package com.catchiz.service.impl;

import org.springframework.security.web.authentication.WebAuthenticationDetails;
import javax.servlet.http.HttpServletRequest;

/**
 * 获取用户登录时携带的额外信息
 * 也就是验证码
 */
public class CustomWebAuthenticationDetails extends WebAuthenticationDetails {

    private static final long serialVersionUID = 1L;

    private final String verifyCode;

    public CustomWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        /* verifyCode为页面中验证码的name */
        verifyCode = request.getParameter("verifyCode");
    }

    public String getVerifyCode() {
        return this.verifyCode;
    }

    @Override
    public String toString() {
        return super.toString() + "; VerifyCode: " + this.getVerifyCode();
    }
}