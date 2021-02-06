package com.catchiz.utils;

import com.catchiz.domain.MyFile;
import com.catchiz.mapper.FileMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Queue;

@Service
public class FileUtils {
    private final FileMapper fileMapper;

    public FileUtils(FileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    public void scanFile(Queue<MyFile> queue) throws IOException {
        MyFile file=queue.poll();
        fileMapper.storeFile(file);
        File directory = new File(file.getFilePath());
        if(!directory.isDirectory()){
            return;
        }
        directory.mkdir();
        File [] files = directory.listFiles();
        for (File subFile : files) {
            MyFile mySubFile=new MyFile();
            file.setFilename(subFile.getName());
            file.setFilePath(file.getFilePath()+"\\"+subFile.getName());
            file.setFileSize(subFile.length());
            //TODO 默认为0，代表非法资源，需审核，试验期间暂为1
            file.setIsValidFile(1);
            file.setUploadDate(new Timestamp(System.currentTimeMillis()));
            file.setContentType(getContentType(subFile.getPath()));
            file.setUid(file.getUid());
            file.setPid(file.getFileId());
            queue.offer(mySubFile);
        }
    }

    public String getContentType(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.probeContentType(path);
    }

    public void createDir(String foldName,String path,int uid,int pid) throws IOException {
        File dir=new File(path);
        dir.mkdir();
        MyFile file=new MyFile();
        file.setFilename(foldName);
        file.setFilePath(path);
        file.setFileSize(0);
        //TODO 默认为0，代表非法资源，需审核，试验期间暂为1
        file.setIsValidFile(1);
        file.setUploadDate(new Timestamp(System.currentTimeMillis()));
        file.setContentType(getContentType(file.getFilePath()));
        file.setUid(uid);
        file.setPid(pid);
        fileMapper.storeFile(file);
    }

    public void delFile(String filePath){
        File file=new File(filePath);
        delFileRecur(file);
    }

    public void delFileRecur(File file) throws NullPointerException{
        if(!file.isDirectory()){
            fileMapper.delFileByPath(file.getPath());
            file.delete();
            return;
        }
        File[] files=file.listFiles();
        for (File subFile : files) {
            delFileRecur(subFile);
        }
        file.delete();
    }
}
