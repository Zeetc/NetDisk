package com.catchiz.utils;

import com.catchiz.domain.MyFile;
import com.catchiz.mapper.FileMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Timestamp;

@Service
public class FileUtils {
    private final FileMapper fileMapper;

    public FileUtils(FileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    public boolean createDir(String foldName,String path,int uid,int pid){
        boolean dir=new File(path).mkdir();
        if(!dir)return false;
        MyFile file=new MyFile();
        file.setFilename(foldName);
        file.setFilePath(path);
        file.setFileSize(0);
        file.setIsValidFile(1);
        file.setUploadDate(new Timestamp(System.currentTimeMillis()));
        file.setUid(uid);
        file.setPid(pid);
        fileMapper.storeFile(file);
        return true;
    }

    public boolean delFile(String filePath){
        File file=new File(filePath);
        return delFileRecur(file);
    }

    public boolean delFileRecur(File file) throws NullPointerException{
        if(!file.isDirectory()){
            fileMapper.delFileByPath(file.getPath());
            return file.delete();
        }
        File[] files=file.listFiles();
        if (files != null) {
            for (File subFile : files) {
                delFileRecur(subFile);
            }
        }
        fileMapper.delFileByPath(file.getPath());
        return file.delete();
    }
}
