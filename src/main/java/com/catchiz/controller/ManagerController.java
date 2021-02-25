package com.catchiz.controller;

import com.catchiz.domain.*;
import com.catchiz.service.FileService;
import com.catchiz.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

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
    @ResponseBody
    @ApiOperation("管理用户登录")
    public CommonResult login(User user,
                              @ApiIgnore HttpSession session){
        User manager=userService.managerLogin(user);
        if(manager==null){
            return new CommonResult(CommonStatus.NOTFOUND,"登录失败");
        }
        session.setAttribute("manager",true);
        session.setAttribute("user",manager);
        return getAllUser();
    }

    @GetMapping("/loginUI")
    @ApiOperation("跳转到管理登录页面")
    public String loginUI(@ApiIgnore HttpSession session){
        if(session.getAttribute("manager")!=null)return "redirect:/manager/getAllUser";
        return "managerLogin";
    }

    @GetMapping("/getAllUser")
    @ResponseBody
    @ApiOperation("获取所有用户")
    public CommonResult getAllUser(){
        List<User> userList=userService.getAllUser();
        return new CommonResult(CommonStatus.OK,"查询成功",userList);
    }

    @DeleteMapping("/delUser/{userId}")
    @ResponseBody
    @ApiOperation("删除用户")
    public CommonResult delUser(@PathVariable("userId") int userId){
        userService.delUser(userId);
        return getAllUser();
    }

    @DeleteMapping("/delFile")
    @ResponseBody
    @ApiOperation("删除文件")
    public CommonResult delFile(int fileId,int uid,
                          @RequestParam(value = "curPage",required = false,defaultValue = "1")int curPage,
                          @RequestParam(value = "fileName",required = false,defaultValue = "null")String fileName){
        if(!fileService.delFile(fileId))return new CommonResult(CommonStatus.EXCEPTION,"删除失败");
        return subFile(fileService.getCurPid(fileId),uid,curPage,fileName);
    }

    @PatchMapping("/changeFileValid")
    @ResponseBody
    @ApiOperation("改变文件合法属性->false的话普通用户无法获取文件")
    public CommonResult changeFileValid(int fileId, int isValidFile, int uid){
        fileService.changeFileValid(fileId,isValidFile);
        return subFile(fileService.getCurPid(fileId),uid,0,null);
    }

    private static final int PAGE_SIZE=5;

    @GetMapping("/subFile")
    @ResponseBody
    @ApiOperation("根据信息查看当前文件夹下所有文件")
    public CommonResult subFile(@RequestParam(value = "pid",required = false,defaultValue = "-1")int pid,
                                @RequestParam("userId") int userId,
                                @RequestParam(value = "curPage",required = false,defaultValue = "1")int curPage,
                                @RequestParam(value = "fileName",required = false,defaultValue = "null")String fileName){
        if(fileService.getFileById(pid)==null)return new CommonResult(CommonStatus.NOTFOUND,"查询失败");
        List<MyFile> myFileList=fileService.findByInfo(pid,userId,curPage,PAGE_SIZE,fileName,true);
        int totalCount=fileService.findCountByInfo(pid,userId,fileName,false);
        int curPid=(pid==-1?-1:fileService.getCurPid(pid));
        int totalPage = totalCount % PAGE_SIZE == 0 ? totalCount/ PAGE_SIZE : (totalCount/ PAGE_SIZE) + 1 ;
        if(totalPage==0)totalPage = 1;
        return new CommonResult(CommonStatus.OK,"查询成功",myFileList,new PageBean(curPid,totalPage,curPage,fileName,userId));
    }

    @GetMapping("/parentFile")
    @ResponseBody
    @ApiOperation("返回上一级")
    public CommonResult parentFile(@RequestParam(value = "pid",required = false,defaultValue = "-1")int pid,
                             @RequestParam("userId") int userId){
        int curPid=(pid==-1?-1:fileService.getCurPid(pid));
        return subFile(curPid,userId,0,null);
    }

    @GetMapping("/exit")
    @ResponseBody
    @ApiOperation("管理用户退出")
    public CommonResult exit(@ApiIgnore HttpServletRequest request){
        request.getSession().invalidate();
        return new CommonResult(CommonStatus.OK,"退出成功");
    }

}
