package com.catchiz.controller;

import com.catchiz.domain.*;
import com.catchiz.service.FileService;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/file")
public class FileController {

    public static final String fileStorePath="F:/Cui/fileStore";
    private static final int PAGE_SIZE=5;

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    @ResponseBody
    @ApiOperation("用户上传文件")
    public CommonResult uploadFile(MultipartFile[] uploadFile,
                             @RequestParam(value = "pid",required = false,defaultValue = "-1") int pid,
                             @ApiIgnore HttpSession session) throws IOException {
        User user= (User) session.getAttribute("user");
        for (MultipartFile multipartFile : uploadFile) {
            if(multipartFile.isEmpty())continue;
            if(!fileService.storeFilePrepare(multipartFile,user,pid)){
                return new CommonResult(CommonStatus.EXCEPTION,"上传文件失败");
            }
        }
        return subFile(pid,0,null,session);
    }

    @GetMapping("/download")
    @ApiOperation("用户下载文件")
    public void download(@RequestParam("fileId") int fileId,
                         @ApiIgnore HttpServletResponse response,
                         @ApiIgnore HttpSession session) throws IOException {
        MyFile file=fileService.getFileById(fileId);
        User user=(User)session.getAttribute("user");
        if(!user.getId().equals(file.getUid()))return;
        FileInputStream fis=new FileInputStream(file.getFilePath());
        response.setHeader("content-type",file.getContentType());
        response.addHeader("Content-Disposition", "attachment;fileName=" + file.getFilename());
        ServletOutputStream sos=response.getOutputStream();
        byte[] buff=new byte[1024*8];
        int len;
        while ((len=fis.read(buff)) != -1){
            sos.write(buff,0,len);
        }
    }

    @GetMapping("/subFile")
    @ResponseBody
    @ApiOperation("根据信息查看当前文件夹下所有文件")
    public CommonResult subFile(@RequestParam(value = "pid",required = false,defaultValue = "-1")int pid,
                                @RequestParam(value = "curPage",required = false,defaultValue = "1")int curPage,
                                @RequestParam(value = "fileName",required = false,defaultValue = "null")String fileName,
                                @ApiIgnore HttpSession session){
        if(fileService.getFileById(pid)==null)return new CommonResult(CommonStatus.NOTFOUND,"查询失败");
        User user= (User) session.getAttribute("user");
        List<MyFile> myFileList=fileService.findByInfo(pid,user.getId(),curPage,PAGE_SIZE,fileName,false);
        int totalCount=fileService.findCountByInfo(pid,user.getId(),fileName,false);
        int totalPage = totalCount % PAGE_SIZE == 0 ? totalCount/ PAGE_SIZE : (totalCount/ PAGE_SIZE) + 1 ;
        if(totalPage==0)totalPage = 1;
        return new CommonResult(CommonStatus.OK,"查询成功",myFileList, new PageBean(pid,totalPage,curPage,fileName,user.getId()));
    }

    @GetMapping("/parentFile")
    @ResponseBody
    @ApiOperation("返回上一级")
    public CommonResult parentFile(@RequestParam(value = "pid",required = false,defaultValue = "-1")int pid,
                             @ApiIgnore HttpSession session){
        int curPid=(pid==-1?-1:fileService.getCurPid(pid));
        return subFile(curPid,1,null,session);
    }

    @PostMapping("/addFolder")
    @ResponseBody
    @ApiOperation("在该目录下添加文件夹")
    public CommonResult addFolder(@RequestParam("foldName") String foldName,
                            @RequestParam(value = "pid",required = false,defaultValue = "-1")int pid,
                            @ApiIgnore HttpSession session) throws IOException {
        User user=(User)session.getAttribute("user");
        boolean flag=true;
        if(!foldName.equals(""))flag=fileService.createFolder(foldName,user.getId(),pid);
        if(flag)return subFile(pid,0,null,session);
        return new CommonResult(CommonStatus.FORBIDDEN,"添加文件夹失败");
    }

    @DeleteMapping("/delFile")
    @ResponseBody
    @ApiOperation("删除文件")
    public CommonResult delFile(int fileId,
                          @RequestParam(value = "curPage",required = false,defaultValue = "1")int curPage,
                          @RequestParam(value = "fileName",required = false,defaultValue = "null")String fileName,
                          @ApiIgnore HttpSession session){
        User user= (User) session.getAttribute("user");
        MyFile file=fileService.getFileById(fileId);
        if(!user.getId().equals(file.getUid()))return new CommonResult(CommonStatus.FORBIDDEN,"无权限");
        fileService.delFile(fileId);
        return subFile(file.getPid(),curPage,fileName,session);
    }
}
