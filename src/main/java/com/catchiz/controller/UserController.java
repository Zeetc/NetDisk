package com.catchiz.controller;


import com.catchiz.domain.CommonResult;
import com.catchiz.domain.CommonStatus;
import com.catchiz.domain.User;
import com.catchiz.service.UserService;
import com.catchiz.utils.JwtUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public CommonResult register(User user) throws SQLIntegrityConstraintViolationException, DataIntegrityViolationException {
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

    @PostMapping("/login")
    @ApiOperation("普通用户登录")
    public CommonResult login(User user) throws EmptyResultDataAccessException{
        User u=userService.login(user);
        if(u==null)return new CommonResult(CommonStatus.NOTFOUND,"登录失败");
        Map<String, Object> map = new HashMap<>();
        map.put("userId", u.getId());
        return new CommonResult(CommonStatus.OK,"登录成功", JwtUtils.generate(map));
    }

    @GetMapping("/exit")
    @ApiOperation("普通用户退出")
    public CommonResult exit(@ApiIgnore HttpServletRequest request){
        request.getSession().invalidate();
        return new CommonResult(CommonStatus.OK,"退出成功");
    }

    @PatchMapping("/resetPassword/{password}")
    @ApiOperation("修改密码")
    public CommonResult resetPassword(@PathVariable("password")String password,
                                      @RequestHeader String Authorization){
        Integer userId= (Integer) JwtUtils.getClaim(Authorization).get("userId");
        if(userService.resetPassword(userId,password)){
            return new CommonResult(CommonStatus.OK,"修改成功");
        }
        return new CommonResult(CommonStatus.FORBIDDEN,"修改失败");
    }

    @GetMapping("/checkEmailExist/{email}")
    @ApiOperation("查看邮箱是否存在")
    public CommonResult checkEmailExist(@PathVariable("email")String email){
        if(userService.checkEmailExist(email))return new CommonResult(CommonStatus.FORBIDDEN,"邮箱已存在");
        return new CommonResult(CommonStatus.OK,"邮箱合法");
    }

}
