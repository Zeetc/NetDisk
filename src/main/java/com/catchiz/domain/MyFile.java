package com.catchiz.domain;

import lombok.Data;

import java.sql.Timestamp;

@Data
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

}
