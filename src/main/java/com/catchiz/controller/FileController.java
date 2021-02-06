package com.catchiz.controller;

import com.catchiz.domain.MyFile;
import com.catchiz.domain.User;
import com.catchiz.service.FileService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

@Controller
@RequestMapping("/file")
public class FileController {

    public static final String fileStorePath="F:\\Cui\\fileStore";

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @RequestMapping("/upload")
    public String uploadFile(MultipartFile[] uploadFile,
                             @RequestParam(value = "pid",required = false,defaultValue = "-1") int pid,
                             HttpSession session,RedirectAttributes attributes) throws IOException {
        User user= (User) session.getAttribute("user");
        String prePath=(pid==-1?fileStorePath+"\\"+user.getId():fileService.getFilePathById(pid));
        for (MultipartFile multipartFile : uploadFile) {
            String filename= multipartFile.getOriginalFilename();
            //存储位置 仓库路径+用户名+文件名
            String path=prePath+"\\"+filename;
            multipartFile.transferTo(new File(path));

            MyFile file=new MyFile();
            file.setFilename(filename);
            file.setFilePath(path);
            file.setFileSize(multipartFile.getSize());
            //TODO 默认为0，代表非法资源，需审核，试验期间暂为1
            file.setIsValidFile(1);
            file.setUploadDate(new Timestamp(System.currentTimeMillis()));
            file.setContentType(multipartFile.getContentType());
            file.setUid(user.getId());
            file.setPid(pid);
            fileService.storeFile(file);
        }
        attributes.addAttribute("pid",pid);
        return "redirect:/file/subFile";
    }

    @RequestMapping("/download")
    public void download(@RequestParam("fileId") int fileId,
                         HttpServletResponse response,HttpSession session) throws IOException {
        MyFile file=fileService.getFileById(fileId);
        User user=(User)session.getAttribute("user");
        if(user.getId()!=file.getUid())return;
        FileInputStream fis=new FileInputStream(file.getFilePath());
        response.setHeader("content-type",file.getContentType());
        response.setHeader("content-disposition","attachment;filename="+file.getFilename());
        ServletOutputStream sos=response.getOutputStream();
        byte[] buff=new byte[1024*8];
        int len;
        while ((len=fis.read(buff)) != -1){
            sos.write(buff,0,len);
        }
    }

    private static final int PAGE_SIZE=5;

    @RequestMapping("/subFile")
    public ModelAndView subFile(@RequestParam(value = "pid",required = false,defaultValue = "-1")int pid,
                                @RequestParam(value = "curPage",required = false,defaultValue = "1")int curPage,
                                @RequestParam(value = "fileName",required = false,defaultValue = "null")String fileName,
                                HttpSession session){
        ModelAndView modelAndView=new ModelAndView();
        User user= (User) session.getAttribute("user");
        List<MyFile> myFileList=fileService.findByInfo(pid,user.getId(),curPage,PAGE_SIZE,fileName,false);
        modelAndView.addObject("myFileList",myFileList);
        modelAndView.addObject("pid",pid);
        modelAndView.setViewName("homePage");
        int totalCount=fileService.findCountByInfo(pid,user.getId(),fileName,false);
        packModelInfo(curPage, fileName, modelAndView, totalCount, PAGE_SIZE);
        return modelAndView;
    }

    static void packModelInfo(@RequestParam(value = "curPage", required = false, defaultValue = "1") int curPage,
                              @RequestParam(value = "fileName", required = false, defaultValue = "null") String fileName,
                              ModelAndView modelAndView,
                              int totalCount,
                              int pageSize) {
        int totalPage = totalCount % pageSize == 0 ? totalCount/ pageSize : (totalCount/ pageSize) + 1 ;
        if(totalPage==0)totalPage = 1;
        modelAndView.addObject("totalPage",totalPage);
        modelAndView.addObject("curPage",curPage);
        modelAndView.addObject("fileName",fileName);
    }

    @RequestMapping("/parentFile")
    public String parentFile(@RequestParam(value = "pid",required = false,defaultValue = "-1")int pid,
                             RedirectAttributes attributes){
        int curPid=(pid==-1?-1:fileService.getCurPid(pid));
        attributes.addAttribute("pid",curPid);
        return "redirect:/file/subFile";
    }

    @RequestMapping("/addFolder")
    public String addFolder(@RequestParam("foldName") String foldName,
                            @RequestParam(value = "pid",required = false,defaultValue = "-1")int pid,
                            HttpSession session,
                            RedirectAttributes attributes) throws IOException {
        User user=(User)session.getAttribute("user");
        fileService.createFolder(foldName,user.getId(),pid);
        attributes.addAttribute("pid",pid);
        return "redirect:/file/subFile";
    }

    @RequestMapping("/delFile")
    public String delFile(int fileId,
                          @RequestParam(value = "curPage",required = false,defaultValue = "1")int curPage,
                          @RequestParam(value = "fileName",required = false,defaultValue = "null")String fileName,
                          HttpSession session,
                          RedirectAttributes attributes){
        User user= (User) session.getAttribute("user");
        MyFile file=fileService.getFileById(fileId);
        if(user.getId()==file.getUid())fileService.delFile(fileId);
        attributes.addAttribute("pid",file.getPid());
        attributes.addAttribute("curPage",curPage);
        attributes.addAttribute("fileName",fileName);
        return "redirect:/file/subFile";
    }
}
