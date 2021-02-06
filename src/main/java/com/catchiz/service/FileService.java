package com.catchiz.service;

import com.catchiz.domain.MyFile;

import java.io.IOException;
import java.util.List;

public interface FileService {
    List<MyFile> listAllMyFile(int userId,int pid);

    void storeFile(MyFile file) throws IOException;

    void delFile(int fileId);

    void changeFileValid(int fileId,int isValidFile);

    List<MyFile> listAllMyFileIgnoreValid(int userId,int pid);

    MyFile getFileById(int fileId);

    int getCurPid(int pid);

    void createFolder(String foldName, int uid, int pid) throws IOException;

    String getFilePathById(int id);

    List<MyFile> findByInfo(int pid,int userId,int curPage,int pageSize,String fileName,boolean isIgnoreValid);

    int findCountByInfo(int pid, int userId, String fileName, boolean isIgnoreValid);
}
