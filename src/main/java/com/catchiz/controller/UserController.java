package com.catchiz.controller;


import com.catchiz.domain.CommonResult;
import com.catchiz.domain.CommonStatus;
import com.catchiz.service.UserService;
import com.catchiz.utils.JwtTokenUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/exit")
    @ApiOperation("普通用户退出")
    public CommonResult exit(@ApiIgnore HttpServletRequest request){
        request.getSession().invalidate();
        return new CommonResult(CommonStatus.OK,"退出成功");
    }

    @PatchMapping("/resetPassword/{password}")
    @ApiOperation("修改账户密码")
    public CommonResult resetPassword(@PathVariable("password")String password,
                                      @RequestHeader String Authorization){
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
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
