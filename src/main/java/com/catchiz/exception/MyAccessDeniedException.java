package com.catchiz.exception;

import org.springframework.security.access.AccessDeniedException;

/**
 * 自定义授权异常
 */
public class MyAccessDeniedException extends AccessDeniedException {

    public MyAccessDeniedException(String msg) {
        super(msg);
    }
}