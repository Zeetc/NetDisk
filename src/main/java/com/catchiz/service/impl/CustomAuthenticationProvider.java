package com.catchiz.service.impl;

import com.catchiz.exception.MyAccessDeniedException;
import com.catchiz.exception.MyAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.file.AccessDeniedException;

/**
 * 自定义验证用户名密码验证码逻辑，用户通过访问登录url，进入SysUserDetailsService自定义类
 * 然后通过赋值成UserDetails后进入该类，判断密码是否正确
 */
@Slf4j
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Resource
    private SysUserDetailsService sysUserDetailsService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        /* 获取用户输入的用户名和密码 */
        String inputName = authentication.getName();
        String inputPassword = authentication.getCredentials().toString();
        CustomWebAuthenticationDetails details = (CustomWebAuthenticationDetails) authentication.getDetails();
        String verifyCode = details.getVerifyCode();
        //TODO 验证码认证查询
        if (!validateVerify(verifyCode)) {
            throw new MyAccessDeniedException("验证码输入错误");
        }
        /* userDetails为数据库中查询到的用户信息 */
        UserDetails userDetails = sysUserDetailsService.loadUserByUsername(inputName);
        String password = userDetails.getPassword();
        /* 密码加密后 */
        String encodePassword = passwordEncoder.encode(inputPassword);
        /* 校验密码是否一致 */
        System.out.println(passwordEncoder.matches(password,encodePassword));
        if (!passwordEncoder.matches(password,encodePassword)) {
            throw new MyAuthenticationException("密码错误");
        }
        return new UsernamePasswordAuthenticationToken(inputName, encodePassword, userDetails.getAuthorities());
    }

    private boolean validateVerify(String inputVerify) {
        /* 获取当前线程绑定的request对象 */
        //HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        /* 这个validateCode是在servlet中存入session的名字 */
        //String validateCode = ((String) request.getSession().getAttribute("validateCode")).toLowerCase();
        //inputVerify = inputVerify.toLowerCase();
        //log.info("验证码：" + validateCode + "用户输入：" + inputVerify);
        //TODO 修改
        //return validateCode.equals("1111");
        return true;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        /* 和UsernamePasswordAuthenticationToken比较 */
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}