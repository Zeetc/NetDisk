package com.catchiz.service.impl;

import com.catchiz.controller.FileController;
import com.catchiz.domain.MyFile;
import com.catchiz.mapper.FileMapper;
import com.catchiz.service.FileService;
import com.catchiz.utils.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

@Service("fileService")
@Transactional
public class FileServiceImpl implements FileService {
    private final FileMapper fileMapper;
    private final FileUtils fileUtils;

    public FileServiceImpl(FileMapper fileMapper, FileUtils fileUtils) {
        this.fileMapper = fileMapper;
        this.fileUtils = fileUtils;
    }

    @Override
    public void storeFile(MyFile file) {
        if(fileMapper.getFileByPath(file.getFilePath())!=null)return;
        fileMapper.storeFile(file);
    }

    @Override
    public boolean delFile(int fileId) {
        String filePath=fileMapper.getPathById(fileId);
        fileMapper.delFile(fileId);
        return fileUtils.delFile(filePath);
    }

    @Override
    public void changeFileValid(int fileId,int isValidFile) {
        fileMapper.changeFileValid(fileId,isValidFile==1?0:1);
    }

    @Override
    public MyFile getFileById(int fileId) {
        return fileMapper.getFileById(fileId);
    }

    @Override
    public int getCurPid(int pid) {
        return fileMapper.getCurPid(pid);
    }

    @Override
    public boolean createFolder(String foldName, int uid,int pid) {
        String prePath=(pid==-1?FileController.FILE_STORE_PATH +"/"+uid:fileMapper.getPathById(pid));
        String path=prePath+"/"+foldName;
        return fileUtils.createDir(foldName,path,uid,pid);
    }

    @Override
    public String getFilePathById(int id) {
        return fileMapper.getPathById(id);
    }

    @Override
    public List<MyFile> findByInfo(int pid,int userId,int curPage,int pageSize,String fileName,boolean isIgnoreValid){
        int start= (curPage-1)*pageSize;
        return fileMapper.findByInfo(pid,userId,start,pageSize, "%" + fileName + "%",isIgnoreValid);
    }

    @Override
    public int findCountByInfo(int pid, int userId, String fileName, boolean isIgnoreValid) {
        return fileMapper.findCountByInfo(pid,userId, "%" + fileName + "%",isIgnoreValid);
    }

    @Override
    public MyFile getFileByPath(String filePath) {
        return fileMapper.getFileByPath(filePath);
    }

    @Override
    public boolean storeFilePrepare(MultipartFile multipartFile, int userId,int pid) throws IOException {
        String filename= multipartFile.getOriginalFilename();
        if(filename==null)return false;
        String prePath=(pid==-1?FileController.FILE_STORE_PATH +"/"+userId:getFilePathById(pid));
        String[] dirs=filename.split("/");
        int dynamicPid=pid;
        for (int i = 0; i < dirs.length - 1; i++) {
            prePath+="/"+dirs[i];
            MyFile myFile=getFileByPath(prePath);
            if(myFile==null){
                myFile=new MyFile();
                File temp=new File(prePath);
                if(!temp.mkdir())return false;
                //TODO 默认为0，代表非法资源，需审核，试验期间暂为1
                dynamicConstructMyFile(myFile,dirs[i],prePath,0,1,null, userId, dynamicPid);
                storeFile(myFile);
            }
            dynamicPid=myFile.getFileId();
        }
        //存储位置 仓库路径+用户名+文件名
        String path=prePath+"/"+dirs[dirs.length-1];
        multipartFile.transferTo(new File(path));
        MyFile file=new MyFile();
        //TODO 默认为0，代表非法资源，需审核，试验期间暂为1
        dynamicConstructMyFile(file,dirs[dirs.length-1],path, multipartFile.getSize(), 1,multipartFile.getContentType(), userId, dynamicPid);
        storeFile(file);
        return true;
    }

    public void dynamicConstructMyFile(MyFile myFile,String fileName,String filePath,long fileSize,int isValidFile,String contentType,int uid,int pid){
        if(fileName!=null)myFile.setFilename(fileName);
        if(filePath!=null)myFile.setFilePath(filePath);
        if(fileSize!=0)myFile.setFileSize(fileSize);
        myFile.setIsValidFile(isValidFile);
        myFile.setUploadDate(new Timestamp(System.currentTimeMillis()));
        if(contentType!=null)myFile.setContentType(contentType);
        myFile.setUid(uid);
        myFile.setPid(pid);
    }


}
