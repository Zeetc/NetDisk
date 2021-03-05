package com.catchiz.controller;


import com.catchiz.pojo.CommonResult;
import com.catchiz.pojo.CommonStatus;
import com.catchiz.service.UserService;
import com.catchiz.utils.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final StringRedisTemplate redisTemplate;

    private final Pattern emailPattern=Pattern.compile("^\\s*\\w+(?:\\.?[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$");


    public UserController(UserService userService, StringRedisTemplate redisTemplate) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/exit")
    @ApiOperation("普通用户退出")
    public CommonResult exit(@RequestHeader String Authorization){
        redisTemplate.opsForValue().set(Authorization,Authorization,30, TimeUnit.MINUTES);
        return new CommonResult(CommonStatus.OK,"退出成功");
    }

    @GetMapping("/getUserInfo")
    @ApiOperation("获取用户信息")
    public CommonResult getUserInfo(@RequestHeader String Authorization){
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        return new CommonResult(CommonStatus.OK,"查询成功",userService.getUserById(userId));
    }

    @GetMapping("/refreshToken")
    @ApiOperation("刷新token")
    public CommonResult refreshToken(@RequestHeader String Authorization){
        Claims claims=JwtTokenUtil.getClaimsFromToken(Authorization);
        if(claims==null)return new CommonResult(CommonStatus.FORBIDDEN,"无效认证");
        return new CommonResult(CommonStatus.OK,"刷新认证成功",JwtTokenUtil.generateToken(claims.getSubject(),(boolean) claims.get("isManager")));
    }

    @PatchMapping("/resetEmail/{email}")
    @ApiOperation("修改账户邮箱")
    public CommonResult resetEmail(@PathVariable("email")String email,
                                   @RequestHeader String Authorization){
        if(email==null||email.equals("")||!emailPattern.matcher(email).matches()) {
            return new CommonResult(CommonStatus.FORBIDDEN,"邮箱不合法");
        }
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        if(userService.resetEmail(userId,email)){
            return new CommonResult(CommonStatus.OK,"修改成功");
        }
        return new CommonResult(CommonStatus.EXCEPTION,"修改失败");
    }

    @PatchMapping("/resetUsername/{username}")
    @ApiOperation("修改账户名称")
    public CommonResult resetUsername(@PathVariable("username")String username,
                                      @RequestHeader String Authorization){
        if(username==null||username.equals(""))return new CommonResult(CommonStatus.FORBIDDEN,"用户名不合法");
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        if(userService.resetUsername(userId,username)){
            return new CommonResult(CommonStatus.OK,"修改成功");
        }
        return new CommonResult(CommonStatus.EXCEPTION,"修改失败");
    }

    @GetMapping("/checkEmailExist/{email}")
    @ApiOperation("查看邮箱是否存在")
    public CommonResult checkEmailExist(@PathVariable("email")String email){
        if(email==null||email.equals(""))return new CommonResult(CommonStatus.FORBIDDEN,"邮箱不合法");
        if(userService.checkEmailExist(email))return new CommonResult(CommonStatus.FORBIDDEN,"邮箱已存在");
        return new CommonResult(CommonStatus.OK,"邮箱合法");
    }

    @PatchMapping("/applyForResetPassword")
    @ApiOperation("修改密码")
    public CommonResult applyForResetPassword(@RequestHeader String Authorization,
                                              @RequestParam("originPassword")String originPassword,
                                              @RequestParam("password")String password){
        if(password==null||password.equals(""))return new CommonResult(CommonStatus.FORBIDDEN,"密码不合法");
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        if(originPassword==null||originPassword.equals("")||userService.checkPassword(userId,originPassword)!=1){
            return new CommonResult(CommonStatus.FORBIDDEN,"原密码错误");
        }
        userService.resetPassword(userId,password);
        return new CommonResult(CommonStatus.OK,"修改成功");
    }

    @GetMapping("/checkPassword")
    @ApiOperation("检查密码是否是原密码")
    public CommonResult checkPassword(@RequestHeader String Authorization,
                                      @RequestParam("password")String password){
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        int result=userService.checkPassword(userId,password);
        return new CommonResult(CommonStatus.OK,"查询成功",result==1);
    }

}
