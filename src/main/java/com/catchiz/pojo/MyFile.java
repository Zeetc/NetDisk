package com.catchiz.pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyFile {
    @ApiModelProperty(hidden = true)
    private Integer fileId;

    private String filename;

    @ApiModelProperty(hidden = true)
    private String filePath;

    @ApiModelProperty(hidden = true)
    private long fileSize;

    @ApiModelProperty(hidden = true)
    private Integer isValidFile;

    @ApiModelProperty(hidden = true)
    private Timestamp uploadDate;

    @ApiModelProperty(hidden = true)
    private String contentType;

    private Integer uid;

    private Integer pid;

    private int check;

}
