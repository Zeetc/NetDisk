package com.catchiz.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 自定义认证异常
 */
public class MyAuthenticationException extends AuthenticationException {

    public MyAuthenticationException(String msg) {
        super(msg);
    }
}