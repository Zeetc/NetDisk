package com.catchiz.controller;

import com.catchiz.domain.MyFile;
import com.catchiz.domain.User;
import com.catchiz.service.FileService;
import com.catchiz.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/manager")
public class ManagerController {
    private final UserService userService;
    private final FileService fileService;

    public ManagerController(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService = fileService;
    }

    @PostMapping("/login")
    @ApiOperation("管理用户登录")
    public String login(User user, HttpSession session){
        User manager=userService.managerLogin(user);
        if(manager!=null){
            session.setAttribute("manager",true);
            session.setAttribute("user",manager);
        }
        return "redirect:/manager/getAllUser";
    }

    @GetMapping("/loginUI")
    @ApiOperation("跳转到管理登录页面")
    public String loginUI(HttpSession session){
        if(session.getAttribute("manager")!=null)return "redirect:/manager/getAllUser";
        return "managerLogin";
    }

    @GetMapping("/getAllUser")
    @ApiOperation("获取所有用户")
    public ModelAndView getAllUser(){
        List<User> userList=userService.getAllUser();
        ModelAndView modelAndView=new ModelAndView();
        modelAndView.setViewName("managerPage");
        modelAndView.addObject("userList",userList);
        return modelAndView;
    }

    @DeleteMapping("/delUser/{userId}")
    @ApiOperation("删除用户")
    public String delUser(@PathVariable("userId") int userId){
        userService.delUser(userId);
        return "redirect:/manager/getAllUser";
    }

    @DeleteMapping("/delFile")
    @ApiOperation("删除文件")
    public String delFile(int fileId,int uid,
                          @RequestParam(value = "curPage",required = false,defaultValue = "1")int curPage,
                          @RequestParam(value = "fileName",required = false,defaultValue = "null")String fileName,
                          RedirectAttributes attributes){
        if(!fileService.delFile(fileId))return "errorPage";
        attributes.addAttribute("userId",uid);
        attributes.addAttribute("curPage",curPage);
        attributes.addAttribute("fileName",fileName);
        return "redirect:/manager/subFile";
    }

    @PatchMapping("/changeFileValid")
    @ApiOperation("改变文件合法属性->false的话普通用户无法获取文件")
    public String changeFileValid(int fileId, int isValidFile, int uid, RedirectAttributes attributes){
        fileService.changeFileValid(fileId,isValidFile);
        attributes.addAttribute("userId",uid);
        return "redirect:/manager/subFile";
    }

    private static final int PAGE_SIZE=5;

    @GetMapping("/subFile")
    @ApiOperation("根据信息查看当前文件夹下所有文件")
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
        FileController.packModelInfo(curPage, fileName, modelAndView, totalCount, PAGE_SIZE);
        modelAndView.addObject("userId",userId);
        return modelAndView;
    }

    @GetMapping("/parentFile")
    @ApiOperation("返回上一级")
    public String parentFile(@RequestParam(value = "pid",required = false,defaultValue = "-1")int pid,
                             @RequestParam("userId") int userId,
                             RedirectAttributes attributes){
        int curPid=(pid==-1?-1:fileService.getCurPid(pid));
        attributes.addAttribute("pid",curPid);
        attributes.addAttribute("userId",userId);
        return "redirect:/manager/subFile";
    }

    @GetMapping("/exit")
    @ApiOperation("管理用户退出")
    public String exit(HttpServletRequest request){
        request.getSession().invalidate();
        return "managerLogin";
    }

}
