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

@Service
public class FileUtils {
    private final FileMapper fileMapper;

    public FileUtils(FileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    public void createDir(String foldName,String path,int uid,int pid) throws IOException {
        File dir=new File(path);
        dir.mkdir();
        MyFile file=new MyFile();
        file.setFilename(foldName);
        file.setFilePath(path);
        file.setFileSize(0);
        file.setIsValidFile(1);
        file.setUploadDate(new Timestamp(System.currentTimeMillis()));
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
