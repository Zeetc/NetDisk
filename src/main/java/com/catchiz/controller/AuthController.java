package com.catchiz.controller;

import com.catchiz.domain.CommonResult;
import com.catchiz.domain.CommonStatus;
import com.catchiz.domain.User;
import com.catchiz.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public CommonResult register(User user) throws SQLIntegrityConstraintViolationException, DataIntegrityViolationException, IOException {
        if(user.getUsername()==null||user.getPassword()==null||user.getEmail()==null||
                user.getUsername().trim().length()<1||
                user.getPassword().trim().length()<1||
                user.getEmail().trim().length()<1){
            return new CommonResult(CommonStatus.FORBIDDEN,"账号输入不合法");
        }
        if(userService.checkEmailExist(user.getEmail())){
            return new CommonResult(CommonStatus.FORBIDDEN,"邮箱已存在");
        }
        int userId=userService.register(user);
        if(userId!=-1) {
            return new CommonResult(CommonStatus.CREATE,"注册成功",userId);
        }else {
            return new CommonResult(CommonStatus.EXCEPTION,"注册失败");
        }
    }
}
