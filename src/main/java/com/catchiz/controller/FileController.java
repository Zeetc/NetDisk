package com.catchiz.controller;

import com.catchiz.pojo.*;
import com.catchiz.service.FileService;
import com.catchiz.utils.FileUtils;
import com.catchiz.utils.JwtTokenUtil;
import com.catchiz.utils.ZipUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
@RequestMapping("/file")
public class FileController {

    public static String FILE_STORE_PATH;
    private static int PAGE_SIZE;
    public static String USER_ICON_FOLDER;
    public static String SHARE_FOLD;

    private final FileService fileService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private final FileUtils fileUtils;

    public FileController(FileService fileService, StringRedisTemplate redisTemplate, ObjectMapper objectMapper, FileUtils fileUtils) {
        this.fileService = fileService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.fileUtils = fileUtils;
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

    @Value("${NetDisk.shareFolder}")
    public void setShareFold(String shareFold){
        FileController.SHARE_FOLD = shareFold;
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
    public CommonResult download(@RequestParam("fileId") Integer fileId,
                                 @ApiIgnore HttpServletResponse response,
                                 @RequestHeader String Authorization) throws IOException {
        if(fileId==null)return new CommonResult(CommonStatus.FORBIDDEN,"文件ID不能为空");
        MyFile file=fileService.getFileById(fileId);
        int userId = Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        if (userId != file.getUid()||!file.getIsValidFile()) return new CommonResult(CommonStatus.FORBIDDEN, "无下载权限");
        return sendFileToUser(response, file);
    }

    @ApiIgnore
    private CommonResult sendFileToUser(@ApiIgnore HttpServletResponse response, MyFile file) throws IOException {
        String sendFilePath;
        if(file.getContentType()==null){
            String filePath=file.getFilePath();
            filePath=filePath.substring(0,filePath.lastIndexOf("/"));
            String zipFilePath= filePath+"/"+file.getFilename()+ UUID.randomUUID().toString().substring(0,6)+".zip";
            FileOutputStream fos = new FileOutputStream(zipFilePath);
            ZipUtils.toZip(file.getFilePath(),fos,true);
            sendFilePath=zipFilePath;
        }else {
            sendFilePath=file.getFilePath();
        }
        FileInputStream fis = new FileInputStream(sendFilePath);
        if(file.getContentType()!=null)response.setContentType(file.getContentType());
        else response.setContentType("application/x-zip-compressed");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getFilename(), "UTF-8"));
        writeFileToUser(response, fis);
        fis.close();
        if(file.getContentType()==null){
            File zipFile=new File(sendFilePath);
            if(zipFile.exists()&&zipFile.delete())return null;
            else log.info("删除压缩文件异常，异常文件地址 ： "+sendFilePath);
        }
        return null;
    }

    @ApiIgnore
    private void writeFileToUser(HttpServletResponse response, FileInputStream fis) throws IOException {
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
        return packPageBean(pid, curPage, fileName, userId, myFileList, totalCount, PAGE_SIZE);

    }

    static CommonResult packPageBean(int pid, int curPage, String fileName, int userId, List<MyFile> myFileList, int totalCount, int pageSize) {
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
        List<MyFile> list=fileService.findByInfo(pid,userId,1,PAGE_SIZE,foldName,false,false);
        for (MyFile file : list) {
            if(file.getFilename().equals(foldName)){
                return new CommonResult(CommonStatus.FORBIDDEN,"当前文件夹下有重名文件");
            }
        }
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

    @GetMapping("/icon/{id}")
    @ApiOperation("传入用户id,得到用户头像")
    public CommonResult getIcon(@PathVariable("id")Integer id,
                                @ApiIgnore HttpServletResponse response,
                                @RequestHeader String Authorization) throws IOException {
        if(id==null)return new CommonResult(CommonStatus.FORBIDDEN,"用户id参数不能为空");
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        if(userId!=id)return new CommonResult(CommonStatus.FORBIDDEN,"无权限");
        FileInputStream fis=new FileInputStream(FILE_STORE_PATH+"\\"+USER_ICON_FOLDER+"\\"+id+".jpg");
        writeFileToUser(response, fis);
        return new CommonResult(CommonStatus.OK,"查询成功");
    }

    @GetMapping("/images/{fileId}")
    @ApiOperation("获取图片的预览,传入fileId")
    public CommonResult getImage(@PathVariable("fileId")Integer fileId,
                                 @ApiIgnore HttpServletResponse response,
                                 @RequestHeader String Authorization) throws IOException {
        if(fileId==null)return new CommonResult(CommonStatus.FORBIDDEN,"文件id参数不能为空");
        MyFile myFile= fileService.getFileById(fileId);
        if(myFile==null||!myFile.getContentType().toLowerCase().startsWith("images/"))return new CommonResult(CommonStatus.FORBIDDEN,"只允许获取图片类型");
        return packFileToUser(response, Authorization, myFile);
    }

    @GetMapping("/enjoy/{fileId}")
    @ApiOperation("在线播放音乐类型或者视频的文件")
    public CommonResult getMusic(@PathVariable("fileId")Integer fileId,
                                 @ApiIgnore HttpServletResponse response,
                                 @RequestHeader String Authorization) throws IOException {
        if(fileId==null)return new CommonResult(CommonStatus.FORBIDDEN,"文件id参数不能为空");
        MyFile myFile= fileService.getFileById(fileId);
        if(myFile==null)return new CommonResult(CommonStatus.NOTFOUND,"未找到文件");
        String contentType= myFile.getContentType().toLowerCase();
        if(!contentType.startsWith("audio/")|| !contentType.startsWith("video/")){
            return new CommonResult(CommonStatus.FORBIDDEN,"只允许获取音频类型");
        }
        return packFileToUser(response, Authorization, myFile);
    }

    @ApiIgnore
    private CommonResult packFileToUser(@ApiIgnore HttpServletResponse response, @RequestHeader String Authorization, MyFile myFile) throws IOException {
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        if(userId!= myFile.getUid())return new CommonResult(CommonStatus.FORBIDDEN,"无权限");
        FileInputStream fis=new FileInputStream(myFile.getFilePath());
        response.setContentType(myFile.getContentType());
        writeFileToUser(response, fis);
        return null;
    }

    @PostMapping("/updateIcon")
    @ApiOperation("更换头像，需要上传一张jpg图片")
    public CommonResult updateIcon(MultipartFile multipartFile,
                                   @RequestHeader String Authorization) throws IOException {
        if(multipartFile==null||multipartFile.isEmpty()||multipartFile.getContentType()==null)return new CommonResult(CommonStatus.FORBIDDEN,"无效文件");
        if(!multipartFile.getContentType().toLowerCase().startsWith("image/"))return new CommonResult(CommonStatus.FORBIDDEN,"只允许上传图片类型");
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        multipartFile.transferTo(Paths.get(FILE_STORE_PATH + "\\" + USER_ICON_FOLDER + "\\" + userId + ".jpg"));
        return new CommonResult(CommonStatus.OK,"更换成功");
    }

    @GetMapping("/share")
    @ApiOperation("分享文件，会返回一个链接地址，以及一个提取码，分享文件有效期3天")
    public CommonResult shareFiles(int[] file) throws IOException {
        if(file==null||file.length==0)return new CommonResult(CommonStatus.FORBIDDEN,"文件id参数不能为空");
        FileTree fileTree=fileService.getFileTree(file);
        String uuid= UUID.randomUUID().toString();
        String verifyCode=uuid.substring(0,4);
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set(uuid+verifyCode,objectMapper.writeValueAsString(fileTree),3,TimeUnit.DAYS);
        boolean flag=fileService.createShareFolder(FILE_STORE_PATH+"/"+SHARE_FOLD+"/"+uuid+verifyCode,file);
        if(!flag){
            redisTemplate.delete(uuid+verifyCode);
            fileUtils.delFile(FILE_STORE_PATH+"/"+SHARE_FOLD+"/"+uuid+verifyCode,true);
            return new CommonResult(CommonStatus.EXCEPTION,"分享失败");
        }
        return new CommonResult(CommonStatus.OK,"分享成功", Arrays.asList(uuid,verifyCode));
    }

    @GetMapping("/getShare")
    @ApiOperation("获取分享的文件，需要输入正确的链接和提取码,wantDownload表示是否需要下载该文件,默认是false，需要下载再传")
    public CommonResult getShare(String uuid,String verifyCode, String path,
                                 @RequestParam(value = "wantDownload",required = false,defaultValue = "false") Boolean wantDownload,
                                 @ApiIgnore HttpServletResponse response) throws IOException {
        if(uuid==null||verifyCode==null)return new CommonResult(CommonStatus.FORBIDDEN,"参数不能为空");
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        String val = operations.get(uuid + verifyCode);
        if (val == null) return new CommonResult(CommonStatus.FORBIDDEN, "错误的分享链接");
        FileTree fileTree = objectMapper.readValue(val, FileTree.class);
        if(path!=null&&!path.equals("")){
            String[] paths = path.split("/");
            fileTree=fileTree.search(paths,fileTree);
        }
        if(fileTree==null)return new CommonResult(CommonStatus.NOTFOUND,"错误的文件路径");
        List<MyFile> files = fileService.getFilesByFileTree(fileTree);
        if(!wantDownload)return new CommonResult(CommonStatus.OK, "查询成功", files);
        File file=new File(FILE_STORE_PATH+"/"+SHARE_FOLD+"/"+uuid+verifyCode+"/"+path);
        if(!file.exists())return new CommonResult(CommonStatus.NOTFOUND,"该文件夹下未找到文件");
        if(path==null||path.trim().equals(""))return new CommonResult(CommonStatus.NOTFOUND,"无法直接下载根目录，请逐个下载");
        MyFile myFile=new MyFile();
        myFile.setFilePath(FILE_STORE_PATH+"/"+SHARE_FOLD+"/"+uuid+verifyCode+"/"+path);
        myFile.setFilename(file.getName());
        myFile.setContentType(file.isDirectory()?null:new MimetypesFileTypeMap().getContentType(file));
        return sendFileToUser(response,myFile);
    }

    @PostMapping("/copyFileTo")
    @ApiOperation("复制文件到另一个文件夹，需要当前文件的文件id，和目标文件的id，如果想复制到根目录下，目标文件的id是-1")
    public CommonResult copyFileTo(Integer curFileId,
                                   Integer targetFileId,
                                   @RequestHeader String Authorization) throws IOException {
        if(curFileId==null||targetFileId==null)return new CommonResult(CommonStatus.FORBIDDEN,"文件id参数不能为空");
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        if(fileService.copyFileTo(curFileId,targetFileId,userId)) return new CommonResult(CommonStatus.OK,"复制成功");
        return new CommonResult(CommonStatus.EXCEPTION,"复制异常");
    }

    @PatchMapping("/renameFile")
    @ApiOperation("更改文件名称，需要参数：要更改的文件Id，新文件名")
    public CommonResult renameFile(Integer fileId,String newName,@RequestHeader String Authorization){
        if(fileId==null||newName==null||newName.equals("")||fileId==-1)return new CommonResult(CommonStatus.FORBIDDEN,"参数不能为空");
        int userId= Integer.parseInt(Objects.requireNonNull(JwtTokenUtil.getUsernameFromToken(Authorization)));
        MyFile myFile=fileService.getFileById(fileId);
        if(myFile==null)return new CommonResult(CommonStatus.NOTFOUND,"未找到文件");
        if(newName.equals(myFile.getFilename()))return new CommonResult(CommonStatus.FORBIDDEN,"新名字与原名字相同");
        List<MyFile> list=fileService.findByInfo(myFile.getPid(),userId,1,PAGE_SIZE,newName,false,false);
        if(myFile.getUid()!=userId)return new CommonResult(CommonStatus.FORBIDDEN,"没有权限");
        for (MyFile file : list) {
            if(!file.getFileId().equals(fileId) &&file.getFilename().equals(newName)){
                return new CommonResult(CommonStatus.FORBIDDEN,"当前文件夹下有重名文件");
            }
        }
        String originFilePath=myFile.getFilePath();
        int lastPartition=originFilePath.lastIndexOf("/");
        if(lastPartition==-1)return new CommonResult(CommonStatus.EXCEPTION,"更改失败");
        String newPath=originFilePath.substring(0,lastPartition+1)+newName;
        boolean flag=fileService.renameFile(fileId,newName, myFile.getFilePath(),newPath);
        if(flag) return new CommonResult(CommonStatus.OK,"更改成功");
        return new CommonResult(CommonStatus.EXCEPTION,"更改失败");
    }
}
