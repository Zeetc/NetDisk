package com.catchiz.service.impl;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

/*
 * 获取用户登录时携带的额外信息
 * 也就是验证码
 * 重写自WebAuthenticationDetails类后，构造方法就多了一个内容，也就是获取request里面的verifyCode参数
 * 添加verifyCode私有变量，以便后期判断验证码是否正确，这个验证码是用户输入的验证码
 */
public class CustomWebAuthenticationDetails extends WebAuthenticationDetails {

    private static final long serialVersionUID = 1L;

    private final String verifyCode;

    private final String uuid;

    public String getUuid() {
        return uuid;
    }

    public CustomWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        // verifyCode为页面中验证码的name
        verifyCode = request.getParameter("verifyCode");
        uuid = request.getHeader("Authorization");
    }

    public String getVerifyCode() {
        return this.verifyCode;
    }

    @Override
    public String toString() {
        return super.toString() + "; VerifyCode: " + this.getVerifyCode();
    }
}
