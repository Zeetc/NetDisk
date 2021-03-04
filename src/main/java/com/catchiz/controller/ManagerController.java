package com.catchiz.controller;

import com.catchiz.pojo.*;
import com.catchiz.service.FileService;
import com.catchiz.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/manager")
public class ManagerController {
    private final UserService userService;
    private final FileService fileService;

    private static int PAGE_SIZE;

    public ManagerController(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService = fileService;
    }

    @Value("${NetDisk.pageSize}")
    public void setPageSize(int pageSize){
        ManagerController.PAGE_SIZE = pageSize;
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

    @PatchMapping("/changeFileCheck")
    @ApiOperation("设置文件为已检查")
    public CommonResult changeFileCheck(int fileId){
        fileService.setChecked(fileId);
        return new CommonResult(CommonStatus.OK,"修改文件属性成功");
    }

    @PatchMapping("/changeFileUnCheck")
    @ApiOperation("设置文件为未检查")
    public CommonResult changeFileUnCheck(int fileId){
        fileService.setUnchecked(fileId);
        return new CommonResult(CommonStatus.OK,"修改文件属性成功");
    }

    @GetMapping("/subFile")
    @ApiOperation("根据信息查看当前文件夹下所有文件")
    public CommonResult subFile(@RequestParam(value = "pid",required = false,defaultValue = "-1")int pid,
                                @RequestParam("userId") int userId,
                                @RequestParam(value = "curPage",required = false,defaultValue = "1")int curPage,
                                @RequestParam(value = "fileName",required = false,defaultValue = "null")String fileName,
                                @RequestParam(value = "pageCut",required = false,defaultValue = "true")boolean pageCut){
        if(pid!=-1&&fileService.getFileById(pid)==null)return new CommonResult(CommonStatus.NOTFOUND,"查询失败");
        List<MyFile> myFileList=fileService.findByInfo(pid,userId,curPage,PAGE_SIZE,fileName,true,pageCut);
        int totalCount=fileService.findCountByInfo(pid,userId,fileName,false);
        int curPid=(pid==-1?-1:fileService.getCurPid(pid));
        return FileController.getCommonResult(curPid, curPage, fileName, userId, myFileList, totalCount, PAGE_SIZE);
    }

    @GetMapping("/parentFile")
    @ApiOperation("查询父文件ID，可以根据父文件ID返回文件上一级目录")
    public CommonResult parentFile(@RequestParam(value = "pid",required = false,defaultValue = "-1")int pid){
        int curPid=(pid==-1?-1:fileService.getCurPid(pid));
        return new CommonResult(CommonStatus.OK,"查询父文件ID成功",curPid);
    }

    @GetMapping("/getAllFileByCheck")
    @ApiOperation("根据是否已经审核获取文件，check为1代表已审核，0为未审核")
    public CommonResult getAllCheckedFile(@RequestParam int check){
        if(check==0)return new CommonResult(CommonStatus.OK,"查询成功",fileService.getAllUnCheckedFile());
        return new CommonResult(CommonStatus.OK,"查询成功",fileService.getAllCheckedFile());
    }

}
