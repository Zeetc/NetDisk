package com.catchiz.controller;

import com.catchiz.domain.*;
import com.catchiz.service.FileService;
import com.catchiz.utils.JwtUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {

    public static String FILE_STORE_PATH;
    private static int PAGE_SIZE;

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Value("${NetDisk.fileStorePath}")
    public void setFileStorePath(String fileStorePath) {
        FileController.FILE_STORE_PATH = fileStorePath;
    }
    @Value("${NetDisk.pageSize}")
    public void setPageSize(int pageSize){
        FileController.PAGE_SIZE = pageSize;
    }

    @PostMapping("/upload")
    @ApiOperation("用户上传文件/文件夹")
    public CommonResult uploadFile(MultipartFile[] uploadFile,
                             @RequestParam(value = "pid",required = false,defaultValue = "-1") int pid,
                             @RequestHeader String Authorization) throws IOException {
        Integer userId= (Integer) JwtUtils.getClaim(Authorization).get("userId");
        if(uploadFile==null) return new CommonResult(CommonStatus.FORBIDDEN,"文件上传不能为空");
        for (MultipartFile multipartFile : uploadFile) {
            if (multipartFile == null || multipartFile.isEmpty()) continue;
            if (!fileService.storeFilePrepare(multipartFile, userId, pid)) {
                return new CommonResult(CommonStatus.EXCEPTION, "上传文件失败");
            }
        }
        return new CommonResult(CommonStatus.OK,"上传文件成功");
    }

    @GetMapping("/download")
    @ApiOperation("用户下载文件")
    public CommonResult download(@RequestParam("fileId") int fileId,
                                 @ApiIgnore HttpServletResponse response,
                                 @RequestHeader String Authorization) throws IOException {
        MyFile file=fileService.getFileById(fileId);
        Integer userId= (Integer) JwtUtils.getClaim(Authorization).get("userId");
        if(!userId.equals(file.getUid()))return new CommonResult(CommonStatus.FORBIDDEN,"无下载权限");
        FileInputStream fis=new FileInputStream(file.getFilePath());
        response.setHeader("content-type",file.getContentType());
        response.addHeader("Content-Disposition", "attachment;fileName=" + file.getFilename());
        ServletOutputStream sos=response.getOutputStream();
        byte[] buff=new byte[1024*8];
        int len;
        while ((len=fis.read(buff)) != -1){
            sos.write(buff,0,len);
        }
        sos.flush();
        sos.close();
        return new CommonResult(CommonStatus.OK,"下载成功");
    }

    @GetMapping("/subFile")
    @ApiOperation("根据信息查看当前文件夹下所有文件")
    public CommonResult subFile(@RequestParam(value = "pid",required = false,defaultValue = "-1")int pid,
                                @RequestParam(value = "curPage",required = false,defaultValue = "1")int curPage,
                                @RequestParam(value = "fileName",required = false,defaultValue = "null")String fileName,
                                @RequestParam(value = "pageCut", required = false,defaultValue = "true")boolean pageCut,
                                @RequestHeader String Authorization){
        if(pid!=-1&&fileService.getFileById(pid)==null)return new CommonResult(CommonStatus.NOTFOUND,"查询失败");
        Integer userId= (Integer) JwtUtils.getClaim(Authorization).get("userId");
        List<MyFile> myFileList=fileService.findByInfo(pid,userId,curPage,PAGE_SIZE,fileName,false,pageCut);
        int totalCount=fileService.findCountByInfo(pid,userId,fileName,false);
        int totalPage = totalCount % PAGE_SIZE == 0 ? totalCount / PAGE_SIZE : (totalCount / PAGE_SIZE) + 1;
        if (totalPage == 0) totalPage = 1;
        return new CommonResult(CommonStatus.OK, "查询成功", myFileList, new PageBean(pid, totalPage, curPage, fileName, userId));

    }

    @GetMapping("/parentFile")
    @ApiOperation("返回上一级")
    public CommonResult parentFile(@RequestParam(value = "pid",required = false,defaultValue = "-1")int pid){
        int curPid=(pid==-1?-1:fileService.getCurPid(pid));
        return new CommonResult(CommonStatus.OK,"查询成功",curPid);
    }

    @PostMapping("/addFolder")
    @ApiOperation("在当前目录下新建文件夹")
    public CommonResult addFolder(@RequestParam("foldName") String foldName,
                                  @RequestParam(value = "pid",required = false,defaultValue = "-1")int pid,
                                  @RequestHeader String Authorization) throws IOException {
        Integer userId= (Integer) JwtUtils.getClaim(Authorization).get("userId");
        if(!foldName.equals("")&&fileService.createFolder(foldName,userId,pid)){
            return new CommonResult(CommonStatus.OK,"添加文件夹成功");
        }
        return new CommonResult(CommonStatus.FORBIDDEN,"添加文件夹失败");
    }

    @DeleteMapping("/delFile")
    @ApiOperation("删除文件")
    public CommonResult delFile(int fileId,
                                @RequestHeader String Authorization){
        Integer userId= (Integer) JwtUtils.getClaim(Authorization).get("userId");
        MyFile file=fileService.getFileById(fileId);
        if(!userId.equals(file.getUid()))return new CommonResult(CommonStatus.FORBIDDEN,"无权限");
        fileService.delFile(fileId);
        return new CommonResult(CommonStatus.OK,"删除成功");
    }
}
