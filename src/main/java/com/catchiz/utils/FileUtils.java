package com.catchiz.utils;

import com.catchiz.controller.FileController;
import com.catchiz.domain.MyFile;
import com.catchiz.mapper.FileMapper;
import org.springframework.stereotype.Service;

import java.io.*;
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
        file.setFileSize(0L);
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
            fileMapper.delFileByPath(file.getPath().replace("\\","/"));
            return file.delete();
        }
        File[] files=file.listFiles();
        if (files != null) {
            for (File subFile : files) {
                delFileRecur(subFile);
            }
        }
        fileMapper.delFileByPath(file.getPath().replace("\\","/"));
        return file.delete();
    }

    public void copyFileAndRename(int userId) throws IOException {
        FileInputStream in = new FileInputStream(FileController.FILE_STORE_PATH+"\\"+FileController.USER_ICON_FOLDER+"\\default.jpg");
        FileOutputStream out = new FileOutputStream(FileController.FILE_STORE_PATH+"\\"+FileController.USER_ICON_FOLDER+"\\"+userId+".jpg");
        byte[] buff = new byte[512];
        int temp;
        while ((temp = in.read(buff)) != -1) {
            out.write(buff, 0, temp);
        }
        out.flush();
        in.close();
        out.close();
    }

    public boolean delUserIcon(int userId) {
        return new File(FileController.FILE_STORE_PATH+"\\"+FileController.USER_ICON_FOLDER+"\\"+userId+".jpg").delete();
    }
}
