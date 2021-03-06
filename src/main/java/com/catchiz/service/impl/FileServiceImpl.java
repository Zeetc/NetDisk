package com.catchiz.service.impl;

import com.catchiz.controller.FileController;
import com.catchiz.pojo.FileTree;
import com.catchiz.pojo.MyFile;
import com.catchiz.mapper.FileMapper;
import com.catchiz.service.FileService;
import com.catchiz.utils.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

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
        return fileUtils.delFile(filePath,false);
    }

    @Override
    public void changeFileValid(int fileId,boolean isValidFile) {
        fileMapper.changeFileValid(fileId,isValidFile);
        fileMapper.changeCheck(fileId,true);
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
    public List<MyFile> findByInfo(int pid,int userId,int curPage,int pageSize,String fileName,boolean isIgnoreValid,boolean pageCut){
        int start= (curPage-1)*pageSize;
        return fileMapper.findByInfo(pid,userId,start,pageSize, "%" + fileName + "%",isIgnoreValid,pageCut);
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
                dynamicConstructMyFile(myFile,dirs[i],prePath,0,true,null, userId, dynamicPid);
                storeFile(myFile);
            }
            dynamicPid=myFile.getFileId();
        }
        //存储位置 仓库路径+用户名+文件名
        String path=prePath+"/"+dirs[dirs.length-1];
        multipartFile.transferTo(new File(path));
        MyFile file=new MyFile();
        //TODO 默认为0，代表非法资源，需审核，试验期间暂为1
        dynamicConstructMyFile(file,dirs[dirs.length-1],path, multipartFile.getSize(), true,multipartFile.getContentType(), userId, dynamicPid);
        storeFile(file);
        return true;
    }

    @Override
    public void setChecked(int fileId) {
        fileMapper.changeCheck(fileId,true);
    }

    @Override
    public void setUnchecked(int fileId) {
        fileMapper.changeCheck(fileId,false);
    }

    @Override
    public List<MyFile> getAllCheckedFile(){
        return fileMapper.getAllFileByCheck(true);
    }

    @Override
    public List<MyFile> getAllUnCheckedFile(){
        return fileMapper.getAllFileByCheck(false);
    }

    public void dynamicConstructMyFile(MyFile myFile,String fileName,String filePath,long fileSize,boolean isValidFile,String contentType,int uid,int pid){
        if(fileName!=null)myFile.setFilename(fileName);
        if(filePath!=null)myFile.setFilePath(filePath);
        if(fileSize!=0)myFile.setFileSize(fileSize);
        myFile.setIsValidFile(isValidFile);
        myFile.setUploadDate(new Timestamp(System.currentTimeMillis()));
        if(contentType!=null)myFile.setContentType(contentType);
        myFile.setUid(uid);
        myFile.setPid(pid);
        myFile.setIsChecked(false);
    }

    @Override
    public FileTree getFileTree(int[] fileIds) {
        FileTree fileTree=new FileTree();
        for (Integer fileId : fileIds) {
            FileTree curFileTree=new FileTree(fileId);
            Queue<FileTree> queue=new LinkedList<>();
            queue.offer(curFileTree);
            while (!queue.isEmpty()){
                int size=queue.size();
                for (int i = 0; i < size; i++) {
                    FileTree f=queue.poll();
                    if(f==null)continue;
                    List<Integer> child=fileMapper.getChildFiles(f.getFileId());
                    for (Integer id : child) {
                        FileTree c=new FileTree(id);
                        f.getChildFiles().put(fileMapper.getFilenameById(id),c);
                        queue.add(c);
                    }
                }
            }
            fileTree.getChildFiles().put(fileMapper.getFilenameById(fileId),curFileTree);
        }
        return fileTree;
    }

    @Override
    public List<MyFile> getFilesByFileTree(FileTree fileTree) {
        List<MyFile> list=new ArrayList<>();
        Map<String,FileTree> childFiles =fileTree.getChildFiles();
        for (Map.Entry<String, FileTree> entry : childFiles.entrySet()) {
            MyFile myFile=new MyFile();
            myFile.setFilename(entry.getKey());
            list.add(myFile);
        }
        return list;
    }

    @Override
    public boolean copyFileTo(int curFileId, int targetFileId,int uid) throws IOException {
        MyFile originFile=fileMapper.getFileById(curFileId);
        String targetFilePath;
        if(targetFileId!=-1){
            MyFile targetFile=fileMapper.getFileById(targetFileId);
            if(targetFile.getFilePath().startsWith(originFile.getFilePath()))return false;
            targetFilePath=targetFile.getFilePath();
        }else targetFilePath=FileController.FILE_STORE_PATH+"/"+uid;
        File origin=new File(originFile.getFilePath());
        return fileUtils.copyFileTo(origin,targetFilePath,uid,targetFileId);
    }

    @Override
    public boolean renameFile(Integer fileId, String newName,String originFilePath,String newPath) {
        fileMapper.renameFile(fileId,newName);
        fileMapper.renameFilePath(fileId,newPath);
        return fileUtils.renameFile(originFilePath,newPath);
    }

    @Override
    public boolean createShareFolder(String path, int[] file) throws IOException {
        File shareFile=new File(path);
        if(!shareFile.mkdir())return false;
        for (int fileId : file) {
            MyFile myFile=fileMapper.getFileById(fileId);
            if(!fileUtils.copyFileToShare(new File(myFile.getFilePath()),path)){
                return false;
            }
        }
        return true;
    }
}
