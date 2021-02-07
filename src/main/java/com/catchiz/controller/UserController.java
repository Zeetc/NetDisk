package com.catchiz.controller;


import com.catchiz.domain.User;
import com.catchiz.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
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

    @RequestMapping("/register")
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

    @RequestMapping("/loginUI")
    public String loginUi(HttpSession session){
        if(session.getAttribute("user")!=null)return "redirect:/file/subFile";
        return "redirect:login";
    }

    @RequestMapping("/login")
    public String login(User user, HttpSession session) throws EmptyResultDataAccessException{
        User u=userService.login(user);
        if(user!=null)session.setAttribute("user",u);
        return "redirect:/file/subFile";
    }

    @RequestMapping("/exit")
    public String exit(HttpServletRequest request){
        request.getSession().invalidate();
        return "redirect:login";
    }

}
