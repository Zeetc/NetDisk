package com.catchiz.utils;

import com.catchiz.controller.FileController;
import com.catchiz.pojo.MyFile;
import com.catchiz.mapper.FileMapper;
import org.springframework.stereotype.Service;

import javax.activation.MimetypesFileTypeMap;
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
        file.setIsValidFile(true);
        file.setUploadDate(new Timestamp(System.currentTimeMillis()));
        file.setUid(uid);
        file.setPid(pid);
        file.setIsChecked(false);
        fileMapper.storeFile(file);
        return true;
    }

    public boolean delFile(String filePath,boolean isShare){
        File file=new File(filePath);
        return delFileRecur(file,isShare);
    }

    public boolean delFileRecur(File file,boolean isShare) throws NullPointerException{
        if(!file.isDirectory()){
            if(!isShare)fileMapper.delFileByPath(file.getPath().replace("\\","/"));
            return file.delete();
        }
        File[] files=file.listFiles();
        if (files != null) {
            for (File subFile : files) {
                delFileRecur(subFile,isShare);
            }
        }
        if(!isShare)fileMapper.delFileByPath(file.getPath().replace("\\","/"));
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

    public boolean copyFileTo(File source,String targetPath,int uid,int pid) throws IOException {
        File targetFile = new File(targetPath);
        if(!targetFile.exists()){
            if(!targetFile.mkdir())return false;
            MyFile myFile=constructMyFileByFile(targetFile,uid,pid);
            if(fileMapper.getFileByPath(myFile.getFilePath())==null){
                fileMapper.storeFile(myFile);
            }
        }
        //如果source是文件夹，则在目的地址中创建新的文件夹
        if(source.isDirectory()){
            File file = new File(targetPath+"\\"+source.getName());//用目的地址加上source的文件夹名称，创建新的文件夹
            if(!file.mkdir())return false;
            MyFile myFile=constructMyFileByFile(file,uid,pid);
            if(fileMapper.getFileByPath(myFile.getFilePath())==null){
                fileMapper.storeFile(myFile);
            }
            //得到source文件夹的所有文件及目录
            File[] files = source.listFiles();
            if(files==null||files.length==0){
                return true;
            }else{
                for (File value : files) {
                    copyFileTo(value, file.getPath(), uid, myFile.getFileId());
                }
            }
        }
        //source是文件，则用字节输入输出流复制文件
        else if(source.isFile()){
            FileInputStream fis = new FileInputStream(source);
            //创建新的文件，保存复制内容，文件名称与源文件名称一致
            File curFile = new File(targetPath+"\\"+source.getName());
            if(!copyFile(curFile,fis))return false;
            MyFile myFile=constructMyFileByFile(curFile,uid,pid);
            if(fileMapper.getFileByPath(myFile.getFilePath())==null){
                fileMapper.storeFile(myFile);
            }
        }
        return true;
    }

    public boolean copyFile(File curFile,FileInputStream fis) throws IOException {
        if(!curFile.exists()&&!curFile.createNewFile()){
            return false;
        }
        FileOutputStream fos = new FileOutputStream(curFile);
        // 读写数据
        // 定义数组
        byte[] b = new byte[1024];
        // 定义长度
        int len;
        // 循环读取
        while ((len = fis.read(b))!=-1) {
            // 写出数据
            fos.write(b, 0 , len);
        }
        //关闭资源
        fos.flush();
        fos.close();
        fis.close();
        return true;
    }

    public MyFile constructMyFileByFile(File file,int uid,int pid){
        return new MyFile(
                null,
                file.getName(),
                file.getPath().replace("\\","/"),
                file.length(),
                true,
                new Timestamp(System.currentTimeMillis()),
                file.isDirectory() ? null : new MimetypesFileTypeMap().getContentType(file),
                uid,
                pid,
                false
        );
    }

    public boolean renameFile(String originFilePath, String newPath) {
        File file=new File(originFilePath);
        return file.renameTo(new File(newPath));
    }

    public boolean copyFileToShare(File source,String targetPath) throws IOException {
        File targetFile = new File(targetPath);
        if(!targetFile.exists()){
            if(!targetFile.mkdir())return false;
        }
        //如果source是文件夹，则在目的地址中创建新的文件夹
        if(source.isDirectory()){
            File file = new File(targetPath+"\\"+source.getName());//用目的地址加上source的文件夹名称，创建新的文件夹
            if(!file.mkdir())return false;
            //得到source文件夹的所有文件及目录
            File[] files = source.listFiles();
            if(files==null||files.length==0){
                return true;
            }else{
                for (File value : files) {
                    copyFileToShare(value, file.getPath());
                }
            }
        }
        //source是文件，则用字节输入输出流复制文件
        else if(source.isFile()){
            FileInputStream fis = new FileInputStream(source);
            //创建新的文件，保存复制内容，文件名称与源文件名称一致
            File curFile = new File(targetPath+"\\"+source.getName());
            copyFile(curFile,fis);
        }
        return true;
    }
}
