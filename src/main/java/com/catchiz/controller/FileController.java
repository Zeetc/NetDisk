package com.catchiz.controller;

import com.catchiz.domain.*;
import com.catchiz.service.FileService;
import com.catchiz.utils.JwtTokenUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/file")
public class FileController {

    public static String FILE_STORE_PATH;
    private static int PAGE_SIZE;
    public static String USER_ICON_FOLDER;

    private final FileService fileService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public FileController(FileService fileService, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.fileService = fileService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Value("${NetDisk.fileStorePath}")
    public void setFileStorePath(String fileStorePath) {
        FileController.FILE_STORE_PATH = fileStorePath;
    }
    @Value("${NetDisk.pageSize}")
    public void setPageSize(int pageSize){
        FileController.PAGE_SIZE = pageSize;
    }

    @Value("${NetDisk.userIconFolder}")
    public void setUserIconFolder(String iconFolder){
        FileController.USER_ICON_FOLDER = iconFolder;
    }

    @PostMapping("/upload")
    @ApiOperation("用户上传文件/文件夹")
    public CommonResult uploadFile(MultipartFile[] uploadFile,
                             @RequestParam(value = "pid",required = false,defaultValue = "-1") int pid,
                             @RequestHeader String Authorization) throws IOException {
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
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
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        if(userId!=file.getUid())return new CommonResult(CommonStatus.FORBIDDEN,"无下载权限");
        FileInputStream fis=new FileInputStream(file.getFilePath());
        response.setHeader("content-type",file.getContentType());
        response.addHeader("Content-Disposition", "attachment;fileName=" + file.getFilename());
        sendFileToUser(response, fis);
        return new CommonResult(CommonStatus.OK,"下载成功");
    }

    @ApiIgnore
    private void sendFileToUser(HttpServletResponse response, FileInputStream fis) throws IOException {
        ServletOutputStream sos=response.getOutputStream();
        byte[] buff=new byte[1024*8];
        int len;
        while ((len=fis.read(buff)) != -1){
            sos.write(buff,0,len);
        }
        sos.flush();
        sos.close();
    }

    @GetMapping("/subFile")
    @ApiOperation("根据信息查看当前文件夹下所有文件")
    public CommonResult subFile(@RequestParam(value = "pid",required = false,defaultValue = "-1")int pid,
                                @RequestParam(value = "curPage",required = false,defaultValue = "1")int curPage,
                                @RequestParam(value = "fileName",required = false,defaultValue = "null")String fileName,
                                @RequestParam(value = "pageCut", required = false,defaultValue = "true")boolean pageCut,
                                @RequestHeader String Authorization){
        if(pid!=-1&&fileService.getFileById(pid)==null)return new CommonResult(CommonStatus.NOTFOUND,"查询失败");
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        List<MyFile> myFileList=fileService.findByInfo(pid,userId,curPage,PAGE_SIZE,fileName,false,pageCut);
        int totalCount=fileService.findCountByInfo(pid,userId,fileName,false);
        return getCommonResult(pid, curPage, fileName, userId, myFileList, totalCount, PAGE_SIZE);

    }

    static CommonResult getCommonResult(int pid, int curPage,String fileName, int userId, List<MyFile> myFileList, int totalCount, int pageSize) {
        int totalPage = totalCount % pageSize == 0 ? totalCount / pageSize : (totalCount / pageSize) + 1;
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
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        if(!foldName.equals("")&&fileService.createFolder(foldName,userId,pid)){
            return new CommonResult(CommonStatus.OK,"添加文件夹成功");
        }
        return new CommonResult(CommonStatus.FORBIDDEN,"添加文件夹失败");
    }

    @DeleteMapping("/delFile")
    @ApiOperation("删除文件，需要传一个要删除的文件id，该文件及其子文件将全部被删除")
    public CommonResult delFile(int[] fileId,
                                @RequestHeader String Authorization){
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        for (int fid : fileId) {
            MyFile file=fileService.getFileById(fid);
            if(file==null)continue;
            if(userId!=file.getUid())return new CommonResult(CommonStatus.FORBIDDEN,"无权限");
            fileService.delFile(fid);
        }
        return new CommonResult(CommonStatus.OK,"删除成功");
    }

    @GetMapping("/images/{id}")
    @ApiOperation("传入用户id,得到用户头像")
    public CommonResult image(@PathVariable("id")int id,
                              @ApiIgnore HttpServletResponse response,
                              @RequestHeader String Authorization) throws IOException {
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        if(userId!=id)return new CommonResult(CommonStatus.FORBIDDEN,"无权限");
        FileInputStream fis=new FileInputStream(FILE_STORE_PATH+"\\"+USER_ICON_FOLDER+"\\"+id+".jpg");
        sendFileToUser(response, fis);
        return new CommonResult(CommonStatus.OK,"查询成功");
    }

    @PostMapping("/updateIcon")
    @ApiOperation("更换头像，需要上传一张jpg图片")
    public CommonResult updateIcon(MultipartFile multipartFile,
                                   @RequestHeader String Authorization) throws IOException {
        if(multipartFile.isEmpty()||multipartFile.getContentType()==null)return new CommonResult(CommonStatus.FORBIDDEN,"无效文件");
        if(!multipartFile.getContentType().toLowerCase().startsWith("image/"))return new CommonResult(CommonStatus.FORBIDDEN,"只允许上传图片类型");
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        multipartFile.transferTo(Paths.get(FILE_STORE_PATH + "\\" + USER_ICON_FOLDER + "\\" + userId + ".jpg"));
        return new CommonResult(CommonStatus.OK,"更换成功");
    }

    @GetMapping("/share")
    @ApiOperation("分享文件，会返回一个链接地址，以及一个提取码，分享文件有效期3天")
    public CommonResult shareFiles(int[] file) throws JsonProcessingException {
        FileTree fileTree=fileService.getFileTree(file);
        String uuid= UUID.randomUUID().toString();
        String verifyCode=uuid.substring(0,4);
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set(uuid+verifyCode,objectMapper.writeValueAsString(fileTree),3,TimeUnit.DAYS);
        return new CommonResult(CommonStatus.OK,"分享成功", Arrays.asList(uuid,verifyCode));
    }

    @GetMapping("/getShare")
    @ApiOperation("获取分享的文件，需要输入正确的链接和提取码")
    public CommonResult getShare(String uuid,String verifyCode,String path) throws JsonProcessingException {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String val = operations.get(uuid + verifyCode);
        if (val == null) return new CommonResult(CommonStatus.FORBIDDEN, "错误的分享链接");
        FileTree fileTree = objectMapper.readValue(val, FileTree.class);
        String[] paths = path.split("/");
        List<MyFile> files = fileService.getFilesByFileTree(fileTree.search(paths,fileTree));
        return new CommonResult(CommonStatus.OK, "查询成功", files);
    }

    @PostMapping("/copyFileTo")
    @ApiOperation("复制文件到另一个文件夹，需要当前文件的文件id，和目标文件的id，如果想复制到根目录下，目标文件的id是-1")
    public CommonResult copyFileTo(int curFileId,
                                   int targetFileId,
                                   @RequestHeader String Authorization) throws IOException {
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        fileService.copyFileTo(curFileId,targetFileId,userId);
        return new CommonResult(CommonStatus.OK,"复制成功");
    }
}
