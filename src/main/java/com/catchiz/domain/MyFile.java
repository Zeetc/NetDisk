package com.catchiz.domain;

import java.sql.Timestamp;

public class MyFile {
    private int fileId;
    private String filename;
    private String filePath;
    private long fileSize;
    private int isValidFile;
    private Timestamp uploadDate;
    private String contentType;
    private int uid;
    private int pid;

    @Override
    public String toString() {
        return "MyFile{" +
                "fileId=" + fileId +
                ", filename='" + filename + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", isValidFile=" + isValidFile +
                ", uploadDate=" + uploadDate +
                ", contentType='" + contentType + '\'' +
                ", uid=" + uid +
                ", pid=" + pid +
                '}';
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getIsValidFile() {
        return isValidFile;
    }

    public void setIsValidFile(int isValidFile) {
        this.isValidFile = isValidFile;
    }

    public Timestamp getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Timestamp uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

}
