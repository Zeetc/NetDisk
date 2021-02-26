package com.catchiz.controller;

import com.catchiz.domain.*;
import com.catchiz.service.FileService;
import com.catchiz.service.UserService;
import com.catchiz.utils.JwtUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
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
    public CommonResult login(User user){
        User manager=userService.managerLogin(user);
        if(manager==null){
            return new CommonResult(CommonStatus.NOTFOUND,"登录失败");
        }
        Map<String,Object> map=new HashMap<>();
        map.put("manager",true);
        map.put("userId",manager.getId());
        return new CommonResult(CommonStatus.OK,"登录成功", JwtUtils.generate(map));
    }

    @GetMapping("/getAllUser")
    @ApiOperation("获取所有用户")
    public CommonResult getAllUser(){
        List<User> userList=userService.getAllUser();
        return new CommonResult(CommonStatus.OK,"查询成功",userList);
    }

    @DeleteMapping("/delUser/{userId}")
    @ApiOperation("删除用户")
    public CommonResult delUser(@PathVariable("userId") int userId){
        if(!userService.delUser(userId))return new CommonResult(CommonStatus.FORBIDDEN,"删除失败");
        return new CommonResult(CommonStatus.OK,"删除成功");
    }

    @DeleteMapping("/delFile")
    @ApiOperation("删除文件")
    public CommonResult delFile(int fileId){
        if(!fileService.delFile(fileId))return new CommonResult(CommonStatus.EXCEPTION,"删除失败");
        return new CommonResult(CommonStatus.OK,"删除成功");
    }

    @PatchMapping("/changeFileValid")
    @ApiOperation("改变文件合法属性->false的话普通用户无法获取文件")
    public CommonResult changeFileValid(int fileId, int isValidFile){
        fileService.changeFileValid(fileId,isValidFile);
        return new CommonResult(CommonStatus.OK,"修改文件属性成功");
    }

    private static final int PAGE_SIZE=5;

    @GetMapping("/subFile")
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
    @ApiOperation("查询父文件ID，可以根据父文件ID返回文件上一级目录")
    public CommonResult parentFile(@RequestParam(value = "pid",required = false,defaultValue = "-1")int pid){
        int curPid=(pid==-1?-1:fileService.getCurPid(pid));
        return new CommonResult(CommonStatus.OK,"查询父文件ID成功",curPid);
    }

    @GetMapping("/exit")
    @ApiOperation("管理用户退出")
    public CommonResult exit(@ApiIgnore HttpServletRequest request){
        request.getSession().invalidate();
        return new CommonResult(CommonStatus.OK,"退出成功");
    }

}
