package com.catchiz.service;

import com.catchiz.pojo.FileTree;
import com.catchiz.pojo.MyFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {

    void storeFile(MyFile file) throws IOException;

    boolean delFile(int fileId);

    void changeFileValid(int fileId,boolean isValidFile);

    MyFile getFileById(int fileId);

    int getCurPid(int pid);

    boolean createFolder(String foldName, int uid, int pid) throws IOException;

    String getFilePathById(int id);

    List<MyFile> findByInfo(int pid,int userId,int curPage,int pageSize,String fileName,boolean isIgnoreValid,boolean pageCut);

    int findCountByInfo(int pid, int userId, String fileName, boolean isIgnoreValid);

    MyFile getFileByPath(String filePath);

    boolean storeFilePrepare(MultipartFile multipartFile, int userId, int pid) throws IOException;

    void setChecked(int fileId);

    void setUnchecked(int fileId);

    List<MyFile> getAllCheckedFile();

    List<MyFile> getAllUnCheckedFile();

    FileTree getFileTree(int[] fileIds);

    List<MyFile> getFilesByFileTree(FileTree fileTree);

    boolean copyFileTo(int curFileId, int targetFileId,int uid) throws IOException;

    boolean renameFile(Integer fileId, String newName,String originFilePath,String newPath);
}
