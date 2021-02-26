package com.catchiz.controller;


import com.catchiz.domain.CommonResult;
import com.catchiz.domain.CommonStatus;
import com.catchiz.domain.User;
import com.catchiz.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.SQLIntegrityConstraintViolationException;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public CommonResult register(User user,
                                 @ApiIgnore HttpSession session) throws SQLIntegrityConstraintViolationException, DataIntegrityViolationException {
        if(user.getUsername()==null||user.getPassword()==null||user.getEmail()==null||
            user.getUsername().trim().length()<1||
            user.getPassword().trim().length()<1||
            user.getEmail().trim().length()<1){
            return new CommonResult(CommonStatus.FORBIDDEN,"账户输入不合法");
        }
        int userId=userService.register(user);
        if(userId!=-1) {
            session.setAttribute("user",user);
            return new CommonResult(CommonStatus.CREATE,"注册成功",userId);
        }else {
            return new CommonResult(CommonStatus.EXCEPTION,"注册失败");
        }
    }

    @PostMapping("/login")
    @ApiOperation("普通用户登录")
    public CommonResult login(User user,
                              @ApiIgnore HttpSession session) throws EmptyResultDataAccessException{
        User u=userService.login(user);
        if(u==null)return new CommonResult(CommonStatus.NOTFOUND,"登录失败");
        session.setAttribute("user",u);
        return new CommonResult(CommonStatus.OK,"登录成功");
    }

    @GetMapping("/exit")
    @ApiOperation("普通用户退出")
    public CommonResult exit(@ApiIgnore HttpServletRequest request){
        request.getSession().invalidate();
        return new CommonResult(CommonStatus.OK,"退出成功");
    }

}
