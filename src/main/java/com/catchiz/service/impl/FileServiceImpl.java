package com.catchiz.service.impl;

import com.catchiz.controller.FileController;
import com.catchiz.domain.MyFile;
import com.catchiz.mapper.FileMapper;
import com.catchiz.service.FileService;
import com.catchiz.utils.FileUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("fileService")
public class FileServiceImpl implements FileService {
    private final FileMapper fileMapper;
    private final FileUtils fileUtils;

    public FileServiceImpl(FileMapper fileMapper, FileUtils fileUtils) {
        this.fileMapper = fileMapper;
        this.fileUtils = fileUtils;
    }

    @Override
    public void storeFile(MyFile file) {
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
        MyFile myFile=fileMapper.getFileById(fileId);
        myFile.setFilePath(myFile.getFilePath().replace("\\","/"));
        return myFile;
    }

    @Override
    public int getCurPid(int pid) {
        return fileMapper.getCurPid(pid);
    }

    @Override
    public boolean createFolder(String foldName, int uid,int pid) {
        String prePath=(pid==-1?FileController.fileStorePath+"\\"+uid:fileMapper.getPathById(pid));
        String path=prePath+"\\"+foldName;
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




}
