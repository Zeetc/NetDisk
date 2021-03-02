package com.catchiz.controller;


import com.catchiz.domain.CommonResult;
import com.catchiz.domain.CommonStatus;
import com.catchiz.service.UserService;
import com.catchiz.utils.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String emailSendUser;

    public UserController(UserService userService, StringRedisTemplate redisTemplate, JavaMailSender mailSender) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
    }

    @GetMapping("/exit")
    @ApiOperation("普通用户退出")
    public CommonResult exit(@ApiIgnore HttpServletRequest request){
        request.getSession().invalidate();
        return new CommonResult(CommonStatus.OK,"退出成功");
    }

    @GetMapping("/refreshToken")
    @ApiOperation("刷新token")
    public CommonResult refreshToken(@RequestHeader String Authorization){
        Claims claims=JwtTokenUtil.getClaimsFromToken(Authorization);
        if(claims==null)return new CommonResult(CommonStatus.FORBIDDEN,"无效认证");
        return new CommonResult(CommonStatus.OK,"刷新认证成功",JwtTokenUtil.generateToken(claims.getSubject(),(boolean) claims.get("isManager")));
    }

    @GetMapping("/applyForResetPassword")
    @ApiOperation("申请修改密码")
    public CommonResult applyForResetPassword(@RequestHeader String Authorization){
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String uid=Integer.toString(userId);
        if(operations.get(uid)!=null)return new CommonResult(CommonStatus.FORBIDDEN,"频繁请求, 1分钟后再试");
        String uuid= UUID.randomUUID().toString().substring(0,6);
        operations.set(uuid, uid,24, TimeUnit.HOURS);
        operations.set(uid,uid,1, TimeUnit.MINUTES);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailSendUser);
        message.setTo(userService.getEmailById(userId));
        message.setSubject("网盘修改密码邮箱验证");
        message.setText("验证码是："+uuid);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            return new CommonResult(CommonStatus.EXCEPTION,"邮箱发送失败");
        }
        return new CommonResult(CommonStatus.OK,"申请成功");
    }

    @PatchMapping("/resetPassword")
    @ApiOperation("修改账户密码")
    public CommonResult resetPassword(@RequestParam("password")String password,
                                      @RequestParam("uuid")String uuid,
                                      @RequestHeader String Authorization){
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String uid=operations.get(uuid);
        if(uid==null)return new CommonResult(CommonStatus.FORBIDDEN,"非法参数");
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        if(userId!=Integer.parseInt(uid))return new CommonResult(CommonStatus.FORBIDDEN,"没有权限");
        redisTemplate.delete(uuid);
        if(userService.resetPassword(userId,password)){
            return new CommonResult(CommonStatus.OK,"修改成功");
        }
        return new CommonResult(CommonStatus.EXCEPTION,"修改失败");
    }

    @PatchMapping("/resetEmail/{email}")
    @ApiOperation("修改账户邮箱")
    public CommonResult resetEmail(@PathVariable("email")String email,
                                   @RequestHeader String Authorization){
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
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        if(userService.resetUsername(userId,username)){
            return new CommonResult(CommonStatus.OK,"修改成功");
        }
        return new CommonResult(CommonStatus.EXCEPTION,"修改失败");
    }

    @GetMapping("/checkEmailExist/{email}")
    @ApiOperation("查看邮箱是否存在")
    public CommonResult checkEmailExist(@PathVariable("email")String email){
        if(userService.checkEmailExist(email))return new CommonResult(CommonStatus.FORBIDDEN,"邮箱已存在");
        return new CommonResult(CommonStatus.OK,"邮箱合法");
    }

}
