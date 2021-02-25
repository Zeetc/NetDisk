package com.catchiz.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageBean {
    @ApiModelProperty("父文件的ID")
    private int pid;
    @ApiModelProperty("当前查询条件下，总页数")
    private int totalPage;
    @ApiModelProperty("当前查询条件下，当前页")
    private int curPage;
    @ApiModelProperty("查询条件名")
    private String fileName;
    @ApiModelProperty("用户ID")
    private int userId;
}
