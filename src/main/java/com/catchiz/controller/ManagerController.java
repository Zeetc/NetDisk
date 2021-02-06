package com.catchiz.controller;

import com.catchiz.domain.MyFile;
import com.catchiz.domain.User;
import com.catchiz.service.impl.FileServiceImpl;
import com.catchiz.service.impl.UserServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/manager")
public class ManagerController {
    private final UserServiceImpl userService;
    private final FileServiceImpl fileService;

    public ManagerController(UserServiceImpl userService, FileServiceImpl fileService) {
        this.userService = userService;
        this.fileService = fileService;
    }

    @RequestMapping("/login")
    public String login(User user, HttpSession session){
        User manager=userService.managerLogin(user);
        System.out.println(manager);
        if(manager!=null){
            session.setAttribute("manager",true);
            session.setAttribute("user",manager);
        }
        return "redirect:/manager/getAllUser";
    }

    @RequestMapping("/loginUI")
    public String loginUI(HttpSession session){
        if(session.getAttribute("manager")!=null)return "redirect:/manager/getAllUser";
        return "redirect:managerLogin";
    }

    @RequestMapping("/getAllUser")
    public ModelAndView getAllUser(){
        List<User> userList=userService.getAllUser();
        ModelAndView modelAndView=new ModelAndView();
        modelAndView.setViewName("managerPage");
        modelAndView.addObject("userList",userList);
        return modelAndView;
    }

    @RequestMapping("/delUser/{userId}")
    public String delUser(@PathVariable("userId") int userId){
        userService.delUser(userId);
        return "redirect:/manager/getAllUser";
    }

    @RequestMapping("/delFile")
    public String delFile(int fileId,int uid,
                          @RequestParam(value = "curPage",required = false,defaultValue = "1")int curPage,
                          @RequestParam(value = "fileName",required = false,defaultValue = "null")String fileName,
                          RedirectAttributes attributes){
        fileService.delFile(fileId);
        attributes.addAttribute("userId",uid);
        attributes.addAttribute("curPage",curPage);
        attributes.addAttribute("fileName",fileName);
        return "redirect:/manager/subFile";
    }

    @RequestMapping("/changeFileValid")
    public String changeFileValid(int fileId, int isValidFile, int uid, RedirectAttributes attributes){
        fileService.changeFileValid(fileId,isValidFile);
        attributes.addAttribute("userId",uid);
        return "redirect:/manager/subFile";
    }

    private static final int PAGE_SIZE=5;

    @RequestMapping("/subFile")
    public ModelAndView subFile(@RequestParam(value = "pid",required = false,defaultValue = "-1")int pid,
                                @RequestParam("userId") int userId,
                                @RequestParam(value = "curPage",required = false,defaultValue = "1")int curPage,
                                @RequestParam(value = "fileName",required = false,defaultValue = "null")String fileName){
        ModelAndView modelAndView=new ModelAndView();
        List<MyFile> myFileList=fileService.findByInfo(pid,userId,curPage,PAGE_SIZE,fileName,true);
        modelAndView.addObject("myFileList",myFileList);
        modelAndView.addObject("pid",pid);
        modelAndView.setViewName("userFiles");
        int totalCount=fileService.findCountByInfo(pid,userId,fileName,false);
        int totalPage = totalCount % PAGE_SIZE == 0 ? totalCount/PAGE_SIZE : (totalCount/PAGE_SIZE) + 1 ;
        if(totalPage==0)totalPage = 1;
        modelAndView.addObject("totalPage",totalPage);
        modelAndView.addObject("curPage",curPage);
        modelAndView.addObject("fileName",fileName);
        modelAndView.addObject("userId",userId);
        return modelAndView;
    }

    @RequestMapping("/parentFile")
    public String parentFile(@RequestParam(value = "pid",required = false,defaultValue = "-1")int pid,
                             @RequestParam("userId") int userId,
                             RedirectAttributes attributes){
        int curPid=(pid==-1?-1:fileService.getCurPid(pid));
        attributes.addAttribute("pid",curPid);
        attributes.addAttribute("userId",userId);
        return "redirect:/manager/subFile";
    }

    @RequestMapping("/exit")
    public String exit(HttpServletRequest request){
        request.getSession().invalidate();
        return "redirect:/managerLogin";
    }

}
