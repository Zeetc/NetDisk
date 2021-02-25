package com.catchiz.controller;


import com.catchiz.domain.CommonResult;
import com.catchiz.domain.CommonStatus;
import com.catchiz.domain.User;
import com.catchiz.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.SQLIntegrityConstraintViolationException;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseBody
    @ApiOperation("用户注册")
    public CommonResult register(User user,
                                 @ApiIgnore HttpSession session) throws SQLIntegrityConstraintViolationException, DataIntegrityViolationException {
        int userId=userService.register(user);
        if(userId!=-1) {
            session.setAttribute("user",user);
            return new CommonResult(CommonStatus.CREATE,"注册成功",userId);
        }else {
            return new CommonResult(CommonStatus.EXCEPTION,"注册失败");
        }
    }

    @GetMapping("/loginUI")
    @ApiOperation("跳转到普通用户登录界面")
    public String loginUi(@ApiIgnore HttpSession session){
        if(session.getAttribute("user")!=null)return "redirect:/file/subFile";
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    @ApiOperation("普通用户登录")
    public CommonResult login(User user,
                              @ApiIgnore HttpSession session) throws EmptyResultDataAccessException{
        User u=userService.login(user);
        if(u==null)return new CommonResult(CommonStatus.NOTFOUND,"登录失败");
        session.setAttribute("user",u);
        return new CommonResult(CommonStatus.OK,"登录成功");
    }

    @GetMapping("/exit")
    @ResponseBody
    @ApiOperation("普通用户退出")
    public CommonResult exit(@ApiIgnore HttpServletRequest request){
        request.getSession().invalidate();
        return new CommonResult(CommonStatus.OK,"退出成功");
    }

}
