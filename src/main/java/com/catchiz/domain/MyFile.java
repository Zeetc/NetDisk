package com.catchiz.domain;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class MyFile {
    private Integer fileId;

    private String filename;

    private String filePath;

    private Long fileSize;

    private Integer isValidFile;

    private Timestamp uploadDate;

    private String contentType;

    private Integer uid;

    private Integer pid;

}
