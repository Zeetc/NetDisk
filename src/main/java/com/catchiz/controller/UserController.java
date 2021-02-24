package com.catchiz.controller;


import com.catchiz.domain.User;
import com.catchiz.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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
    @ApiOperation("用户注册")
    public ModelAndView register(User user,HttpSession session) throws SQLIntegrityConstraintViolationException, DataIntegrityViolationException {
        ModelAndView modelAndView=new ModelAndView();
        int userId=userService.register(user);
        if(userId!=-1) {
            modelAndView.addObject("userId", userId);
        }else {
            modelAndView.addObject("registerFail",true);
        }
        modelAndView.setViewName("registerInfo");
        session.setAttribute("user",user);
        return modelAndView;
    }

    @GetMapping("/loginUI")
    @ApiOperation("跳转到普通用户登录界面")
    public String loginUi(HttpSession session){
        if(session.getAttribute("user")!=null)return "redirect:/file/subFile";
        return "login";
    }

    @PostMapping("/login")
    @ApiOperation("普通用户登录")
    public String login(User user, HttpSession session) throws EmptyResultDataAccessException{
        User u=userService.login(user);
        if(user!=null)session.setAttribute("user",u);
        return "redirect:/file/subFile";
    }

    @GetMapping("/exit")
    @ApiOperation("普通用户退出")
    public String exit(HttpServletRequest request){
        request.getSession().invalidate();
        return "login";
    }

}
