package com.catchiz.service;

import com.catchiz.domain.MyFile;
import com.catchiz.domain.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {

    void storeFile(MyFile file) throws IOException;

    boolean delFile(int fileId);

    void changeFileValid(int fileId,int isValidFile);

    MyFile getFileById(int fileId);

    int getCurPid(int pid);

    boolean createFolder(String foldName, int uid, int pid) throws IOException;

    String getFilePathById(int id);

    List<MyFile> findByInfo(int pid,int userId,int curPage,int pageSize,String fileName,boolean isIgnoreValid);

    int findCountByInfo(int pid, int userId, String fileName, boolean isIgnoreValid);

    MyFile getFileByPath(String filePath);

    boolean storeFilePrepare(MultipartFile multipartFile, User user, int pid) throws IOException;
}
